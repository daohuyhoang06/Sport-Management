import { DataTypes } from "sequelize";
import bcrypt from "bcryptjs";
import sequelize from "../config/database.js";

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
      field: "full_name",
      validate: {
        notEmpty: {
          msg: "Tên không được để trống",
        },
        len: {
          args: [2, 50],
          msg: "Tên phải từ 2-50 ký tự",
        },
      },
    },
    birthday: {
      type: DataTypes.DATEONLY,
      allowNull: true,
    },
    sex: {
      type: DataTypes.STRING(10),
      allowNull: true,
      field: "gender",
      validate: {
        isIn: {
          args: [["male", "female", "other", null]],
          msg: "Giới tính không hợp lệ",
        },
      },
    },
    address: {
      type: DataTypes.STRING(45),
      allowNull: true,
    },
    email: {
      type: DataTypes.STRING(45),
      allowNull: true,
      unique: true,
      validate: {
        isEmail: {
          msg: "Email không hợp lệ",
        },
      },
    },
    phone: {
      type: DataTypes.STRING(10),
      allowNull: true,
      validate: {
        is: {
          args: /^[0-9]{10}$/,
          msg: "Số điện thoại phải có 10 chữ số",
        },
      },
    },
    username: {
      type: DataTypes.STRING(45),
      allowNull: false,
      unique: true,
      validate: {
        notEmpty: {
          msg: "Username không được để trống",
        },
        len: {
          args: [3, 45],
          msg: "Username phải từ 3-45 ký tự",
        },
      },
    },
    password: {
      type: DataTypes.STRING(255), // Changed from 45 to 255 for hashed password
      allowNull: false,
      validate: {
        notEmpty: {
          msg: "Mật khẩu không được để trống",
        },
        len: {
          args: [6, 255],
          msg: "Mật khẩu phải có ít nhất 6 ký tự",
        },
      },
    },
    role: {
      type: DataTypes.STRING(45),
      allowNull: true,
      defaultValue: "user",
      validate: {
        isIn: {
          args: [["user", "staff", "manager", "admin"]],
          msg: "Role không hợp lệ",
        },
      },
    },
    status: {
      type: DataTypes.STRING(45),
      allowNull: true,
      defaultValue: "active",
      validate: {
        isIn: {
          args: [["active", "inactive", "banned"]],
          msg: "Status không hợp lệ",
        },
      },
    },
  },
  {
    tableName: "person",
    timestamps: false,
    hooks: {
      // Hash password before creating user
      beforeCreate: async (person) => {
        if (person.password) {
          const salt = await bcrypt.genSalt(10);
          person.password = await bcrypt.hash(person.password, salt);
        }
      },
      // Hash password before updating if it's changed
      beforeUpdate: async (person) => {
        if (person.changed("password")) {
          const salt = await bcrypt.genSalt(10);
          person.password = await bcrypt.hash(person.password, salt);
        }
      },
    },
  },
);

// Instance method to check password
Person.prototype.comparePassword = async function (candidatePassword) {
  return await bcrypt.compare(candidatePassword, this.password);
};

// Instance method to hide password in JSON responses
Person.prototype.toJSON = function () {
  const values = { ...this.get() };
  delete values.password;
  return values;
};

export default Person;
export const User = Person;
