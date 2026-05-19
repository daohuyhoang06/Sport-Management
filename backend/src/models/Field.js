import { DataTypes } from 'sequelize';
import sequelize from '../config/database.js';

const Field = sequelize.define('Field', {
  field_id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true,
    allowNull: false
  },
  manager_id: {
    type: DataTypes.INTEGER,
    allowNull: true
  },
  field_name: {
    type: DataTypes.STRING(50),
    allowNull: false
  },
  location: {
    type: DataTypes.STRING(100),
    allowNull: true
  },
  latitude: {
    type: DataTypes.DECIMAL(10, 7),
    allowNull: true
  },
  longitude: {
    type: DataTypes.DECIMAL(10, 7),
    allowNull: true
  },
  phone: {
    type: DataTypes.STRING(20),
    allowNull: true
  },
  open_time: {
    type: DataTypes.TIME,
    allowNull: true
  },
  close_time: {
    type: DataTypes.TIME,
    allowNull: true
  },
  avatar_image_url: {
    type: DataTypes.STRING(255),
    allowNull: true,
    defaultValue: null
  },
  card_image_url: {
    type: DataTypes.STRING(255),
    allowNull: true,
    defaultValue: null
  },
  slot_minutes: {
    type: DataTypes.INTEGER,
    allowNull: false,
    defaultValue: 60
  },
  slot_price: {
    type: DataTypes.DECIMAL(10, 2),
    allowNull: true,
    defaultValue: null
  },
  status: {
    type: DataTypes.STRING(45),
    allowNull: true,
    defaultValue: 'active'
  },
  created_at: {
    type: DataTypes.DATE,
    allowNull: false,
    defaultValue: DataTypes.NOW
  },
  updated_at: {
    type: DataTypes.DATE,
    allowNull: false,
    defaultValue: DataTypes.NOW
  }
}, {
  tableName: 'fields',
  timestamps: false
});

export default Field;
