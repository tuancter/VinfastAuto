INSERT INTO permissions (name, description) VALUES
('create', 'Tạo mới dữ liệu'),
('read', 'Xem dữ liệu'),
('update', 'Cập nhật dữ liệu'),
('delete', 'Xóa dữ liệu');

INSERT INTO roles (name, description) VALUES
('ADMIN', 'Quản trị viên toàn quyền'),
('USER', 'Người dùng cơ bản'),
('MANAGER', 'Quản lý dữ liệu');

-- admin: tất cả quyền
INSERT INTO roles_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4);

-- user: chỉ được đọc
INSERT INTO roles_permissions (role_id, permission_id) VALUES
(2, 2);

-- manager: đọc + cập nhật
INSERT INTO roles_permissions (role_id, permission_id) VALUES
(3, 2), (3, 3);
