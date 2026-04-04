// Central models export with associations
import User from './User.js';
import Field from './Field.js';
import FieldImage from './FieldImage.js';
import FieldSchedule from './FieldSchedule.js';
import Booking from './Booking.js';
import Payment from './Payment.js';
import Review from './Review.js';

// Define associations
User.hasMany(Field, { foreignKey: 'manager_id', as: 'managedFields' });
Field.belongsTo(User, { foreignKey: 'manager_id', as: 'manager' });

Field.hasMany(FieldImage, { foreignKey: 'field_id', as: 'images' });
FieldImage.belongsTo(Field, { foreignKey: 'field_id', as: 'field' });

Field.hasMany(FieldSchedule, { foreignKey: 'field_id', as: 'schedules' });
FieldSchedule.belongsTo(Field, { foreignKey: 'field_id', as: 'field' });

User.hasMany(FieldSchedule, { foreignKey: 'manager_id', as: 'managedSchedules' });
FieldSchedule.belongsTo(User, { foreignKey: 'manager_id', as: 'manager' });

User.hasMany(Booking, { foreignKey: 'customer_id', as: 'bookings' });
Booking.belongsTo(User, { foreignKey: 'customer_id', as: 'customer' });

Field.hasMany(Booking, { foreignKey: 'field_id', as: 'bookings' });
Booking.belongsTo(Field, { foreignKey: 'field_id', as: 'field' });

User.hasMany(Booking, { foreignKey: 'manager_id', as: 'managedBookings' });
Booking.belongsTo(User, { foreignKey: 'manager_id', as: 'manager' });

Booking.hasOne(Payment, { foreignKey: 'booking_id', as: 'payment' });
Payment.belongsTo(Booking, { foreignKey: 'booking_id', as: 'booking' });

User.hasMany(Payment, { foreignKey: 'customer_id', as: 'payments' });
Payment.belongsTo(User, { foreignKey: 'customer_id', as: 'customer' });

Field.hasMany(Payment, { foreignKey: 'field_id', as: 'payments' });
Payment.belongsTo(Field, { foreignKey: 'field_id', as: 'field' });

User.hasMany(Review, { foreignKey: 'customer_id', as: 'reviews' });
Review.belongsTo(User, { foreignKey: 'customer_id', as: 'customer' });

Field.hasMany(Review, { foreignKey: 'field_id', as: 'reviews' });
Review.belongsTo(Field, { foreignKey: 'field_id', as: 'field' });

Booking.hasMany(Review, { foreignKey: 'booking_id', as: 'reviews' });
Review.belongsTo(Booking, { foreignKey: 'booking_id', as: 'booking' });

export {
  User,
  Field,
  FieldImage,
  FieldSchedule,
  Booking,
  Payment,
  Review
};
