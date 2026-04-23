-- ============================================================
-- SAMPLE DATA INSERT - Sport Field Management System
-- PostgreSQL/Supabase Version
-- Generated: December 29, 2025
-- ============================================================

-- ============================================================
-- Insert Users (Admins, Managers, Regular Users)
-- ============================================================

-- Admin accounts (password: admin123 - must be hashed by application)
INSERT INTO person (person_name, birthday, sex, address, email, phone, username, password, role, status) VALUES
('Nguyễn Văn Admin', '1990-01-15', 'Nam', '88 Tôn Thất Thuyết, Cầu Giấy, Hà Nội', 'admin@gmail.com', '0901234567', 'admin', '$2b$10$YourHashedPasswordHere', 'admin', 'active'),
('Trần Thị Quản Trị', '1992-03-20', 'Nữ', '201 Láng Hạ, Đống Đa, Hà Nội', 'admin2@gmail.com', '0902345678', 'admin2', '$2b$10$YourHashedPasswordHere', 'admin', 'active');

-- Manager accounts (password: manager123)
INSERT INTO person (person_name, birthday, sex, address, email, phone, username, password, role, status) VALUES
('Lê Minh Quản', '1988-05-10', 'Nam', '123 Dịch Vọng Hậu, Cầu Giấy, Hà Nội', 'manager1@gmail.com', '0903456789', 'manager1', '$2b$10$YourHashedPasswordHere', 'manager', 'active'),
('Phạm Thu Hương', '1991-07-25', 'Nữ', '45 Trung Hòa, Cầu Giấy, Hà Nội', 'manager2@gmail.com', '0904567890', 'manager2', '$2b$10$YourHashedPasswordHere', 'manager', 'active'),
('Hoàng Văn Tùng', '1989-09-12', 'Nam', '156 Duy Tân, Cầu Giấy, Hà Nội', 'manager3@gmail.com', '0905678901', 'manager3', '$2b$10$YourHashedPasswordHere', 'manager', 'active'),
('Đặng Thị Mai', '1993-11-08', 'Nữ', '92 Xuân Thủy, Cầu Giấy, Hà Nội', 'manager4@gmail.com', '0906789012', 'manager4', '$2b$10$YourHashedPasswordHere', 'manager', 'active');

-- Regular users (password: user123)
INSERT INTO person (person_name, birthday, sex, address, email, phone, username, password, role, status) VALUES
('Nguyễn Văn An', '1995-02-14', 'Nam', '56 Cầu Giấy, Cầu Giấy, Hà Nội', 'user1@gmail.com', '0907890123', 'user1', '$2b$10$YourHashedPasswordHere', 'user', 'active'),
('Trần Thị Bình', '1996-04-20', 'Nữ', '188 Nguyễn Khang, Cầu Giấy, Hà Nội', 'user2@gmail.com', '0908901234', 'user2', '$2b$10$YourHashedPasswordHere', 'user', 'active'),
('Lê Quang Cường', '1994-06-30', 'Nam', '67 Nghĩa Tân, Cầu Giấy, Hà Nội', 'user3@gmail.com', '0909012345', 'user3', '$2b$10$YourHashedPasswordHere', 'user', 'active'),
('Phạm Thu Dung', '1997-08-15', 'Nữ', '234 Trần Kim Xuyến, Cầu Giấy, Hà Nội', 'user4@gmail.com', '0910123456', 'user4', '$2b$10$YourHashedPasswordHere', 'user', 'active'),
('Hoàng Minh Em', '1998-10-05', 'Nam', '78 Phạm Văn Đồng, Cầu Giấy, Hà Nội', 'user5@gmail.com', '0911234567', 'user5', '$2b$10$YourHashedPasswordHere', 'user', 'active'),
('Vũ Thị Phượng', '1995-12-22', 'Nữ', '145 Hoàng Quốc Việt, Cầu Giấy, Hà Nội', 'user6@gmail.com', '0912345678', 'user6', '$2b$10$YourHashedPasswordHere', 'user', 'active'),
('Bùi Văn Giang', '1996-03-18', 'Nam', '99 Trần Thái Tông, Cầu Giấy, Hà Nội', 'user7@gmail.com', '0913456789', 'user7', '$2b$10$YourHashedPasswordHere', 'user', 'active'),
('Đỗ Thị Hà', '1994-05-28', 'Nữ', '167 Chùa Bộc, Đống Đa, Hà Nội', 'user8@gmail.com', '0914567890', 'user8', '$2b$10$YourHashedPasswordHere', 'user', 'active'),
('Ngô Văn Khải', '1997-07-11', 'Nam', '89 Phạm Hùng, Nam Từ Liêm, Hà Nội', 'user9@gmail.com', '0915678901', 'user9', '$2b$10$YourHashedPasswordHere', 'user', 'active'),
('Trương Thị Lan', '1998-09-03', 'Nữ', '210 Nguyễn Văn Huyên, Cầu Giấy, Hà Nội', 'user10@gmail.com', '0916789012', 'user10', '$2b$10$YourHashedPasswordHere', 'user', 'active');

