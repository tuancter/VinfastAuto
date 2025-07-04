-- CREATE DATABASE vinfastauto;
USE vinfastauto;

-- Bảng permissions
CREATE TABLE permissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- Bảng roles
CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- Bảng trung gian: role_permissions (nhiều permission cho 1 role)
CREATE TABLE roles_permissions (
    role_id INT,
    permission_id INT,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Bảng users
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    birthday DATE,
    position VARCHAR(100),
    mobilephone VARCHAR(20),
    email VARCHAR(100),
    created_date DATE
);

-- Bảng trung gian: user_roles (nhiều role cho 1 user)
CREATE TABLE users_roles (
    user_id VARCHAR(255),
    role_id INT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Bảng cars
CREATE TABLE cars (
    car_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(15,2) NOT NULL,
    manufactured_year SMALLINT UNSIGNED NOT NULL,
    state VARCHAR(50) NOT NULL,
    mileage INT UNSIGNED NOT NULL,
    origin VARCHAR(100) NOT NULL,
    vehicle_type VARCHAR(50) NOT NULL,
    engine VARCHAR(50) NOT NULL,
    exterior_color VARCHAR(50) NOT NULL,
    interior_color VARCHAR(50) NOT NULL,
    seats TINYINT UNSIGNED NOT NULL,
    doors TINYINT UNSIGNED NOT NULL,
    img_link TEXT NOT NULL,
    description TEXT
);

CREATE TABLE orders (
    order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id varchar(255) NOT NULL,
    car_id BIGINT UNSIGNED NOT NULL,
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (car_id) REFERENCES cars(car_id)
);

