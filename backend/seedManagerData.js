/**
 * Seed rich data for manager01.
 * Run: node seedManagerData.js
 */

import sequelize from "./src/config/database.js";
import bcrypt from "bcrypt";

// ── Date helpers (string-based, timezone-safe) ────────────────────────────────

/** Get current VN date as "YYYY-MM-DD" (VN = UTC+7) */
const vnToday = () => {
  const vnNow = new Date(Date.now() + 7 * 3600 * 1000);
  return vnNow.toISOString().slice(0, 10);
};

/** Add/subtract days from a "YYYY-MM-DD" date string */
const shiftDate = (dateStr, days) => {
  const d = new Date(dateStr + "T12:00:00.000Z"); // midday UTC avoids boundary issues
  d.setUTCDate(d.getUTCDate() + days);
  return d.toISOString().slice(0, 10);
};

/**
 * Build a MySQL DATETIME string (UTC) from a VN date + VN hour.
 * VN = UTC+7 → subtract 7 hours; handle crossing midnight.
 * mysql2 reads DATETIME as UTC, so "01:00:00" → API returns "01:00:00Z"
 * → Android UTC parser shows "08:00 VN". ✅
 */
const vnDt = (vnDateStr, hhVN, mm = 0) => {
  const utcHH = hhVN - 7;
  if (utcHH >= 0) {
    return `${vnDateStr} ${String(utcHH).padStart(2, "0")}:${String(mm).padStart(2, "0")}:00`;
  }
  // crosses midnight: use previous UTC day
  const prevDay = shiftDate(vnDateStr, -1);
  return `${prevDay} ${String(utcHH + 24).padStart(2, "0")}:${String(mm).padStart(2, "0")}:00`;
};

// ── DB helpers ───────────────────────────────────────────────────────────────

const getOne = async (sql, replacements) => {
  const [rows] = await sequelize.query(sql, { replacements });
  return rows[0] || null;
};

// ── Upsert helpers ────────────────────────────────────────────────────────────

const upsertPerson = async ({ username, name, role, email, phone, password }) => {
  const existing = await getOne("SELECT person_id FROM person WHERE username=:username LIMIT 1", { username });
  const hashed = await bcrypt.hash(password, 10);
  const qi = sequelize.getQueryInterface();
  const cols = await qi.describeTable("person");
  const nameCol = cols.name ? "name" : "person_name";
  if (!existing) {
    await sequelize.query(
      `INSERT INTO person (${nameCol}, username, password, email, phone, role, status)
       VALUES (:name, :username, :password, :email, :phone, :role, 'active')`,
      { replacements: { name, username, password: hashed, email, phone, role } }
    );
  } else {
    await sequelize.query(
      `UPDATE person SET ${nameCol}=:name, password=:password, email=:email,
       phone=:phone, role=:role, status='active' WHERE username=:username`,
      { replacements: { name, username, password: hashed, email, phone, role } }
    );
  }
  return getOne("SELECT person_id, username FROM person WHERE username=:username LIMIT 1", { username });
};

const upsertField = async ({ manager_id, field_name, location, sport_id, open_time, close_time, slot_minutes, slot_price }) => {
  const existing = await getOne(
    "SELECT field_id FROM fields WHERE field_name=:field_name AND manager_id=:manager_id LIMIT 1",
    { field_name, manager_id }
  );
  if (!existing) {
    await sequelize.query(
      `INSERT INTO fields (manager_id, field_name, location, sport_id, open_time, close_time, slot_minutes, slot_price, status)
       VALUES (:manager_id, :field_name, :location, :sport_id, :open_time, :close_time, :slot_minutes, :slot_price, 'active')`,
      { replacements: { manager_id, field_name, location, sport_id, open_time, close_time, slot_minutes, slot_price } }
    );
  } else {
    await sequelize.query(
      `UPDATE fields SET location=:location, sport_id=:sport_id, open_time=:open_time, close_time=:close_time,
       slot_minutes=:slot_minutes, slot_price=:slot_price, status='active' WHERE field_id=:field_id`,
      { replacements: { location, sport_id, open_time, close_time, slot_minutes, slot_price, field_id: existing.field_id } }
    );
  }
  return getOne(
    "SELECT field_id, field_name FROM fields WHERE field_name=:field_name AND manager_id=:manager_id LIMIT 1",
    { field_name, manager_id }
  );
};

