# Hệ thống Đăng nhập & Đăng ký

## Tổng quan

Hệ thống xác thực hoàn chỉnh với JWT tokens, mã hóa mật khẩu, và kết nối database MySQL.

## Cấu trúc Backend

### 1. Models
- **Person.js** (`backend/src/models/Person.js`)
  - Model Sequelize cho bảng `person`
  - Tự động hash password với bcrypt trước khi lưu
  - Method `comparePassword()` để kiểm tra mật khẩu
  - Tự động ẩn password trong JSON response

### 2. Controllers
- **authController.js** (`backend/src/controllers/authController.js`)
  - `register()` - Đăng ký user mới
  - `login()` - Đăng nhập với username/email
  - `getMe()` - Lấy thông tin user hiện tại
  - `refreshToken()` - Làm mới access token
  - `logout()` - Đăng xuất user

### 3. Middleware
- **auth.js** (`backend/src/middleware/auth.js`)
  - `protect` - Bảo vệ routes, yêu cầu JWT token
  - `authorize(...roles)` - Kiểm tra quyền theo role
  - `optionalAuth` - Auth không bắt buộc

### 4. Routes
- **authRoutes.js** (`backend/src/routes/authRoutes.js`)
  ```
  POST   /api/auth/register    - Đăng ký
  POST   /api/auth/login       - Đăng nhập
  POST   /api/auth/refresh     - Refresh token
  GET    /api/auth/me          - Lấy thông tin (Protected)
  POST   /api/auth/logout      - Đăng xuất (Protected)
  ```

## Cấu trúc Frontend

### 1. Services
- **api.js** (`frontend/src/services/api.js`)
  - ApiClient class với auto token refresh
  - authAPI với các methods:
    - `register(userData)`
    - `login(credentials)`
    - `logout()`
    - `getMe()`
    - `isAuthenticated()`
    - `getCurrentUser()`

### 2. Pages
- **LoginPage.jsx** - Form đăng nhập
- **RegisterPage.jsx** - Form đăng ký

## Cài đặt

### Backend

1. Cài đặt dependencies:
```bash
cd backend
npm install bcryptjs
```

2. Cập nhật database:
```bash
npm run db:migrate
```

3. Khởi động server:
```bash
npm run dev
```

### Frontend

1. Tạo file `.env`:
```
VITE_API_URL=http://localhost:5000/api
```

2. Khởi động frontend:
```bash
cd frontend
npm run dev
```

## Sử dụng

### Đăng ký User mới

**Request:**
```javascript
POST /api/auth/register
Content-Type: application/json

{
  "person_name": "Nguyễn Văn A",
  "username": "nguyenvana",
  "email": "nguyenvana@example.com",
  "phone": "0123456789",
  "password": "matkhau123"
}
```

**Response:**
```javascript
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "user": {
      "person_id": 1,
      "person_name": "Nguyễn Văn A",
      "username": "nguyenvana",
      "email": "nguyenvana@example.com",
      "phone": "0123456789",
      "role": "user",
      "status": "active"
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### Đăng nhập

**Request:**
```javascript
POST /api/auth/login
Content-Type: application/json

{
  "username": "nguyenvana",  // hoặc email
  "password": "matkhau123"
}
```

**Response:**
```javascript
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "user": { ... },
    "token": "...",
    "refreshToken": "..."
  }
}
```

### Sử dụng Protected Routes

**Request:**
```javascript
GET /api/auth/me
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```javascript
{
  "success": true,
  "data": {
    "person_id": 1,
    "person_name": "Nguyễn Văn A",
    "username": "nguyenvana",
    "email": "nguyenvana@example.com",
    "role": "user",
    "status": "active"
  }
}
```

## Frontend Usage

### Đăng ký
```javascript
import { authAPI } from '../../services/api'

const handleRegister = async (formData) => {
  try {
    const response = await authAPI.register(formData)
    if (response.success) {
      // Token đã được tự động lưu vào localStorage
      navigate('/user')
    }
  } catch (error) {
    console.error(error.message)
  }
}
```

### Đăng nhập
```javascript
const handleLogin = async (credentials) => {
  try {
    const response = await authAPI.login(credentials)
    if (response.success) {
      // Token đã được tự động lưu
      navigate('/user')
    }
  } catch (error) {
    console.error(error.message)
  }
}
```

### Kiểm tra đăng nhập
```javascript
import { authAPI } from '../../services/api'

// Kiểm tra user đã đăng nhập chưa
if (authAPI.isAuthenticated()) {
  const user = authAPI.getCurrentUser()
  console.log('Current user:', user)
}
```

### Đăng xuất
```javascript
const handleLogout = async () => {
  await authAPI.logout()
  navigate('/user/login')
}
```

## Bảo mật

1. **Password Hashing**: Sử dụng bcrypt với salt rounds = 10
2. **JWT Tokens**: 
   - Access token: 7 ngày
   - Refresh token: 30 ngày
3. **Auto Token Refresh**: Tự động refresh khi access token hết hạn
4. **Password không được trả về** trong JSON responses
5. **Validation**: 
   - Username: 3-45 ký tự, unique
   - Email: format hợp lệ, unique
   - Password: tối thiểu 6 ký tự
   - Phone: 10 chữ số

## Database Schema

Bảng `person`:
```sql
CREATE TABLE person (
  person_id INT PRIMARY KEY AUTO_INCREMENT,
  person_name VARCHAR(50) NOT NULL,
  username VARCHAR(45) NOT NULL UNIQUE,
  email VARCHAR(45) UNIQUE,
  phone VARCHAR(10),
  password VARCHAR(255) NOT NULL,  -- Hashed
  role VARCHAR(45) DEFAULT 'user',
  status VARCHAR(45) DEFAULT 'active',
  birthday DATE,
  sex VARCHAR(10),
  address VARCHAR(45),
  fieldId INT
);
```

## Roles & Permissions

- **user**: Người dùng thường (mặc định)
- **staff**: Nhân viên
- **manager**: Quản lý sân
- **admin**: Quản trị viên

## Error Handling

Tất cả API responses đều có format:
```javascript
{
  "success": true/false,
  "message": "Message mô tả",
  "data": { ... }  // Nếu success = true
}
```

HTTP Status Codes:
- `200` - Success
- `201` - Created (đăng ký thành công)
- `400` - Bad Request (validation error)
- `401` - Unauthorized (chưa đăng nhập/token không hợp lệ)
- `403` - Forbidden (không có quyền)
- `404` - Not Found
- `500` - Server Error

## Testing

### Test đăng ký với curl:
```bash
curl -X POST http://localhost:5000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "person_name": "Test User",
    "username": "testuser",
    "email": "test@example.com",
    "phone": "0123456789",
    "password": "password123"
  }'
```

### Test đăng nhập:
```bash
curl -X POST http://localhost:5000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### Test protected route:
```bash
curl -X GET http://localhost:5000/api/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Troubleshooting

### Lỗi "Unable to connect to database"
- Kiểm tra MySQL đang chạy
- Kiểm tra thông tin trong `.env`
- Chạy `npm run db:migrate`

### Lỗi "Token không hợp lệ"
- Token có thể đã hết hạn
- Kiểm tra `JWT_SECRET` trong `.env`
- Đăng nhập lại để lấy token mới

### Lỗi "Username đã tồn tại"
- Username hoặc email đã được sử dụng
- Thử username/email khác

## Next Steps

- [ ] Implement password reset functionality
- [ ] Add email verification
- [ ] Implement OAuth (Google, Facebook)
- [ ] Add rate limiting
- [ ] Implement session management
- [ ] Add 2FA (Two-Factor Authentication)
