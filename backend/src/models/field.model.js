const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const Field = sequelize.define('Field', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true
  },
  name: {
    type: DataTypes.STRING,
    allowNull: false
  },
  type: {
    type: DataTypes.ENUM('5v5', '7v7', '11v11'),
    allowNull: false
  },
  pricePerHour: {
    type: DataTypes.DECIMAL(10, 2),
    allowNull: false
  },
  status: {
    type: DataTypes.ENUM('available', 'maintenance', 'booked'),
    defaultValue: 'available'
  },
  description: {
    type: DataTypes.TEXT
  }
});

module.exports = Field;