import sequelize from "../config/database.js";

export const getAllFieldsService = async () => {
  const [rows] = await sequelize.query("SELECT * FROM fields");
  return rows;
};

export const getFieldByIdService = async (id) => {
  try {
    const [rows] = await sequelize.query("SELECT * FROM fields WHERE field_id = ?", {
      replacements: [id]
    });

    return rows[0] || null;
  } catch (err) {
    console.error("🔥 Lỗi SQL:", err);
    throw err;
  }
};

/**
 *Cập nhật thông tin sân bóng: giá, vị trí, trạng thái sân
 */
export const updateFieldById = async (id, data) => {
  const updateField = [];
  const values = [];

  if (data.price !== undefined) {
    updateField.push("price= ?");
    values.push(data.price);
  }

  if (data.location !== undefined) {
    updateField.push("location= ?");
    values.push(data.location);
  }
  if (data.status !== undefined) {
    updateField.push("status= ?");
    values.push(data.status);
  }
  const sql = 'UPDATE fields SET ${updateField.join(", ")} where field_id = ?';
  values.push(id);
};