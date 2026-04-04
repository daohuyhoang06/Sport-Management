import {
  getAllFieldsService,
  getFieldByIdService,
  createFieldService,
  updateFieldService,
  deleteFieldService,
  toggleFieldStatusService,
  getFieldStatsService,
  uploadFieldImagesService,
  deleteFieldImageService
} from '../../services/admin/fieldManagementService.js';

/**
 * Get all fields
 */
export const getAllFields = async (req, res) => {
  try {
    const { page, limit, search, status } = req.query;
    const result = await getAllFieldsService(
      { search, status },
      { page, limit }
    );

    res.json({
      success: true,
      data: result
    });
  } catch (error) {
    console.error('Error in getAllFields:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi lấy danh sách sân',
      error: error.message
    });
  }
};

/**
 * Get field by ID
 */
export const getFieldById = async (req, res) => {
  try {
    const { id } = req.params;
    const field = await getFieldByIdService(id);

    if (!field) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy sân'
      });
    }

    res.json({
      success: true,
      data: field
    });
  } catch (error) {
    console.error('Error in getFieldById:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi lấy thông tin sân',
      error: error.message
    });
  }
};

/**
 * Create new field
 */
export const createField = async (req, res) => {
  try {
    const fieldData = req.body;
    const field = await createFieldService(fieldData);

    res.status(201).json({
      success: true,
      message: 'Tạo sân thành công',
      data: field
    });
  } catch (error) {
    console.error('Error in createField:', error);
    res.status(400).json({
      success: false,
      message: 'Lỗi khi tạo sân',
      error: error.message
    });
  }
};

/**
 * Update field
 */
export const updateField = async (req, res) => {
  try {
    const { id } = req.params;
    const fieldData = req.body;
    const field = await updateFieldService(id, fieldData);

    res.json({
      success: true,
      message: 'Cập nhật sân thành công',
      data: field
    });
  } catch (error) {
    console.error('Error in updateField:', error);
    res.status(400).json({
      success: false,
      message: 'Lỗi khi cập nhật sân',
      error: error.message
    });
  }
};

/**
 * Delete field
 */
export const deleteField = async (req, res) => {
  try {
    const { id } = req.params;
    const result = await deleteFieldService(id);

    res.json({
      success: true,
      message: result.message
    });
  } catch (error) {
    console.error('Error in deleteField:', error);
    res.status(400).json({
      success: false,
      message: 'Lỗi khi xóa sân',
      error: error.message
    });
  }
};

/**
 * Toggle field status
 */
export const toggleFieldStatus = async (req, res) => {
  try {
    const { id } = req.params;
    const result = await toggleFieldStatusService(id);

    res.json({
      success: true,
      message: result.message,
      data: { status: result.status }
    });
  } catch (error) {
    console.error('Error in toggleFieldStatus:', error);
    res.status(400).json({
      success: false,
      message: 'Lỗi khi thay đổi trạng thái sân',
      error: error.message
    });
  }
};

/**
 * Get field statistics
 */
export const getFieldStats = async (req, res) => {
  try {
    const stats = await getFieldStatsService();

    res.json({
      success: true,
      data: stats
    });
  } catch (error) {
    console.error('Error in getFieldStats:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi lấy thống kê sân',
      error: error.message
    });
  }
};

/**
 * Upload field images
 */
export const uploadFieldImages = async (req, res) => {
  try {
    const { id } = req.params;
    const { images } = req.body; // Array of image URLs

    if (!images || !Array.isArray(images) || images.length === 0) {
      return res.status(400).json({
        success: false,
        message: 'Vui lòng cung cấp danh sách hình ảnh'
      });
    }

    const result = await uploadFieldImagesService(id, images);

    res.status(201).json({
      success: true,
      message: 'Upload hình ảnh thành công',
      data: result
    });
  } catch (error) {
    console.error('Error in uploadFieldImages:', error);
    res.status(400).json({
      success: false,
      message: 'Lỗi khi upload hình ảnh',
      error: error.message
    });
  }
};

/**
 * Delete field image
 */
export const deleteFieldImage = async (req, res) => {
  try {
    const { imageId } = req.params;
    const result = await deleteFieldImageService(imageId);

    res.json({
      success: true,
      message: result.message
    });
  } catch (error) {
    console.error('Error in deleteFieldImage:', error);
    res.status(400).json({
      success: false,
      message: 'Lỗi khi xóa hình ảnh',
      error: error.message
    });
  }
};
