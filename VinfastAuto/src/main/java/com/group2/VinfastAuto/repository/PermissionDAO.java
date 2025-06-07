package com.group2.VinfastAuto.repository;

import com.group2.VinfastAuto.database.ConnectionPool;
import com.group2.VinfastAuto.database.ConnectionPoolImpl;
import com.group2.VinfastAuto.entity.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PermissionDAO implements DAOInterface<Permission, Long> {

    @Autowired
    private ConnectionPool connectionPool;

    @Override
    public List<Permission> findAll() {
        List<Permission> permissions = new ArrayList<>();
        String sql = "SELECT * FROM permissions";

        try (Connection conn = connectionPool.getConnection("PermissionDAO");
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                permissions.add(extractPermission(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return permissions;
    }

    @Override
    public Optional<Permission> findById(Long id) {
        String sql = "SELECT * FROM permissions WHERE id = ?";
        try (Connection conn = connectionPool.getConnection("PermissionDAO");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractPermission(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public Permission add(Permission permission) {
        String sql = "INSERT INTO permissions (name, description) VALUES (?, ?)";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection("PermissionDAO");
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, permission.getName());
                stmt.setString(2, permission.getDescription());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        permission.setId(rs.getLong(1));
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            if (conn != null) try { connectionPool.releaseConnection(conn, "PermissionDAO"); } catch (SQLException e) { e.printStackTrace(); }
        }

        return permission;
    }

    @Override
    public Permission update(Permission permission) {
        String sql = "UPDATE permissions SET name = ?, description = ? WHERE id = ?";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection("PermissionDAO");
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, permission.getName());
                stmt.setString(2, permission.getDescription());
                stmt.setLong(3, permission.getId());
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            if (conn != null) try { connectionPool.releaseConnection(conn, "PermissionDAO"); } catch (SQLException e) { e.printStackTrace(); }
        }

        return permission;
    }

    @Override
    public void delete(Permission permission) {
        String sql = "DELETE FROM permissions WHERE id = ?";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection("PermissionDAO");
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, permission.getId());
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            if (conn != null) try { connectionPool.releaseConnection(conn, "PermissionDAO"); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private Permission extractPermission(ResultSet rs) throws SQLException {
        Permission permission = new Permission();
        permission.setId(rs.getLong("id"));
        permission.setName(rs.getString("name"));
        permission.setDescription(rs.getString("description"));
        return permission;
    }
}
