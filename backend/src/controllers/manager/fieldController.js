import {
  createFieldService,
  deleteFieldService,
  getFieldStatsService,
  getManagerFieldByIdService,
  getManagerFieldsService,
  updateFieldService,
  updateFieldStatusService,
} from "../../services/manager/fieldService.js";
import { uploadFieldImage, handleUploadErrors } from "../../middleware/upload.js";

const isBlank = (value) =>
  value === undefined || value === null || String(value).trim().length === 0;

/**
 * Create new field
 * POST /api/manager/fields
 */
export const createField = async (req, res) => {
  try {
    const managerId = req.user.id;
    const fieldData = req.body;

    if (isBlank(fieldData.field_name) || isBlank(fieldData.location)) {
      return res.status(400).json({
        success: false,
        message: "field_name va location la bat buoc",
      });
    }
    if (!fieldData.sport_id) {
      return res.status(400).json({
        success: false,
        message: "sport_id la bat buoc",
      });
    }
    if (
      fieldData.slot_minutes !== undefined &&
      Number(fieldData.slot_minutes) <= 0
    ) {
      return res.status(400).json({
        success: false,
        message: "slot_minutes phai lon hon 0",
      });
    }
    if (fieldData.slot_price !== undefined && Number(fieldData.slot_price) < 0) {
      return res.status(400).json({
        success: false,
        message: "slot_price khong hop le",
      });
    }

    const newField = await createFieldService(managerId, fieldData);

    res.status(201).json({
      success: true,
      message: "Tao san moi thanh cong",
      data: newField,
    });
  } catch (error) {
    console.error("Error creating field:", error);
    res.status(400).json({
      success: false,
      message: error.message || "Loi khi tao san",
    });
  }
};

/**
 * Update field
 * PATCH /api/manager/fields/:id
 */
export const updateField = async (req, res) => {
  try {
    const managerId = req.user.id;
    const fieldId = req.params.id;
    const fieldData = req.body;

    if (fieldData.field_name !== undefined && isBlank(fieldData.field_name)) {
      return res.status(400).json({
        success: false,
        message: "field_name khong duoc de trong",
      });
    }
    if (fieldData.location !== undefined && isBlank(fieldData.location)) {
      return res.status(400).json({
        success: false,
        message: "location khong duoc de trong",
      });
    }
    if (
      fieldData.slot_minutes !== undefined &&
      Number(fieldData.slot_minutes) <= 0
    ) {
      return res.status(400).json({
        success: false,
        message: "slot_minutes phai lon hon 0",
      });
    }
    if (fieldData.slot_price !== undefined && Number(fieldData.slot_price) < 0) {
      return res.status(400).json({
        success: false,
        message: "slot_price khong hop le",
      });
    }

    const updatedField = await updateFieldService(managerId, fieldId, fieldData);

    res.json({
      success: true,
      message: "Cap nhat san thanh cong",
      data: updatedField,
    });
  } catch (error) {
    console.error("Error updating field:", error);
    const status =
      error.message === "Field not found or unauthorized" ? 404 : 400;
    res.status(status).json({
      success: false,
      message: error.message || "Loi khi cap nhat san",
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
      message: "Xoa san thanh cong",
    });
  } catch (error) {
    console.error("Error deleting field:", error);
    const status =
      error.message === "Field not found or unauthorized" ? 404 : 400;
    res.status(status).json({
      success: false,
      message: error.message || "Loi khi xoa san",
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
    console.error("Error fetching fields:", error);
    res.status(500).json({
      success: false,
      message: "Loi khi lay danh sach san",
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
        message: "Khong tim thay san hoac ban khong co quyen truy cap",
      });
    }

    res.json({
      success: true,
      data: field,
    });
  } catch (error) {
    console.error("Error in getFieldById:", error);
    res.status(500).json({
      success: false,
      message: "Loi khi lay thong tin san",
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

    if (!["active", "inactive", "maintenance"].includes(status)) {
      return res.status(400).json({
        success: false,
        message: "Invalid status. Must be active, inactive, or maintenance",
      });
    }

    await updateFieldStatusService(managerId, fieldId, status);

    res.json({
      success: true,
      message: "Cap nhat trang thai san thanh cong",
    });
  } catch (error) {
    console.error("Error updating field status:", error);
    const status =
      error.message === "Field not found or unauthorized" ? 404 : 400;
    res.status(status).json({
      success: false,
      message: error.message || "Loi khi cap nhat trang thai san",
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
    console.error("Error getting field stats:", error);
    const status =
      error.message === "Field not found or unauthorized" ? 404 : 400;
    res.status(status).json({
      success: false,
      message: error.message || "Loi khi lay thong ke san",
    });
  }
};

/**
 * Upload field image
 * POST /api/manager/upload/field-image
 */
export const uploadFieldImageController = (req, res) => {
  uploadFieldImage(req, res, (err) => {
    if (err) {
      return handleUploadErrors(err, req, res, () => {});
    }
    if (!req.file) {
      return res.status(400).json({ success: false, message: "Khong co file anh" });
    }
    const baseUrl = `${req.protocol}://${req.get("host")}`;
    const url = `${baseUrl}/uploads/fields/${req.file.filename}`;
    res.json({ success: true, url });
  });
};
