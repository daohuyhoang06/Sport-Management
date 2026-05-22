# Bao Cao Dong Nhat Chuc Nang, Thong Tin, Du Lieu
Ngay lap: 2026-05-21

## 1) Muc tieu
Dong bo contract giua:
- `frontend/mobile-user` (app nguoi dung, hien dang mock-first)
- `frontend/mobile-manager` + `frontend/admin-web` (manage side)
- `backend/src` (API va data model hien co)

Muc tieu chinh: backend sau nay phai bam theo mock contract o frontend de khi gan API khong vo UI.

## 2) Hien trang frontend

### 2.1 Mobile User (`frontend/mobile-user`)
- Da co day du UI flow: auth, home, map, filter, booking schedule, booking confirmation, payment, profile, chatbot.
- Du lieu chinh dang lay tu `MockUserRepository` (khong phai API that).
- API that hien chi dang dung cho:
  - Chatbot: `POST /api/ai/chat`
  - MoMo demo payment: `POST /api/payments/momo/create`

Ket luan: mobile user la nguon contract UI quan trong nhat cho backend user-side.

### 2.2 Mobile Manager (`frontend/mobile-manager`)
- Hien la demo UI tinh: Dashboard/Schedule/Reviews/Reports.
- Chua co data layer, chua goi backend.

Ket luan: manage app mobile chua du de lam source of truth contract.

### 2.3 Admin Web (`frontend/admin-web`)
- Da dung API backend that cho login, dashboard, users, fields, bookings, employees, sport-types.
- Co normalize data o hooks nen de thay ky vong payload/response.

Ket luan: admin-web dang la nguon contract manage-side thuc te.

## 3) Contract du lieu frontend dang "ky vong"

### 3.1 UserField (mobile-user)
UI dang dung cac thuoc tinh:
- `name`, `location`, `price`, `rating`
- `sportIconType`, `latitude`, `longitude`
- `distance`, `hours`, `isProLeague`, `tags`, `availability`, `cardType`
- `region`, `province`, `district`, `distanceKm`

Can backend tra du metadata nay neu muon UI user hoat dong day du (home/map/filter/detail).

### 3.2 Booking schedule va multi-court (mobile-user)
UI booking dang ky vong:
- `grid.openTime`, `grid.closeTime`, `grid.gridStepMinutes`, `grid.minBookingMinutes`
- `grid.courts[]` (sub-court)
- `grid.bookedSlots[]`, `grid.blockedSlots[]` theo `courtId + start/end`
- `pricePerHour`, `estimatedPrice`

Can model booking/schedule backend ho tro sub-court va slot-level data.

### 3.3 Profile + dang ky (mobile-user)
UI form dang thu thap:
- Dang ky step 1: `fullName`, `email`, `phone`, `password`, `birthDate`
- Step 2: danh sach mon the thao ua thich
- Step 3: khu vuc, thong bao, email offers
- Profile: `name`, `email`, `phone`, `membership`, `birthday`, `gender`, `location`, `bookingCount`, `rating`

Can backend co endpoint va schema luu duoc thong tin mo rong nay (khong chi auth co ban).

### 3.4 Chatbot + Payment (mobile-user)
- Chatbot ky vong response:
  - `{ success: true, message: "..." }`
- MoMo ky vong response:
  - `payment_id`, `order_id`, `request_id`, `amount`, `resultCode`, `message`, `payUrl`, `deeplink`, `qrCodeUrl`

## 4) Contract manage-side (admin-web)

### 4.1 Users
Admin web ky vong:
- list item: `person_id`, `name`, `email`, `username`, `phone`, `address`, `birthday`, `sex`, `role`, `status`
- CRUD + toggle status:
  - `POST/PUT /api/admin/users`
  - `PATCH /api/admin/users/:id/status`

### 4.2 Fields
Ky vong:
- list item: `field_id`, `manager_id`, `sport_id`, `sport_name`, `field_name`, `location`, `manager_name`, `slot_price`, `status`
- CRUD + toggle status:
  - `POST/PUT /api/admin/fields`
  - `PATCH /api/admin/fields/:id/status`

### 4.3 Bookings
Ky vong:
- list item: `booking_id`, `customer_name`, `field_name`, `start_time`, `end_time`, `status`
- update status:
  - `PATCH /api/admin/bookings/:id/status` body `{ status, note }`
- cancel:
  - `POST /api/admin/bookings/:id/cancel` body `{ reason }`

