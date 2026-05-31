import sequelize from "../../config/database.js";
import FavoriteField from "../../models/FavoriteField.js";
import Field from "../../models/Field.js";
import { mapFieldRowToListPayload } from "./fieldController.js";

const getFavoriteFieldRows = async (userId) => {
  const [rows] = await sequelize.query(
    `
    SELECT
      f.field_id,
      f.field_name,
      f.location,
      f.status,
      f.latitude,
      f.longitude,
      f.open_time,
      f.close_time,
      f.slot_price,
      f.avatar_image_url,
      f.card_image_url,
      f.sport_id,
      f.display_rating,
      f.featured,
      f.availability_note,
      f.card_type,
      f.region,
      f.province,
      f.district,
      NULL AS distance_km,
      st.sport_name,
      COALESCE(f.display_rating, AVG(r.rating), 0) AS rating_value,
      COUNT(DISTINCT r.review_id) AS review_count,
      GROUP_CONCAT(DISTINCT ft.tag_name ORDER BY ft.sort_order SEPARATOR '|||') AS tags_csv
    FROM favorite_fields ff
    INNER JOIN fields f ON f.field_id = ff.field_id
    LEFT JOIN sport_types st ON st.sport_id = f.sport_id
    LEFT JOIN reviews r ON r.field_id = f.field_id
    LEFT JOIN field_tags ft ON ft.field_id = f.field_id
    WHERE ff.user_id = ?
    GROUP BY
      f.field_id, f.field_name, f.location, f.status, f.latitude, f.longitude,
      f.open_time, f.close_time, f.slot_price, f.avatar_image_url, f.card_image_url,
      f.sport_id, f.display_rating, f.featured, f.availability_note, f.card_type,
      f.region, f.province, f.district, st.sport_name
    ORDER BY ff.created_at DESC, f.field_id DESC
    `,
    { replacements: [userId] },
  );

  return rows;
};

export const listFavoriteFields = async (req, res) => {
  try {
    const userId = Number(req.user?.id);
    if (!userId) {
      return res.status(401).json({ message: "Unauthorized" });
    }

    const rows = await getFavoriteFieldRows(userId);
    return res.json(rows.map(mapFieldRowToListPayload));
  } catch (error) {
    console.error("List favorite fields error:", error);
    return res.status(500).json({ message: "Server error when fetching favorite fields" });
  }
};

export const addFavoriteField = async (req, res) => {
  try {
    const userId = Number(req.user?.id);
    const fieldId = Number(req.params.fieldId);

    if (!userId) {
      return res.status(401).json({ message: "Unauthorized" });
    }

    if (!Number.isInteger(fieldId) || fieldId <= 0) {
      return res.status(400).json({ message: "Invalid fieldId" });
    }

    const field = await Field.findByPk(fieldId);
    if (!field) {
      return res.status(404).json({ message: "Field not found" });
    }

    await FavoriteField.findOrCreate({
      where: {
        user_id: userId,
        field_id: fieldId,
      },
      defaults: {
        user_id: userId,
        field_id: fieldId,
      },
    });

    return res.status(201).json({
      message: "Favorite added",
      field_id: fieldId,
    });
  } catch (error) {
    console.error("Add favorite field error:", error);
    return res.status(500).json({ message: "Server error when adding favorite field" });
  }
};

export const removeFavoriteField = async (req, res) => {
  try {
    const userId = Number(req.user?.id);
    const fieldId = Number(req.params.fieldId);

    if (!userId) {
      return res.status(401).json({ message: "Unauthorized" });
    }

    if (!Number.isInteger(fieldId) || fieldId <= 0) {
      return res.status(400).json({ message: "Invalid fieldId" });
    }

    await FavoriteField.destroy({
      where: {
        user_id: userId,
        field_id: fieldId,
      },
    });

    return res.json({
      message: "Favorite removed",
      field_id: fieldId,
    });
  } catch (error) {
    console.error("Remove favorite field error:", error);
    return res.status(500).json({ message: "Server error when removing favorite field" });
  }
};