-- ============================================================
-- Insert sport fields (Hanoi - Cầu Giấy area)
-- ============================================================

INSERT INTO fields (manager_id, field_name, location, status, rental_price, sport_id) VALUES
(3, 'Sân Bóng Dịch Vọng', 'Số 123 Dịch Vọng Hậu, Cầu Giấy, Hà Nội', 'active', 300000, 1),
(3, 'Sân Bóng Trung Hòa', 'Số 45 Trung Hòa, Cầu Giấy, Hà Nội', 'active', 350000, 1),
(4, 'Sân Bóng Mỹ Đình', 'Số 89 Phạm Hùng, Nam Từ Liêm, Hà Nội', 'active', 280000, 1),
(4, 'Sân Bóng Nghĩa Tân', 'Số 67 Nghĩa Tân, Cầu Giấy, Hà Nội', 'active', 320000, 1),
(5, 'Sân Bóng Duy Tân', 'Số 156 Duy Tân, Cầu Giấy, Hà Nội', 'active', 400000, 1),
(5, 'Sân Bóng Yên Hòa', 'Số 234 Trần Kim Xuyến, Cầu Giấy, Hà Nội', 'active', 290000, 1),
(6, 'Sân Bóng Mai Dịch', 'Số 78 Phạm Văn Đồng, Cầu Giấy, Hà Nội', 'active', 380000, 1),
(6, 'Sân Bóng Xuân Thủy', 'Số 92 Xuân Thủy, Cầu Giấy, Hà Nội', 'active', 420000, 1),
(3, 'Sân Bóng Hoàng Quốc Việt', 'Số 145 Hoàng Quốc Việt, Cầu Giấy, Hà Nội', 'active', 310000, 1),
(4, 'Sân Bóng Cầu Giấy', 'Số 56 Cầu Giấy, Cầu Giấy, Hà Nội', 'active', 360000, 1),
(5, 'Sân Bóng Nguyễn Khang', 'Số 188 Nguyễn Khang, Cầu Giấy, Hà Nội', 'active', 330000, 1),
(6, 'Sân Bóng Trần Thái Tông', 'Số 99 Trần Thái Tông, Cầu Giấy, Hà Nội', 'active', 340000, 1),
(3, 'Sân Bóng Láng Hạ', 'Số 201 Láng Hạ, Đống Đa, Hà Nội', 'active', 320000, 1),
(4, 'Sân Bóng Chùa Bộc', 'Số 167 Chùa Bộc, Đống Đa, Hà Nội', 'active', 300000, 1),
(5, 'Sân Bóng Tôn Thất Thuyết', 'Số 88 Tôn Thất Thuyết, Cầu Giấy, Hà Nội', 'active', 370000, 1),
(NULL, 'Sân Bóng Nguyễn Văn Huyên', 'Số 210 Nguyễn Văn Huyên, Cầu Giấy, Hà Nội', 'maintenance', 350000, 1);

-- ============================================================
-- Insert Field Images
-- ============================================================

