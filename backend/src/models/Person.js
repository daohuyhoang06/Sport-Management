import { DataTypes } from "sequelize";
import sequelize from "../config/database.js";
import bcrypt from "bcrypt";

const Person = sequelize.define(
  "Person",
  {
    person_id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true,
      allowNull: false,
    },
    name: {
      type: DataTypes.STRING(100),
      allowNull: false,
      field: "person_name",
    },
    birthday: {
      type: DataTypes.DATEONLY,
      allowNull: true,
    },
    sex: {
      type: DataTypes.STRING(10),
      allowNull: true,
      field: "sex",
    },
    address: {
      type: DataTypes.STRING(45),
      allowNull: true,
    },
    email: {
      type: DataTypes.STRING(45),
      allowNull: true,
      unique: true,
    },
    firebase_uid: {
      type: DataTypes.STRING(128),
      allowNull: true,
      unique: true,
    },
    phone: {
      type: DataTypes.STRING(10),
      allowNull: true,
    },
    username: {
      type: DataTypes.STRING(45),
      allowNull: false,
      unique: true,
    },
    password: {
      type: DataTypes.STRING(255),
      allowNull: false,
    },
    role: {
      type: DataTypes.STRING(45),
      allowNull: true,
      defaultValue: "user",
    },
    status: {
      type: DataTypes.STRING(45),
      allowNull: true,
      defaultValue: "active",
    },
    membership_level: {
      type: DataTypes.STRING(20),
      allowNull: false,
      defaultValue: "\u0110\u1ed3ng",
    },
    avatar_url: {
      type: DataTypes.STRING(255),
      allowNull: true,
    },
    favorite_sport_ids: {
      type: DataTypes.TEXT,
      allowNull: true,
    },
  },
  {
    tableName: "person",
    timestamps: false,
    indexes: [
      { unique: true, fields: ["email"] },
      { unique: true, fields: ["username"] },
      { unique: true, fields: ["firebase_uid"] },
    ],
    hooks: {
      beforeCreate: async (person) => {
        if (person.password) {
          const salt = await bcrypt.genSalt(10);
          person.password = await bcrypt.hash(person.password, salt);
        }
      },
      beforeUpdate: async (person) => {
        if (person.changed("password")) {
          const salt = await bcrypt.genSalt(10);
          person.password = await bcrypt.hash(person.password, salt);
        }
      },
    },
  },
);

// Instance method to compare password
Person.prototype.comparePassword = async function (candidatePassword) {
  return await bcrypt.compare(candidatePassword, this.password);
};

Person.prototype.toJSON = function () {
  const values = { ...this.get() };
  delete values.password;
  return values;
};

export default Person;
