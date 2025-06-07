package com.group2.VinfastAuto.service;

import com.group2.VinfastAuto.dto.request.AuthenticationRequest;
import com.group2.VinfastAuto.dto.request.IntrospectRequest;
import com.group2.VinfastAuto.dto.request.LogoutRequest;
import com.group2.VinfastAuto.dto.request.RefreshRequest;
import com.group2.VinfastAuto.dto.response.AuthenticationResponse;
import com.group2.VinfastAuto.dto.response.IntrospectResponse;
import com.group2.VinfastAuto.entity.InvalidatedToken;
import com.group2.VinfastAuto.entity.User;
import com.group2.VinfastAuto.enums.StatusCode;
import com.group2.VinfastAuto.exception.AppException;
import com.group2.VinfastAuto.repository.InvalidatedTokenRepository;
import com.group2.VinfastAuto.repository.UserDAO;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserDAO userDao;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Value("${jwt.valid-duration}")
    private long VALID_DURATION;

    @Value("${jwt.refresh-duration}")
    private long REFRESH_DURATION;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userDao.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(StatusCode.USER_NOT_FOUND));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated)
            throw new AppException(StatusCode.UNAUTHENTICATED);

        log.info("User '{}' authenticated successfully", user.getUsername());

        String token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    public IntrospectResponse introspect(IntrospectRequest request) {
        boolean isValid = true;

        try {
            verifyToken(request.getToken(), false);
        } catch (AppException | JOSEException | ParseException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            SignedJWT signedJWT = verifyToken(request.getToken(), true);

            String jit = signedJWT.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            invalidatedTokenRepository.save(
                    InvalidatedToken.builder()
                            .id(jit)
                            .expiryTime(expiryTime)
                            .build()
            );

            log.info("Token '{}' invalidated successfully", jit);

        } catch (AppException e) {
            log.info("Token already expired or invalid");
        }
    }

    public AuthenticationResponse refresh(RefreshRequest request) throws ParseException, JOSEException {
        SignedJWT signedJWT = verifyToken(request.getToken(), true);

        // Invalidate old token
        String jit = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        invalidatedTokenRepository.save(
                InvalidatedToken.builder()
                        .id(jit)
                        .expiryTime(expiryTime)
                        .build()
        );

        // Generate new token
        String username = signedJWT.getJWTClaimsSet().getSubject();
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new AppException(StatusCode.UNAUTHENTICATED));

        String newToken = generateToken(user);

        return AuthenticationResponse.builder()
                .token(newToken)
                .build();
    }

    // ====================== PRIVATE METHODS ======================

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("group2.com")
                .subject(user.getUsername())
                .expirationTime(Date.from(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS)))
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        JWSObject jwsObject = new JWSObject(header, new Payload(claimsSet.toJSONObject()));

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to sign JWT", e);
        }

        return jwsObject.serialize();
    }

    private String buildScope(User user) {
        if (CollectionUtils.isEmpty(user.getRoles())) {
            throw new AppException(StatusCode.USER_HAS_NO_ROLE);
        }

        StringJoiner joiner = new StringJoiner(" ");

        user.getRoles().forEach(role -> {
            joiner.add("ROLE_" + role.getName());
            if (role.getPermissions() != null) {
                role.getPermissions().forEach(permission -> {
                    joiner.add(permission.getName());
                });
            }
        });

        return joiner.toString();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh)
            throws JOSEException, ParseException {

        SignedJWT signedJWT = SignedJWT.parse(token);

        if (!signedJWT.verify(new MACVerifier(SIGNER_KEY.getBytes()))) {
            throw new AppException(StatusCode.UNAUTHENTICATED);
        }

        // Optional: Check issuer
        String issuer = signedJWT.getJWTClaimsSet().getIssuer();
        if (!"group2.com".equals(issuer)) {
            throw new AppException(StatusCode.UNAUTHENTICATED);
        }

        Date expiry = signedJWT.getJWTClaimsSet().getExpirationTime();
        Instant extendedExpiry = expiry.toInstant().plus(REFRESH_DURATION, ChronoUnit.SECONDS);

        if ((isRefresh && extendedExpiry.isBefore(Instant.now())) || (!isRefresh && expiry.before(new Date()))) {
            throw new AppException(StatusCode.UNAUTHENTICATED);
        }

        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        if (invalidatedTokenRepository.existsById(jwtId)) {
            throw new AppException(StatusCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }
}