INSERT INTO field_images (field_id, image_url, is_primary) VALUES
(1, '/uploads/fields/field1_main.jpg', true),
(1, '/uploads/fields/field1_view1.jpg', false),
(1, '/uploads/fields/field1_view2.jpg', false),
(2, '/uploads/fields/field2_main.jpg', true),
(2, '/uploads/fields/field2_view1.jpg', false),
(3, '/uploads/fields/field3_main.jpg', true),
(3, '/uploads/fields/field3_view1.jpg', false),
(4, '/uploads/fields/field4_main.jpg', true),
(5, '/uploads/fields/field5_main.jpg', true),
(5, '/uploads/fields/field5_view1.jpg', false),
(6, '/uploads/fields/field6_main.jpg', true),
(7, '/uploads/fields/field7_main.jpg', true),
(8, '/uploads/fields/field8_main.jpg', true),
(9, '/uploads/fields/field9_main.jpg', true),
(10, '/uploads/fields/field10_main.jpg', true),
(11, '/uploads/fields/field11_main.jpg', true),
(12, '/uploads/fields/field12_main.jpg', true),
(13, '/uploads/fields/field13_main.jpg', true),
(14, '/uploads/fields/field14_main.jpg', true),
(15, '/uploads/fields/field15_main.jpg', true),
(16, '/uploads/fields/field16_main.jpg', true);

-- ============================================================
-- Insert Field Schedules (Next 7 days, 6 time slots per day)
-- ============================================================

-- Field 1 schedules for next 7 days
INSERT INTO field_schedules (field_id, manager_id, date, start_time, end_time, status, price) VALUES
-- Day 1
(1, 3, CURRENT_DATE, '06:00:00', '09:00:00', 'available', 250000),
(1, 3, CURRENT_DATE, '09:00:00', '12:00:00', 'available', 300000),
(1, 3, CURRENT_DATE, '12:00:00', '14:00:00', 'available', 280000),
(1, 3, CURRENT_DATE, '14:00:00', '17:00:00', 'available', 300000),
(1, 3, CURRENT_DATE, '17:00:00', '19:00:00', 'available', 350000),
(1, 3, CURRENT_DATE, '19:00:00', '22:00:00', 'available', 400000),
-- Day 2
(1, 3, CURRENT_DATE + 1, '06:00:00', '09:00:00', 'available', 250000),
(1, 3, CURRENT_DATE + 1, '09:00:00', '12:00:00', 'available', 300000),
(1, 3, CURRENT_DATE + 1, '12:00:00', '14:00:00', 'booked', 280000),
(1, 3, CURRENT_DATE + 1, '14:00:00', '17:00:00', 'available', 300000),
(1, 3, CURRENT_DATE + 1, '17:00:00', '19:00:00', 'available', 350000),
(1, 3, CURRENT_DATE + 1, '19:00:00', '22:00:00', 'available', 400000);

-- Field 2 schedules
INSERT INTO field_schedules (field_id, manager_id, date, start_time, end_time, status, price) VALUES
(2, 3, CURRENT_DATE, '06:00:00', '09:00:00', 'available', 280000),
(2, 3, CURRENT_DATE, '09:00:00', '12:00:00', 'available', 350000),
(2, 3, CURRENT_DATE, '14:00:00', '17:00:00', 'available', 350000),
(2, 3, CURRENT_DATE, '17:00:00', '19:00:00', 'available', 380000),
(2, 3, CURRENT_DATE, '19:00:00', '22:00:00', 'available', 450000);

-- ============================================================
-- Insert Bookings
-- ============================================================

