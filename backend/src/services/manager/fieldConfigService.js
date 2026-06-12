import sequelize from "../../config/database.js";
import { getManagerFieldByIdService } from "./fieldService.js";

const normalizeBoolean = (value) => {
  if (typeof value === "boolean") return value;
  if (value === 1 || value === "1" || value === "true") return true;
  if (value === 0 || value === "0" || value === "false") return false;
  return Boolean(value);
};

const normalizeNullableText = (value) => {
  if (value === undefined || value === null) return null;
  const trimmed = String(value).trim();
  return trimmed.length > 0 ? trimmed : null;
};

const normalizePositiveNumber = (value) => {
  const numeric = Number(value);
  return Number.isFinite(numeric) ? numeric : NaN;
};

const assertManagerOwnsField = async (managerId, fieldId) => {
  const field = await getManagerFieldByIdService(managerId, fieldId);
  if (!field) {
    throw new Error("Field not found or unauthorized");
  }
  return field;
};

const getManagedCourt = async (managerId, fieldId, courtId) => {
  const [[court]] = await sequelize.query(
    `
    SELECT fc.*
    FROM field_courts fc
    INNER JOIN fields f ON f.field_id = fc.field_id
    WHERE fc.court_id = ? AND fc.field_id = ? AND f.manager_id = ?
    LIMIT 1
  `,
    { replacements: [courtId, fieldId, managerId] },
  );
  return court || null;
};

const getManagedService = async (managerId, fieldId, serviceId) => {
  const [[service]] = await sequelize.query(
    `
    SELECT fs.*
    FROM field_services fs
    INNER JOIN fields f ON f.field_id = fs.field_id
    WHERE fs.id = ? AND fs.field_id = ? AND f.manager_id = ?
    LIMIT 1
  `,
    { replacements: [serviceId, fieldId, managerId] },
  );
  return service || null;
};

const getManagedPolicy = async (managerId, fieldId, policyId) => {
  const [[policy]] = await sequelize.query(
    `
    SELECT fp.*
    FROM field_policies fp
    INNER JOIN fields f ON f.field_id = fp.field_id
    WHERE fp.id = ? AND fp.field_id = ? AND f.manager_id = ?
    LIMIT 1
  `,
    { replacements: [policyId, fieldId, managerId] },
  );
  return policy || null;
};

const listCourtsByFieldId = async (fieldId) => {
  const [rows] = await sequelize.query(
    `
    SELECT court_id, field_id, court_code, court_name, status, sort_order, created_at, updated_at
    FROM field_courts
    WHERE field_id = ?
    ORDER BY sort_order ASC, court_id ASC
  `,
    { replacements: [fieldId] },
  );
  return rows;
};

const listServicesByFieldId = async (fieldId) => {
  const [rows] = await sequelize.query(
    `
    SELECT id, field_id, service_name, description, is_free, price
    FROM field_services
    WHERE field_id = ?
    ORDER BY is_free DESC, service_name ASC, id ASC
  `,
    { replacements: [fieldId] },
  );
  return rows;
};

const listPoliciesByFieldId = async (fieldId) => {
  const [rows] = await sequelize.query(
    `
    SELECT id, field_id, title, content, policy_type
    FROM field_policies
    WHERE field_id = ?
    ORDER BY policy_type ASC, id ASC
  `,
    { replacements: [fieldId] },
  );
  return rows;
};

export const getFieldConfigService = async (managerId, fieldId) => {
  const field = await assertManagerOwnsField(managerId, fieldId);
  const [courts, services, policies] = await Promise.all([
    listCourtsByFieldId(fieldId),
    listServicesByFieldId(fieldId),
    listPoliciesByFieldId(fieldId),
  ]);

  return {
    field,
    courts,
    services,
    policies,
  };
};

export const listFieldCourtsService = async (managerId, fieldId) => {
  await assertManagerOwnsField(managerId, fieldId);
  return listCourtsByFieldId(fieldId);
};

