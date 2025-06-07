package com.group2.VinfastAuto.repository;

import com.group2.VinfastAuto.database.ConnectionPool;
import com.group2.VinfastAuto.dto.response.StatisticResponse;
import com.group2.VinfastAuto.entity.Permission;
import com.group2.VinfastAuto.entity.Role;
import com.group2.VinfastAuto.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserDAO implements DAOInterface<User, String> {

    private final ConnectionPool connectionPool;

    @Autowired
    private RoleDAO roleDAO;

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = connectionPool.getConnection("UserDAO");
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    @Override
    public Optional<User> findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = connectionPool.getConnection("UserDAO");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractUserFromResultSet(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = connectionPool.getConnection("UserDAO");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractUserFromResultSet(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public User add(User user) {
        String sql = "INSERT INTO users (id, username, password, first_name, last_name, birthday, position, mobilephone, email, created_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = connectionPool.getConnection("UserDAO");
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user.getId());
                stmt.setString(2, user.getUsername());
                stmt.setString(3, user.getPassword());
                stmt.setString(4, user.getFirstName());
                stmt.setString(5, user.getLastName());

                if (user.getBirthday() != null) {
                    stmt.setDate(6, Date.valueOf(user.getBirthday()));
                } else {
                    stmt.setNull(6, Types.DATE);
                }

                stmt.setString(7, user.getPosition());
                stmt.setString(8, user.getMobilephone());
                stmt.setString(9, user.getEmail());

                if (user.getCreatedDate() != null) {
                    stmt.setDate(10, Date.valueOf(user.getCreatedDate()));
                } else {
                    stmt.setNull(10, Types.DATE);
                }

                stmt.executeUpdate();
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            if (conn != null) try { connectionPool.releaseConnection(conn, "UserDAO"); } catch (SQLException e) { e.printStackTrace(); }
        }

        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET username=?, password=?, first_name=?, last_name=?, birthday=?, position=?, mobilephone=?, email=? WHERE id=?";

        Connection conn = null;
        try {
            conn = connectionPool.getConnection("UserDAO");
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getPassword());
                stmt.setString(3, user.getFirstName());
                stmt.setString(4, user.getLastName());

                if (user.getBirthday() != null) {
                    stmt.setDate(5, Date.valueOf(user.getBirthday()));
                } else {
                    stmt.setNull(5, Types.DATE);
                }

                stmt.setString(6, user.getPosition());
                stmt.setString(7, user.getMobilephone());
                stmt.setString(8, user.getEmail());

                stmt.setString(9, user.getId());

                stmt.executeUpdate();
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            if (conn != null) try { connectionPool.releaseConnection(conn, "UserDAO"); } catch (SQLException e) { e.printStackTrace(); }
        }

        return user;
    }

    @Override
    public void delete(User user) {
        String sql = "DELETE FROM users WHERE id = ?";

        Connection conn = null;
        try {
            conn = connectionPool.getConnection("UserDAO");
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user.getId());
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            if (conn != null) try { connectionPool.releaseConnection(conn, "UserDAO"); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        String userId = rs.getString("id");

        user.setId(userId);
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));

        Date birthdayDate = rs.getDate("birthday");
        user.setBirthday(birthdayDate != null ? birthdayDate.toLocalDate() : null);

        user.setPosition(rs.getString("position"));
        user.setMobilephone(rs.getString("mobilephone"));
        user.setEmail(rs.getString("email"));

        Date createdDate = rs.getDate("created_date");
        user.setCreatedDate(createdDate != null ? createdDate.toLocalDate() : null);

        user.setRoles(getRolesOfUser(userId));

        return user;
    }

    public Set<Role> getRolesOfUser(String userId) {
        Set<Role> roles = new HashSet<>();
        String sql = "SELECT r.id, r.name, r.description FROM roles r " +
                "JOIN users_roles ur ON r.id = ur.role_id " +
                "WHERE ur.user_id = ?";

        try (Connection conn = connectionPool.getConnection("UserDAO");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Role role = new Role();
                    role.setId(rs.getLong("id"));
                    role.setName(rs.getString("name"));
                    role.setDescription(rs.getString("description"));

                    // Gọi RoleDAO để lấy permissions của role này
                    Set<Permission> permissions = roleDAO.getPermissionsOfRole(role.getId());
                    role.setPermissions(permissions);

                    roles.add(role);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return roles;
    }


    public void addRolesToUser(String userId, Set<Role> roles) {
        String sql = "INSERT INTO users_roles (user_id, role_id) VALUES (?, ?)";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection("UserDAO");
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Role role : roles) {
                    stmt.setString(1, userId);
                    stmt.setLong(2, role.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            if (conn != null) try { connectionPool.releaseConnection(conn, "UserDAO"); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public void updateRolesOfUser(String userId, Set<Role> newRoles) {
        Connection conn = null;

        try {
            conn = connectionPool.getConnection("UserDAO");
            conn.setAutoCommit(false);

            try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM users_roles WHERE user_id = ?")) {
                deleteStmt.setString(1, userId);
                deleteStmt.executeUpdate();
            }

            try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO users_roles (user_id, role_id) VALUES (?, ?)")) {
                for (Role role : newRoles) {
                    insertStmt.setString(1, userId);
                    insertStmt.setLong(2, role.getId());
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            if (conn != null) try { connectionPool.releaseConnection(conn, "UserDAO"); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Pagination
    public List<User> searchUsers(String keyword, String sortBy, String direction, int offset, int limit) {
        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM users WHERE username LIKE ? ORDER BY " + sortBy + " " + direction + " LIMIT ? OFFSET ?";

        try (Connection conn = connectionPool.getConnection("UserDAO");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(extractUserFromResultSet(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    public int countUsers(String keyword) {
        String sql = "SELECT COUNT(*) FROM users WHERE username LIKE ?";
        try (Connection conn = connectionPool.getConnection("UserDAO");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    // Statistic
    // Thống kê theo năm tạo user
    public List<StatisticResponse> countUsersByYear() {
        List<StatisticResponse> result = new ArrayList<>();
        String sql = "SELECT YEAR(created_date) AS year, COUNT(*) AS count FROM users GROUP BY YEAR(created_date) ORDER BY year";

        try (Connection conn = connectionPool.getConnection("UserDAO");
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int year = rs.getInt("year");
                int count = rs.getInt("count");
                result.add(new StatisticResponse(String.valueOf(year), count));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    // Thống kê theo vị trí
    public List<StatisticResponse> countUsersByPosition() {
        List<StatisticResponse> result = new ArrayList<>();
        String sql = "SELECT position, COUNT(*) AS count FROM users GROUP BY position ORDER BY count DESC";

        try (Connection conn = connectionPool.getConnection("UserDAO");
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String position = rs.getString("position");
                int count = rs.getInt("count");
                result.add(new StatisticResponse(position, count));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    // Thống kê theo nhóm tuổi
    public List<StatisticResponse> countUsersByAgeGroup() {
        List<StatisticResponse> result = new ArrayList<>();
        String sql = "SELECT " +
                "CASE " +
                " WHEN TIMESTAMPDIFF(YEAR, birthday, CURDATE()) BETWEEN 0 AND 17 THEN '0-17' " +
                " WHEN TIMESTAMPDIFF(YEAR, birthday, CURDATE()) BETWEEN 18 AND 24 THEN '18-24' " +
                " WHEN TIMESTAMPDIFF(YEAR, birthday, CURDATE()) BETWEEN 25 AND 34 THEN '25-34' " +
                " WHEN TIMESTAMPDIFF(YEAR, birthday, CURDATE()) BETWEEN 35 AND 44 THEN '35-44' " +
                " WHEN TIMESTAMPDIFF(YEAR, birthday, CURDATE()) BETWEEN 45 AND 54 THEN '45-54' " +
                " ELSE '55+' " +
                "END AS age_group, COUNT(*) AS count " +
                "FROM users " +
                "WHERE birthday IS NOT NULL " +
                "GROUP BY age_group ORDER BY age_group";

        try (Connection conn = connectionPool.getConnection("UserDAO");
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String ageGroup = rs.getString("age_group");
                int count = rs.getInt("count");
                result.add(new StatisticResponse(ageGroup, count));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


}
