require('dotenv').config();
const mysql = require('mysql2/promise');

(async () => {
  const conn = await mysql.createConnection({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    port: Number(process.env.DB_PORT || 3306),
  });

  const [users] = await conn.query("SELECT person_id, person_name FROM person WHERE role='user' ORDER BY person_id");
  const [fields] = await conn.query("SELECT field_id, field_name, manager_id FROM fields WHERE manager_id IS NOT NULL ORDER BY field_id LIMIT 1");
  if (!fields.length) throw new Error('No field with manager found');
  const field = fields[0];

  let seededUsers = 0;

  for (const u of users) {
    const userId = u.person_id;

    const [existingInbox] = await conn.query("SELECT COUNT(*) AS c FROM notifications WHERE user_id=?", [userId]);
    const [existingChats] = await conn.query("SELECT COUNT(*) AS c FROM chats WHERE user_id=?", [userId]);
    if (Number(existingInbox[0].c) > 0 || Number(existingChats[0].c) > 0) {
      continue;
    }

    await conn.query(
      `INSERT INTO chats (user_id, manager_id, field_id, booking_id, last_message, last_message_at, user_unread_count, owner_unread_count, created_at, updated_at)
       VALUES (?, ?, ?, NULL, ?, NOW(), 1, 0, NOW(), NOW())`,
      [userId, field.manager_id, field.field_id, 'San da san sang, ban qua check-in truoc 10 phut nhe!']
    );

    const [chatRows] = await conn.query(
      "SELECT chat_id FROM chats WHERE user_id=? AND manager_id=? AND field_id=? ORDER BY chat_id DESC LIMIT 1",
      [userId, field.manager_id, field.field_id]
    );
    const chatId = chatRows[0].chat_id;

    await conn.query(
      `INSERT INTO messages (chat_id, sender_id, sender_type, message_type, message_text, content, is_read, created_at, updated_at)
       VALUES
       (?, ?, 'manager', 'text', ?, ?, 0, NOW() - INTERVAL 20 MINUTE, NOW() - INTERVAL 20 MINUTE),
       (?, ?, 'user', 'text', ?, ?, 1, NOW() - INTERVAL 10 MINUTE, NOW() - INTERVAL 10 MINUTE),
       (?, ?, 'manager', 'text', ?, ?, 0, NOW() - INTERVAL 2 MINUTE, NOW() - INTERVAL 2 MINUTE)`,
      [
        chatId, field.manager_id,
        `Xin chao ban, minh la chu san ${field.field_name}.`,
        `Xin chao ban, minh la chu san ${field.field_name}.`,
        chatId, userId,
        'Ok ban, toi se den dung gio.',
        'Ok ban, toi se den dung gio.',
        chatId, field.manager_id,
        'San da san sang, ban qua check-in truoc 10 phut nhe!',
        'San da san sang, ban qua check-in truoc 10 phut nhe!',
      ]
    );

    await conn.query(
      `INSERT INTO notifications (user_id, type, section, title, subtitle, content, target_type, target_id, field_id, is_read, metadata, created_at, updated_at)
       VALUES
       (?, 'booking_success', 'priority', 'Dat san thanh cong', ?, ?, 'booking', NULL, ?, 0, NULL, NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 3 HOUR),
       (?, 'message', 'messages', ?, ?, ?, 'conversation', ?, ?, 0, NULL, NOW() - INTERVAL 90 MINUTE, NOW() - INTERVAL 90 MINUTE),
       (?, 'system_notice', 'activity', 'Thong bao he thong', 'Tinh nang hop thu da san sang', 'Ban co the nhan tin truc tiep voi chu san ngay trong app.', 'none', NULL, ?, 0, NULL, NOW() - INTERVAL 30 MINUTE, NOW() - INTERVAL 30 MINUTE)`,
      [
        userId,
        `${field.field_name} - 18:00 hom nay`,
        'Don dat san cua ban da duoc xac nhan.',
        field.field_id,
        userId,
        `${field.field_name} da phan hoi`,
        'Ban co 1 tin nhan moi tu chu san.',
        'Nhan de mo hoi thoai voi chu san.',
        chatId,
        field.field_id,
        userId,
        field.field_id,
      ]
    );

    seededUsers += 1;
  }

  const [sumNoti] = await conn.query('SELECT user_id, COUNT(*) AS c FROM notifications GROUP BY user_id ORDER BY user_id');
  const [sumChat] = await conn.query('SELECT user_id, COUNT(*) AS c FROM chats GROUP BY user_id ORDER BY user_id');

  console.log('Seed done. users seeded:', seededUsers);
  console.log('Notifications per user:', sumNoti);
  console.log('Chats per user:', sumChat);

  await conn.end();
})().catch((e) => {
  console.error('Seed failed:', e.message);
  process.exit(1);
});
