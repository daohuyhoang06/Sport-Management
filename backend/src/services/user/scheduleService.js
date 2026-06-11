import sequelize from "../../config/database.js";
import FieldSchedule from "../../models/FieldSchedule.js";
import { Op } from "sequelize";
import { ACTIVE_BOOKING_STATUS_CONDITION } from "../bookingSlotService.js";

export const releaseExpiredPendingBookings = async (transaction = null) => {
  await sequelize.query(
    `UPDATE bookings
     SET status = 'cancelled'
     WHERE status = 'pending'
       AND pending_expires_at IS NOT NULL
       AND pending_expires_at <= NOW()`,
    transaction ? { transaction } : undefined,
  );
};

/**
 * Get available time slots for a field on a specific date or date range
 * Combines field_schedules with bookings to determine availability
 * @param {string|number} field_id - Field ID
 * @param {Date|Array<Date>} dateOrDates - Single date or array of dates
 * @returns {Object|Array<Object>} Slots for single date or grouped by date
 */
export const getAvailableSlots = async (field_id, dateOrDates) => {
  try {
    await releaseExpiredPendingBookings();

    const dates = Array.isArray(dateOrDates) ? dateOrDates : [dateOrDates];
    const firstDate = dates[0];
    const lastDate = dates[dates.length - 1];

    const startOfDay = new Date(firstDate);
    startOfDay.setHours(0, 0, 0, 0);

    const endOfDay = new Date(lastDate);
    endOfDay.setHours(23, 59, 59, 999);

    // Define default time slots (if no schedules exist in DB)
    const defaultSlots = [
      { start: 6, end: 9, label: "Ca sáng sớm", price_multiplier: 1.0 },
      { start: 9, end: 12, label: "Ca sáng", price_multiplier: 1.0 },
      { start: 12, end: 14, label: "Ca trưa", price_multiplier: 0.9 },
      { start: 14, end: 17, label: "Ca chiều", price_multiplier: 1.1 },
      { start: 17, end: 19, label: "Ca tối sớm", price_multiplier: 1.2 },
      { start: 19, end: 22, label: "Ca tối", price_multiplier: 1.3 },
    ];

    // Check if field has custom schedules for the entire date range
    const schedules = await FieldSchedule.findAll({
      where: {
        field_id: field_id,
        start_time: {
          [Op.gte]: startOfDay,
          [Op.lt]: endOfDay,
        },
      },
      order: [["start_time", "ASC"]],
    });

    // Get all bookings for this field and date range in a single query
    const bookings = await sequelize.query(
      `SELECT
        bs.booking_id,
        bs.court_id,
        bs.start_time,
        bs.end_time
       FROM booking_slots bs
       INNER JOIN bookings b ON b.booking_id = bs.booking_id
       WHERE bs.field_id = ?
       AND bs.start_time >= ? AND bs.start_time < ?
       AND ${ACTIVE_BOOKING_STATUS_CONDITION}`,
      {
        replacements: [field_id, startOfDay, endOfDay],
        type: sequelize.QueryTypes.SELECT,
      },
    );

    const blockedSlots = await sequelize.query(
      `SELECT
        block_date,
        start_time,
        end_time
       FROM field_blocked_slots
       WHERE field_id = ?
         AND block_date BETWEEN DATE(?) AND DATE(?)`,
      {
        replacements: [field_id, startOfDay, endOfDay],
        type: sequelize.QueryTypes.SELECT,
      },
    );

    // Group results by date
    const slotsByDate = {};

    dates.forEach((date) => {
      const dateKey = new Date(date).toISOString().split("T")[0];
      const dayStart = new Date(date);
      dayStart.setHours(0, 0, 0, 0);
      const dayEnd = new Date(date);
      dayEnd.setHours(23, 59, 59, 999);

      // Get schedules for this specific day
      const daySchedules = schedules.filter((s) => {
        const sTime = new Date(s.start_time);
        return sTime >= dayStart && sTime < dayEnd;
      });

      let slots = [];

      if (daySchedules.length > 0) {
        // Use custom schedules from database
        slots = daySchedules.map((schedule) => ({
          schedule_id: schedule.schedule_id,
          start_time: schedule.start_time,
          end_time: schedule.end_time,
          is_available: schedule.is_available,
          shift_label: getShiftLabel(schedule.start_time),
        }));
      } else {
        // Use default slots
        slots = defaultSlots.map((slot, index) => {
          const start = new Date(date);
          start.setHours(slot.start, 0, 0, 0);

          const end = new Date(date);
          end.setHours(slot.end, 0, 0, 0);

          return {
            schedule_id: `default-${index}`,
            start_time: start,
            end_time: end,
            is_available: true,
            shift_label: slot.label,
            price_multiplier: slot.price_multiplier,
          };
        });
      }

      // Get bookings for this specific day
      const dayBookings = bookings.filter((b) => {
        const bTime = new Date(b.start_time);
        return bTime >= dayStart && bTime < dayEnd;
      });

      const dayBlockedSlots = blockedSlots.filter((slot) => {
        const blockDate = String(slot.block_date || "").slice(0, 10);
        return blockDate === dateKey;
      });

      // Mark slots as booked if there's any overlap
      slots.forEach((slot) => {
        const slotStart = new Date(slot.start_time);
        const slotEnd = new Date(slot.end_time);

        const isBooked = dayBookings.some((booking) => {
          const bookingStart = new Date(booking.start_time);
          const bookingEnd = new Date(booking.end_time);

          // Check for any time overlap
          return slotStart < bookingEnd && slotEnd > bookingStart;
        });

        const isBlocked = dayBlockedSlots.some((blocked) => {
          const blockedStart = new Date(
            `${dateKey}T${String(blocked.start_time).slice(0, 8)}`,
          );
          const blockedEnd = new Date(
            `${dateKey}T${String(blocked.end_time).slice(0, 8)}`,
          );
          return slotStart < blockedEnd && slotEnd > blockedStart;
        });

        const isActive = slot.is_available !== false;
        slot.available = isActive && !isBooked && !isBlocked;
        slot.booking_status = isBlocked
          ? "blocked"
          : isBooked
            ? "booked"
            : "available";
      });

      slotsByDate[dateKey] = slots;
    });

    // Return single date result or all dates grouped
    if (!Array.isArray(dateOrDates)) {
      const dateKey = new Date(firstDate).toISOString().split("T")[0];
      return slotsByDate[dateKey] || [];
    }

    return slotsByDate;
  } catch (error) {
    console.error("Error getting available slots:", error);
    throw error;
  }
};

