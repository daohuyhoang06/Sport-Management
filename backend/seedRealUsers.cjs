/**
 * Seed: tạo người dùng thật, đặt sân thật, chat có tin nhắn
 * Field 56 (Sân Bóng Đá Thành Công) – courts 1,2 – manager01 (person_id=2)
 */
const mysql = require('mysql2/promise');
const bcrypt = require('bcrypt');

const DB = { host: 'localhost', user: 'root', password: 'admin@123', database: 'sport_management' };

async function main() {
  const c = await mysql.createConnection(DB);

  // ── 1. Tạo người dùng ───────────────────────────────────────────────────────
  const users = [
    { name: 'Nguyễn Văn Minh', phone: '0912300101', email: 'minh.nv@gmail.com', username: 'minhnv101' },
    { name: 'Trần Thị Hoa',    phone: '0987600202', email: 'hoa.tt@gmail.com',   username: 'hoatt202'  },
    { name: 'Lê Quang Nam',    phone: '0971200303', email: 'nam.lq@gmail.com',   username: 'namlq303'  },
  ];
  const password = await bcrypt.hash('123456', 10);

  const userIds = [];
  for (const u of users) {
    const [exist] = await c.query('SELECT person_id FROM person WHERE username=?', [u.username]);
    if (exist.length > 0) {
      console.log(`User ${u.username} already exists (id=${exist[0].person_id})`);
      userIds.push(exist[0].person_id);
      continue;
    }
    const [r] = await c.query(
      `INSERT INTO person (person_name, phone, email, username, password, role, status)
       VALUES (?,?,?,?,?,'user','active')`,
      [u.name, u.phone, u.email, u.username, password]
    );
    userIds.push(r.insertId);
    console.log(`Created user: ${u.name} (id=${r.insertId}, phone=${u.phone})`);
  }

  // ── 2. Tạo bookings ─────────────────────────────────────────────────────────
  const bookingSlots = [
    { userId: userIds[0], fieldId: 56, courtId: 1, start: '2026-06-15 08:00:00', end: '2026-06-15 10:00:00', price: 200000, status: 'confirmed', note: 'Đặt sân lần đầu' },
    { userId: userIds[1], fieldId: 56, courtId: 2, start: '2026-06-15 10:00:00', end: '2026-06-15 12:00:00', price: 200000, status: 'pending',   note: 'Xin sân giờ sáng' },
    { userId: userIds[2], fieldId: 56, courtId: 1, start: '2026-06-16 14:00:00', end: '2026-06-16 16:00:00', price: 200000, status: 'confirmed', note: 'Đặt cho nhóm 5 người' },
    { userId: userIds[0], fieldId: 56, courtId: 2, start: '2026-06-17 09:00:00', end: '2026-06-17 11:00:00', price: 200000, status: 'completed', note: 'Đã chơi xong' },
  ];

  for (const b of bookingSlots) {
    const [exist] = await c.query(
      'SELECT booking_id FROM bookings WHERE customer_id=? AND start_time=? AND field_id=?',
      [b.userId, b.start, b.fieldId]
    );
    if (exist.length > 0) {
      console.log(`Booking already exists for userId=${b.userId} at ${b.start}`);
      continue;
    }
    const [r] = await c.query(
      `INSERT INTO bookings (customer_id, field_id, court_id, manager_id, start_time, end_time, status, note, price)
       VALUES (?,?,?,2,?,?,?,?,?)`,
      [b.userId, b.fieldId, b.courtId, b.start, b.end, b.status, b.note, b.price]
    );
    console.log(`Created booking id=${r.insertId} for userId=${b.userId} status=${b.status}`);
  }

  // ── 3. Tạo chat + tin nhắn ───────────────────────────────────────────────────
  const managerId = 2;
  const convMessages = [
    {
      userId: userIds[0],
      msgs: [
        { sender: userIds[0], text: 'Chào anh/chị, tôi muốn hỏi về sân bóng đá ạ' },
        { sender: managerId,   text: 'Chào bạn! Bạn cần hỗ trợ gì ạ?' },
        { sender: userIds[0], text: 'Sân A1 còn trống thứ 7 tuần này không ạ?' },
        { sender: managerId,   text: 'Dạ, thứ 7 sáng từ 8h-10h còn trống bạn nhé. Bạn có muốn đặt không?' },
        { sender: userIds[0], text: 'Ok anh ơi, tôi đặt nhé!' },
      ]
    },
    {
      userId: userIds[1],
      msgs: [
        { sender: userIds[1], text: 'Xin chào! Tôi vừa đặt sân lúc 10h sáng mai, xác nhận giúp tôi với ạ' },
        { sender: managerId,   text: 'Chào chị! Đơn đặt sân của chị đã nhận được rồi ạ, chúng tôi sẽ xác nhận trong 30 phút' },
        { sender: userIds[1], text: 'Cảm ơn anh/chị nhiều ạ' },
      ]
    },
    {
      userId: userIds[2],
      msgs: [
        { sender: userIds[2], text: 'Anh ơi sân bóng có cho thuê áo không?' },
        { sender: managerId,   text: 'Dạ có bạn nhé, 20k/áo. Bạn cần bao nhiêu chiếc?' },
        { sender: userIds[2], text: 'Cho mình 10 cái nhé anh' },
        { sender: managerId,   text: 'Ok bạn nhé, mình sẽ chuẩn bị sẵn trước giờ đặt!' },
      ]
    },
  ];

  for (const conv of convMessages) {
    let [chatRows] = await c.query(
      'SELECT chat_id FROM chats WHERE user_id=? AND manager_id=?',
      [conv.userId, managerId]
    );
    let chatId;
    if (chatRows.length > 0) {
      chatId = chatRows[0].chat_id;
      console.log(`Chat already exists: chat_id=${chatId}`);
    } else {
      const [r] = await c.query(
        'INSERT INTO chats (user_id, manager_id, created_at, updated_at) VALUES (?,?,NOW(),NOW())',
        [conv.userId, managerId]
      );
      chatId = r.insertId;
      console.log(`Created chat id=${chatId} for userId=${conv.userId}`);
    }

    // Xóa tin nhắn cũ nếu có để tránh trùng
    const [existMsgs] = await c.query('SELECT COUNT(*) as cnt FROM messages WHERE chat_id=?', [chatId]);
    if (existMsgs[0].cnt > 0) {
      console.log(`Chat ${chatId} already has messages, skipping`);
      continue;
    }

    let offset = -conv.msgs.length * 5;
    for (const msg of conv.msgs) {
      offset += 5;
      await c.query(
        `INSERT INTO messages (chat_id, sender_id, message_text, content, is_read, created_at)
         VALUES (?,?,?,?,1, DATE_ADD(NOW(), INTERVAL ? MINUTE))`,
        [chatId, msg.sender, msg.text, msg.text, offset]
      );
    }
    const lastMsg = conv.msgs[conv.msgs.length - 1].text;
    await c.query(
      'UPDATE chats SET last_message=?, last_message_at=NOW(), updated_at=NOW() WHERE chat_id=?',
      [lastMsg, chatId]
    );
    console.log(`Added ${conv.msgs.length} messages to chat ${chatId}`);
  }

  await c.end();
  console.log('\nDone! Seed data created successfully.');
}

main().catch(console.error);
