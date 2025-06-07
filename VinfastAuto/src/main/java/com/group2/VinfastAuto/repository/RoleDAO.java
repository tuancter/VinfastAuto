package com.group2.VinfastAuto.repository;

import com.group2.VinfastAuto.database.ConnectionPool;
import com.group2.VinfastAuto.database.ConnectionPoolImpl;
import com.group2.VinfastAuto.entity.Permission;
import com.group2.VinfastAuto.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository
public class RoleDAO implements DAOInterface<Role, Long> {

    @Autowired
    private ConnectionPool connectionPool;

    // Lấy tất cả các role từ bảng `roles`
    @Override
    public List<Role> findAll() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles";

        try (Connection conn = connectionPool.getConnection("RoleDAO");
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                roles.add(extractRole(rs));  // Chuyển dữ liệu từ ResultSet thành đối tượng Role
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return roles;
    }

    // Tìm một role theo ID
    @Override
    public Optional<Role> findById(Long id) {
        String sql = "SELECT * FROM roles WHERE id = ?";
        try (Connection conn = connectionPool.getConnection("RoleDAO");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Role role = extractRole(rs);
                    // Gán permissions cho role ngay khi lấy ra
                    role.setPermissions(getPermissionsOfRole(role.getId()));
                    return Optional.of(role);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }




    // Thêm một role vào bảng `roles`
    @Override
    public Role add(Role role) {
        String sql = "INSERT INTO roles (name, description) VALUES (?, ?)";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection("RoleDAO");
            conn.setAutoCommit(false);  // Bắt đầu một transaction

            // Thực hiện câu lệnh SQL
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, role.getName());  // Set name của role
                stmt.setString(2, role.getDescription());  // Set description của role
                stmt.executeUpdate();  // Thực thi câu lệnh

                // Lấy ID được sinh ra khi thêm mới role vào cơ sở dữ liệu
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        role.setId(rs.getLong(1));  // Cập nhật ID của role
                    }
                }
            }

            conn.commit();  // Commit transaction khi không có lỗi
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }  // Rollback nếu có lỗi
        } finally {
            if (conn != null) try { connectionPool.releaseConnection(conn, "RoleDAO"); } catch (SQLException e) { e.printStackTrace(); }
        }

        return role;  // Trả về role đã được thêm vào DB (với ID mới)
    }

    // Cập nhật thông tin một role trong bảng `roles`
    @Override
    public Role update(Role role) {
        String sql = "UPDATE roles SET name = ?, description = ? WHERE id = ?";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection("RoleDAO");
            conn.setAutoCommit(false);  // Bắt đầu một transaction

            // Thực hiện câu lệnh SQL
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, role.getName());  // Set name của role
                stmt.setString(2, role.getDescription());  // Set description của role
                stmt.setLong(3, role.getId());  // Set ID của role
                stmt.executeUpdate();  // Thực thi câu lệnh
            }

            conn.commit();  // Commit transaction khi không có lỗi
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }  // Rollback nếu có lỗi
        } finally {
            if (conn != null) try { connectionPool.releaseConnection(conn, "RoleDAO"); } catch (SQLException e) { e.printStackTrace(); }
        }

        return role;  // Trả về role đã được cập nhật
    }

    // Xóa một role theo ID trong bảng `roles`
    @Override
    public void delete(Role role) {
        String sql = "DELETE FROM roles WHERE id = ?";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection("RoleDAO");
            conn.setAutoCommit(false);  // Bắt đầu một transaction

            // Thực hiện câu lệnh SQL
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, role.getId());  // Set ID của role
                stmt.executeUpdate();  // Thực thi câu lệnh
            }

            conn.commit();  // Commit transaction khi không có lỗi
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }  // Rollback nếu có lỗi
        } finally {
            if (conn != null) try { connectionPool.releaseConnection(conn, "RoleDAO"); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Chuyển dữ liệu từ ResultSet thành đối tượng Role
    private Role extractRole(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getLong("id"));
        role.setName(rs.getString("name"));
        role.setDescription(rs.getString("description"));
        return role;
    }

    // Lấy permissions của một role
    public Set<Permission> getPermissionsOfRole(Long roleId) {
        Set<Permission> permissions = new HashSet<>();
        String sql = "SELECT p.* FROM permissions p " +
                "JOIN roles_permissions rp ON p.id = rp.permission_id " +
                "WHERE rp.role_id = ?";

        try (Connection conn = connectionPool.getConnection("RoleDAO");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, roleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Permission permission = new Permission();
                    permission.setId(rs.getLong("id"));
                    permission.setName(rs.getString("name"));
                    permission.setDescription(rs.getString("description"));
                    permissions.add(permission);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }
}
