import { DataTypes } from 'sequelize';
import sequelize from '../config/database.js';

const FieldImage = sequelize.define('FieldImage', {
  image_id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true,
    allowNull: false
  },
  field_id: {
    type: DataTypes.INTEGER,
    allowNull: false,
    references: {
      model: 'fields',
      key: 'field_id'
    }
  },
  image_url: {
    type: DataTypes.STRING(255),
    allowNull: false
  },
  is_primary: {
    type: DataTypes.BOOLEAN,
    allowNull: true,
    defaultValue: false
  }
}, {
  tableName: 'field_images',
  timestamps: false,
  indexes: [
    { fields: ['field_id'] }
  ]
});

export default FieldImage;
