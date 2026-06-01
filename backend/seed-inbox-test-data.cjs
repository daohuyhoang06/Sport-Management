require('dotenv').config();
const mysql = require('mysql2/promise');

function argValue(name, fallback = null) {
  const prefix = `--${name}=`;
  const found = process.argv.find((x) => x.startsWith(prefix));
  return found ? found.slice(prefix.length) : fallback;
}

(async () => {
  const username = argValue('username');
  if (!username) {
    throw new Error('Missing --username=<username>');
  }

  const conn = await mysql.createConnection({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    port: Number(process.env.DB_PORT || 3306),
  });

  const [users] = await conn.query(
    "SELECT person_id, person_name, phone FROM person WHERE username = ? AND role = 'user' LIMIT 1",
    [username],
  );
  if (!users.length) throw new Error(`User not found: ${username}`);
  const user = users[0];

  const [managers] = await conn.query(
    "SELECT person_id, person_name, phone FROM person WHERE role='manager' AND status='active' ORDER BY person_id LIMIT 1",
  );
  if (!managers.length) throw new Error('No active manager found');
  const manager = managers[0];

  const [fields] = await conn.query(
    "SELECT field_id, field_name, location, phone, card_image_url, avatar_image_url FROM fields WHERE manager_id = ? ORDER BY field_id LIMIT 1",
    [manager.person_id],
  );
  if (!fields.length) throw new Error('No field for manager found');
  const field = fields[0];

  // Ensure booking
  let bookingId;
  const [existingBooking] = await conn.query(
    `SELECT booking_id FROM bookings
     WHERE customer_id = ? AND field_id = ?
     ORDER BY booking_id DESC LIMIT 1`,
    [user.person_id, field.field_id],
  );
  if (existingBooking.length) {
    bookingId = existingBooking[0].booking_id;
  } else {
    const [insertBooking] = await conn.query(
      `INSERT INTO bookings
       (customer_id, field_id, manager_id, start_time, end_time, status, note, price, pending_expires_at)
       VALUES (?, ?, ?, DATE_ADD(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 3 HOUR), 'confirmed', ?, ?, NULL)`,
      [
        user.person_id,
        field.field_id,
        manager.person_id,
        'Dat san test hop thu',
        250000,
      ],
    );
    bookingId = insertBooking.insertId;
  }

  // Ensure conversation
  let conversationId;
  const [existingConv] = await conn.query(
    `SELECT chat_id FROM chats
     WHERE user_id = ? AND manager_id = ? AND field_id = ? AND booking_id = ?
     ORDER BY chat_id DESC LIMIT 1`,
    [user.person_id, manager.person_id, field.field_id, bookingId],
  );
  if (existingConv.length) {
    conversationId = existingConv[0].chat_id;
  } else {
    const [insertConv] = await conn.query(
      `INSERT INTO chats
       (user_id, manager_id, field_id, booking_id, last_message, last_message_at, user_unread_count, owner_unread_count, created_at, updated_at)
       VALUES (?, ?, ?, ?, ?, NOW(), 0, 1, NOW(), NOW())`,
      [
        user.person_id,
        manager.person_id,
        field.field_id,
        bookingId,
        'Cho san da san sang, hen gap ban luc 18:00.',
      ],
    );
    conversationId = insertConv.insertId;
  }

  // Ensure sample messages
  const [msgCountRows] = await conn.query(
    'SELECT COUNT(*) AS c FROM messages WHERE chat_id = ?',
    [conversationId],
  );
  if (Number(msgCountRows[0].c) < 3) {
    await conn.query(
      `INSERT INTO messages
       (chat_id, sender_id, sender_type, message_type, message_text, content, is_read, created_at, updated_at)
       VALUES
       (?, ?, 'manager', 'text', ?, ?, 0, NOW() - INTERVAL 40 MINUTE, NOW() - INTERVAL 40 MINUTE),
       (?, ?, 'user', 'text', ?, ?, 1, NOW() - INTERVAL 25 MINUTE, NOW() - INTERVAL 25 MINUTE),
       (?, ?, 'manager', 'text', ?, ?, 0, NOW() - INTERVAL 5 MINUTE, NOW() - INTERVAL 5 MINUTE)`,
      [
        conversationId,
        manager.person_id,
        `Xin chao ${user.person_name || 'ban'}, ben minh da nhan lich dat san.`,
        `Xin chao ${user.person_name || 'ban'}, ben minh da nhan lich dat san.`,
        conversationId,
        user.person_id,
        'Cam on chu san, minh se toi dung gio.',
        'Cam on chu san, minh se toi dung gio.',
        conversationId,
        manager.person_id,
        'Cho san da san sang, hen gap ban luc 18:00.',
        'Cho san da san sang, hen gap ban luc 18:00.',
      ],
    );
  }

  await conn.query(
    `UPDATE chats
     SET last_message = ?, last_message_at = NOW(), updated_at = NOW(), owner_unread_count = 1
     WHERE chat_id = ?`,
    ['Cho san da san sang, hen gap ban luc 18:00.', conversationId],
  );

  // Notifications: booking_success, upcoming_match, promotion, system
  const notificationTemplates = [
    {
      type: 'booking_success',
      section: 'priority',
      title: 'Dat san thanh cong',
      subtitle: `${field.field_name} • 18:00 hom nay`,
      content: `Booking #B${bookingId} da duoc xac nhan.`,
      target_type: 'booking',
      target_id: bookingId,
      booking_id: bookingId,
      field_id: field.field_id,
      metadata: null,
    },
    {
      type: 'upcoming_match',
      section: 'priority',
      title: 'Sap den gio thi dau',
      subtitle: 'Con 1 gio nua toi lich dat san',
      content: 'Vui long den truoc 10 phut de check-in.',
      target_type: 'booking',
      target_id: bookingId,
      booking_id: bookingId,
      field_id: field.field_id,
      metadata: null,
    },
    {
      type: 'promotion',
      section: 'activity',
      title: 'Uu dai danh cho ban',
      subtitle: 'Giam 20% cho khung gio sang',
      content: 'Ap dung den 30/06/2026 cho dat san qua app.',
      target_type: 'promotion',
      target_id: field.field_id,
      booking_id: null,
      field_id: field.field_id,
      metadata: null,
    },
    {
      type: 'system',
      section: 'activity',
      title: 'Thong bao he thong',
      subtitle: 'Cap nhat tinh nang hop thu',
      content: 'Ban da co the nhan tin truc tiep voi chu san.',
      target_type: 'none',
      target_id: null,
      booking_id: null,
      field_id: field.field_id,
      metadata: null,
    },
  ];

  for (const tpl of notificationTemplates) {
    const [exists] = await conn.query(
      `SELECT id FROM notifications
       WHERE user_id = ? AND type = ? AND booking_id <=> ? AND field_id <=> ?
       ORDER BY id DESC LIMIT 1`,
      [user.person_id, tpl.type, tpl.booking_id, tpl.field_id],
    );

    if (!exists.length) {
      await conn.query(
        `INSERT INTO notifications
         (user_id, type, section, title, subtitle, content, target_type, target_id, booking_id, field_id, is_read, metadata, created_at, updated_at)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, NOW(), NOW())`,
        [
          user.person_id,
          tpl.type,
          tpl.section,
          tpl.title,
          tpl.subtitle,
          tpl.content,
          tpl.target_type,
          tpl.target_id,
          tpl.booking_id,
          tpl.field_id,
          tpl.metadata,
        ],
      );
    }
  }

  const [notificationSummary] = await conn.query(
    'SELECT type, COUNT(*) AS c FROM notifications WHERE user_id = ? GROUP BY type ORDER BY type',
    [user.person_id],
  );

  console.log('Seed inbox test data done');
  console.log({
    username,
    userId: user.person_id,
    managerId: manager.person_id,
    fieldId: field.field_id,
    bookingId,
    conversationId,
    notificationSummary,
  });

  await conn.end();
})().catch((error) => {
  console.error('Seed failed:', error.message);
  process.exit(1);
});