export const createFieldCourtService = async (managerId, fieldId, courtData) => {
  await assertManagerOwnsField(managerId, fieldId);

  const courtCode = String(courtData.court_code).trim();
  const courtName = String(courtData.court_name).trim();
  const status = courtData.status || "active";
  const sortOrder = courtData.sort_order ?? 0;

  await sequelize.query(
    `
    INSERT INTO field_courts (field_id, court_code, court_name, status, sort_order, created_at, updated_at)
    VALUES (?, ?, ?, ?, ?, NOW(), NOW())
  `,
    { replacements: [fieldId, courtCode, courtName, status, sortOrder] },
  );

  const [[createdCourt]] = await sequelize.query(
    "SELECT * FROM field_courts WHERE court_id = LAST_INSERT_ID()",
  );
  return createdCourt;
};

export const updateFieldCourtService = async (
  managerId,
  fieldId,
  courtId,
  courtData,
) => {
  const court = await getManagedCourt(managerId, fieldId, courtId);
  if (!court) {
    throw new Error("Court not found or unauthorized");
  }

  const updates = [];
  const params = [];

  if (courtData.court_code !== undefined) {
    updates.push("court_code = ?");
    params.push(String(courtData.court_code).trim());
  }
  if (courtData.court_name !== undefined) {
    updates.push("court_name = ?");
    params.push(String(courtData.court_name).trim());
  }
  if (courtData.status !== undefined) {
    updates.push("status = ?");
    params.push(courtData.status);
  }
  if (courtData.sort_order !== undefined) {
    updates.push("sort_order = ?");
    params.push(courtData.sort_order);
  }

  if (updates.length === 0) {
    throw new Error("No court data provided for update");
  }

  params.push(courtId, fieldId);
  await sequelize.query(
    `
    UPDATE field_courts
    SET ${updates.join(", ")}, updated_at = NOW()
    WHERE court_id = ? AND field_id = ?
  `,
    { replacements: params },
  );

  return getManagedCourt(managerId, fieldId, courtId);
};

export const reorderFieldCourtsService = async (managerId, fieldId, courts) => {
  await assertManagerOwnsField(managerId, fieldId);

  const existingRows = await listCourtsByFieldId(fieldId);
  const existingCourtIds = new Set(existingRows.map((item) => Number(item.court_id)));
  const hasUnknownCourt = courts.some(
    (item) => !existingCourtIds.has(Number(item.court_id)),
  );

  if (hasUnknownCourt) {
    throw new Error("One or more courts not found");
  }

  await sequelize.transaction(async (transaction) => {
    for (const item of courts) {
      await sequelize.query(
        `
        UPDATE field_courts
        SET sort_order = ?, updated_at = NOW()
        WHERE court_id = ? AND field_id = ?
      `,
        {
          replacements: [item.sort_order, item.court_id, fieldId],
          transaction,
        },
      );
    }
  });

  return listCourtsByFieldId(fieldId);
};

export const deleteFieldCourtService = async (managerId, fieldId, courtId) => {
  const court = await getManagedCourt(managerId, fieldId, courtId);
  if (!court) {
    throw new Error("Court not found or unauthorized");
  }

  await sequelize.query(
    "DELETE FROM field_courts WHERE court_id = ? AND field_id = ?",
    { replacements: [courtId, fieldId] },
  );
};

export const listFieldServicesService = async (managerId, fieldId) => {
  await assertManagerOwnsField(managerId, fieldId);
  return listServicesByFieldId(fieldId);
};

export const createFieldServiceItemService = async (
  managerId,
  fieldId,
  serviceData,
) => {
  await assertManagerOwnsField(managerId, fieldId);

  const serviceName = String(serviceData.service_name).trim();
  const description = normalizeNullableText(serviceData.description);
  const isFree = normalizeBoolean(serviceData.is_free);
  const price = isFree ? 0 : normalizePositiveNumber(serviceData.price);

  if (!isFree && (!Number.isFinite(price) || price <= 0)) {
    throw new Error("Price must be greater than 0 for paid services");
  }

  await sequelize.query(
    `
    INSERT INTO field_services (field_id, service_name, description, is_free, price)
    VALUES (?, ?, ?, ?, ?)
  `,
    {
      replacements: [
        fieldId,
        serviceName,
        description,
        isFree ? 1 : 0,
        isFree ? 0 : price,
      ],
    },
  );

  const [[createdService]] = await sequelize.query(
    "SELECT * FROM field_services WHERE id = LAST_INSERT_ID()",
  );
  return createdService;
};

