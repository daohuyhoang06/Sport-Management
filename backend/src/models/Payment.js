import { DataTypes } from 'sequelize';
import sequelize from '../config/database.js';

const Payment = sequelize.define('Payment', {
  payment_id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true,
    allowNull: false
  },
  booking_id: {
    type: DataTypes.INTEGER,
    allowNull: false,
    references: {
      model: 'bookings',
      key: 'booking_id'
    }
  },
  customer_id: {
    type: DataTypes.INTEGER,
    allowNull: false,
    references: {
      model: 'person',
      key: 'person_id'
    }
  },
  field_id: {
    type: DataTypes.INTEGER,
    allowNull: false,
    references: {
      model: 'fields',
      key: 'field_id'
    }
  },
  schedule_id: {
    type: DataTypes.INTEGER,
    allowNull: true,
    references: {
      model: 'field_schedules',
      key: 'schedule_id'
    }
  },
  manager_id: {
    type: DataTypes.INTEGER,
    allowNull: true,
    references: {
      model: 'person',
      key: 'person_id'
    }
  },
  amount: {
    type: DataTypes.DECIMAL(10, 2),
    allowNull: false
  },
  payment_method: {
    type: DataTypes.STRING(50),
    allowNull: false,
    validate: {
      isIn: [['cash', 'bank_transfer', 'momo', 'zalopay', 'vnpay', 'credit_card']]
    }
  },
  payment_date: {
    type: DataTypes.DATE,
    allowNull: false,
    defaultValue: DataTypes.NOW
  },
  transaction_code: {
    type: DataTypes.STRING(45),
    allowNull: true
  },
  status: {
    type: DataTypes.STRING(45),
    allowNull: false,
    defaultValue: 'pending',
    validate: {
      isIn: [['pending', 'completed', 'failed', 'refunded']]
    }
  }
}, {
  tableName: 'payments',
  timestamps: false,
  indexes: [
    { fields: ['booking_id'] },
    { fields: ['customer_id'] },
    { fields: ['status'] },
    { fields: ['transaction_code'] }
  ]
});

export default Payment;