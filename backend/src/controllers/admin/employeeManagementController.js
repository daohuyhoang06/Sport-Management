import {
  getAllEmployeesService,
  getEmployeeByIdService,
  createEmployeeService,
  updateEmployeeService,
  deleteEmployeeService,
  assignFieldToEmployeeService,
  getEmployeeStatsService
} from '../../services/admin/employeeManagementService.js';

/**
 * Get all employees
 */
export const getAllEmployees = async (req, res) => {
  try {
    const { page, limit, search, status } = req.query;
    const result = await getAllEmployeesService(
      { search, status },
      { page, limit }
    );

    res.json({
      success: true,
      data: result
    });
  } catch (error) {
    console.error('Error in getAllEmployees:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi lấy danh sách nhân viên',
      error: error.message
    });
  }
};

/**
 * Get employee by ID
 */
export const getEmployeeById = async (req, res) => {
  try {
    const { id } = req.params;
    const employee = await getEmployeeByIdService(id);

    if (!employee) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy nhân viên'
      });
    }

    res.json({
      success: true,
      data: employee
    });
  } catch (error) {
    console.error('Error in getEmployeeById:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi lấy thông tin nhân viên',
      error: error.message
    });
  }
};

/**
 * Create new employee
 */
export const createEmployee = async (req, res) => {
  try {
    const employeeData = req.body;
    const employee = await createEmployeeService(employeeData);

    res.status(201).json({
      success: true,
      message: 'Tạo nhân viên thành công',
      data: employee
    });
  } catch (error) {
    console.error('Error in createEmployee:', error);
    res.status(400).json({
      success: false,
      message: 'Lỗi khi tạo nhân viên',
      error: error.message
    });
  }
};

/**
 * Update employee
 */
export const updateEmployee = async (req, res) => {
  try {
    const { id } = req.params;
    const employeeData = req.body;
    const employee = await updateEmployeeService(id, employeeData);

    res.json({
      success: true,
      message: 'Cập nhật nhân viên thành công',
      data: employee
    });
  } catch (error) {
    console.error('Error in updateEmployee:', error);
    res.status(400).json({
      success: false,
      message: 'Lỗi khi cập nhật nhân viên',
      error: error.message
    });
  }
};

/**
 * Delete employee
 */
export const deleteEmployee = async (req, res) => {
  try {
    const { id } = req.params;
    const result = await deleteEmployeeService(id);

    res.json({
      success: true,
      message: result.message
    });
  } catch (error) {
    console.error('Error in deleteEmployee:', error);
    res.status(400).json({
      success: false,
      message: 'Lỗi khi xóa nhân viên',
      error: error.message
    });
  }
};

/**
 * Assign field to employee
 */
export const assignFieldToEmployee = async (req, res) => {
  try {
    const { employeeId, fieldId } = req.body;

    if (!employeeId || !fieldId) {
      return res.status(400).json({
        success: false,
        message: 'Vui lòng cung cấp ID nhân viên và ID sân'
      });
    }

    const result = await assignFieldToEmployeeService(employeeId, fieldId);

    res.json({
      success: true,
      message: result.message,
      data: result.field
    });
  } catch (error) {
    console.error('Error in assignFieldToEmployee:', error);
    res.status(400).json({
      success: false,
      message: 'Lỗi khi phân công sân cho nhân viên',
      error: error.message
    });
  }
};

/**
 * Get employee statistics
 */
export const getEmployeeStats = async (req, res) => {
  try {
    const stats = await getEmployeeStatsService();

    res.json({
      success: true,
      data: stats
    });
  } catch (error) {
    console.error('Error in getEmployeeStats:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi lấy thống kê nhân viên',
      error: error.message
    });
  }
};
