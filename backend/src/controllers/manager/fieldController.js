import {
  getManagerFieldsService,
  getManagerFieldByIdService,
  createFieldService,
  updateFieldService,
  deleteFieldService,
  updateFieldStatusService,
  getFieldStatsService
} from '../../services/manager/fieldService.js';

/**
 * Create new field
 * POST /api/manager/fields
 */
export const createField = async (req, res) => {
  try {
    const managerId = req.user.id;
    const { field_name, location, rental_price } = req.body;

    if (!field_name || !location) {
      return res.status(400).json({
        success: false,
        message: 'Tên sân và địa điểm là bắt buộc',
      });
    }

    const newField = await createFieldService(managerId, { field_name, location, rental_price });

    res.status(201).json({
      success: true,
      message: 'Tạo sân mới thành công',
      data: newField,
    });
  } catch (error) {
    console.error('Error creating field:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Lỗi server khi tạo sân',
    });
  }
};

/**
 * Update field
 * PUT /api/manager/fields/:id
 */
export const updateField = async (req, res) => {
  try {
    const managerId = req.user.id;
    const fieldId = req.params.id;
    const { field_name, location } = req.body;

    if (!field_name || !location) {
      return res.status(400).json({
        success: false,
        message: 'Tên sân và địa điểm là bắt buộc',
      });
    }

    await updateFieldService(managerId, fieldId, { field_name, location });

    res.json({
      success: true,
      message: 'Cập nhật sân thành công',
    });
  } catch (error) {
    console.error('Error updating field:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Lỗi server khi cập nhật sân',
    });
  }
};

/**
 * Delete field
 * DELETE /api/manager/fields/:id
 */
export const deleteField = async (req, res) => {
  try {
    const managerId = req.user.id;
    const fieldId = req.params.id;

    await deleteFieldService(managerId, fieldId);

    res.json({
      success: true,
      message: 'Xóa sân thành công',
    });
  } catch (error) {
    console.error('Error deleting field:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Lỗi server khi xóa sân',
    });
  }
};

/**
 * Get all fields managed by this manager
 * GET /api/manager/fields
 */
export const getAllFields = async (req, res) => {
  try {
    const managerId = req.user.id;
    
    const fields = await getManagerFieldsService(managerId);

    res.json({
      success: true,
      data: fields,
    });
  } catch (error) {
    console.error('Error fetching fields:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi server khi lấy danh sách sân',
    });
  }
};

/**
 * Get field by ID (only if managed by this manager)
 * GET /api/manager/fields/:id
 */
export const getFieldById = async (req, res) => {
  try {
    const managerId = req.user.id;
    const fieldId = req.params.id;

    const field = await getManagerFieldByIdService(managerId, fieldId);

    if (!field) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy sân bóng hoặc bạn không có quyền truy cập',
      });
    }

    res.json({
      success: true,
      data: field,
    });
  } catch (error) {
    console.error('Error in getFieldById:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi server khi lấy thông tin sân',
    });
  }
};

/**
 * Update field status
 * PUT /api/manager/fields/:id/status
 */
export const updateFieldStatus = async (req, res) => {
  try {
    const managerId = req.user.id;
    const fieldId = req.params.id;
    const { status } = req.body;

    if (!['active', 'inactive'].includes(status)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid status. Must be active or inactive',
      });
    }

    await updateFieldStatusService(managerId, fieldId, status);

    res.json({
      success: true,
      message: 'Cập nhật trạng thái sân thành công',
    });
  } catch (error) {
    console.error('Error updating field status:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Lỗi server khi cập nhật trạng thái sân',
    });
  }
};

/**
 * Get field statistics
 * GET /api/manager/fields/:id/stats
 */
export const getFieldStats = async (req, res) => {
  try {
    const managerId = req.user.id;
    const fieldId = req.params.id;

    const stats = await getFieldStatsService(managerId, fieldId);

    res.json({
      success: true,
      data: stats,
    });
  } catch (error) {
    console.error('Error getting field stats:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Lỗi server khi lấy thống kê sân',
    });
  }
};
