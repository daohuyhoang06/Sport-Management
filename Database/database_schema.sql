-- ============================================================
-- DATABASE SCHEMA - Sport Field Management System (MySQL)
-- Generated: December 29, 2025
-- ============================================================

-- Drop tables if exists (in reverse order of dependencies)
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS chats;
DROP TABLE IF EXISTS revenue_monthly;
DROP TABLE IF EXISTS revenue_weekly;
DROP TABLE IF EXISTS revenue_daily;
DROP TABLE IF EXISTS replies;
DROP TABLE IF EXISTS feedbacks;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS password_resets;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS field_schedules;
DROP TABLE IF EXISTS field_images;
DROP TABLE IF EXISTS fields;
DROP TABLE IF EXISTS sport_types;
DROP TABLE IF EXISTS person;

-- ============================================================
-- Table: person
-- Description: Stores user information (customers, managers, admins)
-- ============================================================
CREATE TABLE person (
  person_id INT AUTO_INCREMENT PRIMARY KEY,
  person_name VARCHAR(50) NOT NULL,
  birthday DATE,
  sex VARCHAR(10),
  address VARCHAR(45),
  email VARCHAR(45) UNIQUE,
  phone VARCHAR(10),
  username VARCHAR(45) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(45) DEFAULT 'user',
  status VARCHAR(45) DEFAULT 'active',
  fieldId INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_username ON person(username);
CREATE INDEX idx_email ON person(email);
CREATE INDEX idx_role ON person(role);

-- ============================================================
-- Table: sport_types
-- Description: Stores types of sports (bóng đá, bóng chuyền, pickleball, cầu lông...)
-- ============================================================
CREATE TABLE sport_types (
  sport_id INT AUTO_INCREMENT PRIMARY KEY,
  sport_name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Table: fields
-- Description: Stores sport field information
-- ============================================================
CREATE TABLE fields (
  field_id INT AUTO_INCREMENT PRIMARY KEY,
  manager_id INT,
  field_name VARCHAR(50) NOT NULL,
  location VARCHAR(100),
  status VARCHAR(45) DEFAULT 'active',
  rental_price DECIMAL(10,2),
  sport_id INT,
  FOREIGN KEY (manager_id) REFERENCES person(person_id) ON DELETE SET NULL,
  FOREIGN KEY (sport_id) REFERENCES sport_types(sport_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_manager_id ON fields(manager_id);
CREATE INDEX idx_status_fields ON fields(status);

-- ============================================================
-- Table: field_images
-- Description: Stores images for sport fields
-- ============================================================
CREATE TABLE field_images (
  image_id INT AUTO_INCREMENT PRIMARY KEY,
  field_id INT NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  is_primary BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_field_id_images ON field_images(field_id);

-- ============================================================
-- Table: field_schedules
-- Description: Stores field availability schedules
-- ============================================================
CREATE TABLE field_schedules (
  schedule_id INT AUTO_INCREMENT PRIMARY KEY,
  field_id INT NOT NULL,
  manager_id INT,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  is_available BOOLEAN NOT NULL DEFAULT TRUE,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE,
  FOREIGN KEY (manager_id) REFERENCES person(person_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_field_schedule_times ON field_schedules(field_id, start_time, end_time);
CREATE INDEX idx_manager_schedules ON field_schedules(manager_id);

-- ============================================================
-- Table: bookings
-- Description: Stores field booking information
-- ============================================================
CREATE TABLE bookings (
  booking_id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NOT NULL,
  field_id INT NOT NULL,
  manager_id INT,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  status VARCHAR(45) DEFAULT 'pending',
  price DECIMAL(10,2),
  note TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (customer_id) REFERENCES person(person_id) ON DELETE CASCADE,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE,
  FOREIGN KEY (manager_id) REFERENCES person(person_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_customer_id ON bookings(customer_id);
CREATE INDEX idx_field_id_bookings ON bookings(field_id);
CREATE INDEX idx_status_bookings ON bookings(status);
CREATE INDEX idx_start_time ON bookings(start_time);

-- ============================================================
-- Table: payments
-- Description: Stores payment information for bookings
-- ============================================================
CREATE TABLE payments (
  payment_id INT AUTO_INCREMENT PRIMARY KEY,
  booking_id INT NOT NULL,
  customer_id INT NOT NULL,
  field_id INT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  payment_method VARCHAR(45),
  payment_status VARCHAR(45) DEFAULT 'pending',
  transaction_id VARCHAR(100),
  paid_at TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
  FOREIGN KEY (customer_id) REFERENCES person(person_id) ON DELETE CASCADE,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_booking_id ON payments(booking_id);
CREATE INDEX idx_payment_status ON payments(payment_status);

-- ============================================================
-- Table: reviews
-- Description: Stores customer reviews for fields
-- ============================================================
CREATE TABLE reviews (
  review_id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NOT NULL,
  field_id INT NOT NULL,
  booking_id INT,
  rating INT NOT NULL,
  comment TEXT,
  images TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (customer_id) REFERENCES person(person_id) ON DELETE CASCADE,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE,
  FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_customer_id_reviews ON reviews(customer_id);
CREATE INDEX idx_field_id_reviews ON reviews(field_id);
CREATE INDEX idx_rating ON reviews(rating);

-- ============================================================
-- Table: feedbacks
-- Description: Stores general feedback from users
-- ============================================================
CREATE TABLE feedbacks (
  feedback_id INT AUTO_INCREMENT PRIMARY KEY,
  person_id INT NOT NULL,
  subject VARCHAR(100),
  message TEXT NOT NULL,
  status VARCHAR(45) DEFAULT 'pending',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (person_id) REFERENCES person(person_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_person_id_feedbacks ON feedbacks(person_id);
CREATE INDEX idx_status_feedbacks ON feedbacks(status);

-- ============================================================
-- Table: replies
-- Description: Stores admin/manager replies to feedbacks
-- ============================================================
CREATE TABLE replies (
  reply_id INT AUTO_INCREMENT PRIMARY KEY,
  feedback_id INT NOT NULL,
  admin_id INT NOT NULL,
  message TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (feedback_id) REFERENCES feedbacks(feedback_id) ON DELETE CASCADE,
  FOREIGN KEY (admin_id) REFERENCES person(person_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_feedback_id ON replies(feedback_id);

-- ============================================================
-- Table: revenue_daily
-- Description: Stores daily revenue statistics
-- ============================================================
CREATE TABLE revenue_daily (
  id INT AUTO_INCREMENT PRIMARY KEY,
  date DATE NOT NULL,
  field_id INT,
  total_bookings INT DEFAULT 0,
  total_revenue DECIMAL(10,2) DEFAULT 0.00,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE,
  UNIQUE KEY uq_date_field (date, field_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_date_daily ON revenue_daily(date);

-- ============================================================
-- Table: revenue_weekly
-- Description: Stores weekly revenue statistics
-- ============================================================
CREATE TABLE revenue_weekly (
  id INT AUTO_INCREMENT PRIMARY KEY,
  year INT NOT NULL,
  week INT NOT NULL,
  field_id INT,
  total_bookings INT DEFAULT 0,
  total_revenue DECIMAL(10,2) DEFAULT 0.00,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE,
  UNIQUE KEY uq_year_week_field (year, week, field_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_year_week ON revenue_weekly(year, week);

-- ============================================================
-- Table: revenue_monthly
-- Description: Stores monthly revenue statistics
-- ============================================================
CREATE TABLE revenue_monthly (
  id INT AUTO_INCREMENT PRIMARY KEY,
  year INT NOT NULL,
  month INT NOT NULL,
  field_id INT,
  total_bookings INT DEFAULT 0,
  total_revenue DECIMAL(10,2) DEFAULT 0.00,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE,
  UNIQUE KEY uq_year_month_field (year, month, field_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_year_month ON revenue_monthly(year, month);

-- ============================================================
-- Table: password_resets
-- Description: Stores password reset tokens
-- ============================================================
CREATE TABLE password_resets (
  id INT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(45) NOT NULL,
  token VARCHAR(255) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_email_resets ON password_resets(email);
CREATE INDEX idx_token ON password_resets(token);

-- ============================================================
-- Table: chats
-- Description: Stores chat conversations between users and managers
-- ============================================================
CREATE TABLE chats (
  chat_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  manager_id INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES person(person_id) ON DELETE CASCADE,
  FOREIGN KEY (manager_id) REFERENCES person(person_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_user_chats ON chats(user_id);
CREATE INDEX idx_manager_chats ON chats(manager_id);
CREATE INDEX idx_updated_chats ON chats(updated_at);

-- ============================================================
-- Table: messages
-- Description: Stores individual messages in chat conversations
-- ============================================================
CREATE TABLE messages (
  message_id INT AUTO_INCREMENT PRIMARY KEY,
  chat_id INT NOT NULL,
  sender_id INT NOT NULL,
  message_text TEXT NOT NULL,
  is_read TINYINT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
  FOREIGN KEY (sender_id) REFERENCES person(person_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_chat_messages ON messages(chat_id);
CREATE INDEX idx_sender_messages ON messages(sender_id);
CREATE INDEX idx_created_messages ON messages(created_at);
CREATE INDEX idx_read_messages ON messages(is_read);

-- ============================================================
-- Add foreign key for person.fieldId
-- ============================================================
ALTER TABLE person
  ADD CONSTRAINT fk_person_field
  FOREIGN KEY (fieldId) REFERENCES fields(field_id) ON DELETE SET NULL;

-- ============================================================
-- END OF SCHEMA
-- ============================================================
--
-- Instructions:
-- 1. Create a MySQL database first: CREATE DATABASE sport_management;
-- 2. USE sport_management;
-- 3. Run this script
-- 4. Verify tables with: SHOW TABLES;
-- ============================================================