export const updateFieldServiceItemService = async (
  managerId,
  fieldId,
  serviceId,
  serviceData,
) => {
  const service = await getManagedService(managerId, fieldId, serviceId);
  if (!service) {
    throw new Error("Service not found or unauthorized");
  }

  const nextIsFree =
    serviceData.is_free !== undefined
      ? normalizeBoolean(serviceData.is_free)
      : normalizeBoolean(service.is_free);

  const nextPrice =
    serviceData.price !== undefined
      ? normalizePositiveNumber(serviceData.price)
      : normalizePositiveNumber(service.price);

  if (!nextIsFree && (!Number.isFinite(nextPrice) || nextPrice <= 0)) {
    throw new Error("Price must be greater than 0 for paid services");
  }

  const updates = [];
  const params = [];

  if (serviceData.service_name !== undefined) {
    updates.push("service_name = ?");
    params.push(String(serviceData.service_name).trim());
  }
  if (serviceData.description !== undefined) {
    updates.push("description = ?");
    params.push(normalizeNullableText(serviceData.description));
  }
  if (serviceData.is_free !== undefined || serviceData.price !== undefined) {
    updates.push("is_free = ?");
    params.push(nextIsFree ? 1 : 0);
    updates.push("price = ?");
    params.push(nextIsFree ? 0 : nextPrice);
  }

  if (updates.length === 0) {
    throw new Error("No service data provided for update");
  }

  params.push(serviceId, fieldId);
  await sequelize.query(
    `
    UPDATE field_services
    SET ${updates.join(", ")}
    WHERE id = ? AND field_id = ?
  `,
    { replacements: params },
  );

  return getManagedService(managerId, fieldId, serviceId);
};

export const deleteFieldServiceItemService = async (
  managerId,
  fieldId,
  serviceId,
) => {
  const service = await getManagedService(managerId, fieldId, serviceId);
  if (!service) {
    throw new Error("Service not found or unauthorized");
  }

  await sequelize.query("DELETE FROM field_services WHERE id = ? AND field_id = ?", {
    replacements: [serviceId, fieldId],
  });
};

export const listFieldPoliciesService = async (managerId, fieldId) => {
  await assertManagerOwnsField(managerId, fieldId);
  return listPoliciesByFieldId(fieldId);
};

export const createFieldPolicyService = async (managerId, fieldId, policyData) => {
  await assertManagerOwnsField(managerId, fieldId);

  const title = String(policyData.title).trim();
  const content = String(policyData.content).trim();
  const policyType = String(policyData.policy_type).trim();

  await sequelize.query(
    `
    INSERT INTO field_policies (field_id, title, content, policy_type)
    VALUES (?, ?, ?, ?)
  `,
    { replacements: [fieldId, title, content, policyType] },
  );

  const [[createdPolicy]] = await sequelize.query(
    "SELECT * FROM field_policies WHERE id = LAST_INSERT_ID()",
  );
  return createdPolicy;
};

export const updateFieldPolicyService = async (
  managerId,
  fieldId,
  policyId,
  policyData,
) => {
  const policy = await getManagedPolicy(managerId, fieldId, policyId);
  if (!policy) {
    throw new Error("Policy not found or unauthorized");
  }

  const updates = [];
  const params = [];

  if (policyData.title !== undefined) {
    updates.push("title = ?");
    params.push(String(policyData.title).trim());
  }
  if (policyData.content !== undefined) {
    updates.push("content = ?");
    params.push(String(policyData.content).trim());
  }
  if (policyData.policy_type !== undefined) {
    updates.push("policy_type = ?");
    params.push(String(policyData.policy_type).trim());
  }

  if (updates.length === 0) {
    throw new Error("No policy data provided for update");
  }

  params.push(policyId, fieldId);
  await sequelize.query(
    `
    UPDATE field_policies
    SET ${updates.join(", ")}
    WHERE id = ? AND field_id = ?
  `,
    { replacements: params },
  );

  return getManagedPolicy(managerId, fieldId, policyId);
};

export const deleteFieldPolicyService = async (managerId, fieldId, policyId) => {
  const policy = await getManagedPolicy(managerId, fieldId, policyId);
  if (!policy) {
    throw new Error("Policy not found or unauthorized");
  }

  await sequelize.query("DELETE FROM field_policies WHERE id = ? AND field_id = ?", {
    replacements: [policyId, fieldId],
  });
};
