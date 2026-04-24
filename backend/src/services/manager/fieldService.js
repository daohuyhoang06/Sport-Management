import sequelize from '../../config/database.js';

/**
 * Get all fields managed by this manager
 */
export const getManagerFieldsService = async (managerId) => {
  try {
    const [fields] = await sequelize.query(`
      SELECT
        f.field_id,
        f.field_name,
        f.location,
        f.status,
        f.manager_id,
        f.rental_price,
        f.sport_id,
        st.sport_name
      FROM fields f
      LEFT JOIN sport_types st ON f.sport_id = st.sport_id
      WHERE f.manager_id = ?
      ORDER BY f.field_id DESC
    `, { replacements: [managerId] });

    return fields;
  } catch (error) {
    console.error('Error in getManagerFieldsService:', error);
    throw error;
  }
};

/**
 * Create new field
 */
export const createFieldService = async (managerId, fieldData) => {
  try {
    const { field_name, location, rental_price, sport_id } = fieldData;
    
    await sequelize.query(`
      INSERT INTO fields (field_name, location, rental_price, status, manager_id, sport_id)
      VALUES (?, ?, ?, 'active', ?, ?)
    `, { replacements: [field_name, location, rental_price || null, managerId, sport_id || null] });

    const [[result]] = await sequelize.query(
      `SELECT * FROM fields WHERE field_id = LAST_INSERT_ID()`
    );

    return result;
  } catch (error) {
    console.error('Error in createFieldService:', error);
    throw error;
  }
};

/**
 * Update field
 */
export const updateFieldService = async (managerId, fieldId, fieldData) => {
  try {
    const field = await getManagerFieldByIdService(managerId, fieldId);
    if (!field) throw new Error('Field not found or unauthorized');

    const { field_name, location, sport_id } = fieldData;

    const updates = [];
    const params = [];
    if (field_name !== undefined) { updates.push('field_name = ?'); params.push(field_name); }
    if (location !== undefined) { updates.push('location = ?'); params.push(location); }
    if (sport_id !== undefined) { updates.push('sport_id = ?'); params.push(sport_id); }

    if (updates.length > 0) {
      params.push(fieldId, managerId);
      await sequelize.query(`
        UPDATE fields SET ${updates.join(', ')}
        WHERE field_id = ? AND manager_id = ?
      `, { replacements: params });
    }

    return { success: true };
  } catch (error) {
    console.error('Error in updateFieldService:', error);
    throw error;
  }
};

/**
 * Delete field
 */
export const deleteFieldService = async (managerId, fieldId) => {
  try {
    const field = await getManagerFieldByIdService(managerId, fieldId);
    if (!field) throw new Error('Field not found or unauthorized');

    const [bookings] = await sequelize.query(`
      SELECT COUNT(*) as count FROM bookings WHERE field_id = ?
    `, { replacements: [fieldId] });

    if (bookings[0].count > 0) {
      throw new Error('Cannot delete field with bookings');
    }

    await sequelize.query(`
      DELETE FROM fields WHERE field_id = ? AND manager_id = ?
    `, { replacements: [fieldId, managerId] });

    return { success: true };
  } catch (error) {
    console.error('Error in deleteFieldService:', error);
    throw error;
  }
};

/**
 * Get field by ID (only if managed by this manager)
 */
export const getManagerFieldByIdService = async (managerId, fieldId) => {
  try {
    const [fields] = await sequelize.query(`
      SELECT
        f.field_id,
        f.field_name,
        f.location,
        f.status,
        f.manager_id,
        f.rental_price,
        f.sport_id,
        st.sport_name
      FROM fields f
      LEFT JOIN sport_types st ON f.sport_id = st.sport_id
      WHERE f.field_id = ? AND f.manager_id = ?
    `, { replacements: [fieldId, managerId] });

    return fields[0] || null;
  } catch (error) {
    console.error('Error in getManagerFieldByIdService:', error);
    throw error;
  }
};

/**
 * Update field status
 */
export const updateFieldStatusService = async (managerId, fieldId, status) => {
  try {
    // Verify field belongs to manager
    const field = await getManagerFieldByIdService(managerId, fieldId);
    if (!field) {
      throw new Error('Field not found or unauthorized');
    }

    await sequelize.query(`
      UPDATE fields
      SET status = ?
      WHERE field_id = ? AND manager_id = ?
    `, { replacements: [status, fieldId, managerId] });

    return { success: true };
  } catch (error) {
    console.error('Error in updateFieldStatusService:', error);
    throw error;
  }
};

/**
 * Get field statistics
 */
export const getFieldStatsService = async (managerId, fieldId) => {
  try {
    // Verify field belongs to manager
    const field = await getManagerFieldByIdService(managerId, fieldId);
    if (!field) {
      throw new Error('Field not found or unauthorized');
    }

    const [stats] = await sequelize.query(`
      SELECT 
        COUNT(*) as totalBookings,
        SUM(CASE WHEN status = 'confirmed' THEN 1 ELSE 0 END) as confirmedBookings,
        SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completedBookings,
        COALESCE(SUM(CASE WHEN status IN ('confirmed', 'completed') THEN price ELSE 0 END), 0) as totalRevenue
      FROM bookings
      WHERE field_id = ?
    `, { replacements: [fieldId] });

    return {
      totalBookings: Number(stats[0].totalbookings) || 0,
      confirmedBookings: Number(stats[0].confirmedbookings) || 0,
      completedBookings: Number(stats[0].completedbookings) || 0,
      totalRevenue: parseFloat(stats[0].totalrevenue) || 0
    };
  } catch (error) {
    console.error('Error in getFieldStatsService:', error);
    throw error;
  }
};
