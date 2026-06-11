import sequelize from "../../config/database.js";

const isBlank = (value) =>
  value === undefined || value === null || String(value).trim().length === 0;

const normalizeDate = (value) => {
  const raw = String(value || "").trim();
  const match = raw.match(/^(\d{4})-(\d{2})-(\d{2})$/);
  if (!match) return null;
  const normalized = `${match[1]}-${match[2]}-${match[3]}`;
  const parsed = new Date(`${normalized}T00:00:00`);
  return Number.isNaN(parsed.getTime()) ? null : normalized;
};

const normalizeTime = (value) => {
  if (value instanceof Date) {
    return `${String(value.getHours()).padStart(2, "0")}:${String(
      value.getMinutes(),
    ).padStart(2, "0")}:${String(value.getSeconds()).padStart(2, "0")}`;
  }
  const raw = String(value || "").trim();
  const match = raw.match(/^(\d{2}):(\d{2})(?::(\d{2}))?$/);
  if (!match) return null;
  return `${match[1]}:${match[2]}:${match[3] || "00"}`;
};

const timeToMinutes = (value) => {
  const normalized = normalizeTime(value);
  if (!normalized) return null;
  const [hours, minutes] = normalized
    .split(":")
    .map((item) => Number.parseInt(item, 10) || 0);
  return hours * 60 + minutes;
};

const mapBlockedSlotRow = (row) => ({
  slot_id: Number(row.slot_id),
  field_id: Number(row.field_id),
  court_id:
    row.court_id === null || row.court_id === undefined
      ? null
      : Number(row.court_id),
  block_date: String(row.block_date || "").slice(0, 10),
  start_time: normalizeTime(row.start_time),
  end_time: normalizeTime(row.end_time),
  reason: row.reason ?? null,
  block_type: row.block_type ?? "maintenance",
  court_code: row.court_code ?? null,
  court_name: row.court_name ?? null,
});

const loadManagedField = async (managerId, fieldId) => {
  const [[field]] = await sequelize.query(
    `SELECT field_id, field_name
     FROM fields
     WHERE field_id = ? AND manager_id = ?
     LIMIT 1`,
    { replacements: [fieldId, managerId] },
  );

  return field || null;
};

const loadCourtForField = async (fieldId, courtId) => {
  const [[court]] = await sequelize.query(
    `SELECT court_id
     FROM field_courts
     WHERE field_id = ? AND court_id = ?
     LIMIT 1`,
    { replacements: [fieldId, courtId] },
  );

  return court || null;
};

const loadBlockedSlotById = async (slotId, managerId, fieldId) => {
  const [[slot]] = await sequelize.query(
    `SELECT
       fs.slot_id,
       fs.field_id,
       fs.court_id,
       fs.block_date,
       fs.start_time,
       fs.end_time,
       fs.reason,
       fs.block_type,
       fc.court_code,
       fc.court_name
     FROM field_blocked_slots fs
     INNER JOIN fields f ON f.field_id = fs.field_id
     LEFT JOIN field_courts fc ON fc.court_id = fs.court_id
     WHERE fs.slot_id = ? AND f.manager_id = ? AND fs.field_id = ?
     LIMIT 1`,
    { replacements: [slotId, managerId, fieldId] },
  );

  return slot || null;
};

const buildErrorResponse = (res, error, fallbackMessage) => {
  const message = error?.message || "";
  const status =
    message.includes("not found") || message.includes("unauthorized")
      ? 404
      : 400;

  return res.status(status).json({
    success: false,
    message: fallbackMessage,
    error: message,
  });
};

export const listBlockedSlots = async (req, res) => {
  try {
    const managerId = req.user.id;
    const fieldId = Number.parseInt(req.params.id, 10);

    if (!Number.isInteger(fieldId)) {
      return res.status(400).json({
        success: false,
        message: "Field ID khong hop le",
      });
    }

    const field = await loadManagedField(managerId, fieldId);
    if (!field) {
      return res.status(404).json({
        success: false,
        message: "Field not found or unauthorized",
      });
    }

    const [rows] = await sequelize.query(
      `SELECT
         fs.slot_id,
         fs.field_id,
         fs.court_id,
         fs.block_date,
         fs.start_time,
         fs.end_time,
         fs.reason,
         fs.block_type,
         fc.court_code,
         fc.court_name
       FROM field_blocked_slots fs
       LEFT JOIN field_courts fc ON fc.court_id = fs.court_id
       WHERE fs.field_id = ?
       ORDER BY fs.block_date DESC, fs.start_time DESC, fs.slot_id DESC`,
      { replacements: [fieldId] },
    );

    res.json({
      success: true,
      data: rows.map(mapBlockedSlotRow),
    });
  } catch (error) {
    console.error("Error listing blocked slots:", error);
    buildErrorResponse(res, error, "Loi khi lay danh sach blocked slots");
  }
};

