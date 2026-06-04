import { DataTypes } from 'sequelize';
import sequelize from '../config/database.js';

const Booking = sequelize.define('Booking', {
  booking_id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true,
    allowNull: false
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
  court_id: {
    type: DataTypes.INTEGER,
    allowNull: true,
    references: {
      model: 'field_courts',
      key: 'court_id'
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
  start_time: {
    type: DataTypes.DATE,
    allowNull: false
  },
  end_time: {
    type: DataTypes.DATE,
    allowNull: false
  },
  status: {
    type: DataTypes.STRING(45),
    allowNull: false,
    defaultValue: 'pending',
    validate: {
      isIn: [['pending', 'confirmed', 'approved', 'rejected', 'cancelled', 'completed']]
    }
  },
  pending_expires_at: {
    type: DataTypes.DATE,
    allowNull: true
  },
  note: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  price: {
    type: DataTypes.DECIMAL(10, 2),
    allowNull: false
  },
  share_token: {
    type: DataTypes.STRING(120),
    allowNull: true
  },
  checkin_code: {
    type: DataTypes.STRING(32),
    allowNull: true
  },
  checked_in_at: {
    type: DataTypes.DATE,
    allowNull: true
  }
}, {
  tableName: 'bookings',
  timestamps: false,
  indexes: [
    { fields: ['customer_id'] },
    { fields: ['field_id'] },
    { fields: ['status'] },
    { fields: ['status', 'pending_expires_at'] },
    { fields: ['start_time', 'end_time'] },
    { fields: ['share_token'] },
    { fields: ['checkin_code'] }
  ]
});

export default Booking;
