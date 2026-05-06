-- ============================================================
-- SPORT MANAGEMENT SYSTEM - OPTIMIZED HYBRID VERSION
-- MySQL Full Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS sport_management;
USE sport_management;

-- ============================================================
-- SPORT TYPES
-- ============================================================
CREATE TABLE sport_types (
  sport_id INT AUTO_INCREMENT PRIMARY KEY,
  sport_name VARCHAR(50) NOT NULL UNIQUE
);

-- ============================================================
-- PERSON
-- ============================================================
CREATE TABLE person (
  person_id INT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(100) NOT NULL,
  birthday DATE,
  gender VARCHAR(10),
  address VARCHAR(255),
  email VARCHAR(100) UNIQUE,
  phone VARCHAR(15),
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role ENUM('user','manager','admin') DEFAULT 'user',
  status ENUM('active','inactive','banned') DEFAULT 'active',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_person_username ON person(username);
CREATE INDEX idx_person_email ON person(email);
CREATE INDEX idx_person_role ON person(role);

-- ============================================================
-- FIELDS
-- ============================================================
CREATE TABLE fields (
  field_id INT AUTO_INCREMENT PRIMARY KEY,
  manager_id INT,
  sport_id INT NOT NULL,

  field_name VARCHAR(100) NOT NULL,
  description TEXT,
  location VARCHAR(255),
  capacity INT,
  rental_price DECIMAL(10,2),

  status ENUM('active','inactive','maintenance') DEFAULT 'active',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (manager_id) REFERENCES person(person_id) ON DELETE SET NULL,
  FOREIGN KEY (sport_id) REFERENCES sport_types(sport_id) ON DELETE CASCADE
);

CREATE INDEX idx_fields_manager ON fields(manager_id);
CREATE INDEX idx_fields_sport ON fields(sport_id);

-- ============================================================
-- FIELD IMAGES
-- ============================================================
CREATE TABLE field_images (
  image_id INT AUTO_INCREMENT PRIMARY KEY,
  field_id INT NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  is_primary BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE
);

-- ============================================================
-- FIELD SCHEDULES
-- ============================================================
CREATE TABLE field_schedules (
  schedule_id INT AUTO_INCREMENT PRIMARY KEY,
  field_id INT NOT NULL,
  manager_id INT,

  date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,

  status ENUM('available','blocked','booked') DEFAULT 'available',
  price DECIMAL(10,2),

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE,
  FOREIGN KEY (manager_id) REFERENCES person(person_id) ON DELETE SET NULL
);

CREATE INDEX idx_schedule_field_date ON field_schedules(field_id, date);

-- ============================================================
-- BOOKINGS
-- ============================================================
CREATE TABLE bookings (
  booking_id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NOT NULL,
  field_id INT NOT NULL,
  sport_id INT,

  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,

  status ENUM('pending','confirmed','cancelled','completed') DEFAULT 'pending',

  price DECIMAL(10,2),
  note TEXT,

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  FOREIGN KEY (customer_id) REFERENCES person(person_id) ON DELETE CASCADE,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE,
  FOREIGN KEY (sport_id) REFERENCES sport_types(sport_id) ON DELETE SET NULL
);

CREATE INDEX idx_booking_field ON bookings(field_id);
CREATE INDEX idx_booking_customer ON bookings(customer_id);
CREATE INDEX idx_booking_time ON bookings(start_time);

-- ============================================================
-- PAYMENTS
-- ============================================================
CREATE TABLE payments (
  payment_id INT AUTO_INCREMENT PRIMARY KEY,
  booking_id INT NOT NULL,
  customer_id INT NOT NULL,

  amount DECIMAL(10,2) NOT NULL,
  payment_method ENUM('cash','bank_transfer','momo','vnpay'),
  payment_status ENUM('pending','paid','failed','refunded') DEFAULT 'pending',

  transaction_id VARCHAR(100),
  paid_at DATETIME,

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
  FOREIGN KEY (customer_id) REFERENCES person(person_id) ON DELETE CASCADE
);

-- ============================================================
-- REVIEWS
-- ============================================================
CREATE TABLE reviews (
  review_id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NOT NULL,
  field_id INT NOT NULL,
  booking_id INT,

  rating INT CHECK (rating BETWEEN 1 AND 5),
  comment TEXT,

  images JSON,

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (customer_id) REFERENCES person(person_id) ON DELETE CASCADE,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE,
  FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE SET NULL
);

-- ============================================================
-- FEEDBACKS
-- ============================================================
CREATE TABLE feedbacks (
  feedback_id INT AUTO_INCREMENT PRIMARY KEY,
  person_id INT NOT NULL,
  subject VARCHAR(100),
  message TEXT NOT NULL,
  status ENUM('pending','resolved') DEFAULT 'pending',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (person_id) REFERENCES person(person_id) ON DELETE CASCADE
);

-- ============================================================
-- REPLIES
-- ============================================================
CREATE TABLE replies (
  reply_id INT AUTO_INCREMENT PRIMARY KEY,
  feedback_id INT NOT NULL,
  admin_id INT NOT NULL,

  message TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (feedback_id) REFERENCES feedbacks(feedback_id) ON DELETE CASCADE,
  FOREIGN KEY (admin_id) REFERENCES person(person_id) ON DELETE CASCADE
);

-- ============================================================
-- REVENUE DAILY
-- ============================================================
CREATE TABLE revenue_daily (
  id INT AUTO_INCREMENT PRIMARY KEY,
  date DATE NOT NULL,
  field_id INT NOT NULL,

  total_bookings INT DEFAULT 0,
  total_revenue DECIMAL(10,2) DEFAULT 0,

  UNIQUE KEY uq_daily (date, field_id),

  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE
);

-- ============================================================
-- REVENUE WEEKLY
-- ============================================================
CREATE TABLE revenue_weekly (
  id INT AUTO_INCREMENT PRIMARY KEY,
  year INT NOT NULL,
  week INT NOT NULL,
  field_id INT NOT NULL,

  total_bookings INT DEFAULT 0,
  total_revenue DECIMAL(10,2) DEFAULT 0,

  UNIQUE KEY uq_weekly (year, week, field_id),

  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE
);

-- ============================================================
-- REVENUE MONTHLY
-- ============================================================
CREATE TABLE revenue_monthly (
  id INT AUTO_INCREMENT PRIMARY KEY,
  year INT NOT NULL,
  month INT NOT NULL,
  field_id INT NOT NULL,

  total_bookings INT DEFAULT 0,
  total_revenue DECIMAL(10,2) DEFAULT 0,

  UNIQUE KEY uq_monthly (year, month, field_id),

  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE
);

-- ============================================================
-- PASSWORD RESET
-- ============================================================
CREATE TABLE password_resets (
  id INT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(100) NOT NULL,
  token VARCHAR(255) NOT NULL,
  expires_at DATETIME NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- CHATS
-- ============================================================
CREATE TABLE chats (
  chat_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  manager_id INT NOT NULL,

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  FOREIGN KEY (user_id) REFERENCES person(person_id) ON DELETE CASCADE,
  FOREIGN KEY (manager_id) REFERENCES person(person_id) ON DELETE CASCADE
);

-- ============================================================
-- MESSAGES
-- ============================================================
CREATE TABLE messages (
  message_id INT AUTO_INCREMENT PRIMARY KEY,
  chat_id INT NOT NULL,
  sender_id INT NOT NULL,

  message_text TEXT NOT NULL,
  is_read BOOLEAN DEFAULT FALSE,

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
  FOREIGN KEY (sender_id) REFERENCES person(person_id) ON DELETE CASCADE
);

CREATE INDEX idx_messages_chat ON messages(chat_id);

-- ============================================================
-- END
-- ============================================================