INSERT INTO bookings (customer_id, field_id, manager_id, start_time, end_time, status, price, note) VALUES
(7, 1, 3, CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '17 hours', CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '19 hours', 'completed', 350000, 'Đặt sân cho đội công ty'),
(8, 1, 3, CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '19 hours', CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '22 hours', 'completed', 400000, 'Trận giao hữu cuối tuần'),
(9, 2, 3, CURRENT_TIMESTAMP - INTERVAL '3 days' + INTERVAL '14 hours', CURRENT_TIMESTAMP - INTERVAL '3 days' + INTERVAL '17 hours', 'completed', 350000, 'Tập luyện thường xuyên'),
(10, 3, 4, CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '09 hours', CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '12 hours', 'completed', 280000, 'Đặt sân cho nhóm bạn'),
(11, 4, 4, CURRENT_TIMESTAMP + INTERVAL '1 day' + INTERVAL '17 hours', CURRENT_TIMESTAMP + INTERVAL '1 day' + INTERVAL '19 hours', 'confirmed', 320000, 'Đặt sân tối mai'),
(12, 5, 5, CURRENT_TIMESTAMP + INTERVAL '2 days' + INTERVAL '19 hours', CURRENT_TIMESTAMP + INTERVAL '2 days' + INTERVAL '22 hours', 'pending', 400000, 'Trận đấu quan trọng'),
(13, 6, 5, CURRENT_TIMESTAMP + INTERVAL '3 days' + INTERVAL '14 hours', CURRENT_TIMESTAMP + INTERVAL '3 days' + INTERVAL '17 hours', 'pending', 290000, NULL),
(14, 7, 6, CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '17 hours', CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '19 hours', 'completed', 380000, 'Đội bóng trường'),
(15, 8, 6, CURRENT_TIMESTAMP - INTERVAL '4 days' + INTERVAL '19 hours', CURRENT_TIMESTAMP - INTERVAL '4 days' + INTERVAL '22 hours', 'completed', 420000, 'Sinh nhật team'),
(16, 9, 3, CURRENT_TIMESTAMP - INTERVAL '6 days' + INTERVAL '09 hours', CURRENT_TIMESTAMP - INTERVAL '6 days' + INTERVAL '12 hours', 'completed', 310000, NULL);

-- ============================================================
-- Insert Payments
-- ============================================================

INSERT INTO payments (booking_id, customer_id, field_id, amount, payment_method, payment_status, transaction_id, paid_at) VALUES
(1, 7, 1, 350000, 'cash', 'completed', NULL, CURRENT_TIMESTAMP - INTERVAL '2 days'),
(2, 8, 1, 400000, 'momo', 'completed', 'MOMO123456789', CURRENT_TIMESTAMP - INTERVAL '1 day'),
(3, 9, 2, 350000, 'bank_transfer', 'completed', 'BANK987654321', CURRENT_TIMESTAMP - INTERVAL '3 days'),
(4, 10, 3, 280000, 'cash', 'completed', NULL, CURRENT_TIMESTAMP - INTERVAL '5 days'),
(5, 11, 4, 320000, 'momo', 'completed', 'MOMO111222333', CURRENT_TIMESTAMP + INTERVAL '1 day'),
(6, 12, 5, 400000, 'momo', 'pending', NULL, NULL),
(7, 13, 6, 290000, 'cash', 'pending', NULL, NULL),
(8, 14, 7, 380000, 'bank_transfer', 'completed', 'BANK444555666', CURRENT_TIMESTAMP - INTERVAL '1 day'),
(9, 15, 8, 420000, 'momo', 'completed', 'MOMO777888999', CURRENT_TIMESTAMP - INTERVAL '4 days'),
(10, 16, 9, 310000, 'cash', 'completed', NULL, CURRENT_TIMESTAMP - INTERVAL '6 days');

-- ============================================================
-- Insert Reviews
-- ============================================================

INSERT INTO reviews (customer_id, field_id, booking_id, rating, comment, images) VALUES
(7, 1, 1, 5, 'Sân đẹp, cỏ xanh tốt, tiện nghi đầy đủ. Tôi rất hài lòng!', '[]'),
(8, 1, 2, 5, 'Chất lượng tốt, giá cả hợp lý. Sẽ quay lại lần sau.', '[]'),
(9, 2, 3, 4, 'Sân rộng rãi, có chỗ đỗ xe tiện lợi. Nhân viên thân thiện.', '[]'),
(10, 3, 4, 5, 'Mặt sân phẳng, cỏ được chăm sóc tốt. Phòng thay đồ sạch sẽ.', '["review1.jpg","review2.jpg"]'),
(14, 7, 8, 4, 'Vị trí thuận tiện, dễ tìm. Có căng tin phục vụ đồ uống.', '[]'),
(15, 8, 9, 5, 'Sân bóng chất lượng cao, ánh sáng tốt vào buổi tối.', '["review3.jpg"]'),
(16, 9, 10, 4, 'Giá thuê hợp lý, dịch vụ chu đáo. Rất đáng để trải nghiệm.', '[]');

