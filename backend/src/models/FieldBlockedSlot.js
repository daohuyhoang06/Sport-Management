import { DataTypes } from 'sequelize';
import sequelize from '../config/database.js';

const FieldBlockedSlot = sequelize.define('FieldBlockedSlot', {
  slot_id:    { type: DataTypes.INTEGER, primaryKey: true, autoIncrement: true },
  field_id:   { type: DataTypes.INTEGER, allowNull: false },
  court_id:   { type: DataTypes.INTEGER, allowNull: true },
  block_date: { type: DataTypes.DATEONLY, allowNull: false },
  start_time: { type: DataTypes.TIME, allowNull: false },
  end_time:   { type: DataTypes.TIME, allowNull: false },
  reason:     { type: DataTypes.STRING(255), allowNull: true },
  block_type: {
    type: DataTypes.ENUM('maintenance', 'event', 'other'),
    defaultValue: 'maintenance',
  },
  created_at: { type: DataTypes.DATE },
}, {
  tableName: 'field_blocked_slots',
  timestamps: false,
});

export default FieldBlockedSlot;
