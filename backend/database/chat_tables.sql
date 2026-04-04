-- Create chats table
CREATE TABLE IF NOT EXISTS chats (
  chat_id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  manager_id INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES person(person_id) ON DELETE CASCADE,
  FOREIGN KEY (manager_id) REFERENCES person(person_id) ON DELETE CASCADE,
  INDEX idx_user (user_id),
  INDEX idx_manager (manager_id),
  INDEX idx_updated (updated_at)
);

-- Create messages table
CREATE TABLE IF NOT EXISTS messages (
  message_id INT PRIMARY KEY AUTO_INCREMENT,
  chat_id INT NOT NULL,
  sender_id INT NOT NULL,
  message_text TEXT NOT NULL,
  is_read TINYINT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
  FOREIGN KEY (sender_id) REFERENCES person(person_id) ON DELETE CASCADE,
  INDEX idx_chat (chat_id),
  INDEX idx_sender (sender_id),
  INDEX idx_created (created_at),
  INDEX idx_read (is_read)
);
