import { DataTypes } from 'sequelize';
import sequelize from '../config/database.js';

const FieldCourt = sequelize.define(
  'FieldCourt',
  {
    court_id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true,
      allowNull: false,
    },
    field_id: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    court_code: {
      type: DataTypes.STRING(50),
      allowNull: false,
    },
    court_name: {
      type: DataTypes.STRING(100),
      allowNull: false,
    },
    status: {
      type: DataTypes.STRING(45),
      allowNull: false,
      defaultValue: 'active',
    },
    sort_order: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 0,
    },
    created_at: {
      type: DataTypes.DATE,
      allowNull: false,
      defaultValue: DataTypes.NOW,
    },
    updated_at: {
      type: DataTypes.DATE,
      allowNull: false,
      defaultValue: DataTypes.NOW,
    },
  },
  {
    tableName: 'field_courts',
    timestamps: false,
  },
);

export default FieldCourt;