-- ============================================================
-- Insert Feedbacks
-- ============================================================

INSERT INTO feedbacks (person_id, subject, message, status) VALUES
(7, 'Đề xuất cải thiện', 'Mong sân có thêm điều hòa ở phòng chờ.', 'pending'),
(8, 'Phản hồi tích cực', 'Dịch vụ rất tốt, nhân viên nhiệt tình.', 'resolved'),
(9, 'Yêu cầu hỗ trợ', 'Cần hỗ trợ đặt lịch định kỳ hàng tuần.', 'in_progress'),
(10, 'Góp ý', 'Nên có thêm gói ưu đãi cho khách hàng thường xuyên.', 'pending'),
(11, 'Thắc mắc', 'Làm sao để trở thành thành viên VIP?', 'resolved');

-- ============================================================
-- Insert Replies to Feedbacks
-- ============================================================

INSERT INTO replies (feedback_id, admin_id, message) VALUES
(2, 1, 'Cảm ơn bạn đã góp ý. Chúng tôi sẽ tiếp tục cải thiện dịch vụ.'),
(5, 1, 'Để trở thành thành viên VIP, bạn cần đặt sân ít nhất 10 lần trong 3 tháng. Chúng tôi sẽ liên hệ trực tiếp với bạn.');

-- ============================================================
-- Insert Chats
-- ============================================================

INSERT INTO chats (user_id, manager_id) VALUES
(7, 3),
(8, 3),
(9, 4),
(10, 4),
(11, 5),
(12, 6);

-- ============================================================
-- Insert Messages
-- ============================================================

INSERT INTO messages (chat_id, sender_id, message_text, is_read) VALUES
-- Chat 1 (User 7 với Manager 3)
(1, 7, 'Chào anh, tôi muốn đặt sân vào tối mai được không?', 1),
(1, 3, 'Chào bạn, tối mai sân còn trống từ 19h-22h. Bạn có muốn đặt không?', 1),
(1, 7, 'Vâng, tôi đặt khung giờ đó. Giá bao nhiêu ạ?', 1),
(1, 3, 'Giá 400.000đ cho 3 tiếng. Bạn đặt qua hệ thống hoặc tôi hỗ trợ đặt cho bạn.', 1),
(1, 7, 'Tôi đặt qua app luôn. Cảm ơn anh!', 1),

-- Chat 2 (User 8 với Manager 3)
(2, 8, 'Sân có phục vụ nước uống không anh?', 1),
(2, 3, 'Có ạ, chúng tôi có căng tin phục vụ nước uống và đồ ăn nhẹ.', 1),
(2, 8, 'Tuyệt vời. Cảm ơn anh!', 1),

-- Chat 3 (User 9 với Manager 4)
(3, 9, 'Cho em hỏi sân có chỗ đỗ xe không ạ?', 1),
(3, 4, 'Có em, bãi đỗ xe rộng rãi, miễn phí cho khách đặt sân.', 1),
(3, 9, 'Cảm ơn chị ạ!', 1),

-- Chat 4 (User 10 với Manager 4)
(4, 10, 'Chị ơi, em đặt nhầm giờ, có đổi được không?', 0),

-- Chat 5 (User 11 với Manager 5)
(5, 11, 'Sân có cho thuê bóng và áo không anh?', 0),

-- Chat 6 (User 12 với Manager 6)  
(6, 12, 'Anh cho em hỏi giá thuê sân theo tháng?', 0);

