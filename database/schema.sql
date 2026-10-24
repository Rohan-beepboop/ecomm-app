-- =====================================================================
--  E-Commerce Application : database initialisation script
--  Run once:  mysql -u root -p < database/schema.sql
--  NOTE: the ADMIN user is created automatically at first application
--  start-up (AppInitListener) so that its password is hashed by the same
--  PBKDF2 code used at login, rather than a hash pasted into this file.
-- =====================================================================
CREATE DATABASE IF NOT EXISTS ecommerce_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ecommerce_db;

DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
  id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  full_name     VARCHAR(100) NOT NULL,
  email         VARCHAR(150) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role          ENUM('CUSTOMER','ADMIN') NOT NULL DEFAULT 'CUSTOMER',
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE categories (
  id   INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(60) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE products (
  id          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  category_id INT UNSIGNED NOT NULL,
  name        VARCHAR(150) NOT NULL,
  description TEXT,
  price       DECIMAL(10,2) NOT NULL CHECK (price >= 0),
  stock       INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
  image_url   VARCHAR(500),
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id),
  INDEX idx_products_name (name)
) ENGINE=InnoDB;

CREATE TABLE orders (
  id               INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id          INT UNSIGNED NOT NULL,
  total            DECIMAL(12,2) NOT NULL,
  status           VARCHAR(20) NOT NULL DEFAULT 'PLACED',
  shipping_address VARCHAR(255) NOT NULL,
  created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- product_name / unit_price are SNAPSHOTS: order history must stay accurate even if
-- the admin later edits or deletes the product. Hence product_id is nullable (SET NULL).
CREATE TABLE order_items (
  id           INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  order_id     INT UNSIGNED NOT NULL,
  product_id   INT UNSIGNED NULL,
  product_name VARCHAR(150) NOT NULL,
  unit_price   DECIMAL(10,2) NOT NULL,
  quantity     INT NOT NULL CHECK (quantity > 0),
  CONSTRAINT fk_items_order   FOREIGN KEY (order_id)   REFERENCES orders(id)   ON DELETE CASCADE,
  CONSTRAINT fk_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ------------------------- mock data -------------------------
INSERT INTO categories (name) VALUES ('Electronics'), ('Books'), ('Home & Kitchen'), ('Apparel');

INSERT INTO products (category_id, name, description, price, stock, image_url) VALUES
(1,'Wireless Noise-Cancelling Headphones','Over-ear Bluetooth headphones with 30-hour battery life and active noise cancellation.',129.99,25,'https://placehold.co/600x420/1f3a4d/f3ead9?text=Headphones'),
(1,'Mechanical Keyboard','Hot-swappable tactile switches, aluminium frame, per-key backlight.',89.50,40,'https://placehold.co/600x420/1f3a4d/f3ead9?text=Keyboard'),
(1,'USB-C Fast Charger 65W','Compact GaN charger for laptops, tablets and phones.',34.00,120,'https://placehold.co/600x420/1f3a4d/f3ead9?text=Charger'),
(1,'1080p Webcam','Autofocus webcam with dual microphones and privacy shutter.',49.99,60,'https://placehold.co/600x420/1f3a4d/f3ead9?text=Webcam'),
(2,'Clean Code','A handbook of agile software craftsmanship by Robert C. Martin.',37.95,50,'https://placehold.co/600x420/7a3b2e/f3ead9?text=Clean+Code'),
(2,'Effective Java (3rd Edition)','Best-practice guidance for the Java platform by Joshua Bloch.',44.99,35,'https://placehold.co/600x420/7a3b2e/f3ead9?text=Effective+Java'),
(2,'Head First Servlets & JSP','Visual guide to building Java web applications.',39.50,20,'https://placehold.co/600x420/7a3b2e/f3ead9?text=Servlets+%26+JSP'),
(3,'Stainless Steel Water Bottle','Double-wall vacuum insulated, keeps drinks cold 24h or hot 12h.',22.00,200,'https://placehold.co/600x420/4a5d3a/f3ead9?text=Bottle'),
(3,'Pour-Over Coffee Set','Glass dripper, carafe and reusable steel filter.',31.75,45,'https://placehold.co/600x420/4a5d3a/f3ead9?text=Coffee+Set'),
(3,'Cast Iron Skillet 10"','Pre-seasoned skillet that moves from stovetop to oven.',28.40,3,'https://placehold.co/600x420/4a5d3a/f3ead9?text=Skillet'),
(4,'Merino Wool Beanie','Soft, breathable and naturally odour-resistant.',19.99,80,'https://placehold.co/600x420/b08a3e/1f1a14?text=Beanie'),
(4,'Canvas Everyday Backpack','20L water-resistant canvas with padded laptop sleeve.',58.00,0,'https://placehold.co/600x420/b08a3e/1f1a14?text=Backpack');
