-- ============================================================
--  SecureBank Database Schema Setup
--  Trainer: Rushi | DevOps Multi-Cloud Training
-- ============================================================
--
--  Run this script as MySQL root to set up the database:
--    sudo mysql -u root -p < schema.sql
--
--  Or copy/paste the commands inside MySQL shell.
-- ============================================================

-- Create the SecureBank database
CREATE DATABASE IF NOT EXISTS securebank_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- Create dedicated user (production best practice - never use root!)
CREATE USER IF NOT EXISTS 'securebank_user'@'%' IDENTIFIED BY 'SecurePass@2026';
CREATE USER IF NOT EXISTS 'securebank_user'@'localhost' IDENTIFIED BY 'SecurePass@2026';

-- Grant only required permissions on the SecureBank database
GRANT ALL PRIVILEGES ON securebank_db.* TO 'securebank_user'@'%';
GRANT ALL PRIVILEGES ON securebank_db.* TO 'securebank_user'@'localhost';

FLUSH PRIVILEGES;

-- Switch to the SecureBank database
USE securebank_db;

-- ============================================================
--  NOTE: Tables are auto-created by Hibernate on first run
--  (spring.jpa.hibernate.ddl-auto=update)
--
--  Schema for reference (Hibernate will create these):
--
--  Table: customers
--    - customer_id      BIGINT PK AUTO_INCREMENT
--    - username         VARCHAR(50) UNIQUE NOT NULL
--    - password         VARCHAR(255) NOT NULL  (BCrypt hash)
--    - account_number   VARCHAR(20) UNIQUE NOT NULL  (e.g. SB202600042)
--    - account_type     VARCHAR(20) NOT NULL          (SAVINGS / CURRENT)
--    - balance          DECIMAL(15,2) NOT NULL
--
--  Table: transaction_history
--    - transaction_id     BIGINT PK AUTO_INCREMENT
--    - amount             DECIMAL(15,2) NOT NULL
--    - transaction_type   VARCHAR(30) NOT NULL  (DEPOSIT/WITHDRAWAL/TRANSFER_IN/TRANSFER_OUT)
--    - description        VARCHAR(255)
--    - transaction_date   DATETIME NOT NULL
--    - customer_id        BIGINT NOT NULL  (FK → customers.customer_id)
-- ============================================================

-- Verify setup
SHOW DATABASES LIKE 'securebank_db';
SELECT user, host FROM mysql.user WHERE user = 'securebank_user';
