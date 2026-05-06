import sequelize from "../../config/database.js";

export const getAllSportTypes = async () => {
  const [rows] = await sequelize.query(
    "SELECT sport_id, sport_name FROM sport_types ORDER BY sport_id ASC",
  );
  return rows;
};

export const createSportTypeService = async (payload) => {
  const { sport_name } = payload;
  if (!sport_name || !sport_name.trim()) {
    throw new Error("Sport type name required");
  }

  // check exists
  const [[exists]] = await sequelize.query(
    "SELECT sport_id FROM sport_types WHERE sport_name = ? LIMIT 1",
    { replacements: [sport_name] },
  );
  if (exists) {
    throw new Error("Sport type name already exists");
  }

  await sequelize.query("INSERT INTO sport_types (sport_name) VALUES (?)", {
    replacements: [sport_name],
  });
  const [[row]] = await sequelize.query(
    "SELECT * FROM sport_types WHERE sport_id = LAST_INSERT_ID()",
  );
  return row;
};

export const updateSportTypeService = async (id, payload) => {
  const { sport_name } = payload;
  const [[existing]] = await sequelize.query(
    "SELECT sport_id FROM sport_types WHERE sport_id = ? LIMIT 1",
    { replacements: [id] },
  );
  if (!existing) throw new Error("Sport type not found");

  if (sport_name && sport_name.trim()) {
    // check duplicate name
    const [[dup]] = await sequelize.query(
      "SELECT sport_id FROM sport_types WHERE sport_name = ? AND sport_id <> ? LIMIT 1",
      { replacements: [sport_name, id] },
    );
    if (dup) throw new Error("Sport type name already exists");
    await sequelize.query(
      "UPDATE sport_types SET sport_name = ? WHERE sport_id = ?",
      { replacements: [sport_name, id] },
    );
  }

  const [[row]] = await sequelize.query(
    "SELECT sport_id, sport_name FROM sport_types WHERE sport_id = ? LIMIT 1",
    { replacements: [id] },
  );
  return row;
};

export const deleteSportTypeService = async (id) => {
  // prevent deletion if any fields reference this sport
  const [[{ count }]] = await sequelize.query(
    "SELECT COUNT(*) as count FROM fields WHERE sport_id = ? AND status <> 'inactive'",
    { replacements: [id] },
  );
  if (count > 0) {
    throw new Error("Cannot delete sport type that is being used by fields");
  }

  await sequelize.query("DELETE FROM sport_types WHERE sport_id = ?", {
    replacements: [id],
  });
  return { message: "Sport type deleted" };
};