const upsertCourt = async ({ field_id, court_code, court_name, sort_order }) => {
  const existing = await getOne(
    "SELECT court_id FROM field_courts WHERE field_id=:field_id AND court_code=:court_code LIMIT 1",
    { field_id, court_code }
  );
  if (!existing) {
    await sequelize.query(
      `INSERT INTO field_courts (field_id, court_code, court_name, status, sort_order)
       VALUES (:field_id, :court_code, :court_name, 'active', :sort_order)`,
      { replacements: { field_id, court_code, court_name, sort_order } }
    );
  }
  return getOne(
    "SELECT court_id FROM field_courts WHERE field_id=:field_id AND court_code=:court_code LIMIT 1",
    { field_id, court_code }
  );
};

const upsertBooking = async ({ customer_id, field_id, court_id, manager_id, start_time, end_time, status, price, note }) => {
  const existing = await getOne("SELECT booking_id FROM bookings WHERE note=:note LIMIT 1", { note });
  if (existing) {
    await sequelize.query(
      `UPDATE bookings SET status=:status, customer_id=:customer_id, field_id=:field_id, court_id=:court_id,
       manager_id=:manager_id, start_time=:start_time, end_time=:end_time, price=:price WHERE booking_id=:booking_id`,
      { replacements: { status, customer_id, field_id, court_id, manager_id, start_time, end_time, price, booking_id: existing.booking_id } }
    );
    return existing;
  }
  await sequelize.query(
    `INSERT INTO bookings (customer_id, field_id, court_id, manager_id, start_time, end_time, status, price, note)
     VALUES (:customer_id, :field_id, :court_id, :manager_id, :start_time, :end_time, :status, :price, :note)`,
    { replacements: { customer_id, field_id, court_id, manager_id, start_time, end_time, status, price, note } }
  );
  return getOne("SELECT booking_id FROM bookings WHERE note=:note LIMIT 1", { note });
};

const upsertChat = async ({ manager_id, user_id, field_id }) => {
  const existing = await getOne(
    "SELECT chat_id FROM chats WHERE manager_id=:manager_id AND user_id=:user_id LIMIT 1",
    { manager_id, user_id }
  );
  if (existing) return existing;
  await sequelize.query(
    `INSERT INTO chats (manager_id, user_id, field_id, created_at, updated_at)
     VALUES (:manager_id, :user_id, :field_id, NOW(), NOW())`,
    { replacements: { manager_id, user_id, field_id } }
  );
  return getOne(
    "SELECT chat_id FROM chats WHERE manager_id=:manager_id AND user_id=:user_id LIMIT 1",
    { manager_id, user_id }
  );
};

const insertMessage = async ({ chat_id, sender_id, sender_type, content, created_at }) => {
  const existing = await getOne(
    "SELECT message_id FROM messages WHERE chat_id=:chat_id AND content=:content AND sender_id=:sender_id LIMIT 1",
    { chat_id, content, sender_id }
  );
  if (existing) return;
  await sequelize.query(
    `INSERT INTO messages (chat_id, sender_id, sender_type, content, is_read, created_at, updated_at)
     VALUES (:chat_id, :sender_id, :sender_type, :content, 1, :created_at, :created_at)`,
    { replacements: { chat_id, sender_id, sender_type, content, created_at } }
  );
};

// ── Main ──────────────────────────────────────────────────────────────────────

