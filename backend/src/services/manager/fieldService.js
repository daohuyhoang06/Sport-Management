import sequelize from "../../config/database.js";

const normalizeNullableValue = (value) => {
  if (value === undefined) return undefined;
  if (value === null) return null;
  if (typeof value === "string" && value.trim() === "") return null;
  return value;
};

const normalizeMediaPath = (value) => {
  const normalized = normalizeNullableValue(value);
  if (normalized === undefined || normalized === null) {
    return normalized;
  }
  if (typeof normalized !== "string") {
    return normalized;
  }

  const trimmed = normalized.trim();
  if (!trimmed) {
    return null;
  }

  try {
    const parsed = new URL(trimmed);
    return `${parsed.pathname}${parsed.search}${parsed.hash}` || null;
  } catch (_error) {
    return trimmed;
  }
};

const getSportTypeById = async (sportId) => {
  const [[sportType]] = await sequelize.query(
    "SELECT sport_id FROM sport_types WHERE sport_id = ? LIMIT 1",
    { replacements: [sportId] },
  );
  return sportType || null;
};

/**
 * Get all fields managed by this manager
 */
export const getManagerFieldsService = async (managerId) => {
  try {
    const [fields] = await sequelize.query(
      `
      SELECT
        f.field_id,
        f.field_name,
        f.location,
        f.status,
        f.manager_id,
        f.latitude,
        f.longitude,
        f.phone,
        f.open_time,
        f.close_time,
        f.slot_minutes,
        f.slot_price,
        f.avatar_image_url,
        f.card_image_url,
        f.sport_id,
        st.sport_name
      FROM fields f
      LEFT JOIN sport_types st ON f.sport_id = st.sport_id
      WHERE f.manager_id = ?
      ORDER BY f.field_id DESC
    `,
      { replacements: [managerId] },
    );

    return fields;
  } catch (error) {
    console.error("Error in getManagerFieldsService:", error);
    throw error;
  }
};

/**
 * Create new field
 */
export const createFieldService = async (managerId, fieldData) => {
  try {
    const {
      field_name,
      location,
      latitude,
      longitude,
      phone,
      open_time,
      close_time,
      slot_price,
      slot_minutes,
      avatar_image_url,
      card_image_url,
      sport_id,
      status = "active",
    } = fieldData;

    const sportType = await getSportTypeById(sport_id);
    if (!sportType) {
      throw new Error("Sport type not found");
    }
    if (status === "active" && (!slot_price || Number(slot_price) <= 0)) {
      throw new Error("slot_price is required when field status is active");
    }

    await sequelize.query(
      `
      INSERT INTO fields (
        field_name, location, latitude, longitude, phone, open_time, close_time,
        slot_price, slot_minutes, avatar_image_url, card_image_url, status, manager_id, sport_id
      )
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `,
      {
        replacements: [
          field_name,
          location,
          normalizeNullableValue(latitude) ?? null,
          normalizeNullableValue(longitude) ?? null,
          normalizeNullableValue(phone) ?? null,
          normalizeNullableValue(open_time) ?? null,
          normalizeNullableValue(close_time) ?? null,
          normalizeNullableValue(slot_price) ?? null,
          slot_minutes || 60,
          normalizeMediaPath(avatar_image_url) ?? null,
          normalizeMediaPath(card_image_url) ?? null,
          status,
          managerId,
          sport_id,
        ],
      },
    );

    const [[result]] = await sequelize.query(
      `SELECT * FROM fields WHERE field_id = LAST_INSERT_ID()`,
    );

    return result;
  } catch (error) {
    console.error("Error in createFieldService:", error);
    throw error;
  }
};

/**
 * Update field
 */
export const updateFieldService = async (managerId, field_id, fieldData) => {
  try {
    const field = await getManagerFieldByIdService(managerId, field_id);
    if (!field) throw new Error("Field not found or unauthorized");

    const {
      field_name,
      location,
      sport_id,
      latitude,
      longitude,
      phone,
      open_time,
      close_time,
      slot_price,
      slot_minutes,
      avatar_image_url,
      card_image_url,
      status,
    } = fieldData;

    if (sport_id !== undefined) {
      const sportType = await getSportTypeById(sport_id);
      if (!sportType) {
        throw new Error("Sport type not found");
      }
    }

    const nextStatus = status !== undefined ? status : field.status;
    const nextSlotPrice =
      slot_price !== undefined ? slot_price : field.slot_price;
    if (nextStatus === "active" && (!nextSlotPrice || Number(nextSlotPrice) <= 0)) {
      throw new Error("slot_price is required when field status is active");
    }

    const updates = [];
    const params = [];
    if (field_name !== undefined) {
      updates.push("field_name = ?");
      params.push(field_name);
    }
    if (location !== undefined) {
      updates.push("location = ?");
      params.push(location);
    }
    if (latitude !== undefined) {
      updates.push("latitude = ?");
      params.push(normalizeNullableValue(latitude));
    }
    if (longitude !== undefined) {
      updates.push("longitude = ?");
      params.push(normalizeNullableValue(longitude));
    }
    if (phone !== undefined) {
      updates.push("phone = ?");
      params.push(normalizeNullableValue(phone));
    }
    if (open_time !== undefined) {
      updates.push("open_time = ?");
      params.push(normalizeNullableValue(open_time));
    }
    if (close_time !== undefined) {
      updates.push("close_time = ?");
      params.push(normalizeNullableValue(close_time));
    }
    if (sport_id !== undefined) {
      updates.push("sport_id = ?");
      params.push(sport_id);
    }
    if (slot_price !== undefined) {
      updates.push("slot_price = ?");
      params.push(slot_price);
    }
    if (slot_minutes !== undefined) {
      updates.push("slot_minutes = ?");
      params.push(slot_minutes);
    }
    if (avatar_image_url !== undefined) {
      updates.push("avatar_image_url = ?");
      params.push(normalizeMediaPath(avatar_image_url));
    }
    if (card_image_url !== undefined) {
      updates.push("card_image_url = ?");
      params.push(normalizeMediaPath(card_image_url));
    }
    if (status !== undefined) {
      updates.push("status = ?");
      params.push(status);
    }

    if (updates.length === 0) {
      throw new Error("No field data provided for update");
    }

    params.push(field_id, managerId);
    await sequelize.query(
      `
      UPDATE fields SET ${updates.join(", ")}
      WHERE field_id = ? AND manager_id = ?
    `,
      { replacements: params },
    );

    return getManagerFieldByIdService(managerId, field_id);
  } catch (error) {
    console.error("Error in updateFieldService:", error);
    throw error;
  }
};

