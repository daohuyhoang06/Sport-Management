# Database Migrations

Hướng dẫn sử dụng Sequelize migrations cho dự án Sport Field Management.

## Cấu trúc Database

Database gồm các bảng sau:

1. **person** - Quản lý người dùng (khách hàng, quản lý, admin)
2. **fields** - Quản lý sân bóng
3. **field_images** - Hình ảnh của sân bóng
4. **field_schedules** - Lịch trống của sân bóng
5. **bookings** - Đơn đặt sân
6. **payments** - Thanh toán
7. **reviews** - Đánh giá sân bóng
8. **feedbacks** - Phản hồi từ khách hàng
9. **replies** - Trả lời phản hồi
10. **revenue_daily** - Doanh thu theo ngày
11. **revenue_weekly** - Doanh thu theo tuần
12. **revenue_monthly** - Doanh thu theo tháng

## Cài đặt

### 1. Cài đặt Sequelize CLI

```bash
npm install --save-dev sequelize-cli
```

### 2. Cài đặt MySQL driver

```bash
npm install mysql2
```

### 3. Cấu hình Database

Cập nhật thông tin database trong file `config/config.json`:

```json
{
  "development": {
    "username": "root",
    "password": "your_password",
    "database": "sport_management",
    "host": "localhost",
    "port": 3306,
    "dialect": "mysql"
  }
}
```

Hoặc sử dụng file `.env`:

```env
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=your_password
DB_NAME=sport_management
```

## Chạy Migrations

### Tạo database (nếu chưa có)

```bash
# Đăng nhập MySQL
mysql -u root -p

# Tạo database
CREATE DATABASE sport_management;
exit;
```

### Chạy tất cả migrations

```bash
cd backend
npx sequelize-cli db:migrate
```

### Rollback migration gần nhất

```bash
npx sequelize-cli db:migrate:undo
```

### Rollback tất cả migrations

```bash
npx sequelize-cli db:migrate:undo:all
```

### Rollback đến một migration cụ thể

```bash
npx sequelize-cli db:migrate:undo:all --to 20241107000005-create-bookings.js
```

### Kiểm tra trạng thái migrations

```bash
npx sequelize-cli db:migrate:status
```

## Thứ tự Migration

Migrations phải chạy theo thứ tự sau (đã được đánh số):

1. `20241107000001-create-person.js` - Tạo bảng person trước
2. `20241107000002-create-fields.js` - Tạo bảng fields (có FK đến person)
3. `20241107000003-create-field-images.js`
4. `20241107000004-create-field-schedules.js`
5. `20241107000005-create-bookings.js`
6. `20241107000006-create-payments.js`
7. `20241107000007-create-reviews.js`
8. `20241107000009-create-feedbacks.js` - Tạo feedbacks trước
9. `20241107000008-create-replies.js` - Tạo replies sau (có FK đến feedbacks)
10. `20241107000010-create-revenue-daily.js`
11. `20241107000011-create-revenue-weekly.js`
12. `20241107000012-create-revenue-monthly.js`

## Lưu ý quan trọng

### Foreign Keys

- Bảng `person` có FK `fieldId` tham chiếu đến `fields.field_id`
- Bảng `fields` có FK `manager_id` tham chiếu đến `person.person_id`
- Tất cả bảng khác đều có các FK tương ứng theo sơ đồ database

### Circular Reference

Do có circular reference giữa `person` và `fields`, cần xử lý đặc biệt:

- Tạo bảng `person` trước nhưng chưa có FK `fieldId`
- Tạo bảng `fields` với FK `manager_id`
- Sau đó có thể thêm FK `fieldId` vào bảng `person` nếu cần

### Indexes

Đã tạo indexes cho:

- Primary keys (tự động)
- Foreign keys
- Các trường thường xuyên query (email, username, status, transaction_code, etc.)
- Composite indexes cho date ranges và week/month/year

## Xử lý lỗi thường gặp

### Lỗi: Database không tồn tại

```bash
ERROR 1049 (42000): Unknown database 'sport_management'
```

**Giải pháp:** Tạo database trước khi chạy migration

### Lỗi: Bảng đã tồn tại

```bash
ERROR 1050 (42S01): Table 'person' already exists
```

**Giải pháp:** Rollback hoặc xóa bảng thủ công

### Lỗi: Foreign key constraint fails

**Giải pháp:** Đảm bảo chạy migrations theo đúng thứ tự

## Scripts hữu ích

Thêm vào `package.json`:

```json
{
  "scripts": {
    "db:migrate": "sequelize-cli db:migrate",
    "db:migrate:undo": "sequelize-cli db:migrate:undo",
    "db:migrate:reset": "sequelize-cli db:migrate:undo:all && sequelize-cli db:migrate",
    "db:migrate:status": "sequelize-cli db:migrate:status"
  }
}
```

Sử dụng:

```bash
npm run db:migrate
npm run db:migrate:undo
npm run db:migrate:reset
npm run db:migrate:status
```

## Tạo Migration mới

```bash
npx sequelize-cli migration:generate --name migration-name
```

## Kiểm tra kết quả

Sau khi chạy migrations thành công, kiểm tra trong MySQL:

```sql
USE sport_management;
SHOW TABLES;
DESCRIBE person;
DESCRIBE fields;
-- etc.
```
