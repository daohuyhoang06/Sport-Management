import { Field, FieldImage, User, Booking } from '../../models/index.js';
import { Op } from 'sequelize';
import sequelize from '../../config/database.js';

/**
 * Get all fields with filters and pagination
 */
export const getAllFieldsService = async (filters = {}, pagination = {}) => {
  const { page = 1, limit = 10, search = '', status = '' } = { ...filters, ...pagination };
  const offset = (parseInt(page) - 1) * parseInt(limit);

  let whereConditions = [];
  let queryParams = [];
  
  if (search) {
    whereConditions.push('(f.field_name LIKE ? OR f.location LIKE ?)');
    queryParams.push(`%${search}%`, `%${search}%`);
  }
  
  if (status) {
    whereConditions.push('f.status = ?');
    queryParams.push(status);
  }

  const whereClause = whereConditions.length > 0 ? 'WHERE ' + whereConditions.join(' AND ') : '';

  // Get total count
  const [[{ total }]] = await sequelize.query(
    `SELECT COUNT(*) as total FROM fields f ${whereClause}`,
    { replacements: queryParams }
  );

  // Get fields
  const [rows] = await sequelize.query(
    `SELECT f.field_id, f.manager_id, f.field_name, f.location, f.status, f.rental_price,
            p.person_name as manager_name, p.email as manager_email
     FROM fields f
     LEFT JOIN person p ON f.manager_id = p.person_id
     ${whereClause}
     ORDER BY f.field_id DESC
     LIMIT ? OFFSET ?`,
    { replacements: [...queryParams, parseInt(limit), offset] }
  );

  return {
    fields: rows,
    total: parseInt(total),
    page: parseInt(page),
    totalPages: Math.ceil(total / limit)
  };
};

/**
 * Get field by ID with full details
 */
export const getFieldByIdService = async (id) => {
  const [[field]] = await sequelize.query(
    `SELECT f.field_id, f.manager_id, f.field_name, f.location, f.status, f.rental_price,
            p.person_name as manager_name, p.email as manager_email, p.phone as manager_phone
     FROM fields f
     LEFT JOIN person p ON f.manager_id = p.person_id
     WHERE f.field_id = ?`,
    { replacements: [id] }
  );

  if (!field) return null;

  // Get recent bookings
  const [bookings] = await sequelize.query(
    `SELECT b.booking_id, b.start_time, b.end_time, b.status, b.price,
            p.person_name as customer_name, p.phone as customer_phone
     FROM bookings b
     LEFT JOIN person p ON b.customer_id = p.person_id
     WHERE b.field_id = ?
     ORDER BY b.start_time DESC
     LIMIT 10`,
    { replacements: [id] }
  );

  return {
    ...field,
    bookings
  };
};

/**
 * Create new field
 */
export const createFieldService = async (fieldData) => {
  const { field_name, location, manager_id, rental_price, status = 'active' } = fieldData;
  
  const [result] = await sequelize.query(
    `INSERT INTO fields (field_name, location, manager_id, rental_price, status)
     VALUES (?, ?, ?, ?, ?)
     RETURNING *`,
    { replacements: [field_name, location, manager_id || null, rental_price || null, status] }
  );

  return result[0];
};

/**
 * Update field
 */
export const updateFieldService = async (id, fieldData) => {
  const [[field]] = await sequelize.query(
    'SELECT field_id FROM fields WHERE field_id = ?',
    { replacements: [id] }
  );
  
  if (!field) {
    throw new Error('Field not found');
  }

  const updates = [];
  const params = [];
  
  if (fieldData.field_name) {
    updates.push('field_name = ?');
    params.push(fieldData.field_name);
  }
  if (fieldData.location) {
    updates.push('location = ?');
    params.push(fieldData.location);
  }
  if (fieldData.manager_id !== undefined) {
    updates.push('manager_id = ?');
    params.push(fieldData.manager_id);
  }
  if (fieldData.status) {
    updates.push('status = ?');
    params.push(fieldData.status);
  }

  if (updates.length > 0) {
    params.push(id);
    await sequelize.query(
      `UPDATE fields SET ${updates.join(', ')} WHERE field_id = ?`,
      { replacements: params }
    );
  }

  const [[updatedField]] = await sequelize.query(
    'SELECT * FROM fields WHERE field_id = ?',
    { replacements: [id] }
  );

  return updatedField;
};

/**
 * Delete field
 */
export const deleteFieldService = async (id) => {
  const [[field]] = await sequelize.query(
    'SELECT field_id FROM fields WHERE field_id = ?',
    { replacements: [id] }
  );
  
  if (!field) {
    throw new Error('Field not found');
  }

  // Check if field has active bookings
  const [[{ count }]] = await sequelize.query(
    `SELECT COUNT(*) as count FROM bookings
     WHERE field_id = ?
     AND status IN ('pending', 'confirmed')
     AND start_time >= CURRENT_TIMESTAMP`,
    { replacements: [id] }
  );

  if (count > 0) {
    throw new Error('Cannot delete field with active bookings');
  }

  await sequelize.query(
    "UPDATE fields SET status = 'inactive' WHERE field_id = ?",
    { replacements: [id] }
  );
  
  return { message: 'Field deleted successfully' };
};

/**
 * Toggle field status
 */
export const toggleFieldStatusService = async (id) => {
  const [[field]] = await sequelize.query(
    'SELECT field_id, status FROM fields WHERE field_id = ?',
    { replacements: [id] }
  );
  
  if (!field) {
    throw new Error('Field not found');
  }

  const newStatus = field.status === 'active' ? 'inactive' : 'active';
  
  await sequelize.query(
    'UPDATE fields SET status = ? WHERE field_id = ?',
    { replacements: [newStatus, id] }
  );
  
  return {
    message: `Field status changed to ${newStatus}`,
    status: newStatus
  };
};

/**
 * Get field statistics
 */
export const getFieldStatsService = async () => {
  const [[{ total }]] = await sequelize.query(
    'SELECT COUNT(*) as total FROM fields'
  );
  
  const [[{ active }]] = await sequelize.query(
    "SELECT COUNT(*) as active FROM fields WHERE status = 'active'"
  );
  
  const [[{ inactive }]] = await sequelize.query(
    "SELECT COUNT(*) as inactive FROM fields WHERE status = 'inactive'"
  );
  
  const [[{ maintenance }]] = await sequelize.query(
    "SELECT COUNT(*) as maintenance FROM fields WHERE status = 'maintenance'"
  );

  return {
    total: parseInt(total),
    active: parseInt(active),
    inactive: parseInt(inactive),
    maintenance: parseInt(maintenance)
  };
};

/**
 * Upload field images
 */
export const uploadFieldImagesService = async (fieldId, images) => {
  const field = await Field.findByPk(fieldId);
  if (!field) {
    throw new Error('Field not found');
  }

  const imageRecords = images.map((imageUrl, index) => ({
    field_id: fieldId,
    image_url: imageUrl,
    is_primary: index === 0 // First image is primary
  }));

  const createdImages = await FieldImage.bulkCreate(imageRecords);
  return createdImages;
};

/**
 * Delete field image
 */
export const deleteFieldImageService = async (imageId) => {
  const image = await FieldImage.findByPk(imageId);
  if (!image) {
    throw new Error('Image not found');
  }

  await image.destroy();
  return { message: 'Image deleted successfully' };
};
