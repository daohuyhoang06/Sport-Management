import {
  getAllSportTypes,
  createSportTypeService,
  updateSportTypeService,
  deleteSportTypeService,
} from "../../services/admin/sportTypeManagementService.js";

export const getAllSportTypesController = async (req, res) => {
  try {
    const types = await getAllSportTypes();
    res.json({ success: true, data: types });
  } catch (err) {
    console.error("Error in getAllSportTypes:", err);
    res
      .status(500)
      .json({
        success: false,
        message: "Lỗi khi lấy loại sân",
        error: err.message,
      });
  }
};

export const createSportType = async (req, res) => {
  try {
    const payload = req.body;
    const row = await createSportTypeService(payload);
    res
      .status(201)
      .json({ success: true, message: "Tạo loại sân thành công", data: row });
  } catch (err) {
    console.error("Error in createSportType:", err);
    res
      .status(400)
      .json({
        success: false,
        message: "Lỗi khi tạo loại sân",
        error: err.message,
      });
  }
};

export const updateSportType = async (req, res) => {
  try {
    const { id } = req.params;
    const payload = req.body;
    const row = await updateSportTypeService(id, payload);
    res.json({
      success: true,
      message: "Cập nhật loại sân thành công",
      data: row,
    });
  } catch (err) {
    console.error("Error in updateSportType:", err);
    res
      .status(400)
      .json({
        success: false,
        message: "Lỗi khi cập nhật loại sân",
        error: err.message,
      });
  }
};

export const deleteSportType = async (req, res) => {
  try {
    const { id } = req.params;
    const result = await deleteSportTypeService(id);
    res.json({ success: true, message: result.message });
  } catch (err) {
    console.error("Error in deleteSportType:", err);
    res
      .status(400)
      .json({
        success: false,
        message: "Lỗi khi xóa loại sân",
        error: err.message,
      });
  }
};
