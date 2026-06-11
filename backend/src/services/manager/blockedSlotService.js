import sequelize from '../../config/database.js';

/**
 * Lấy danh sách blocked slots của một field (chỉ field thuộc manager)
 */
export const listBlockedSlotsService = async (managerId, fieldId) => {
  const [rows] = await sequelize.query(
    `SELECT bs.slot_id, bs.field_id, bs.court_id, bs.block_date,
            bs.start_time, bs.end_time, bs.reason, bs.block_type,
            bs.created_at, fc.court_code, fc.court_name
     FROM field_blocked_slots bs
     INNER JOIN fields f ON bs.field_id = f.field_id
     LEFT JOIN field_courts fc ON bs.court_id = fc.court_id
     WHERE bs.field_id = ? AND f.manager_id = ?
     ORDER BY bs.block_date DESC, bs.start_time ASC`,
    { replacements: [fieldId, managerId] }
  );
  return rows;
};

/**
 * Tạo blocked slot mới
 */
export const createBlockedSlotService = async (managerId, fieldId, data) => {
  const { court_id, block_date, start_time, end_time, reason, block_type } = data;

  // Xác nhận field thuộc manager
  const [[field]] = await sequelize.query(
    'SELECT field_id FROM fields WHERE field_id = ? AND manager_id = ?',
    { replacements: [fieldId, managerId] }
  );
  if (!field) throw new Error('Field không tồn tại hoặc không có quyền truy cập');

  // Kiểm tra trùng thời gian
  const [[overlap]] = await sequelize.query(
    `SELECT slot_id FROM field_blocked_slots
     WHERE field_id = ? AND block_date = ?
       AND start_time < ? AND end_time > ?
       AND (court_id IS NULL OR court_id = ? OR ? IS NULL)`,
    { replacements: [fieldId, block_date, end_time, start_time, court_id ?? null, court_id ?? null] }
  );
  if (overlap) throw new Error('Đã có slot bị chặn trong khoảng thời gian này');

  const [result] = await sequelize.query(
    `INSERT INTO field_blocked_slots (field_id, court_id, block_date, start_time, end_time, reason, block_type)
     VALUES (?, ?, ?, ?, ?, ?, ?)`,
    { replacements: [fieldId, court_id ?? null, block_date, start_time, end_time, reason ?? null, block_type ?? 'maintenance'] }
  );

  const [[created]] = await sequelize.query(
    'SELECT * FROM field_blocked_slots WHERE slot_id = ?',
    { replacements: [result] }
  );
  return created;
};

/**
 * Xóa blocked slot
 */
export const deleteBlockedSlotService = async (managerId, fieldId, slotId) => {
  const [[slot]] = await sequelize.query(
    `SELECT bs.slot_id FROM field_blocked_slots bs
     INNER JOIN fields f ON bs.field_id = f.field_id
     WHERE bs.slot_id = ? AND bs.field_id = ? AND f.manager_id = ?`,
    { replacements: [slotId, fieldId, managerId] }
  );
  if (!slot) throw new Error('Slot không tồn tại hoặc không có quyền xóa');

  await sequelize.query(
    'DELETE FROM field_blocked_slots WHERE slot_id = ?',
    { replacements: [slotId] }
  );
  return { success: true };
};
