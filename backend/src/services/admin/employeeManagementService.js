import { User, Field } from '../../models/index.js';
import { Op } from 'sequelize';

/**
 * Get all employees (users with role = 'manager')
 */
export const getAllEmployeesService = async (filters = {}, pagination = {}) => {
  const { page = 1, limit = 10, search = '', status = '' } = { ...filters, ...pagination };
  const offset = (page - 1) * limit;

  // Build WHERE clause for search
  let searchCondition = '';
  if (search) {
    searchCondition = `AND (p.person_name LIKE '%${search}%' OR p.email LIKE '%${search}%' OR p.phone LIKE '%${search}%')`;
  }
  
  let statusCondition = '';
  if (status) {
    statusCondition = `AND p.status = '${status}'`;
  }

  // Get total count
  const [countResult] = await User.sequelize.query(`
    SELECT COUNT(*) as count 
    FROM person p 
    WHERE p.role = 'manager' ${searchCondition} ${statusCondition}
  `);
  const count = countResult[0].count;

  // Get employees with count of their managed fields
  const [employees] = await User.sequelize.query(`
    SELECT 
      p.person_id,
      p.person_name,
      p.email,
      p.phone,
      p.username,
      p.role,
      p.status,
      p.birthday,
      p.sex,
      p.address,
      p.fieldId,
      COUNT(f.field_id) as field_count,
      STRING_AGG(f.field_name, ', ') as field_names
    FROM person p
    LEFT JOIN fields f ON f.manager_id = p.person_id
    WHERE p.role = 'manager' ${searchCondition} ${statusCondition}
    GROUP BY p.person_id, p.person_name, p.email, p.phone, p.username, p.role, p.status, p.birthday, p.sex, p.address, p.fieldId
    ORDER BY p.person_id DESC
    LIMIT ${parseInt(limit)} OFFSET ${parseInt(offset)}
  `);

  // Format employee data
  const employeesList = employees.map(row => ({
    person_id: row.person_id,
    person_name: row.person_name,
    email: row.email,
    phone: row.phone,
    username: row.username,
    role: row.role,
    status: row.status,
    birthday: row.birthday,
    sex: row.sex,
    address: row.address,
    fieldId: row.fieldId,
    field_count: row.field_count,
    field_names: row.field_names
  }));

  return {
    employees: employeesList,
    total: count,
    page: parseInt(page),
    totalPages: Math.ceil(count / limit)
  };
};

/**
 * Get employee by ID
 */
export const getEmployeeByIdService = async (id) => {
  const [employees] = await User.sequelize.query(`
    SELECT 
      p.person_id,
      p.person_name,
      p.email,
      p.phone,
      p.username,
      p.role,
      p.status,
      p.birthday,
      p.sex,
      p.address,
      p.fieldId,
      f.field_id,
      f.field_name,
      f.location,
      f.status as field_status
    FROM person p
    LEFT JOIN fields f ON f.manager_id = p.person_id
    WHERE p.person_id = ${parseInt(id)} AND p.role = 'manager'
    LIMIT 1
  `);

  if (!employees || employees.length === 0) return null;

  const employee = employees[0];
  
  return {
    person_id: employee.person_id,
    person_name: employee.person_name,
    email: employee.email,
    phone: employee.phone,
    username: employee.username,
    role: employee.role,
    status: employee.status,
    birthday: employee.birthday,
    sex: employee.sex,
    address: employee.address,
    fieldId: employee.fieldId,
    field: employee.field_id ? {
      field_id: employee.field_id,
      field_name: employee.field_name,
      location: employee.location,
      status: employee.field_status
    } : null
  };
};

/**
 * Create new employee
 */
export const createEmployeeService = async (employeeData) => {
  // Force role to manager
  const employee = await User.create({
    ...employeeData,
    role: 'manager'
  });

  const employeeObj = employee.toJSON();
  delete employeeObj.password;
  return employeeObj;
};

/**
 * Update employee
 */
export const updateEmployeeService = async (id, employeeData) => {
  const employee = await User.findOne({
    where: {
      person_id: id,
      role: 'manager'
    }
  });

  if (!employee) {
    throw new Error('Employee not found');
  }

  // Don't allow password or role change through this method
  delete employeeData.password;
  delete employeeData.role;

  // Clean up empty values
  Object.keys(employeeData).forEach(key => {
    if (employeeData[key] === '' || employeeData[key] === 'Invalid date') {
      employeeData[key] = null;
    }
  });

  // Allow fieldId update
  if (employeeData.fieldId !== undefined) {
    employeeData.fieldId = employeeData.fieldId || null;
  }

  await employee.update(employeeData);
  const updatedEmployee = employee.toJSON();
  delete updatedEmployee.password;
  return updatedEmployee;
};

/**
 * Delete employee
 */
export const deleteEmployeeService = async (id) => {
  const employee = await User.findOne({
    where: {
      person_id: id,
      role: 'manager'
    }
  });

  if (!employee) {
    throw new Error('Employee not found');
  }

  // Check if employee is managing any fields
  const managedFields = await Field.count({
    where: { manager_id: id }
  });

  if (managedFields > 0) {
    throw new Error('Cannot delete employee who is managing fields. Please reassign fields first.');
  }

  await employee.update({ status: 'inactive' });
  return { message: 'Employee deleted successfully' };
};

/**
 * Assign field to employee
 */
export const assignFieldToEmployeeService = async (employeeId, fieldId) => {
  const employee = await User.findOne({
    where: {
      person_id: employeeId,
      role: 'manager'
    }
  });

  if (!employee) {
    throw new Error('Employee not found');
  }

  const field = await Field.findByPk(fieldId);
  if (!field) {
    throw new Error('Field not found');
  }

  await field.update({ manager_id: employeeId });
  
  return {
    message: 'Field assigned to employee successfully',
    field: field
  };
};

/**
 * Get employee statistics
 */
export const getEmployeeStatsService = async () => {
  const totalEmployees = await User.count({ where: { role: 'manager' } });
  const activeEmployees = await User.count({ where: { role: 'manager', status: 'active' } });
  const inactiveEmployees = await User.count({ where: { role: 'manager', status: 'inactive' } });

  return {
    total: totalEmployees,
    active: activeEmployees,
    inactive: inactiveEmployees
  };
};
