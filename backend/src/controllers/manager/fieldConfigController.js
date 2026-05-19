import {
  createFieldCourtService,
  createFieldPolicyService,
  createFieldServiceItemService,
  deleteFieldCourtService,
  deleteFieldPolicyService,
  deleteFieldServiceItemService,
  getFieldConfigService,
  listFieldCourtsService,
  listFieldPoliciesService,
  listFieldServicesService,
  reorderFieldCourtsService,
  updateFieldCourtService,
  updateFieldPolicyService,
  updateFieldServiceItemService,
} from "../../services/manager/fieldConfigService.js";

const isBlank = (value) =>
  value === undefined || value === null || String(value).trim().length === 0;

const buildErrorResponse = (res, error, fallbackMessage) => {
  const status =
    error.message.includes("not found") || error.message.includes("unauthorized")
      ? 404
      : 400;

  return res.status(status).json({
    success: false,
    message: fallbackMessage,
    error: error.message,
  });
};

export const getFieldConfig = async (req, res) => {
  try {
    const managerId = req.user.id;
    const fieldId = req.params.id;
    const config = await getFieldConfigService(managerId, fieldId);

    res.json({
      success: true,
      data: config,
    });
  } catch (error) {
    console.error("Error getting field config:", error);
    buildErrorResponse(res, error, "Loi khi lay cau hinh san");
  }
};

export const listFieldCourts = async (req, res) => {
  try {
    const courts = await listFieldCourtsService(req.user.id, req.params.id);
    res.json({ success: true, data: courts });
  } catch (error) {
    console.error("Error listing field courts:", error);
    buildErrorResponse(res, error, "Loi khi lay danh sach san con");
  }
};

export const createFieldCourt = async (req, res) => {
  try {
    const { court_code, court_name } = req.body;
    if (isBlank(court_code) || isBlank(court_name)) {
      return res.status(400).json({
        success: false,
        message: "court_code va court_name la bat buoc",
      });
    }

    const court = await createFieldCourtService(req.user.id, req.params.id, req.body);
    res.status(201).json({ success: true, data: court });
  } catch (error) {
    console.error("Error creating field court:", error);
    buildErrorResponse(res, error, "Loi khi tao san con");
  }
};

export const updateFieldCourt = async (req, res) => {
  try {
    if (
      req.body.court_code !== undefined &&
      isBlank(req.body.court_code)
    ) {
      return res.status(400).json({
        success: false,
        message: "court_code khong duoc de trong",
      });
    }
    if (
      req.body.court_name !== undefined &&
      isBlank(req.body.court_name)
    ) {
      return res.status(400).json({
        success: false,
        message: "court_name khong duoc de trong",
      });
    }

    const court = await updateFieldCourtService(
      req.user.id,
      req.params.id,
      req.params.courtId,
      req.body,
    );
    res.json({ success: true, data: court });
  } catch (error) {
    console.error("Error updating field court:", error);
    buildErrorResponse(res, error, "Loi khi cap nhat san con");
  }
};

export const reorderFieldCourts = async (req, res) => {
  try {
    const { courts } = req.body;
    if (!Array.isArray(courts) || courts.length === 0) {
      return res.status(400).json({
        success: false,
        message: "courts phai la mang khong rong",
      });
    }

    const hasInvalidCourt = courts.some(
      (item) => item.court_id === undefined || item.sort_order === undefined,
    );
    if (hasInvalidCourt) {
      return res.status(400).json({
        success: false,
        message: "Moi court can co court_id va sort_order",
      });
    }

    const reorderedCourts = await reorderFieldCourtsService(
      req.user.id,
      req.params.id,
      courts,
    );
    res.json({ success: true, data: reorderedCourts });
  } catch (error) {
    console.error("Error reordering field courts:", error);
    buildErrorResponse(res, error, "Loi khi sap xep san con");
  }
};

export const deleteFieldCourt = async (req, res) => {
  try {
    await deleteFieldCourtService(req.user.id, req.params.id, req.params.courtId);
    res.json({ success: true, message: "Xoa san con thanh cong" });
  } catch (error) {
    console.error("Error deleting field court:", error);
    buildErrorResponse(res, error, "Loi khi xoa san con");
  }
};

