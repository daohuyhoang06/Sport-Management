import sequelize from "../../config/database.js";

const normalizeSportName = (sportName) => String(sportName || "").trim();

const getSportTypeByName = async (sportName, excludeId = null) => {
  const params = [sportName];
  let whereClause = "WHERE sport_name = ?";

  if (excludeId !== null) {
    whereClause += " AND sport_id <> ?";
    params.push(excludeId);
  }

  const [[sportType]] = await sequelize.query(
    `SELECT sport_id, sport_name
     FROM sport_types
     ${whereClause}
     LIMIT 1`,
    { replacements: params },
  );

  return sportType || null;
};

export const getAllSportTypesService = async () => {
  const [rows] = await sequelize.query(
    `SELECT sport_id, sport_name
     FROM sport_types
     ORDER BY sport_id ASC`,
  );

  return rows;
};

export const createSportTypeService = async (payload = {}) => {
  const sportName = normalizeSportName(payload.sport_name);

  if (!sportName) {
    throw new Error("Sport type name is required");
  }

  const existed = await getSportTypeByName(sportName);
  if (existed) {
    throw new Error("Sport type name already exists");
  }

  await sequelize.query(
    `INSERT INTO sport_types (sport_name)
     VALUES (?)`,
    { replacements: [sportName] },
  );

  const [[created]] = await sequelize.query(
    `SELECT sport_id, sport_name
     FROM sport_types
     WHERE sport_id = LAST_INSERT_ID()`,
  );

  return created;
};

export const updateSportTypeService = async (id, payload = {}) => {
  const sportName = normalizeSportName(payload.sport_name);

  if (!sportName) {
    throw new Error("Sport type name is required");
  }

  const [[existing]] = await sequelize.query(
    `SELECT sport_id, sport_name
     FROM sport_types
     WHERE sport_id = ?`,
    { replacements: [id] },
  );

  if (!existing) {
    throw new Error("Sport type not found");
  }

  const duplicated = await getSportTypeByName(sportName, id);
  if (duplicated) {
    throw new Error("Sport type name already exists");
  }

  await sequelize.query(
    `UPDATE sport_types
     SET sport_name = ?
     WHERE sport_id = ?`,
    { replacements: [sportName, id] },
  );

  const [[updated]] = await sequelize.query(
    `SELECT sport_id, sport_name
     FROM sport_types
     WHERE sport_id = ?`,
    { replacements: [id] },
  );

  return updated;
};

export const deleteSportTypeService = async (id) => {
  const [[existing]] = await sequelize.query(
    `SELECT sport_id, sport_name
     FROM sport_types
     WHERE sport_id = ?`,
    { replacements: [id] },
  );

  if (!existing) {
    throw new Error("Sport type not found");
  }

  const [[usage]] = await sequelize.query(
    `SELECT COUNT(*) AS total
     FROM fields
     WHERE sport_id = ?`,
    { replacements: [id] },
  );

  if (Number(usage?.total || 0) > 0) {
    throw new Error("Cannot delete sport type that is being used by fields");
  }

  await sequelize.query(
    `DELETE FROM sport_types
     WHERE sport_id = ?`,
    { replacements: [id] },
  );

  return {
    sport_id: Number(id),
    sport_name: existing.sport_name,
  };
};
