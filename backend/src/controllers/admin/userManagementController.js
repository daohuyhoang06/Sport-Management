import {
  getAllUsersService,
  getUserByIdService,
  createUserService,
  updateUserService,
  deleteUserService,
  toggleUserStatusService,
  getUserStatsService
} from '../../services/admin/userManagementService.js';

/**
 * Get all users with pagination and filters
 */
export const getAllUsers = async (req, res) => {
  try {
    const { page, limit, search, role, status } = req.query;
    const result = await getAllUsersService(
      { search, role, status },
      { page, limit }
    );

    res.json({
      success: true,
      data: result
    });
  } catch (error) {
    console.error('Error in getAllUsers:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi lấy danh sách người dùng',
      error: error.message
    });
  }
};

/**
 * Get user by ID
 */
export const getUserById = async (req, res) => {
  try {
    const { id } = req.params;
    const user = await getUserByIdService(id);

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy người dùng'
      });
    }

    res.json({
      success: true,
      data: user
    });
  } catch (error) {
    console.error('Error in getUserById:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi lấy thông tin người dùng',
      error: error.message
    });
  }
};

/**
 * Create new user
 */
export const createUser = async (req, res) => {
  try {
    const userData = req.body;
    const user = await createUserService(userData);

    res.status(201).json({
      success: true,
      message: 'Tạo người dùng thành công',
      data: user
    });
  } catch (error) {
    console.error('Error in createUser:', error);
    res.status(400).json({
      success: false,
      message: 'Lỗi khi tạo người dùng',
      error: error.message
    });
  }
};

/**
 * Update user
 */
export const updateUser = async (req, res) => {
  try {
    const { id } = req.params;
    const userData = req.body;
    const user = await updateUserService(id, userData);

    res.json({
      success: true,
      message: 'Cập nhật người dùng thành công',
      data: user
    });
  } catch (error) {
    console.error('Error in updateUser:', error);
    res.status(400).json({
      success: false,
      message: 'Lỗi khi cập nhật người dùng',
      error: error.message
    });
  }
};

/**
 * Delete user
 */
export const deleteUser = async (req, res) => {
  try {
    const { id } = req.params;
    const result = await deleteUserService(id);

    res.json({
      success: true,
      message: result.message
    });
  } catch (error) {
    console.error('Error in deleteUser:', error);
    res.status(400).json({
      success: false,
      message: 'Lỗi khi xóa người dùng',
      error: error.message
    });
  }
};

/**
 * Toggle user status
 */
export const toggleUserStatus = async (req, res) => {
  try {
    const { id } = req.params;
    const result = await toggleUserStatusService(id);

    res.json({
      success: true,
      message: result.message,
      data: { status: result.status }
    });
  } catch (error) {
    console.error('Error in toggleUserStatus:', error);
    res.status(400).json({
      success: false,
      message: 'Lỗi khi thay đổi trạng thái người dùng',
      error: error.message
    });
  }
};

/**
 * Get user statistics
 */
export const getUserStats = async (req, res) => {
  try {
    const stats = await getUserStatsService();

    res.json({
      success: true,
      data: stats
    });
  } catch (error) {
    console.error('Error in getUserStats:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi lấy thống kê người dùng',
      error: error.message
    });
  }
};
