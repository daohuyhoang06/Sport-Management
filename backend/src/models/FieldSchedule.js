import { DataTypes } from 'sequelize';
import sequelize from '../config/database.js';

const FieldSchedule = sequelize.define('FieldSchedule', {
  schedule_id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true,
    allowNull: false
  },
  field_id: {
    type: DataTypes.INTEGER,
    allowNull: false
  },
  manager_id: {
    type: DataTypes.INTEGER,
    allowNull: true
  },
  start_time: {
    type: DataTypes.DATE,
    allowNull: false
  },
  end_time: {
    type: DataTypes.DATE,
    allowNull: false
  },
  is_available: {
    type: DataTypes.BOOLEAN,
    allowNull: false,
    defaultValue: true
  }
}, {
  tableName: 'field_schedules',
  timestamps: false
});

export default FieldSchedule;
