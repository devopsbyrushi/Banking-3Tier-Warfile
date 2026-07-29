# 🏦 SecureBank — JAR Edition

> **Trainer:** Rushi | **Course:** DevOps Multi-Cloud Training
> **Build:** Standalone JAR with embedded Tomcat
> **Package:** `com.rushi.securebank`

---
# 🛠️ MySQL Lab Guide — SecureBank Project

> **Trainer:** Rushi | **Course:** DevOps with Multi-Cloud Training
> **Project:** SecureBank (3-Tier Java Application)
> **Lab:** Complete MySQL command reference

---

## 📑 Quick Reference

### 🟢 Main Lab Flow

1. [Install MySQL](#1-install-mysql)
2. [Manage MySQL Service](#2-manage-mysql-service)
3. [Set Root Password (First Time)](#3-set-root-password-first-time)
4. [Create SecureBank Database & User](#4-create-securebank-database--user)
5. [Validate the Setup](#5-validate-the-setup)
6. [Quick Cheat Sheet](#6-quick-cheat-sheet)

### 🔧 Reference (Use Only If Needed)

7. [Change Existing Root Password](#7-change-existing-root-password)
8. [Reset Forgotten Root Password](#8-reset-forgotten-root-password)
9. [Uninstall MySQL Completely](#9-uninstall-mysql-completely)
10. [Reinstall MySQL (Fresh Start)](#10-reinstall-mysql-fresh-start)

---

# 🟢 Main Lab Flow

## 1. Install MySQL

```bash
# Update package list
sudo apt update

# Install MySQL Server
sudo apt-get install mysql-server -y
```

---

## 2. Manage MySQL Service

```bash
# Start MySQL
sudo systemctl start mysql

# Stop MySQL
sudo systemctl stop mysql

# Restart MySQL
sudo systemctl restart mysql

# Enable MySQL to start on boot
sudo systemctl enable mysql

# Disable auto-start on boot
sudo systemctl disable mysql

# Check status
sudo systemctl status mysql
```

---

## 3. Set Root Password (First Time)

When MySQL is **freshly installed** and root has **no password yet**.

### Step 1: Login as root (no password needed)

```bash
sudo mysql
```

### Step 2: Set the root password

Inside the MySQL shell:

```sql
ALTER USER 'root'@'localhost' IDENTIFIED BY 'YourPassword@123';

FLUSH PRIVILEGES;

EXIT;
```

### Step 3: Login with the new password

```bash
sudo mysql -u root -p
```

Enter `YourPassword@123` when prompted.

> 💡 Replace `YourPassword@123` with your actual strong password.

---

## 4. Create SecureBank Database & User

After logging in as root, set up the database and user for our SecureBank app.
```bash
sudo mysql -u root -p

# Create the application user
ALTER USER 'root'@'localhost' IDENTIFIED BY 'YourPassword@123';

# Grant full access to the SecureBank database (only this DB)
GRANT ALL PRIVILEGES ON securebank_db.* TO 'securebank_user'@'localhost';

# Refresh privileges again
FLUSH PRIVILEGES;

# Verify the user was created
SELECT user, host FROM mysql.user;

# Exit
EXIT;

Inside the MySQL shell:

sudo mysql -u root -p

# Create the SecureBank database
CREATE DATABASE securebank_db;

# Verify it was created
SHOW DATABASES;

# Create the user
CREATE USER 'securebank_user'@'localhost'
IDENTIFIED BY 'SecurePass@2026';

# Grant permissions
GRANT ALL PRIVILEGES
ON securebank_db.*
TO 'securebank_user'@'localhost';

FLUSH PRIVILEGES;

# Exit
EXIT;

```

### Test the new user

```bash
mysql -u securebank_user -p
```

Enter password: `SecurePass@2026`

---

## 5. Validate the Setup

After running the application and performing some deposits/withdrawals, verify the data:

```bash
mysql -u securebank_user -p
```

Inside the MySQL shell:

```sql
-- See all databases
SHOW DATABASES;

-- Switch to SecureBank database
USE securebank_db;

-- See all tables (auto-created by Hibernate when app runs)
SHOW TABLES;

-- View customer accounts
SELECT * FROM customers;

-- View transaction history
SELECT * FROM transaction_history;

-- Exit
EXIT;
```

---

## 6. Quick Cheat Sheet

| Task | Command |
|---|---|
| **Install MySQL** | `sudo apt-get install mysql-server -y` |
| **Start MySQL** | `sudo systemctl start mysql` |
| **Stop MySQL** | `sudo systemctl stop mysql` |
| **Status** | `sudo systemctl status mysql` |
| **Login (first time)** | `sudo mysql -u root` |
| **Login (with password)** | `mysql -u root -p` |
| **Set/Change password** | `ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'NewPass';` |
| **Apply changes** | `FLUSH PRIVILEGES;` |
| **Show databases** | `SHOW DATABASES;` |
| **Use database** | `USE securebank_db;` |
| **Show tables** | `SHOW TABLES;` |
| **View customers** | `SELECT * FROM customers;` |
| **View transactions** | `SELECT * FROM transaction_history;` |
| **Exit MySQL** | `EXIT;` |

---

## 📝 Credentials Summary (Save These!)

| What | Value |
|---|---|
| **Database** | `securebank_db` |
| **App User** | `securebank_user` |
| **App Password** | `SecurePass@2026` |
| **Root User** | `root` |
| **Root Password** | `YourPassword@123` *(or whatever you set)* |
| **Default Port** | `3306` |

---

---

# 🔧 Reference Section

> 📌 **Use these only if needed** — they are for password recovery, troubleshooting, and clean-up scenarios.

---

## 7. Change Existing Root Password

Use this when root **already has a password** and you want to change it.

### Step 1: Login with the old password

```bash
mysql -u root -p
```

### Step 2: Set the new password

Inside the MySQL shell:

```sql
ALTER USER 'root'@'localhost'
  IDENTIFIED WITH mysql_native_password BY 'NewPassword@456';

FLUSH PRIVILEGES;

EXIT;
```

### Step 3: Test the new password

```bash
mysql -u root -p
```

---

## 8. Reset Forgotten Root Password

Use this when you **forgot the root password** and can't login.

### Step 1: Stop MySQL

```bash
sudo systemctl stop mysql
```

### Step 2: Start MySQL in safe mode (skip auth)

```bash
sudo mkdir -p /var/run/mysqld
sudo chown mysql:mysql /var/run/mysqld
sudo mysqld_safe --skip-grant-tables --skip-networking &
```

Wait 5-8 seconds. Press `Enter` if your terminal looks frozen.

### Step 3: Login without password

```bash
sudo mysql
```

### Step 4: Reset the password

Inside the MySQL shell:

```sql
FLUSH PRIVILEGES;

ALTER USER 'root'@'localhost'
  IDENTIFIED WITH mysql_native_password BY 'NewPassword@456';

FLUSH PRIVILEGES;

EXIT;
```

### Step 5: Stop safe mode and restart normally

```bash
sudo pkill mysqld
sleep 3
sudo systemctl start mysql
```

### Step 6: Test the new password

```bash
mysql -u root -p
```

---

## 9. Uninstall MySQL Completely

> 🚨 **WARNING:** This will delete **ALL databases and data**. Backup first if needed!

### Step 1: Backup (optional but recommended)

```bash
sudo mysqldump --all-databases -u root -p > /tmp/all_databases_backup.sql
```

### Step 2: Stop MySQL

```bash
sudo systemctl stop mysql
sudo systemctl disable mysql
```

### Step 3: Remove all MySQL packages

```bash
sudo apt-get remove --purge mysql-server mysql-client mysql-common \
  mysql-server-core-* mysql-client-core-* -y

sudo apt-get autoremove -y

sudo apt-get autoclean -y
```

### Step 4: Delete MySQL data and config folders

```bash
sudo rm -rf /etc/mysql
sudo rm -rf /var/lib/mysql
sudo rm -rf /var/log/mysql
sudo rm -rf /var/run/mysqld
```

### Step 5: Remove MySQL user (optional)

```bash
sudo deluser mysql 2>/dev/null
sudo delgroup mysql 2>/dev/null
```

### Step 6: Verify uninstall

```bash
# Should return nothing
which mysql

# Should show no MySQL packages
dpkg -l | grep mysql
```

---

## 10. Reinstall MySQL (Fresh Start)

If you uninstalled and want a clean install:

```bash
# Update package list
sudo apt update

# Reinstall MySQL Server
sudo apt-get install mysql-server -y

# Start MySQL
sudo systemctl start mysql

# Enable on boot
sudo systemctl enable mysql

# Verify
sudo systemctl status mysql
```

Then go back to **Section 3** to set the root password.

---

## ⚠️ Important Notes

| ⚠️ | Reminder |
|---|---|
| 🚨 | **Uninstall deletes ALL data** — there is no undo! Backup first. |
| 🔒 | Use **strong passwords** (12+ chars, mixed case, numbers, symbols). |
| 💾 | **Backup first** before uninstalling: `mysqldump --all-databases > backup.sql` |
| 🔑 | After password change, update **`application.properties`** in your Spring Boot apps! |
| 📌 | Default MySQL port is **3306** — open it in EC2 Security Group if needed. |

---

🛠️ **Trainer:** Rushi · **Brand:** Rushi Infotech — Wise Learners
