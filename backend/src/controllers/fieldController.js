import Field from "../models/Field.js";
import { Op } from "sequelize";
import sequelize from "../config/database.js";

// GET /api/user/fields
export const listFields = async (req, res) => {
  try {
    const { q, location, category, limit = 50, page = 1 } = req.query;
    const offset = (page - 1) * limit;

    const where = { status: "active" };
    
    // Handle search query - support both 'q' and 'location' parameters
    const searchTerm = (q || location || '').trim();
    
    if (searchTerm) {
      // Case-insensitive search using LOWER() function
      // This handles Vietnamese characters with diacritics correctly
      where[Op.or] = [
        sequelize.where(
          sequelize.fn('LOWER', sequelize.col('field_name')),
          'LIKE',
          `%${searchTerm.toLowerCase()}%`
        ),
        sequelize.where(
          sequelize.fn('LOWER', sequelize.col('location')),
          'LIKE',
          `%${searchTerm.toLowerCase()}%`
        )
      ];
    }

    const fields = await Field.findAll({
      where,
      order: [["field_id", "ASC"]],
      limit: Number.parseInt(limit, 10),
      offset: Number.parseInt(offset, 10),
    });

    const data = fields.map((f) => ({
      field_id: f.field_id,
      field_name: f.field_name,
      location: f.location || "Chưa cập nhật",
      status: f.status,
      image: "/images/fields/placeholder.svg",
      price: "Liên hệ",
      pricePerHour: null,
      rating: (Math.random() * 1.5 + 3.5).toFixed(1),
      reviews: Math.floor(Math.random() * 200 + 10),
      type: "Sân 7 người",
      facilities: ["Bãi đỗ xe", "Đèn chiếu sáng", "Phòng thay đồ"],
      openTime: "5h - 23h",
      distance: "2.5km",
      isOpen: true,
    }));

    res.json(data);
  } catch (error) {
    console.error("List fields error:", error);
    res.status(500).json({ message: "Server error when fetching fields" });
  }
};

// GET /api/user/fields/:id
export const getField = async (req, res) => {
  try {
    const { id } = req.params;
    const f = await Field.findOne({ where: { field_id: id } });
    if (!f) return res.status(404).json({ message: "Field not found" });

    const now = new Date();
    const slots = [];
    for (let d = 0; d < 7; d++) {
      const day = new Date(now);
      day.setDate(now.getDate() + d);
      for (let h = 6; h < 22; h += 2) {
        const start = new Date(day);
        start.setHours(h, 0, 0, 0);
        const end = new Date(day);
        end.setHours(h + 2, 0, 0, 0);
        slots.push({
          start_time: start.toISOString(),
          end_time: end.toISOString(),
          available: true,
        });
      }
    }

    const data = {
      field_id: f.field_id,
      field_name: f.field_name,
      location: f.location,
      status: f.status,
      image: "/images/fields/placeholder.svg",
      price: "Liên hệ",
      facilities: ["Bãi đỗ xe", "Đèn chiếu sáng"],
      slots,
    };

    res.json(data);
  } catch (err) {
    console.error("getField error", err);
    res.status(500).json({ message: "Server error" });
  }
};

// POST /api/user/bookings
export const createBooking = async (req, res) => {
  try {
    const { customer_id, field_id, start_time, end_time, price, note } =
      req.body;

    if (!customer_id) {
      return res.status(400).json({ message: "Missing customer_id" });
    }
    if (!field_id) {
      return res.status(400).json({ message: "Missing field_id" });
    }
    if (!start_time) {
      return res.status(400).json({ message: "Missing start_time" });
    }
    if (!end_time) {
      return res.status(400).json({ message: "Missing end_time" });
    }

    const finalPrice = price || 0;
    const finalNote = note || "";

    const formatDatetime = (isoString) => {
      const date = new Date(isoString);
      return date.toISOString().slice(0, 19).replace("T", " ");
    };

    const mysqlStartTime = formatDatetime(start_time);
    const mysqlEndTime = formatDatetime(end_time);

    const [customerCheck] = await sequelize.query(
      "SELECT person_id FROM person WHERE person_id = ? LIMIT 1",
      { replacements: [customer_id] }
    );

    if (!customerCheck || customerCheck.length === 0) {
      return res.status(400).json({
        message:
          "Customer ID does not exist. Please create a user account first.",
        error: "INVALID_CUSTOMER_ID",
      });
    }

    const [fieldCheck] = await sequelize.query(
      "SELECT field_id FROM fields WHERE field_id = ? LIMIT 1",
      { replacements: [field_id] }
    );

    if (!fieldCheck || fieldCheck.length === 0) {
      return res.status(400).json({
        message: "Field ID does not exist",
        error: "INVALID_FIELD_ID",
      });
    }

    await sequelize.query(
      `INSERT INTO bookings (customer_id, field_id, start_time, end_time, price, note, status) VALUES (?, ?, ?, ?, ?, ?, 'pending')`,
      {
        replacements: [
          customer_id,
          field_id,
          mysqlStartTime,
          mysqlEndTime,
          finalPrice,
          finalNote,
        ],
      }
    );

    const [rows] = await sequelize.query(
      "SELECT * FROM bookings ORDER BY booking_id DESC LIMIT 1"
    );
    const booking = rows?.[0] ?? null;

    res.status(201).json({ message: "Booking created", booking });
  } catch (err) {
    console.error("createBooking error", err);
    res.status(500).json({
      message: "Server error when creating booking",
      error: err.message,
      sqlError: err.original?.sqlMessage || err.original?.message,
    });
  }
};

