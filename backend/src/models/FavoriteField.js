import { DataTypes } from "sequelize";
import sequelize from "../config/database.js";

const FavoriteField = sequelize.define(
  "FavoriteField",
  {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true,
      allowNull: false,
    },
    user_id: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    field_id: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    created_at: {
      type: DataTypes.DATE,
      allowNull: false,
      defaultValue: DataTypes.NOW,
    },
  },
  {
    tableName: "favorite_fields",
    timestamps: false,
  },
);

export default FavoriteField;
