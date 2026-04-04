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
    person_name: {
      type: DataTypes.STRING(50),
      allowNull: false,
    },
    birthday: {
      type: DataTypes.DATEONLY,
      allowNull: true,
    },
    sex: {
      type: DataTypes.STRING(10),
      allowNull: true,
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
    fieldId: {
      type: DataTypes.INTEGER,
      allowNull: true,
      field: 'fieldid', // PostgreSQL column name is lowercase
      references: {
        model: "fields",
        key: "field_id",
      },
    },
  },
  {
    tableName: "person",
    timestamps: false,
    indexes: [
      { unique: true, fields: ["email"] },
      { unique: true, fields: ["username"] },
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
  }
);

// Instance method to compare password
Person.prototype.comparePassword = async function (candidatePassword) {
  return await bcrypt.compare(candidatePassword, this.password);
};

export default Person;
