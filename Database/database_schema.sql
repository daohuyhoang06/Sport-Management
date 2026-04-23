-- ============================================================
-- Table: sport_types
-- Description: Stores types of sports (bóng đá, bóng chuyền, pickleball, cầu lông...)
-- ============================================================
CREATE TABLE sport_types (
  sport_id SERIAL PRIMARY KEY,
  sport_name VARCHAR(50) NOT NULL UNIQUE
);
-- ============================================================
-- DATABASE SCHEMA - Sport Field Management System (PostgreSQL/Supabase)
-- Generated: December 29, 2025
-- ============================================================

-- Drop tables if exists (in reverse order of dependencies)
DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS chats CASCADE;
DROP TABLE IF EXISTS revenue_monthly CASCADE;
DROP TABLE IF EXISTS revenue_weekly CASCADE;
DROP TABLE IF EXISTS revenue_daily CASCADE;
DROP TABLE IF EXISTS replies CASCADE;
DROP TABLE IF EXISTS feedbacks CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS password_resets CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS field_schedules CASCADE;
DROP TABLE IF EXISTS field_images CASCADE;
DROP TABLE IF EXISTS fields CASCADE;
DROP TABLE IF EXISTS person CASCADE;

-- ============================================================
-- Table: person
-- Description: Stores user information (customers, managers, admins)
-- ============================================================
CREATE TABLE person (
  person_id SERIAL PRIMARY KEY,
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
  fieldId INTEGER
);

CREATE INDEX idx_username ON person(username);
CREATE INDEX idx_email ON person(email);
CREATE INDEX idx_role ON person(role);

-- ============================================================
-- Table: fields
-- Description: Stores sport field information
-- ============================================================
CREATE TABLE fields (
  field_id SERIAL PRIMARY KEY,
  manager_id INTEGER,
  field_name VARCHAR(50) NOT NULL,
  location VARCHAR(100),
  status VARCHAR(45) DEFAULT 'active',
  rental_price DECIMAL(10,2),
  sport_id INTEGER,
  FOREIGN KEY (manager_id) REFERENCES person(person_id) ON DELETE SET NULL,
  FOREIGN KEY (sport_id) REFERENCES sport_types(sport_id) ON DELETE SET NULL
);

CREATE INDEX idx_manager_id ON fields(manager_id);
CREATE INDEX idx_status_fields ON fields(status);

-- ============================================================
-- Table: field_images
-- Description: Stores images for sport fields
-- ============================================================
CREATE TABLE field_images (
  image_id SERIAL PRIMARY KEY,
  field_id INTEGER NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  is_primary BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE
);

CREATE INDEX idx_field_id_images ON field_images(field_id);

-- ============================================================
-- Table: field_schedules
-- Description: Stores field availability schedules
-- ============================================================
CREATE TABLE field_schedules (
  schedule_id SERIAL PRIMARY KEY,
  field_id INTEGER NOT NULL,
  manager_id INTEGER,
  date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  status VARCHAR(45) DEFAULT 'available',
  price DECIMAL(10,2),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE,
  FOREIGN KEY (manager_id) REFERENCES person(person_id) ON DELETE SET NULL
);

CREATE INDEX idx_field_date ON field_schedules(field_id, date);
CREATE INDEX idx_status_schedules ON field_schedules(status);

-- ============================================================
-- Table: bookings
-- Description: Stores field booking information
-- ============================================================
CREATE TABLE bookings (
  booking_id SERIAL PRIMARY KEY,
  customer_id INTEGER NOT NULL,
  field_id INTEGER NOT NULL,
  manager_id INTEGER,
  start_time TIMESTAMP NOT NULL,
  end_time TIMESTAMP NOT NULL,
  status VARCHAR(45) DEFAULT 'pending',
  price DECIMAL(10,2),
  note TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (customer_id) REFERENCES person(person_id) ON DELETE CASCADE,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE,
  FOREIGN KEY (manager_id) REFERENCES person(person_id) ON DELETE SET NULL
);

CREATE INDEX idx_customer_id ON bookings(customer_id);
CREATE INDEX idx_field_id_bookings ON bookings(field_id);
CREATE INDEX idx_status_bookings ON bookings(status);
CREATE INDEX idx_start_time ON bookings(start_time);

-- ============================================================
-- Table: payments
-- Description: Stores payment information for bookings
-- ============================================================
CREATE TABLE payments (
  payment_id SERIAL PRIMARY KEY,
  booking_id INTEGER NOT NULL,
  customer_id INTEGER NOT NULL,
  field_id INTEGER NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  payment_method VARCHAR(45),
  payment_status VARCHAR(45) DEFAULT 'pending',
  transaction_id VARCHAR(100),
  paid_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
  FOREIGN KEY (customer_id) REFERENCES person(person_id) ON DELETE CASCADE,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE
);

CREATE INDEX idx_booking_id ON payments(booking_id);
CREATE INDEX idx_payment_status ON payments(payment_status);

-- ============================================================
-- Table: reviews
-- Description: Stores customer reviews for fields
-- ============================================================
CREATE TABLE reviews (
  review_id SERIAL PRIMARY KEY,
  customer_id INTEGER NOT NULL,
  field_id INTEGER NOT NULL,
  booking_id INTEGER,
  rating INTEGER NOT NULL,
  comment TEXT,
  images TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (customer_id) REFERENCES person(person_id) ON DELETE CASCADE,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE,
  FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE SET NULL
);

CREATE INDEX idx_customer_id_reviews ON reviews(customer_id);
CREATE INDEX idx_field_id_reviews ON reviews(field_id);
CREATE INDEX idx_rating ON reviews(rating);

