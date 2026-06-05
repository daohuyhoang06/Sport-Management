import { DataTypes } from "sequelize";
import sequelize from "../config/database.js";

const BookingSlot = sequelize.define(
  "BookingSlot",
  {
    booking_slot_id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true,
      allowNull: false,
    },
    booking_id: {
      type: DataTypes.INTEGER,
      allowNull: false,
      references: {
        model: "bookings",
        key: "booking_id",
      },
    },
    field_id: {
      type: DataTypes.INTEGER,
      allowNull: false,
      references: {
        model: "fields",
        key: "field_id",
      },
    },
    court_id: {
      type: DataTypes.INTEGER,
      allowNull: true,
      references: {
        model: "field_courts",
        key: "court_id",
      },
    },
    start_time: {
      type: DataTypes.DATE,
      allowNull: false,
    },
    end_time: {
      type: DataTypes.DATE,
      allowNull: false,
    },
    price: {
      type: DataTypes.DECIMAL(10, 2),
      allowNull: false,
    },
  },
  {
    tableName: "booking_slots",
    timestamps: false,
    indexes: [
      { fields: ["booking_id"] },
      { fields: ["field_id"] },
      { fields: ["court_id"] },
      { fields: ["start_time", "end_time"] },
      { fields: ["booking_id", "court_id", "start_time", "end_time"], unique: true },
    ],
  },
);

export default BookingSlot;
