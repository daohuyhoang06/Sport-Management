import {
  getAllSportTypesService,
  createSportTypeService,
  updateSportTypeService,
  deleteSportTypeService,
} from "../../services/admin/sportTypeManagementService.js";

export const getAllSportTypes = async (req, res) => {
  try {
    const sportTypes = await getAllSportTypesService();

    res.json({
      success: true,
      data: sportTypes,
    });
  } catch (error) {
    console.error("Error in getAllSportTypes:", error);
    res.status(500).json({
      success: false,
      message: "Loi khi lay danh sach loai san",
      error: error.message,
    });
  }
};

export const createSportType = async (req, res) => {
  try {
    const created = await createSportTypeService(req.body);

    res.status(201).json({
      success: true,
      message: "Tao loai san thanh cong",
      data: created,
    });
  } catch (error) {
    console.error("Error in createSportType:", error);
    res.status(400).json({
      success: false,
      message: "Loi khi tao loai san",
      error: error.message,
    });
  }
};

export const updateSportType = async (req, res) => {
  try {
    const { id } = req.params;
    const updated = await updateSportTypeService(id, req.body);

    res.json({
      success: true,
      message: "Cap nhat loai san thanh cong",
      data: updated,
    });
  } catch (error) {
    console.error("Error in updateSportType:", error);
    res.status(400).json({
      success: false,
      message: "Loi khi cap nhat loai san",
      error: error.message,
    });
  }
};

export const deleteSportType = async (req, res) => {
  try {
    const { id } = req.params;
    const deleted = await deleteSportTypeService(id);

    res.json({
      success: true,
      message: "Xoa loai san thanh cong",
      data: deleted,
    });
  } catch (error) {
    console.error("Error in deleteSportType:", error);
    res.status(400).json({
      success: false,
      message: "Loi khi xoa loai san",
      error: error.message,
    });
  }
};
