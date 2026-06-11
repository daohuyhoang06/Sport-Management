# Frontend Workspace

Cau truc moi gom 3 phan:

- `mobile-user`: Android app (Kotlin + Jetpack Compose) cho nguoi dung dat san.
- `mobile-manager`: Android app (Kotlin + Jetpack Compose) cho manager quan ly san.
- `admin-web`: Web dashboard cho admin (React + Vite).

## 1) Mobile User App (Android Studio)

1. Mo Android Studio.
2. Chon **Open** va mo thu muc: `frontend/mobile-user`.
3. Cho Gradle sync.
4. Neu chay tren may that qua USB, mo terminal trong `frontend/mobile-user` va chay:

```bash
adb reverse tcp:5000 tcp:5000
```

5. Run tren emulator/device.

Ghi chu:

- App lay URL backend tu `BuildConfig.API_BASE_URL`.
- Emulator Android mac dinh goi `http://10.0.2.2:5000`.
- Neu chay tren dien thoai that, dung `adb reverse tcp:5000 tcp:5000` hoac override host bang:

```bash
./gradlew assembleDebug -PMOBILE_API_HOST=192.168.1.10 -PMOBILE_API_PORT=5000
```

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