const seed = async () => {
  try {
    await sequelize.authenticate();
    console.log("DB connected.");

    const today = vnToday(); // "2026-06-07" VN date
    console.log("VN today:", today);

    // ── Users ───────────────────────────────────────────────────────────────
    const manager = await upsertPerson({
      username: "manager01", name: "Nguyễn Văn Hoàng", role: "manager",
      email: "manager01.mock@sport.local", phone: "0901000002", password: "123456"
    });

    const cuList = await Promise.all([
      upsertPerson({ username: "khachhang01", name: "Trần Minh Tuấn", role: "user", email: "tuan01@sport.local", phone: "0912345601", password: "123456" }),
      upsertPerson({ username: "khachhang02", name: "Lê Thị Hoa",     role: "user", email: "hoa02@sport.local",  phone: "0912345602", password: "123456" }),
      upsertPerson({ username: "khachhang03", name: "Phạm Quốc Dũng", role: "user", email: "dung03@sport.local", phone: "0912345603", password: "123456" }),
      upsertPerson({ username: "khachhang04", name: "Vũ Thanh Hải",   role: "user", email: "hai04@sport.local",  phone: "0912345604", password: "123456" }),
      upsertPerson({ username: "khachhang05", name: "Đặng Thị Mai",   role: "user", email: "mai05@sport.local",  phone: "0912345605", password: "123456" }),
    ]);
    console.log(`manager id: ${manager.person_id}, customers: ${cuList.map(c => c.person_id).join(", ")}`);

    // ── Fields ──────────────────────────────────────────────────────────────
    const f1 = await upsertField({ manager_id: manager.person_id, field_name: "Sân Bóng Đá Thành Công", location: "12 Nguyễn Chí Thanh, Đống Đa, Hà Nội", sport_id: 1, open_time: "06:00", close_time: "22:00", slot_minutes: 60, slot_price: 350000 });
    const f2 = await upsertField({ manager_id: manager.person_id, field_name: "Sân Cầu Lông Phú Thịnh", location: "88 Trần Duy Hưng, Cầu Giấy, Hà Nội",     sport_id: 3, open_time: "07:00", close_time: "21:00", slot_minutes: 60, slot_price: 120000 });
    const f3 = await upsertField({ manager_id: manager.person_id, field_name: "Sân Tennis Hồ Tây",       location: "Tây Hồ, Hà Nội",                         sport_id: 4, open_time: "06:00", close_time: "20:00", slot_minutes: 60, slot_price: 200000 });
    console.log(`fields: ${f1.field_id} (bóng đá), ${f2.field_id} (cầu lông), ${f3.field_id} (tennis)`);

    // ── Courts ──────────────────────────────────────────────────────────────
    const c1A = await upsertCourt({ field_id: f1.field_id, court_code: "A1", court_name: "Sân 1 - Cỏ nhân tạo", sort_order: 1 });
    const c1B = await upsertCourt({ field_id: f1.field_id, court_code: "A2", court_name: "Sân 2 - Cỏ nhân tạo", sort_order: 2 });
    const c2A = await upsertCourt({ field_id: f2.field_id, court_code: "B1", court_name: "Sân Lông 1",          sort_order: 1 });
    const c2B = await upsertCourt({ field_id: f2.field_id, court_code: "B2", court_name: "Sân Lông 2",          sort_order: 2 });
    const c3A = await upsertCourt({ field_id: f3.field_id, court_code: "T1", court_name: "Sân Tennis 1",        sort_order: 1 });
    const c3B = await upsertCourt({ field_id: f3.field_id, court_code: "T2", court_name: "Sân Tennis 2",        sort_order: 2 });
    console.log(`courts: ${c1A.court_id},${c1B.court_id} | ${c2A.court_id},${c2B.court_id} | ${c3A.court_id},${c3B.court_id}`);

    // ── Bookings ─────────────────────────────────────────────────────────────
    // { d: day offset from today (VN date), hVN: VN hour, field, court, cu, price, s: status }
    const defs = [
      // ── Quá khứ: completed (tạo revenue) ──────────────────────────────────
      { d: -28, hVN: 8,  f: f1, c: c1A, cu: cuList[0], price: 350000, s: "completed" },
      { d: -25, hVN: 17, f: f1, c: c1B, cu: cuList[1], price: 350000, s: "completed" },
      { d: -22, hVN: 19, f: f1, c: c1A, cu: cuList[2], price: 350000, s: "completed" },
      { d: -20, hVN: 8,  f: f1, c: c1B, cu: cuList[3], price: 350000, s: "completed" },
      { d: -18, hVN: 15, f: f1, c: c1A, cu: cuList[4], price: 350000, s: "completed" },
      { d: -15, hVN: 17, f: f1, c: c1B, cu: cuList[0], price: 350000, s: "completed" },
      { d: -12, hVN: 19, f: f1, c: c1A, cu: cuList[1], price: 350000, s: "completed" },
      { d: -10, hVN: 8,  f: f1, c: c1B, cu: cuList[2], price: 350000, s: "completed" },
      { d: -7,  hVN: 17, f: f1, c: c1A, cu: cuList[3], price: 350000, s: "completed" },
      { d: -5,  hVN: 15, f: f1, c: c1B, cu: cuList[4], price: 350000, s: "completed" },
      { d: -3,  hVN: 19, f: f1, c: c1A, cu: cuList[0], price: 350000, s: "completed" },
      { d: -1,  hVN: 8,  f: f1, c: c1B, cu: cuList[1], price: 350000, s: "completed" },
      // Cầu lông - past
      { d: -26, hVN: 8,  f: f2, c: c2A, cu: cuList[2], price: 120000, s: "completed" },
      { d: -21, hVN: 18, f: f2, c: c2B, cu: cuList[3], price: 120000, s: "completed" },
      { d: -14, hVN: 8,  f: f2, c: c2A, cu: cuList[4], price: 120000, s: "completed" },
      { d: -9,  hVN: 18, f: f2, c: c2B, cu: cuList[0], price: 120000, s: "completed" },
      { d: -4,  hVN: 8,  f: f2, c: c2A, cu: cuList[1], price: 120000, s: "completed" },
      // Tennis - past
      { d: -24, hVN: 8,  f: f3, c: c3A, cu: cuList[3], price: 200000, s: "completed" },
      { d: -16, hVN: 14, f: f3, c: c3B, cu: cuList[4], price: 200000, s: "completed" },
      { d: -8,  hVN: 8,  f: f3, c: c3A, cu: cuList[0], price: 200000, s: "completed" },
      { d: -2,  hVN: 14, f: f3, c: c3B, cu: cuList[1], price: 200000, s: "completed" },
      // ── Hôm nay: confirmed + pending ──────────────────────────────────────
      { d: 0, hVN: 8,  f: f1, c: c1A, cu: cuList[0], price: 350000, s: "confirmed" },
      { d: 0, hVN: 10, f: f1, c: c1B, cu: cuList[1], price: 350000, s: "confirmed" },
      { d: 0, hVN: 15, f: f2, c: c2A, cu: cuList[2], price: 120000, s: "pending"   },
      { d: 0, hVN: 17, f: f1, c: c1A, cu: cuList[3], price: 350000, s: "pending"   },
      { d: 0, hVN: 19, f: f3, c: c3A, cu: cuList[4], price: 200000, s: "confirmed" },
      // ── Sắp tới: confirmed/pending ────────────────────────────────────────
      { d: 1, hVN: 8,  f: f1, c: c1A, cu: cuList[2], price: 350000, s: "confirmed" },
      { d: 1, hVN: 17, f: f2, c: c2B, cu: cuList[3], price: 120000, s: "confirmed" },
      { d: 2, hVN: 8,  f: f3, c: c3A, cu: cuList[4], price: 200000, s: "confirmed" },
      { d: 2, hVN: 15, f: f1, c: c1B, cu: cuList[0], price: 350000, s: "confirmed" },
      { d: 3, hVN: 19, f: f1, c: c1A, cu: cuList[1], price: 350000, s: "pending"   },
      { d: 4, hVN: 8,  f: f2, c: c2A, cu: cuList[2], price: 120000, s: "confirmed" },
      { d: 5, hVN: 14, f: f3, c: c3B, cu: cuList[3], price: 200000, s: "confirmed" },
      { d: 7, hVN: 17, f: f1, c: c1B, cu: cuList[4], price: 350000, s: "confirmed" },
    ];

    let ok = 0;
    for (let i = 0; i < defs.length; i++) {
      const b = defs[i];
      const vnDate = shiftDate(today, b.d);
      await upsertBooking({
        customer_id: b.cu.person_id,
        field_id:    b.f.field_id,
        court_id:    b.c.court_id,
        manager_id:  manager.person_id,
        start_time:  vnDt(vnDate, b.hVN),
        end_time:    vnDt(vnDate, b.hVN + 1),
        status: b.s,
        price:  b.price,
        note: `[SEED-MGR] idx-${i}`
      });
      ok++;
    }
    console.log(`${ok}/${defs.length} bookings upserted.`);

    // ── Chats + Messages ──────────────────────────────────────────────────────
    const chatPairs = [
      { cu: cuList[0], field: f1 },
      { cu: cuList[2], field: f2 },
      { cu: cuList[4], field: f3 },
    ];
    const convos = [
      [
        { from: "user",    text: "Chào anh, cho em hỏi sân còn trống chiều nay không ạ?" },
        { from: "manager", text: "Chào bạn! Hiện sân vẫn còn trống buổi chiều từ 15h-17h nhé." },
        { from: "user",    text: "Vậy em đặt slot 15:00-16:00 được không ạ?" },
        { from: "manager", text: "Được bạn nhé, bạn có thể đặt qua app hoặc mình đặt giúp!" },
        { from: "user",    text: "Cảm ơn anh nhiều ạ!" },
      ],
      [
        { from: "user",    text: "Sân cầu lông có cho mượn vợt không anh?" },
        { from: "manager", text: "Có bạn ơi, cho mượn vợt miễn phí khi đặt sân." },
        { from: "user",    text: "Tuyệt! Vậy em đặt sáng thứ 7 nhé." },
        { from: "manager", text: "Ok bạn, mình giữ sân cho bạn từ 8h-9h nhé!" },
      ],
      [
        { from: "user",    text: "Sân tennis giá thuê bao nhiêu 1 tiếng vậy anh?" },
        { from: "manager", text: "200.000đ/giờ bạn nhé, bao gồm bóng tập." },
        { from: "user",    text: "Vậy anh cho em đặt chiều thứ 4 từ 14h-15h." },
        { from: "manager", text: "Đã đặt cho bạn rồi. Nhớ đến đúng giờ nhé!" },
        { from: "user",    text: "Em cảm ơn anh!" },
        { from: "manager", text: "Không có gì, hẹn gặp bạn!" },
      ],
    ];

    const yesterday = shiftDate(today, -1);
    for (let p = 0; p < chatPairs.length; p++) {
      const { cu, field } = chatPairs[p];
      const chat = await upsertChat({ manager_id: manager.person_id, user_id: cu.person_id, field_id: field.field_id });
      if (!chat) continue;
      const msgs = convos[p];
      for (let m = 0; m < msgs.length; m++) {
        const msg = msgs[m];
        const isManager = msg.from === "manager";
        await insertMessage({
          chat_id:     chat.chat_id,
          sender_id:   isManager ? manager.person_id : cu.person_id,
          sender_type: isManager ? "manager" : "user",
          content:     msg.text,
          created_at:  vnDt(yesterday, 9, m * 5)
        });
      }
      const lastMsg = msgs[msgs.length - 1];
      await sequelize.query(
        `UPDATE chats SET last_message=:msg, last_message_at=:ts, updated_at=NOW() WHERE chat_id=:chat_id`,
        { replacements: { msg: lastMsg.text, ts: vnDt(yesterday, 9, (msgs.length - 1) * 5), chat_id: chat.chat_id } }
      );
    }
    console.log("Chat rooms + messages created.");

    // ── Summary ──────────────────────────────────────────────────────────────
    const [stats] = await sequelize.query(`
      SELECT
        COUNT(DISTINCT f.field_id) as fields,
        SUM(CASE WHEN b.status IN ('confirmed','completed') THEN b.price ELSE 0 END) as revenue,
        SUM(CASE WHEN DATE(b.start_time)=CURDATE() THEN 1 ELSE 0 END) as today
      FROM fields f LEFT JOIN bookings b ON b.field_id=f.field_id
      WHERE f.manager_id=:mgr
    `, { replacements: { mgr: manager.person_id } });
    const s = stats[0];

    console.log("\n=== Kết quả ===");
    console.log(`Fields: ${s.fields} | Today bookings: ${s.today} | Revenue: ${Number(s.revenue).toLocaleString("vi")} VND`);
    console.log("\nLogin manager:  manager01 / 123456");
    console.log("Login customer: khachhang01–05 / 123456");
  } catch (err) {
    console.error("Seed lỗi:", err.message);
    process.exitCode = 1;
  } finally {
    await sequelize.close();
  }
};

seed();