export const createBlockedSlot = async (req, res) => {
  try {
    const managerId = req.user.id;
    const fieldId = Number.parseInt(req.params.id, 10);
    const { block_date, start_time, end_time, reason, block_type, court_id } =
      req.body;

    if (!Number.isInteger(fieldId)) {
      return res.status(400).json({
        success: false,
        message: "Field ID khong hop le",
      });
    }

    const normalizedDate = normalizeDate(block_date);
    const normalizedStart = normalizeTime(start_time);
    const normalizedEnd = normalizeTime(end_time);
    const normalizedBlockType = isBlank(block_type)
      ? "maintenance"
      : String(block_type).trim().toLowerCase();
    const normalizedReason = isBlank(reason) ? null : String(reason).trim();

    if (!normalizedDate || !normalizedStart || !normalizedEnd) {
      return res.status(400).json({
        success: false,
        message: "block_date, start_time va end_time la bat buoc",
      });
    }

    if (timeToMinutes(normalizedStart) >= timeToMinutes(normalizedEnd)) {
      return res.status(400).json({
        success: false,
        message: "start_time phai nho hon end_time",
      });
    }

    const field = await loadManagedField(managerId, fieldId);
    if (!field) {
      return res.status(404).json({
        success: false,
        message: "Field not found or unauthorized",
      });
    }

    let normalizedCourtId = null;
    if (!isBlank(court_id)) {
      const parsedCourtId = Number.parseInt(court_id, 10);
      if (!Number.isInteger(parsedCourtId)) {
        return res.status(400).json({
          success: false,
          message: "court_id khong hop le",
        });
      }

      const court = await loadCourtForField(fieldId, parsedCourtId);
      if (!court) {
        return res.status(400).json({
          success: false,
          message: "court_id khong thuoc field nay",
        });
      }
      normalizedCourtId = parsedCourtId;
    }

    const [existingRows] = await sequelize.query(
      `SELECT slot_id
       FROM field_blocked_slots
       WHERE field_id = ?
         AND block_date = ?
         AND start_time = ?
         AND end_time = ?
         AND ((court_id IS NULL AND ? IS NULL) OR court_id = ?)
       LIMIT 1`,
      {
        replacements: [
          fieldId,
          normalizedDate,
          normalizedStart,
          normalizedEnd,
          normalizedCourtId,
          normalizedCourtId,
        ],
      },
    );

    let slotId = existingRows[0]?.slot_id || null;

    if (slotId) {
      await sequelize.query(
        `UPDATE field_blocked_slots
         SET court_id = ?, reason = ?, block_type = ?, updated_at = CURRENT_TIMESTAMP
         WHERE slot_id = ?`,
        {
          replacements: [
            normalizedCourtId,
            normalizedReason,
            normalizedBlockType,
            slotId,
          ],
        },
      );
    } else {
      const [result] = await sequelize.query(
        `INSERT INTO field_blocked_slots (
           field_id, court_id, block_date, start_time, end_time, reason, block_type
         )
         VALUES (?, ?, ?, ?, ?, ?, ?)`,
        {
          replacements: [
            fieldId,
            normalizedCourtId,
            normalizedDate,
            normalizedStart,
            normalizedEnd,
            normalizedReason,
            normalizedBlockType,
          ],
        },
      );
      slotId = result?.insertId || result;
    }

    const slot = await loadBlockedSlotById(slotId, managerId, fieldId);
    return res.status(201).json({
      success: true,
      data: mapBlockedSlotRow(
        slot || {
          slot_id: slotId,
          field_id: fieldId,
          court_id: normalizedCourtId,
          block_date: normalizedDate,
          start_time: normalizedStart,
          end_time: normalizedEnd,
          reason: normalizedReason,
          block_type: normalizedBlockType,
          court_code: null,
          court_name: null,
        },
      ),
    });
  } catch (error) {
    console.error("Error creating blocked slot:", error);
    buildErrorResponse(res, error, "Loi khi tao blocked slot");
  }
};

export const deleteBlockedSlot = async (req, res) => {
  try {
    const managerId = req.user.id;
    const fieldId = Number.parseInt(req.params.id, 10);
    const slotId = Number.parseInt(req.params.slotId, 10);

    if (!Number.isInteger(fieldId) || !Number.isInteger(slotId)) {
      return res.status(400).json({
        success: false,
        message: "Field ID hoac slot ID khong hop le",
      });
    }

    const field = await loadManagedField(managerId, fieldId);
    if (!field) {
      return res.status(404).json({
        success: false,
        message: "Field not found or unauthorized",
      });
    }

    const [result] = await sequelize.query(
      `DELETE fs
       FROM field_blocked_slots fs
       INNER JOIN fields f ON f.field_id = fs.field_id
       WHERE fs.slot_id = ? AND fs.field_id = ? AND f.manager_id = ?`,
      { replacements: [slotId, fieldId, managerId] },
    );

    if (!result?.affectedRows) {
      return res.status(404).json({
        success: false,
        message: "Blocked slot not found",
      });
    }

    return res.json({
      success: true,
      message: "Xoa blocked slot thanh cong",
    });
  } catch (error) {
    console.error("Error deleting blocked slot:", error);
    buildErrorResponse(res, error, "Loi khi xoa blocked slot");
  }
};
