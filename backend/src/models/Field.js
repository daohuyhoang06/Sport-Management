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
  status: {
    type: DataTypes.STRING(45),
    allowNull: true,
    defaultValue: 'active'
  },
  rental_price: {
    type: DataTypes.DECIMAL(10, 2),
    allowNull: true,
    defaultValue: null
  }
}, {
  tableName: 'fields',
  timestamps: false
});

export default Field;