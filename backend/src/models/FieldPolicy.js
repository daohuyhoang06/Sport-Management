import { DataTypes } from "sequelize";
import sequelize from "../config/database.js";

const FieldPolicy = sequelize.define(
  "FieldPolicy",
  {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true,
      allowNull: false,
    },
    field_id: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    title: {
      type: DataTypes.STRING(150),
      allowNull: false,
    },
    content: {
      type: DataTypes.TEXT,
      allowNull: false,
    },
    policy_type: {
      type: DataTypes.STRING(50),
      allowNull: false,
    },
  },
  {
    tableName: "field_policies",
    timestamps: false,
  },
);

export default FieldPolicy;
