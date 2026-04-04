# Quick Start - Authentication System

## 🚀 Khởi động nhanh

### 1. Setup Backend

```bash
# Di chuyển vào thư mục backend
cd backend

# Cài đặt dependencies (nếu chưa cài)
npm install

# Cập nhật database với migration mới (password length)
npm run db:migrate

# Khởi động backend server
npm run dev
```

Server sẽ chạy tại: `http://localhost:5000`

### 2. Setup Frontend

```bash
# Mở terminal mới, di chuyển vào thư mục frontend
cd frontend

# Cài đặt dependencies (nếu chưa cài)
npm install

# Khởi động frontend
npm run dev
```

Frontend sẽ chạy tại: `http://localhost:3000` (hoặc port khác nếu 3000 đã dùng)

## ✅ Test nhanh

### Test 1: Đăng ký tài khoản mới

1. Truy cập: `http://localhost:3000/user/register`
2. Điền form:
   - Họ tên: `Nguyễn Văn Test`
   - Username: `testvana`
   - Email: `test@example.com`
   - Số điện thoại: `0123456789`
   - Mật khẩu: `password123`
   - Xác nhận mật khẩu: `password123`
3. Click "Đăng ký"
4. Kiểm tra console browser để xem response

### Test 2: Đăng nhập

1. Sau khi đăng ký thành công, bạn sẽ được chuyển đến trang login
2. Nhập:
   - Username: `testvana` (hoặc `test@example.com`)
   - Mật khẩu: `password123`
3. Click "Đăng nhập"
4. Bạn sẽ được chuyển đến trang chủ user

### Test 3: Kiểm tra token

1. Mở Developer Tools (F12) → Console
2. Nhập:
```javascript
localStorage.getItem('token')
localStorage.getItem('user')
```
3. Bạn sẽ thấy token JWT và thông tin user

### Test 4: API trực tiếp với curl

#### Đăng ký:
```bash
curl -X POST http://localhost:5000/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"person_name\":\"Curl User\",\"username\":\"curluser\",\"email\":\"curl@example.com\",\"phone\":\"0987654321\",\"password\":\"curl123\"}"
```

#### Đăng nhập:
```bash
curl -X POST http://localhost:5000/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"curluser\",\"password\":\"curl123\"}"
```

Copy token từ response, sau đó test protected endpoint:

```bash
curl -X GET http://localhost:5000/api/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## 🔍 Kiểm tra Database

Kết nối vào MySQL:
```bash
mysql -u root -p
```

Kiểm tra users đã tạo:
```sql
USE sport_management;
SELECT person_id, person_name, username, email, role, status FROM person;
```

**Lưu ý:** Bạn sẽ KHÔNG thấy password gốc vì đã được hash bằng bcrypt!

## 📊 Monitoring

### Check migrations status:
```bash
cd backend
npm run db:migrate:status
```

Bạn sẽ thấy migration mới: `20241108000001-update-person-password-length.cjs`

### Backend logs:
Khi khởi động backend, bạn sẽ thấy:
```
✅ Database connection established successfully.
Server running on port 5000
```

### API Endpoints có sẵn:

**Public:**
- POST `/api/auth/register` - Đăng ký
- POST `/api/auth/login` - Đăng nhập
- POST `/api/auth/refresh` - Refresh token

**Protected (cần token):**
- GET `/api/auth/me` - Lấy thông tin user
- POST `/api/auth/logout` - Đăng xuất

## 🐛 Troubleshooting

### Lỗi "Cannot connect to database"
```bash
# Kiểm tra MySQL đang chạy
# Windows:
net start MySQL80

# Kiểm tra .env file
cd backend
cat .env
```

### Lỗi "Module not found"
```bash
# Backend
cd backend
npm install

# Frontend
cd frontend
npm install
```

### Lỗi "Port already in use"
```bash
# Thay đổi port trong backend/src/server.js
# hoặc kill process đang dùng port 5000

# Windows:
netstat -ano | findstr :5000
taskkill /PID <PID> /F
```

### Frontend không gọi được API
```bash
# Kiểm tra CORS - backend phải cho phép origin của frontend
# Kiểm tra .env của frontend
cd frontend
cat .env
# Phải có: VITE_API_URL=http://localhost:5000/api
```

### Token không được lưu
```bash
# Mở Developer Tools (F12) → Application → Local Storage
# Kiểm tra xem có 'token', 'refreshToken', 'user' không
```

## 🎯 Các features đã hoàn thành

- ✅ User registration với validation
- ✅ User login với username/email
- ✅ Password hashing (bcrypt)
- ✅ JWT token generation & validation
- ✅ Refresh token mechanism
- ✅ Protected routes
- ✅ Auto token refresh khi hết hạn
- ✅ Role-based access control (user, staff, manager, admin)
- ✅ Frontend integration với React
- ✅ Error handling đầy đủ
- ✅ Database integration với Sequelize

## 📝 Notes

1. **Password** được hash tự động trước khi lưu database
2. **Token** được lưu trong localStorage
3. **Auto refresh** token khi hết hạn trong lúc gọi API
4. **Password gốc** không bao giờ được trả về từ API
5. **Username và Email** phải unique

## 🔐 Security Checklist

- ✅ Password hashing với bcrypt (salt rounds: 10)
- ✅ JWT tokens với expiry
- ✅ Password không được log hoặc return trong response
- ✅ Input validation trên cả frontend và backend
- ✅ CORS configuration
- ✅ Environment variables cho sensitive data
- ✅ Status check (active/inactive/banned)

## 📚 Documentation

Chi tiết đầy đủ xem tại:
- `backend/AUTH_GUIDE.md` - Hướng dẫn đầy đủ về authentication system
