USE ecommerce_db;

-- ==========================================
-- 1. CLEANUP (Reset Database State)
-- ==========================================
SET FOREIGN_KEY_CHECKS = 0;

-- Drop dependent tables first to avoid Foreign Key errors
DROP TABLE IF EXISTS discount;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS product_images;

-- Drop core tables last
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================
-- 2. SCHEMA CREATION
-- ==========================================

-- Roles Table
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(100),
    is_flagged BOOLEAN DEFAULT FALSE,
    enabled BOOLEAN DEFAULT TRUE,
    user_discount_percentage DOUBLE DEFAULT 0,
    user_discount_start_date DATE,
    user_discount_end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User Roles Join Table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Product Table
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    description TEXT,
    category VARCHAR(50),
    price DOUBLE
);

-- Product Images
CREATE TABLE product_images (
    product_id BIGINT NOT NULL,
    images VARCHAR(255),
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

-- Review Table
CREATE TABLE review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user VARCHAR(50),
    comment TEXT,
    rating INT,
    product_id BIGINT,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

-- Carts Table (One-to-One with User)
CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Cart Items Table
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quantity INT NOT NULL,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    UNIQUE KEY unique_cart_product (cart_id, product_id)
);

-- Discount Table (One-to-Many with Product)
CREATE TABLE discount (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255),
    percentage DOUBLE NOT NULL,
    start_date DATE,
    end_date DATE,
    product_id BIGINT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

-- ==========================================
-- 3. DATA INSERTION
-- ==========================================

-- 3a. Insert Roles
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_MANAGER');
INSERT INTO roles (name) VALUES ('ROLE_EMPLOYEE');

-- 3b. Insert Users
-- Admin: admin@ecommerce.com / admin123
INSERT INTO users (email, password, username, is_flagged, enabled) 
VALUES ('admin@ecommerce.com', '$2a$10$ehYWzot3e82Pdz0CHG/2JObImXBJypWBlto0dykq00dubbC97veWK', 'Super Admin', false, true);

-- User: user@ecommerce.com / user123
INSERT INTO users (email, password, username, is_flagged, enabled) 
VALUES ('user@ecommerce.com', 'user123', 'John Doe', false, true);

-- Manager: manager@ecommerce.com / manager123
INSERT INTO users (email, password, username, is_flagged, enabled) 
VALUES ('manager@ecommerce.com', 'manager123', 'Manager Account', false, true);

-- 3c. Assign Roles
-- Admin gets ROLE_ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT (SELECT id FROM users WHERE email = 'admin@ecommerce.com'), (SELECT id FROM roles WHERE name = 'ROLE_ADMIN');

-- User gets ROLE_USER
INSERT INTO user_roles (user_id, role_id)
SELECT (SELECT id FROM users WHERE email = 'user@ecommerce.com'), (SELECT id FROM roles WHERE name = 'ROLE_USER');

-- Manager gets ROLE_MANAGER
INSERT INTO user_roles (user_id, role_id)
SELECT (SELECT id FROM users WHERE email = 'manager@ecommerce.com'), (SELECT id FROM roles WHERE name = 'ROLE_MANAGER');

-- 3d. Insert Products
INSERT INTO product (name, description, category, price) VALUES
('Gaming Laptop', 'High-performance gaming laptop', 'Electronics', 1299.99),
('Wireless Earbuds', 'Noise-cancelling Bluetooth earbuds', 'Electronics', 99.99),
('Leather Wallet', 'Genuine leather wallet for men', 'Accessories', 25.50),
('Smartphone', 'Latest Android phone with fast processor', 'Electronics', 699.00),
('Wrist Watch', 'Analog wristwatch with leather strap', 'Accessories', 149.99),
('Running Shoes', 'Comfortable shoes for running', 'Footwear', 89.00),
('Office Chair', 'Ergonomic chair with lumbar support', 'Furniture', 199.00),
('Backpack', 'Waterproof backpack for travel', 'Bags', 49.99),
('Sunglasses', 'Polarized UV protection sunglasses', 'Accessories', 59.99),
('Bluetooth Speaker', 'Portable speaker with deep bass', 'Electronics', 79.99);

