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
  person_name VARCHAR(50) NOT NULL,
  birthday DATE,
  sex VARCHAR(10),
  address VARCHAR(100),
  email VARCHAR(100) UNIQUE,
  phone VARCHAR(15),
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(20) DEFAULT 'user',
  status VARCHAR(20) DEFAULT 'active'
);

CREATE INDEX idx_username ON person(username);
CREATE INDEX idx_email ON person(email);
CREATE INDEX idx_role ON person(role);

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
  status VARCHAR(20) DEFAULT 'active',
  rental_price DECIMAL(10,2),

  FOREIGN KEY (manager_id) REFERENCES person(person_id) ON DELETE SET NULL,
  FOREIGN KEY (sport_id) REFERENCES sport_types(sport_id) ON DELETE CASCADE
);

CREATE INDEX idx_manager_id ON fields(manager_id);
CREATE INDEX idx_sport_id ON fields(sport_id);

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

CREATE INDEX idx_field_images ON field_images(field_id);

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
  status VARCHAR(20) DEFAULT 'available',
  price DECIMAL(10,2),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE,
  FOREIGN KEY (manager_id) REFERENCES person(person_id) ON DELETE SET NULL
);

CREATE INDEX idx_field_date ON field_schedules(field_id, date);

-- ============================================================
-- BOOKINGS
-- ============================================================
CREATE TABLE bookings (
  booking_id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NOT NULL,
  field_id INT NOT NULL,
  sport_id INT,
  manager_id INT,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  status VARCHAR(20) DEFAULT 'pending',
  price DECIMAL(10,2),
  note TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  FOREIGN KEY (customer_id) REFERENCES person(person_id) ON DELETE CASCADE,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE,
  FOREIGN KEY (manager_id) REFERENCES person(person_id) ON DELETE SET NULL,
  FOREIGN KEY (sport_id) REFERENCES sport_types(sport_id) ON DELETE SET NULL
);

CREATE INDEX idx_booking_field ON bookings(field_id);
CREATE INDEX idx_booking_customer ON bookings(customer_id);
CREATE INDEX idx_booking_sport ON bookings(sport_id);
CREATE INDEX idx_booking_time ON bookings(start_time);

-- ============================================================
-- ⚠️ NOTE: CHỐNG TRÙNG LỊCH
-- ============================================================
-- MySQL KHÔNG hỗ trợ EXCLUDE constraint
-- => Phải xử lý ở BACKEND bằng query:

-- SELECT * FROM bookings
-- WHERE field_id = ?
-- AND status IN ('pending', 'confirmed')
-- AND start_time < :new_end
-- AND end_time > :new_start;

-- ============================================================
-- PAYMENTS
-- ============================================================
CREATE TABLE payments (
  payment_id INT AUTO_INCREMENT PRIMARY KEY,
  booking_id INT NOT NULL,
  customer_id INT NOT NULL,
  field_id INT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  payment_method VARCHAR(50),
  payment_status VARCHAR(20) DEFAULT 'pending',
  transaction_id VARCHAR(100),
  paid_at DATETIME,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
  FOREIGN KEY (customer_id) REFERENCES person(person_id) ON DELETE CASCADE,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE
);

CREATE INDEX idx_payment_booking ON payments(booking_id);

-- ============================================================
-- REVIEWS
-- ============================================================
CREATE TABLE reviews (
  review_id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NOT NULL,
  field_id INT NOT NULL,
  booking_id INT,
  rating INT NOT NULL,
  comment TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (customer_id) REFERENCES person(person_id) ON DELETE CASCADE,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE
);

-- ============================================================
-- FEEDBACKS
-- ============================================================
CREATE TABLE feedbacks (
  feedback_id INT AUTO_INCREMENT PRIMARY KEY,
  person_id INT NOT NULL,
  subject VARCHAR(100),
  message TEXT,
  status VARCHAR(20) DEFAULT 'pending',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (person_id) REFERENCES person(person_id) ON DELETE CASCADE
);

-- ============================================================
-- REPLIES
-- ============================================================
CREATE TABLE replies (
  reply_id INT AUTO_INCREMENT PRIMARY KEY,
  feedback_id INT,
  admin_id INT,
  message TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (feedback_id) REFERENCES feedbacks(feedback_id) ON DELETE CASCADE,
  FOREIGN KEY (admin_id) REFERENCES person(person_id) ON DELETE CASCADE
);

-- ============================================================
-- REVENUE
-- ============================================================
CREATE TABLE revenue_daily (
  id INT AUTO_INCREMENT PRIMARY KEY,
  date DATE,
  field_id INT,
  total_bookings INT,
  total_revenue DECIMAL(10,2),
  UNIQUE KEY unique_daily (date, field_id)
);

CREATE TABLE revenue_weekly (
  id INT AUTO_INCREMENT PRIMARY KEY,
  year INT,
  week INT,
  field_id INT,
  total_bookings INT,
  total_revenue DECIMAL(10,2),
  UNIQUE KEY unique_weekly (year, week, field_id)
);

CREATE TABLE revenue_monthly (
  id INT AUTO_INCREMENT PRIMARY KEY,
  year INT,
  month INT,
  field_id INT,
  total_bookings INT,
  total_revenue DECIMAL(10,2),
  UNIQUE KEY unique_monthly (year, month, field_id)
);

-- ============================================================
-- PASSWORD RESET
-- ============================================================
CREATE TABLE password_resets (
  id INT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(100),
  token VARCHAR(255),
  expires_at DATETIME
);

-- ============================================================
-- CHAT
-- ============================================================
CREATE TABLE chats (
  chat_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT,
  manager_id INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  FOREIGN KEY (user_id) REFERENCES person(person_id) ON DELETE CASCADE,
  FOREIGN KEY (manager_id) REFERENCES person(person_id) ON DELETE CASCADE
);

CREATE TABLE messages (
  message_id INT AUTO_INCREMENT PRIMARY KEY,
  chat_id INT,
  sender_id INT,
  message_text TEXT,
  is_read BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
  FOREIGN KEY (sender_id) REFERENCES person(person_id) ON DELETE CASCADE
);

CREATE INDEX idx_chat_messages ON messages(chat_id);