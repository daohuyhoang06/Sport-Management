import { User, Field, Booking } from '../../models/index.js';
import { Op } from 'sequelize';
import sequelize from '../../config/database.js';

/**
 * Get all users with filters and pagination
 */
export const getAllUsersService = async (filters = {}, pagination = {}) => {
  const { page = 1, limit = 10, search = '', role = '', status = '' } = { ...filters, ...pagination };
  const offset = (parseInt(page) - 1) * parseInt(limit);

  let whereConditions = [];
  let queryParams = [];
  
  if (search) {
    whereConditions.push('(LOWER(person_name) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?) OR LOWER(username) LIKE LOWER(?))');
    queryParams.push(`%${search}%`, `%${search}%`, `%${search}%`);
  }
  
  if (role) {
    whereConditions.push('role = ?');
    queryParams.push(role);
  }
  
  if (status) {
    whereConditions.push('status = ?');
    queryParams.push(status);
  }

  const whereClause = whereConditions.length > 0 ? 'WHERE ' + whereConditions.join(' AND ') : '';

  // Get total count
  const [[{ total }]] = await sequelize.query(
    `SELECT COUNT(*) as total FROM person ${whereClause}`,
    { replacements: queryParams }
  );

  // Get users
  const [rows] = await sequelize.query(
    `SELECT person_id, person_name, birthday, sex, address, email, phone, username, role, status, fieldId
     FROM person 
     ${whereClause}
     ORDER BY person_id DESC
     LIMIT ? OFFSET ?`,
    { replacements: [...queryParams, parseInt(limit), offset] }
  );

  return {
    users: rows,
    total: parseInt(total),
    page: parseInt(page),
    totalPages: Math.ceil(total / limit)
  };
};

/**
 * Get user by ID
 */
export const getUserByIdService = async (id) => {
  const [[user]] = await sequelize.query(
    `SELECT person_id, person_name, birthday, sex, address, email, phone, username, role, status, fieldId
     FROM person 
     WHERE person_id = ?`,
    { replacements: [id] }
  );

  if (!user) return null;

  // Get managed fields
  const [managedFields] = await sequelize.query(
    `SELECT field_id, field_name, location, status
     FROM fields 
     WHERE manager_id = ?`,
    { replacements: [id] }
  );

  // Get recent bookings
  const [bookings] = await sequelize.query(
    `SELECT b.booking_id, b.start_time, b.end_time, b.status, b.price,
            f.field_name
     FROM bookings b
     LEFT JOIN fields f ON b.field_id = f.field_id
     WHERE b.customer_id = ?
     ORDER BY b.start_time DESC
     LIMIT 5`,
    { replacements: [id] }
  );

  return {
    ...user,
    managedFields,
    bookings
  };
};

/**
 * Create new user
 */
export const createUserService = async (userData) => {
  const { person_name, email, username, password, role = 'user', status = 'active', phone, address, birthday, sex } = userData;
  
  const [result] = await sequelize.query(
    `INSERT INTO person (person_name, email, username, password, role, status, phone, address, birthday, sex)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
    { replacements: [person_name, email, username, password, role, status, phone, address, birthday, sex] }
  );

  const [[user]] = await sequelize.query(
    `SELECT person_id, person_name, birthday, sex, address, email, phone, username, role, status, fieldId
     FROM person WHERE person_id = ?`,
    { replacements: [result.insertId] }
  );

  return user;
};

/**
 * Update user
 */
export const updateUserService = async (id, userData) => {
  const [[user]] = await sequelize.query(
    'SELECT person_id FROM person WHERE person_id = ?',
    { replacements: [id] }
  );
  
  if (!user) {
    throw new Error('User not found');
  }

  const updates = [];
  const params = [];
  
  if (userData.person_name) {
    updates.push('person_name = ?');
    params.push(userData.person_name);
  }
  if (userData.email) {
    updates.push('email = ?');
    params.push(userData.email);
  }
  if (userData.phone) {
    updates.push('phone = ?');
    params.push(userData.phone);
  }
  if (userData.address) {
    updates.push('address = ?');
    params.push(userData.address);
  }
  if (userData.role) {
    updates.push('role = ?');
    params.push(userData.role);
  }
  if (userData.status) {
    updates.push('status = ?');
    params.push(userData.status);
  }
  if (userData.birthday) {
    updates.push('birthday = ?');
    params.push(userData.birthday);
  }
  if (userData.sex) {
    updates.push('sex = ?');
    params.push(userData.sex);
  }

  if (updates.length > 0) {
    params.push(id);
    await sequelize.query(
      `UPDATE person SET ${updates.join(', ')} WHERE person_id = ?`,
      { replacements: params }
    );
  }

  const [[updatedUser]] = await sequelize.query(
    `SELECT person_id, person_name, birthday, sex, address, email, phone, username, role, status, fieldId
     FROM person WHERE person_id = ?`,
    { replacements: [id] }
  );
  
  return updatedUser;
};

/**
 * Delete user (soft delete - set status to inactive)
 */
export const deleteUserService = async (id) => {
  const [[user]] = await sequelize.query(
    'SELECT person_id FROM person WHERE person_id = ?',
    { replacements: [id] }
  );
  
  if (!user) {
    throw new Error('User not found');
  }

  await sequelize.query(
    "UPDATE person SET status = 'inactive' WHERE person_id = ?",
    { replacements: [id] }
  );
  
  return { message: 'User deleted successfully' };
};

/**
 * Toggle user status
 */
export const toggleUserStatusService = async (id) => {
  const [[user]] = await sequelize.query(
    'SELECT person_id, status FROM person WHERE person_id = ?',
    { replacements: [id] }
  );
  
  if (!user) {
    throw new Error('User not found');
  }

  const newStatus = user.status === 'active' ? 'inactive' : 'active';
  
  await sequelize.query(
    'UPDATE person SET status = ? WHERE person_id = ?',
    { replacements: [newStatus, id] }
  );
  
  return {
    message: `User status changed to ${newStatus}`,
    status: newStatus
  };
};

/**
 * Get user statistics
 */
export const getUserStatsService = async () => {
  const [[{ total }]] = await sequelize.query(
    'SELECT COUNT(*) as total FROM person'
  );
  
  const [[{ active }]] = await sequelize.query(
    "SELECT COUNT(*) as active FROM person WHERE status = 'active'"
  );
  
  const [[{ inactive }]] = await sequelize.query(
    "SELECT COUNT(*) as inactive FROM person WHERE status = 'inactive'"
  );
  
  const [usersByRole] = await sequelize.query(
    'SELECT role, COUNT(*) as count FROM person GROUP BY role'
  );

  return {
    total: parseInt(total),
    active: parseInt(active),
    inactive: parseInt(inactive),
    byRole: usersByRole
  };
};
