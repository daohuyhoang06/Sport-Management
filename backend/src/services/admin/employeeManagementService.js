import { User, Field } from "../../models/index.js";
import { Op } from "sequelize";

/**
 * Get all employees (users with role = 'manager')
 */
export const getAllEmployeesService = async (filters = {}, pagination = {}) => {
  const {
    page = 1,
    limit = 10,
    search = "",
    status = "",
  } = { ...filters, ...pagination };
  const parsedLimit = parseInt(limit);
  const parsedPage = parseInt(page);
  const offset = (parsedPage - 1) * parsedLimit;
  const dialect = User.sequelize.getDialect();
  const fieldNamesAgg =
    dialect === "postgres"
      ? "STRING_AGG(f.field_name, ', ')"
      : "GROUP_CONCAT(f.field_name SEPARATOR ', ')";

  // Build WHERE clause for search
  let searchCondition = "";
  const replacements = {};
  if (search) {
    searchCondition =
      "AND (p.full_name LIKE :search OR p.email LIKE :search OR p.phone LIKE :search)";
    replacements.search = `%${search}%`;
  }

  let statusCondition = "";
  if (status) {
    statusCondition = "AND p.status = :status";
    replacements.status = status;
  }

  // Get total count
  const [countResult] = await User.sequelize.query(
    `
    SELECT COUNT(*) as count 
    FROM person p 
    WHERE p.role = 'manager' ${searchCondition} ${statusCondition}
  `,
    { replacements },
  );
  const count = parseInt(countResult[0].count);

  // Get employees with count of their managed fields
  const [employees] = await User.sequelize.query(
    `
    SELECT 
      p.person_id,
      p.full_name as name,
      p.email,
      p.phone,
      p.username,
      p.role,
      p.status,
      p.birthday,
      p.gender as sex,
      p.address,
      COUNT(f.field_id) as field_count,
      ${fieldNamesAgg} as field_names
    FROM person p
    LEFT JOIN fields f ON f.manager_id = p.person_id
    WHERE p.role = 'manager' ${searchCondition} ${statusCondition}
    GROUP BY p.person_id, p.full_name, p.email, p.phone, p.username, p.role, p.status, p.birthday, p.gender, p.address
    ORDER BY p.person_id DESC
    LIMIT :limit OFFSET :offset
  `,
    {
      replacements: {
        ...replacements,
        limit: parsedLimit,
        offset,
      },
    },
  );

  // Format employee data
  const employeesList = employees.map((row) => ({
    person_id: row.person_id,
    name: row.name,
    email: row.email,
    phone: row.phone,
    username: row.username,
    role: row.role,
    status: row.status,
    birthday: row.birthday,
    sex: row.sex,
    address: row.address,
    field_count: parseInt(row.field_count),
    field_names: row.field_names,
  }));

  return {
    employees: employeesList,
    total: count,
    page: parsedPage,
    totalPages: Math.ceil(count / parsedLimit),
  };
};

/**
 * Get employee by ID
 */
export const getEmployeeByIdService = async (id) => {
  const [employees] = await User.sequelize.query(`
    SELECT 
      p.person_id,
      p.full_name as name,
      p.email,
      p.phone,
      p.username,
      p.role,
      p.status,
      p.birthday,
      p.gender as sex,
      p.address,
      f.field_id as managed_field_id,
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
    name: employee.name,
    email: employee.email,
    phone: employee.phone,
    username: employee.username,
    role: employee.role,
    status: employee.status,
    birthday: employee.birthday,
    sex: employee.sex,
    address: employee.address,
    field_id: employee.managed_field_id,
    field: employee.managed_field_id
      ? {
          field_id: employee.managed_field_id,
          field_name: employee.field_name,
          location: employee.location,
          status: employee.field_status,
        }
      : null,
  };
};

/**
 * Create new employee
 */
export const createEmployeeService = async (employeeData) => {
  // Force role to manager
  const employee = await User.create({
    ...employeeData,
    role: "manager",
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
      role: "manager",
    },
  });

  if (!employee) {
    throw new Error("Employee not found");
  }

  // Don't allow password or role change through this method
  delete employeeData.password;
  delete employeeData.role;

  // Clean up empty values
  Object.keys(employeeData).forEach((key) => {
    if (employeeData[key] === "" || employeeData[key] === "Invalid date") {
      employeeData[key] = null;
    }
  });

  // Allow field_id update
  if (employeeData.field_id !== undefined) {
    employeeData.field_id = employeeData.field_id || null;
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
      role: "manager",
    },
  });

  if (!employee) {
    throw new Error("Employee not found");
  }

  // Check if employee is managing any fields
  const managedFields = await Field.count({
    where: { manager_id: id },
  });

  if (managedFields > 0) {
    throw new Error(
      "Cannot delete employee who is managing fields. Please reassign fields first.",
    );
  }

  // Hard delete - remove employee from database
  await employee.destroy();
  return { message: "Employee deleted successfully" };
};

/**
 * Assign field to employee
 */
export const assignFieldToEmployeeService = async (employeeId, field_id) => {
  const employee = await User.findOne({
    where: {
      person_id: employeeId,
      role: "manager",
    },
  });

  if (!employee) {
    throw new Error("Employee not found");
  }

  const [fields] = await User.sequelize.query(
    `
    SELECT field_id, field_name, location, status, manager_id
    FROM fields
    WHERE field_id = :fieldId
    LIMIT 1
  `,
    {
      replacements: { fieldId: field_id },
    },
  );

  if (!fields || fields.length === 0) {
    throw new Error("Field not found");
  }

  await User.sequelize.query(
    `
    UPDATE fields
    SET manager_id = :employeeId
    WHERE field_id = :fieldId
  `,
    {
      replacements: {
        employeeId,
        fieldId: field_id,
      },
    },
  );

  const [updatedFields] = await User.sequelize.query(
    `
    SELECT field_id, field_name, location, status, manager_id
    FROM fields
    WHERE field_id = :fieldId
    LIMIT 1
  `,
    {
      replacements: { fieldId: field_id },
    },
  );

  return {
    message: "Field assigned to employee successfully",
    field: updatedFields[0] || fields[0],
  };
};

/**
 * Get employee statistics
 */
export const getEmployeeStatsService = async () => {
  const totalEmployees = await User.count({ where: { role: "manager" } });
  const activeEmployees = await User.count({
    where: { role: "manager", status: "active" },
  });
  const inactiveEmployees = await User.count({
    where: { role: "manager", status: "inactive" },
  });

  return {
    total: totalEmployees,
    active: activeEmployees,
    inactive: inactiveEmployees,
  };
};