### 4.4 Employees
Ky vong:
- list item: `person_id`, `name`, `email`, `phone`, `role`, `status`, `field_names|field_count`
- assign field:
  - `POST /api/admin/employees/assign-field` body `{ employeeId, field_id }`

### 4.5 Dashboard
Ky vong tong hop:
- `totalUsers`, `totalManagers`, `totalFields`, `activeFields`, `totalBookings`, `todayBookings`, `totalRevenue`, `monthlyRevenue`
- stats bo sung:
  - users: `active/inactive`
  - fields: `active/inactive/maintenance`
  - bookings: `pending/confirmed/completed/cancelled`

## 5) Gap dong nhat quan trong (frontend vs backend)

### P0 - Can xu ly ngay
1. Mobile-user chua gan API cho home/map/profile/booking/review:
- Hien dung `MockUserRepository`, neu doi backend ma khong giu contract mock thi UI se vo.

2. Bat nhat schema `person` giua code backend:
- Mot so noi dung dung `person_name/sex` (model `Person`).
- Nhieu service admin lai query `full_name/gender`.
- Rui ro loi runtime SQL va data mapping sai tren admin/manage.

### P1 - Anh huong tich hop user/manage
3. Booking contract mobile-user dang theo `sub-court + slot grid`, trong khi booking backend hien tai chu yeu theo `field_id + start/end` (single layer).

4. Field detail o mobile-user co tabs `services/policies/gallery/reviews` nhung hien backend user endpoint chua tra 1 payload hop nhat cho detail theo contract UI.

5. Auth UX mobile-user co login email/phone; backend login hien theo `identifier(username/email)`; can chot ro co ho tro phone hay khong.

6. HomeSearch filter (region/province/district/radius/current location) can metadata dia ly chuan, hien backend list fields chua tra day du.

### P2 - Nen lam de hoan thien
7. Mobile-manager chua gan backend, nen chua co contract quan ly on dinh tren mobile.

8. Payment flow user hien moi tao MoMo order demo; chua dong bo tinh trang booking/payment end-to-end tren UI user.

## 6) De xuat dong nhat (mock-first)

### Phase 1 - Chot contract chung
1. Chot bo DTO chung cho:
- `FieldListItem`, `FieldDetail`, `BookingSchedule`, `BookingRequest`, `BookingHistoryItem`, `UserProfile`.
2. Quy uoc naming 1 kieu duy nhat:
- uu tien snake_case o API raw (`field_id`, `person_name`, ...)
- frontend mapper chuyen doi ve camelCase.
3. Chot enum:
- `booking status`: `pending | confirmed | rejected | cancelled | completed`
- `field status`: `active | inactive | maintenance`

### Phase 2 - Dong bo backend theo contract da chot
1. Sua tat ca query admin/manager dung dung cot cua DB thuc te (`person_name/sex` neu schema hien tai la nhu vay).
2. Bo sung endpoint user-side cho mobile-user:
- field list + filter + map metadata
- field detail hop nhat (courts/services/policies/reviews/images)
- booking schedule grid theo ngay
- profile get/update
3. Chuan hoa response wrapper:
- de nghi thong nhat: `{ success, data, message }` cho toan bo API moi.

### Phase 3 - Gan API mobile-user
1. Thay `MockUserRepository` bang `UserRepositoryImpl`.
2. Viet mapper DTO -> domain dung theo model dang co tren UI.
3. Test 4 flow chinh:
- Home/filter/map
- Booking schedule -> confirmation -> payment
- Profile edit
- Chatbot

### Phase 4 - Mo rong mobile-manager
1. Dua mobile-manager ve chung contract manager API.
2. Uu tien cac module:
- bookings queue
- field status/update
- dashboard stats

## 7) Checklist ky thuat de backend bam theo frontend mock

1. Bao toan cac field UI dang doc trong `UserField`.
2. Ho tro sub-court trong booking schedule.
3. Co endpoint field detail tong hop (khong bat frontend goi qua nhieu endpoint roi tu ghep).
4. Co endpoint profile update user.
5. Chot auth login co/khong ho tro phone.
6. Chuan hoa naming cot `person_name/sex` hoac migrate DB de dong nhat voi `full_name/gender` (chot 1 huong duy nhat).
7. Chuan hoa `success/data/message` cho response.

## 8) Nhan xet tong ket
Nguon contract uu tien de backend bam theo hien tai la:
1. `frontend/mobile-user` (cho user-side features)
2. `frontend/admin-web` (cho manage/admin-side features)

`frontend/mobile-manager` hien chi la demo UI, chua nen dung lam contract chinh.
