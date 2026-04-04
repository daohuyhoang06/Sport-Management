# Frontend Workspace

Cau truc moi gom 3 phan:

- `mobile-user`: Android app (Kotlin + Jetpack Compose) cho nguoi dung dat san.
- `mobile-manager`: Android app (Kotlin + Jetpack Compose) cho manager quan ly san.
- `admin-web`: Web dashboard cho admin (React + Vite).

## 1) Mobile User App (Android Studio)

1. Mo Android Studio.
2. Chon **Open** va mo thu muc: `frontend/mobile-user`.
3. Cho Gradle sync.
4. Run tren emulator/device.

## 2) Mobile Manager App (Android Studio)

1. Mo Android Studio.
2. Chon **Open** va mo thu muc: `frontend/mobile-manager`.
3. Cho Gradle sync.
4. Run tren emulator/device.

## 3) Admin Web (VS Code)

```bash
cd frontend/admin-web
npm install
npm run dev
```

Build production:

```bash
npm run build
npm run preview
```

## Ghi chu

- Hai app Android hien dang dung mock data de ban thao tac UI nhanh.
- Ban co the noi API backend sau qua Retrofit/Ktor client.
- Neu muon, buoc tiep theo minh co the tiep tuc tao layer data/domain va wiring API that.
