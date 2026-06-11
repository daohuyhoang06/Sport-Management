import { DataTypes } from 'sequelize';
import sequelize from '../config/database.js';

const BookingHistory = sequelize.define('BookingHistory', {
  history_id:  { type: DataTypes.INTEGER, primaryKey: true, autoIncrement: true },
  booking_id:  { type: DataTypes.INTEGER, allowNull: false },
  action:      { type: DataTypes.STRING(100), allowNull: false },
  from_status: { type: DataTypes.STRING(50), allowNull: true },
  to_status:   { type: DataTypes.STRING(50), allowNull: true },
  note:        { type: DataTypes.TEXT, allowNull: true },
  author:      { type: DataTypes.STRING(100), allowNull: true },
  created_at:  { type: DataTypes.DATE },
}, {
  tableName: 'booking_history',
  timestamps: false,
});

export default BookingHistory;
