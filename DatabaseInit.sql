USE ecommerce_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS discount;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS product_images;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(100),
    is_flagged BOOLEAN DEFAULT FALSE,
    enabled BOOLEAN DEFAULT TRUE,
    user_discount_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    user_discount_start_date DATE,
    user_discount_end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(12,2) NOT NULL
);

CREATE INDEX idx_product_category ON product(category);

CREATE TABLE product_images (
    product_id BIGINT NOT NULL,
    images VARCHAR(255),
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

CREATE TABLE review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user` VARCHAR(50),
    comment TEXT,
    rating INT NOT NULL,
    product_id BIGINT,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE discount (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255),
    percentage DECIMAL(5,2) NOT NULL,
    start_date DATE,
    end_date DATE,
    product_id BIGINT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quantity INT NOT NULL,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    discount_id BIGINT,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    FOREIGN KEY (discount_id) REFERENCES discount(id) ON DELETE SET NULL,
    UNIQUE KEY unique_cart_product (cart_id, product_id)
);

INSERT INTO roles (name) VALUES
('ROLE_USER'),
('ROLE_ADMIN'),
('ROLE_MANAGER'),
('ROLE_EMPLOYEE');

INSERT INTO users (email, password, username, is_flagged, enabled)
VALUES
('admin@ecommerce.com', '$2a$10$ehYWzot3e82Pdz0CHG/2JObImXBJypWBlto0dykq00dubbC97veWK', 'Super Admin', false, true),
('user@ecommerce.com', '$2a$10$ehYWzot3e82Pdz0CHG/2JObImXBJypWBlto0dykq00dubbC97veWK', 'John Doe', false, true),
('manager@ecommerce.com', '$2a$10$ehYWzot3e82Pdz0CHG/2JObImXBJypWBlto0dykq00dubbC97veWK', 'Manager Account', false, true);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'ROLE_ADMIN' WHERE u.email = 'admin@ecommerce.com';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'ROLE_USER' WHERE u.email = 'user@ecommerce.com';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'ROLE_MANAGER' WHERE u.email = 'manager@ecommerce.com';

INSERT INTO product (name, description, category, price) VALUES
('Gaming Laptop', 'High-performance gaming laptop', 'Electronics', 1299.99),
('Wireless Earbuds', 'Noise-cancelling Bluetooth earbuds', 'Electronics', 99.99),
('Leather Wallet', 'Genuine leather wallet for men', 'Accessories', 25.50);

INSERT INTO product_images (product_id, images) VALUES
(1, 'http://localhost:8080/images/laptop1.jpg'),
(2, 'http://localhost:8080/images/earbuds1.jpg'),
(3, 'http://localhost:8080/images/wallet1.jpg');

INSERT INTO review (`user`, comment, rating, product_id) VALUES
('Alice', 'Fantastic performance!', 5, 1),
('Bob', 'Runs all games smoothly.', 4, 1),
('Carol', 'Battery life could be better.', 3, 2);

INSERT INTO discount (description, percentage, start_date, end_date, product_id) VALUES
('Summer Sale', 10.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1),
('Clearance Deal', 15.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 2);

INSERT INTO carts (user_id)
SELECT id FROM users WHERE email = 'user@ecommerce.com';