-- 3e. Insert Product Images
INSERT INTO product_images (product_id, images) VALUES
(1, 'http://localhost:8080/images/laptop1.jpg'), (1, 'http://localhost:8080/images/laptop2.jpg'),
(2, 'http://localhost:8080/images/earbuds1.jpg'), (2, 'http://localhost:8080/images/earbuds2.jpg'),
(3, 'http://localhost:8080/images/wallet1.jpg'), (3, 'http://localhost:8080/images/wallet2.jpg'),
(4, 'http://localhost:8080/images/phone1.jpg'), (4, 'http://localhost:8080/images/phone2.jpg'),
(5, 'http://localhost:8080/images/watch1.jpg'), (5, 'http://localhost:8080/images/watch2.jpg'),
(6, 'http://localhost:8080/images/shoes1.jpg'), (6, 'http://localhost:8080/images/shoes2.jpg'),
(7, 'http://localhost:8080/images/chair1.jpg'), (7, 'http://localhost:8080/images/chair2.jpg'),
(8, 'http://localhost:8080/images/backpack1.jpg'), (8, 'http://localhost:8080/images/backpack2.jpg'),
(9, 'http://localhost:8080/images/sunglasses1.jpg'), (9, 'http://localhost:8080/images/sunglasses2.jpg'),
(10, 'http://localhost:8080/images/speaker1.jpg'), (10, 'http://localhost:8080/images/speaker2.jpg');

-- 3f. Insert Reviews
INSERT INTO review (user, comment, rating, product_id) VALUES
('Alice', 'Fantastic performance!', 5, 1),
('Bob', 'Runs all games smoothly.', 4, 1),
('Carol', 'Battery life could be better.', 3, 2),
('Dan', 'Clear sound and nice fit.', 5, 2),
('Eve', 'Looks premium and lasts long.', 4, 3),
('Frank', 'Spacious and compact design.', 4, 4),
('Grace', 'Elegant and stylish.', 5, 5),
('Heidi', 'Strap material is soft.', 4, 5),
('Ivan', 'Very comfortable while running.', 5, 6),
('Judy', 'Feels solid and adjustable.', 4, 7),
('Mallory', 'Great for short trips.', 5, 8),
('Niaj', 'Lens quality is impressive.', 4, 9),
('Olivia', 'Loud and clear sound.', 5, 10);

-- 3g. Insert Discounts
-- 1. Gaming Laptop: 10% Off Summer Sale
INSERT INTO discount (description, percentage, start_date, end_date, product_id) 
VALUES ('Summer Sale', 10.0, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1);

-- 2. Wireless Earbuds: 15% Off Clearance
INSERT INTO discount (description, percentage, start_date, end_date, product_id) 
VALUES ('Clearance Deal', 15.0, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 2);

-- 3. Smartphone: Multiple Discounts
INSERT INTO discount (description, percentage, start_date, end_date, product_id) 
VALUES ('HDFC Bank Offer', 5.0, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 90 DAY), 4);

INSERT INTO discount (description, percentage, start_date, end_date, product_id) 
VALUES ('Festive Sale', 10.0, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 4);

-- 4. Running Shoes: Flat 20% Off (No End Date)
INSERT INTO discount (description, percentage, start_date, end_date, product_id) 
VALUES ('Flat Discount', 20.0, CURDATE(), NULL, 6);

-- 3h. Initialize Cart for Default User (Prevents frontend errors on first login)
INSERT INTO carts (user_id) 
SELECT id FROM users WHERE email = 'user@ecommerce.com';

ALTER TABLE cart_items ADD COLUMN discount_id BIGINT;

-- Link it to the discount table
ALTER TABLE cart_items 
ADD CONSTRAINT fk_cart_item_discount 
FOREIGN KEY (discount_id) REFERENCES discount(id) ON DELETE SET NULL;
