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
  payment_status: {
    type: DataTypes.STRING(45),
    allowNull: true
  },
  transaction_id: {
    type: DataTypes.STRING(100),
    allowNull: true
  },
  provider: {
    type: DataTypes.STRING(45),
    allowNull: true
  },
  order_id: {
    type: DataTypes.STRING(200),
    allowNull: true
  },
  request_id: {
    type: DataTypes.STRING(200),
    allowNull: true
  },
  booking_ids_json: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  pay_url: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  deeplink: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  qr_code_url: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  raw_response: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  paid_at: {
    type: DataTypes.DATE,
    allowNull: true
  },
  created_at: {
    type: DataTypes.DATE,
    allowNull: true
  },
  failure_reason: {
    type: DataTypes.TEXT,
    allowNull: true
  }
}, {
  tableName: 'payments',
  timestamps: false,
  indexes: [
    { fields: ['booking_id'] },
    { fields: ['customer_id'] },
    { fields: ['payment_status'] },
    { fields: ['transaction_id'] },
    { fields: ['provider'] },
    { fields: ['order_id'] },
    { fields: ['request_id'] }
  ]
});

export default Payment;