/**
 * Get shift label based on time
 */
function getShiftLabel(datetime) {
  const hour = new Date(datetime).getHours();

  if (hour >= 6 && hour < 9) return "Ca sáng sớm";
  if (hour >= 9 && hour < 12) return "Ca sáng";
  if (hour >= 12 && hour < 14) return "Ca trưa";
  if (hour >= 14 && hour < 17) return "Ca chiều";
  if (hour >= 17 && hour < 19) return "Ca tối sớm";
  if (hour >= 19 && hour < 22) return "Ca tối";
  return "Ca khác";
}

/**
 * Check if a time slot is available for booking
 */
export const checkSlotAvailability = async (field_id, startTime, endTime) => {
  try {
    await releaseExpiredPendingBookings();

    const blockedSlots = await sequelize.query(
      `SELECT slot_id
       FROM field_blocked_slots
       WHERE field_id = ?
         AND block_date = DATE(?)
         AND start_time < TIME(?)
         AND end_time > TIME(?)`,
      {
        replacements: [field_id, startTime, endTime, startTime],
        type: sequelize.QueryTypes.SELECT,
      },
    );

    if (blockedSlots && blockedSlots.length > 0) {
      return {
        available: false,
        reason: "Khung gio nay da bi khoa boi quan ly",
      };
    }

    // Check field_schedules if this time is marked as unavailable
    const scheduleConflict = await FieldSchedule.findOne({
      where: {
        field_id: field_id,
        is_available: false,
        [Op.or]: [
          {
            start_time: { [Op.lt]: endTime },
            end_time: { [Op.gt]: startTime },
          },
        ],
      },
    });

    if (scheduleConflict) {
      return {
        available: false,
        reason: "Khung giờ này đã bị khóa bởi quản lý",
      };
    }

    // Check bookings for conflicts
    const bookings = await sequelize.query(
      `SELECT
        bs.booking_slot_id,
        bs.booking_id,
        bs.start_time,
        bs.end_time
       FROM booking_slots bs
       INNER JOIN bookings b ON b.booking_id = bs.booking_id
       WHERE bs.field_id = ?
       AND ${ACTIVE_BOOKING_STATUS_CONDITION}
       AND bs.start_time < ?
       AND bs.end_time > ?`,
      {
        replacements: [field_id, endTime, startTime],
        type: sequelize.QueryTypes.SELECT,
      },
    );

    if (bookings && bookings.length > 0) {
      return {
        available: false,
        reason: "Khung giờ này đã được đặt",
      };
    }

    return {
      available: true,
      reason: null,
    };
  } catch (error) {
    console.error("Error checking slot availability:", error);
    throw error;
  }
};

/**
 * Create or update field schedules
 */
export const updateFieldSchedules = async (field_id, schedules) => {
  try {
    const results = [];

    for (const schedule of schedules) {
      const { start_time, end_time, is_available } = schedule;

      const available = is_available !== false;

      const [newSchedule, created] = await FieldSchedule.findOrCreate({
        where: {
          field_id: field_id,
          start_time,
          end_time,
        },
        defaults: {
          is_available: available,
        },
      });

      if (!created) {
        // Update existing
        await FieldSchedule.update(
          { is_available: available },
          {
            where: {
              field_id: field_id,
              start_time,
              end_time,
            },
          },
        );
      }

      results.push(newSchedule);
    }

    return results;
  } catch (error) {
    console.error("Error updating field schedules:", error);
    throw error;
  }
};

export default {
  getAvailableSlots,
  checkSlotAvailability,
  updateFieldSchedules,
  releaseExpiredPendingBookings,
};
