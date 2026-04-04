# Database Migration Workflow

## 🔒 Security-First Workflow

Dự án này sử dụng workflow bảo mật để tránh commit password lên Git.

### Cách hoạt động:

1. **Password lưu trong `.env`** (không bao giờ commit)
2. **`config.json` có password = null** (an toàn để commit)
3. **Script tự động sync** password khi cần chạy migrations
4. **Script tự động reset** về null sau khi migration xong

---

## 📝 Commands

### Chạy migrations (Recommended)
```bash
npm run db:migrate
```
**Workflow tự động:**
1. ✅ Sync password từ `.env` → `config.json`
2. ✅ Chạy migrations
3. ✅ Reset `config.json` về password = null

### Reset và chạy lại tất cả
```bash
npm run db:migrate:reset
```
**Workflow tự động:**
1. ✅ Sync password
2. ✅ Undo tất cả migrations
3. ✅ Chạy lại migrations
4. ✅ Reset config

### Kiểm tra trạng thái
```bash
npm run db:migrate:status
```

### Rollback 1 migration
```bash
npm run db:migrate:undo
```

---

## 🛠️ Manual Commands (Advanced)

### Sync password vào config.json (không khuyến khích)
```bash
npm run sync-config
```
⚠️ Nhớ reset sau khi xong!

### Reset password về null
```bash
npm run reset-config
```

---

## ⚠️ QUAN TRỌNG

### ✅ AN TOÀN để commit:
- `config.json` với `"password": null`
- `.env.example` với placeholder passwords
- `sync-config.cjs` và `reset-config.cjs`

### ❌ KHÔNG BAO GIỜ commit:
- `.env` file (đã có trong .gitignore)
- `config.json` với password thật
- Bất kỳ file nào chứa credentials

---

## 🔍 Kiểm tra trước khi commit

```bash
# 1. Chắc chắn config.json đã reset
npm run reset-config

# 2. Kiểm tra git status
git status

# 3. Xem diff để chắc chắn không có password
git diff config/config.json

# 4. Nếu thấy password, chạy reset lại
npm run reset-config
```

---

## 📋 Setup cho thành viên mới

1. Clone repository
2. Copy `.env.example` → `.env`
3. Cập nhật password trong `.env`
4. Chạy `npm run db:migrate`
5. Không bao giờ commit `.env`!

---

## 🐛 Troubleshooting

### Lỗi: Access denied
- Kiểm tra password trong `.env`
- Thử `npm run sync-config` để kiểm tra sync

### Lỗi: config.json has password
- Chạy `npm run reset-config`
- Không commit file này

### Lỗi: require is not defined
- Đảm bảo file script có đuôi `.cjs` (không phải `.js`)