/**
 * Delete field
 */
export const deleteFieldService = async (managerId, field_id) => {
  try {
    const field = await getManagerFieldByIdService(managerId, field_id);
    if (!field) throw new Error("Field not found or unauthorized");

    const [bookings] = await sequelize.query(
      `
      SELECT COUNT(*) as count FROM bookings WHERE field_id = ?
    `,
      { replacements: [field_id] },
    );

    if (bookings[0].count > 0) {
      throw new Error("Cannot delete field with bookings");
    }

    await sequelize.query(
      `
      DELETE FROM fields WHERE field_id = ? AND manager_id = ?
    `,
      { replacements: [field_id, managerId] },
    );

    return { success: true };
  } catch (error) {
    console.error("Error in deleteFieldService:", error);
    throw error;
  }
};

/**
 * Get field by ID (only if managed by this manager)
 */
export const getManagerFieldByIdService = async (managerId, field_id) => {
  try {
    const [fields] = await sequelize.query(
      `
      SELECT
        f.field_id,
        f.field_name,
        f.location,
        f.status,
        f.manager_id,
        f.latitude,
        f.longitude,
        f.phone,
        f.open_time,
        f.close_time,
        f.slot_minutes,
        f.slot_price,
        f.avatar_image_url,
        f.card_image_url,
        f.created_at,
        f.updated_at,
        f.sport_id,
        st.sport_name
      FROM fields f
      LEFT JOIN sport_types st ON f.sport_id = st.sport_id
      WHERE f.field_id = ? AND f.manager_id = ?
    `,
      { replacements: [field_id, managerId] },
    );

    return fields[0] || null;
  } catch (error) {
    console.error("Error in getManagerFieldByIdService:", error);
    throw error;
  }
};

/**
 * Update field status
 */
export const updateFieldStatusService = async (managerId, field_id, status) => {
  try {
    // Verify field belongs to manager
    const field = await getManagerFieldByIdService(managerId, field_id);
    if (!field) {
      throw new Error("Field not found or unauthorized");
    }
    if (status === "active" && (!field.slot_price || Number(field.slot_price) <= 0)) {
      throw new Error("slot_price is required when field status is active");
    }

    await sequelize.query(
      `
      UPDATE fields
      SET status = ?
      WHERE field_id = ? AND manager_id = ?
    `,
      { replacements: [status, field_id, managerId] },
    );

    return { success: true };
  } catch (error) {
    console.error("Error in updateFieldStatusService:", error);
    throw error;
  }
};

/**
 * Get field statistics
 */
export const getFieldStatsService = async (managerId, field_id) => {
  try {
    // Verify field belongs to manager
    const field = await getManagerFieldByIdService(managerId, field_id);
    if (!field) {
      throw new Error("Field not found or unauthorized");
    }

    const [stats] = await sequelize.query(
      `
      SELECT 
        COUNT(*) as totalBookings,
        SUM(CASE WHEN status = 'confirmed' THEN 1 ELSE 0 END) as confirmedBookings,
        SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completedBookings,
        COALESCE(SUM(CASE WHEN status IN ('confirmed', 'completed') THEN price ELSE 0 END), 0) as totalRevenue
      FROM bookings
      WHERE field_id = ?
    `,
      { replacements: [field_id] },
    );

    return {
      totalBookings: Number(stats[0].totalbookings) || 0,
      confirmedBookings: Number(stats[0].confirmedbookings) || 0,
      completedBookings: Number(stats[0].completedbookings) || 0,
      totalRevenue: parseFloat(stats[0].totalrevenue) || 0,
    };
  } catch (error) {
    console.error("Error in getFieldStatsService:", error);
    throw error;
  }
};