export const listFieldServices = async (req, res) => {
  try {
    const services = await listFieldServicesService(req.user.id, req.params.id);
    res.json({ success: true, data: services });
  } catch (error) {
    console.error("Error listing field services:", error);
    buildErrorResponse(res, error, "Loi khi lay danh sach dich vu");
  }
};

export const createFieldServiceItem = async (req, res) => {
  try {
    const { service_name, is_free, price } = req.body;
    if (isBlank(service_name)) {
      return res.status(400).json({
        success: false,
        message: "service_name la bat buoc",
      });
    }
    if (is_free === false && (price === undefined || Number(price) <= 0)) {
      return res.status(400).json({
        success: false,
        message: "price phai lon hon 0 voi dich vu co phi",
      });
    }

    const service = await createFieldServiceItemService(
      req.user.id,
      req.params.id,
      req.body,
    );
    res.status(201).json({ success: true, data: service });
  } catch (error) {
    console.error("Error creating field service:", error);
    buildErrorResponse(res, error, "Loi khi tao dich vu");
  }
};

export const updateFieldServiceItem = async (req, res) => {
  try {
    if (
      req.body.service_name !== undefined &&
      isBlank(req.body.service_name)
    ) {
      return res.status(400).json({
        success: false,
        message: "service_name khong duoc de trong",
      });
    }

    const service = await updateFieldServiceItemService(
      req.user.id,
      req.params.id,
      req.params.serviceId,
      req.body,
    );
    res.json({ success: true, data: service });
  } catch (error) {
    console.error("Error updating field service:", error);
    buildErrorResponse(res, error, "Loi khi cap nhat dich vu");
  }
};

export const deleteFieldServiceItem = async (req, res) => {
  try {
    await deleteFieldServiceItemService(
      req.user.id,
      req.params.id,
      req.params.serviceId,
    );
    res.json({ success: true, message: "Xoa dich vu thanh cong" });
  } catch (error) {
    console.error("Error deleting field service:", error);
    buildErrorResponse(res, error, "Loi khi xoa dich vu");
  }
};

export const listFieldPolicies = async (req, res) => {
  try {
    const policies = await listFieldPoliciesService(req.user.id, req.params.id);
    res.json({ success: true, data: policies });
  } catch (error) {
    console.error("Error listing field policies:", error);
    buildErrorResponse(res, error, "Loi khi lay danh sach chinh sach");
  }
};

export const createFieldPolicy = async (req, res) => {
  try {
    const { title, content, policy_type } = req.body;
    if (isBlank(title) || isBlank(content) || isBlank(policy_type)) {
      return res.status(400).json({
        success: false,
        message: "title, content va policy_type la bat buoc",
      });
    }

    const policy = await createFieldPolicyService(req.user.id, req.params.id, req.body);
    res.status(201).json({ success: true, data: policy });
  } catch (error) {
    console.error("Error creating field policy:", error);
    buildErrorResponse(res, error, "Loi khi tao chinh sach");
  }
};

export const updateFieldPolicy = async (req, res) => {
  try {
    if (req.body.title !== undefined && isBlank(req.body.title)) {
      return res.status(400).json({
        success: false,
        message: "title khong duoc de trong",
      });
    }
    if (req.body.content !== undefined && isBlank(req.body.content)) {
      return res.status(400).json({
        success: false,
        message: "content khong duoc de trong",
      });
    }
    if (
      req.body.policy_type !== undefined &&
      isBlank(req.body.policy_type)
    ) {
      return res.status(400).json({
        success: false,
        message: "policy_type khong duoc de trong",
      });
    }

    const policy = await updateFieldPolicyService(
      req.user.id,
      req.params.id,
      req.params.policyId,
      req.body,
    );
    res.json({ success: true, data: policy });
  } catch (error) {
    console.error("Error updating field policy:", error);
    buildErrorResponse(res, error, "Loi khi cap nhat chinh sach");
  }
};

export const deleteFieldPolicy = async (req, res) => {
  try {
    await deleteFieldPolicyService(req.user.id, req.params.id, req.params.policyId);
    res.json({ success: true, message: "Xoa chinh sach thanh cong" });
  } catch (error) {
    console.error("Error deleting field policy:", error);
    buildErrorResponse(res, error, "Loi khi xoa chinh sach");
  }
};