-- ============================================================
-- Insert Revenue Statistics
-- ============================================================

-- Daily revenue
INSERT INTO revenue_daily (date, field_id, total_bookings, total_revenue) VALUES
(CURRENT_DATE - 6, 1, 2, 750000),
(CURRENT_DATE - 6, 2, 1, 350000),
(CURRENT_DATE - 5, 3, 1, 280000),
(CURRENT_DATE - 4, 7, 1, 380000),
(CURRENT_DATE - 4, 8, 1, 420000),
(CURRENT_DATE - 3, 2, 1, 350000),
(CURRENT_DATE - 2, 1, 1, 350000),
(CURRENT_DATE - 1, 1, 1, 400000),
(CURRENT_DATE - 1, 7, 1, 380000);

-- Weekly revenue (last 4 weeks)
INSERT INTO revenue_weekly (year, week, field_id, total_bookings, total_revenue) VALUES
(EXTRACT(YEAR FROM CURRENT_DATE), EXTRACT(WEEK FROM CURRENT_DATE) - 3, 1, 5, 1500000),
(EXTRACT(YEAR FROM CURRENT_DATE), EXTRACT(WEEK FROM CURRENT_DATE) - 3, 2, 3, 1050000),
(EXTRACT(YEAR FROM CURRENT_DATE), EXTRACT(WEEK FROM CURRENT_DATE) - 2, 1, 4, 1400000),
(EXTRACT(YEAR FROM CURRENT_DATE), EXTRACT(WEEK FROM CURRENT_DATE) - 2, 3, 2, 560000),
(EXTRACT(YEAR FROM CURRENT_DATE), EXTRACT(WEEK FROM CURRENT_DATE) - 1, 7, 3, 1140000),
(EXTRACT(YEAR FROM CURRENT_DATE), EXTRACT(WEEK FROM CURRENT_DATE) - 1, 8, 2, 840000),
(EXTRACT(YEAR FROM CURRENT_DATE), EXTRACT(WEEK FROM CURRENT_DATE), 1, 2, 750000);

-- Monthly revenue (last 3 months)
INSERT INTO revenue_monthly (year, month, field_id, total_bookings, total_revenue) VALUES
(EXTRACT(YEAR FROM CURRENT_DATE - INTERVAL '2 months'), EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '2 months'), 1, 18, 6300000),
(EXTRACT(YEAR FROM CURRENT_DATE - INTERVAL '2 months'), EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '2 months'), 2, 12, 4200000),
(EXTRACT(YEAR FROM CURRENT_DATE - INTERVAL '1 month'), EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '1 month'), 1, 20, 7000000),
(EXTRACT(YEAR FROM CURRENT_DATE - INTERVAL '1 month'), EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '1 month'), 3, 10, 2800000),
(EXTRACT(YEAR FROM CURRENT_DATE - INTERVAL '1 month'), EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '1 month'), 7, 15, 5700000),
(EXTRACT(YEAR FROM CURRENT_DATE), EXTRACT(MONTH FROM CURRENT_DATE), 1, 8, 2800000),
(EXTRACT(YEAR FROM CURRENT_DATE), EXTRACT(MONTH FROM CURRENT_DATE), 2, 5, 1750000),
(EXTRACT(YEAR FROM CURRENT_DATE), EXTRACT(MONTH FROM CURRENT_DATE), 8, 3, 1260000);

-- ============================================================
-- END OF SAMPLE DATA
-- ============================================================
-- 
-- Instructions:
-- 1. Run database_schema.sql first to create all tables
-- 2. Then run this file to insert sample data
-- 3. Update passwords with actual bcrypt hashes before production use
-- 4. 6 sport fields in Hanoi (Cầu Giấy area) - 15 active, 1 maintenancetory
--
-- Note: This creates a complete test environment with:
-- - 2 admins, 4 managers, 10 regular users
-- - 12 sport fields (11 active, 1 maintenance)
-- - Field schedules, bookings, payments
-- - Reviews, feedbacks, chats, and messages
-- - Revenue statistics for analytics
-- ============================================================