// GET /api/user/bookings/:id - Get booking details with field info
export const getBooking = async (req, res) => {
  try {
    const { id } = req.params;

    // Join bookings with fields table to get field info
    const [rows] = await sequelize.query(
      `SELECT 
        b.booking_id, b.customer_id, b.field_id, b.start_time, b.end_time, 
        b.price, b.status, b.note,
        f.field_name, f.location
      FROM bookings b
      LEFT JOIN fields f ON b.field_id = f.field_id
      WHERE b.booking_id = ?
      LIMIT 1`,
      { replacements: [id] }
    );

    const booking = rows?.[0];
    if (!booking) {
      return res.status(404).json({ message: "Booking not found" });
    }

    res.json(booking);
  } catch (err) {
    console.error("getBooking error", err);
    res.status(500).json({ message: "Server error when fetching booking" });
  }
};

// PUT /api/user/bookings/:id - Update booking (payment method, status)
export const updateBooking = async (req, res) => {
  try {
    const { id } = req.params;
    const { payment_method, status } = req.body;

    // Build dynamic update query
    const updates = [];
    const replacements = [];

    if (payment_method) {
      updates.push("note = COALESCE(note, '') || ' | Payment: ' || ?");
      replacements.push(payment_method);
    }

    if (status) {
      updates.push("status = ?");
      replacements.push(status);
    }

    if (updates.length === 0) {
      return res.status(400).json({ message: "No fields to update" });
    }

    replacements.push(id);

    await sequelize.query(
      `UPDATE bookings SET ${updates.join(", ")} WHERE booking_id = ?`,
      { replacements }
    );

    const [rows] = await sequelize.query(
      "SELECT * FROM bookings WHERE booking_id = ? LIMIT 1",
      { replacements: [id] }
    );

    res.json({ message: "Booking updated", booking: rows?.[0] });
  } catch (err) {
    console.error("updateBooking error", err);
    res.status(500).json({ message: "Server error when updating booking" });
  }
};

// GET /api/manager/bookings - Get all bookings for manager
export const listBookings = async (req, res) => {
  try {
    const [rows] = await sequelize.query(
      `SELECT 
        b.booking_id, b.customer_id, b.field_id, b.start_time, b.end_time,
        b.price, b.status, b.note, b.created_at,
        f.field_name, f.location,
        p.person_name as customer_name, p.phone as customer_phone
      FROM bookings b
      LEFT JOIN fields f ON b.field_id = f.field_id
      LEFT JOIN person p ON b.customer_id = p.person_id
      ORDER BY b.created_at DESC`
    );

    res.json(rows || []);
  } catch (err) {
    console.error("listBookings error", err);
    res.status(500).json({ message: "Server error when fetching bookings" });
  }
};

// PUT /api/manager/bookings/:id/approve - Approve booking
export const approveBooking = async (req, res) => {
  try {
    const { id } = req.params;

    await sequelize.query(
      `UPDATE bookings SET status = 'confirmed' WHERE booking_id = ?`,
      { replacements: [id] }
    );

    const [rows] = await sequelize.query(
      "SELECT * FROM bookings WHERE booking_id = ? LIMIT 1",
      { replacements: [id] }
    );

    res.json({ message: "Booking approved", booking: rows?.[0] });
  } catch (err) {
    console.error("approveBooking error", err);
    res.status(500).json({ message: "Server error when approving booking" });
  }
};

// PUT /api/manager/bookings/:id/reject - Reject booking
export const rejectBooking = async (req, res) => {
  try {
    const { id } = req.params;
    const { reason } = req.body;

    const noteUpdate = reason ? ` | Lý do từ chối: ${reason}` : "";

    await sequelize.query(
      `UPDATE bookings SET status = 'rejected', note = COALESCE(note, '') || ? WHERE booking_id = ?`,
      { replacements: [noteUpdate, id] }
    );

    const [rows] = await sequelize.query(
      "SELECT * FROM bookings WHERE booking_id = ? LIMIT 1",
      { replacements: [id] }
    );

    res.json({ message: "Booking rejected", booking: rows?.[0] });
  } catch (err) {
    console.error("rejectBooking error", err);
    res.status(500).json({ message: "Server error when rejecting booking" });
  }
};
