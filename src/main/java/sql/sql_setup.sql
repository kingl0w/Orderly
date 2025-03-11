-- MySQL Database Setup for Orderly Application

-- Save this file as sql/sql_setup.sql

-- Run using: mysql -u root -p < sql/sql_setup.sql

-- Create database user with a placeholder password (change for production)
CREATE USER '<username>'@'localhost' IDENTIFIED BY '<password>';
-- Create the application database
CREATE DATABASE orderly;
-- Grant privileges to the user for the database
GRANT ALL PRIVILEGES ON orderly.* TO '<username>'@'localhost';
-- Apply the privilege changes
FLUSH PRIVILEGES;
-- Optional: Select the database to use (if you want to add table creation below)
USE orderly;

-- Note: Table creation is handled automatically by the DatabaseManager class
-- Tables created:
--   - products
--   - customers
--   - orders
--   - order_items
-- For .env file configuration, use:
-- DB_URL=jdbc:mysql://localhost:3306/orderly (or whatever port you're using)
-- DB_USER=<username>
-- DB_PASSWORD=<password>
