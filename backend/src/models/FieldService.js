import { DataTypes } from "sequelize";
import sequelize from "../config/database.js";

const FieldService = sequelize.define(
  "FieldService",
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
    service_name: {
      type: DataTypes.STRING(100),
      allowNull: false,
    },
    description: {
      type: DataTypes.TEXT,
      allowNull: true,
    },
    is_free: {
      type: DataTypes.BOOLEAN,
      allowNull: false,
      defaultValue: false,
    },
    price: {
      type: DataTypes.DECIMAL(10, 2),
      allowNull: true,
      defaultValue: null,
    },
  },
  {
    tableName: "field_services",
    timestamps: false,
  },
);

export default FieldService;