-- ============================================================
-- Table: feedbacks
-- Description: Stores general feedback from users
-- ============================================================
CREATE TABLE feedbacks (
  feedback_id SERIAL PRIMARY KEY,
  person_id INTEGER NOT NULL,
  subject VARCHAR(100),
  message TEXT NOT NULL,
  status VARCHAR(45) DEFAULT 'pending',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (person_id) REFERENCES person(person_id) ON DELETE CASCADE
);

CREATE INDEX idx_person_id_feedbacks ON feedbacks(person_id);
CREATE INDEX idx_status_feedbacks ON feedbacks(status);

-- ============================================================
-- Table: replies
-- Description: Stores admin/manager replies to feedbacks
-- ============================================================
CREATE TABLE replies (
  reply_id SERIAL PRIMARY KEY,
  feedback_id INTEGER NOT NULL,
  admin_id INTEGER NOT NULL,
  message TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (feedback_id) REFERENCES feedbacks(feedback_id) ON DELETE CASCADE,
  FOREIGN KEY (admin_id) REFERENCES person(person_id) ON DELETE CASCADE
);

CREATE INDEX idx_feedback_id ON replies(feedback_id);

-- ============================================================
-- Table: revenue_daily
-- Description: Stores daily revenue statistics
-- ============================================================
CREATE TABLE revenue_daily (
  id SERIAL PRIMARY KEY,
  date DATE NOT NULL,
  field_id INTEGER,
  total_bookings INTEGER DEFAULT 0,
  total_revenue DECIMAL(10,2) DEFAULT 0.00,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE,
  UNIQUE (date, field_id)
);

CREATE INDEX idx_date_daily ON revenue_daily(date);

-- ============================================================
-- Table: revenue_weekly
-- Description: Stores weekly revenue statistics
-- ============================================================
CREATE TABLE revenue_weekly (
  id SERIAL PRIMARY KEY,
  year INTEGER NOT NULL,
  week INTEGER NOT NULL,
  field_id INTEGER,
  total_bookings INTEGER DEFAULT 0,
  total_revenue DECIMAL(10,2) DEFAULT 0.00,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE,
  UNIQUE (year, week, field_id)
);

CREATE INDEX idx_year_week ON revenue_weekly(year, week);

-- ============================================================
-- Table: revenue_monthly
-- Description: Stores monthly revenue statistics
-- ============================================================
CREATE TABLE revenue_monthly (
  id SERIAL PRIMARY KEY,
  year INTEGER NOT NULL,
  month INTEGER NOT NULL,
  field_id INTEGER,
  total_bookings INTEGER DEFAULT 0,
  total_revenue DECIMAL(10,2) DEFAULT 0.00,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE,
  UNIQUE (year, month, field_id)
);

CREATE INDEX idx_year_month ON revenue_monthly(year, month);

-- ============================================================
-- Table: password_resets
-- Description: Stores password reset tokens
-- ============================================================
CREATE TABLE password_resets (
  id SERIAL PRIMARY KEY,
  email VARCHAR(45) NOT NULL,
  token VARCHAR(255) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_resets ON password_resets(email);
CREATE INDEX idx_token ON password_resets(token);

-- ============================================================
-- Table: chats
-- Description: Stores chat conversations between users and managers
-- ============================================================
CREATE TABLE chats (
  chat_id SERIAL PRIMARY KEY,
  user_id INTEGER NOT NULL,
  manager_id INTEGER NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES person(person_id) ON DELETE CASCADE,
  FOREIGN KEY (manager_id) REFERENCES person(person_id) ON DELETE CASCADE
);

CREATE INDEX idx_user_chats ON chats(user_id);
CREATE INDEX idx_manager_chats ON chats(manager_id);
CREATE INDEX idx_updated_chats ON chats(updated_at);

-- ============================================================
-- Table: messages
-- Description: Stores individual messages in chat conversations
-- ============================================================
CREATE TABLE messages (
  message_id SERIAL PRIMARY KEY,
  chat_id INTEGER NOT NULL,
  sender_id INTEGER NOT NULL,
  message_text TEXT NOT NULL,
  is_read SMALLINT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
  FOREIGN KEY (sender_id) REFERENCES person(person_id) ON DELETE CASCADE
);

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
-- Create function for updated_at trigger
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- ============================================================
-- Add updated_at trigger to bookings
-- ============================================================
CREATE TRIGGER update_bookings_updated_at BEFORE UPDATE ON bookings
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- Add updated_at trigger to reviews
-- ============================================================
CREATE TRIGGER update_reviews_updated_at BEFORE UPDATE ON reviews
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
Add updated_at trigger to chats
-- ============================================================
CREATE TRIGGER update_chats_updated_at BEFORE UPDATE ON chats
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- 
-- ============================================================
-- SAMPLE DATA (Optional - Uncomment to insert)
-- ============================================================

-- Insert default admin account
-- Password: admin123 (hashed by application)
-- INSERT INTO person (person_name, username, password, email, role, status)
-- VALUES ('Administrator', 'admin', '$2b$10$placeholder', 'admin@example.com', 'admin', 'active');

-- ============================================================
-- END OF SCHEMA
-- ============================================================
-- 
-- Instructions for Supabase SQL Editor:
-- 1. Copy the entire script
-- 2. Paste into Supabase SQL Editor
-- 3. Click "Run" to execute
-- 4. Verify tables in Table Editor
--
-- Note: This is PostgreSQL syntax optimized for Supabase
-- ============================================================
