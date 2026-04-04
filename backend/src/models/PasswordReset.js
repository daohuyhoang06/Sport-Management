import { DataTypes } from 'sequelize';
import sequelize from '../config/database.js';

const PasswordReset = sequelize.define('PasswordReset', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true,
    allowNull: false
  },
  person_id: {
    type: DataTypes.INTEGER,
    allowNull: false,
    references: {
      model: 'person',
      key: 'person_id'
    }
  },
  email: {
    type: DataTypes.STRING(45),
    allowNull: false
  },
  otp_code: {
    type: DataTypes.STRING(6),
    allowNull: false
  },
  expires_at: {
    type: DataTypes.DATE,
    allowNull: false
  },
  is_used: {
    type: DataTypes.BOOLEAN,
    defaultValue: false,
    allowNull: false
  },
  created_at: {
    type: DataTypes.DATE,
    allowNull: false,
    defaultValue: DataTypes.NOW
  }
}, {
  tableName: 'password_resets',
  timestamps: false
});

// Method to check if OTP is valid
PasswordReset.prototype.isValid = function() {
  return !this.is_used && new Date() < new Date(this.expires_at);
};

export default PasswordReset;