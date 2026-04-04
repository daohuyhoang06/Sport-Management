import { DataTypes } from 'sequelize';
import bcrypt from 'bcryptjs';
import sequelize from '../config/database.js';

const Person = sequelize.define('Person', {
  person_id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true,
    allowNull: false
  },
  person_name: {
    type: DataTypes.STRING(50),
    allowNull: false,
    validate: {
      notEmpty: {
        msg: 'TÃªn khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng'
      },
      len: {
        args: [2, 50],
        msg: 'TÃªn pháº£i tá»« 2-50 kÃ½ tá»±'
      }
    }
  },
  birthday: {
    type: DataTypes.DATEONLY,
    allowNull: true
  },
  sex: {
    type: DataTypes.STRING(10),
    allowNull: true,
    validate: {
      isIn: {
        args: [['male', 'female', 'other', null]],
        msg: 'Giá»›i tÃ­nh khÃ´ng há»£p lá»‡'
      }
    }
  },
  address: {
    type: DataTypes.STRING(45),
    allowNull: true
  },
  email: {
    type: DataTypes.STRING(45),
    allowNull: true,
    unique: true,
    validate: {
      isEmail: {
        msg: 'Email khÃ´ng há»£p lá»‡'
      }
    }
  },
  phone: {
    type: DataTypes.STRING(10),
    allowNull: true,
    validate: {
      is: {
        args: /^[0-9]{10}$/,
        msg: 'Sá»‘ Ä‘iá»‡n thoáº¡i pháº£i cÃ³ 10 chá»¯ sá»‘'
      }
    }
  },
  username: {
    type: DataTypes.STRING(45),
    allowNull: false,
    unique: true,
    validate: {
      notEmpty: {
        msg: 'Username khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng'
      },
      len: {
        args: [3, 45],
        msg: 'Username pháº£i tá»« 3-45 kÃ½ tá»±'
      }
    }
  },
  password: {
    type: DataTypes.STRING(255), // Changed from 45 to 255 for hashed password
    allowNull: false,
    validate: {
      notEmpty: {
        msg: 'Máº­t kháº©u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng'
      },
      len: {
        args: [6, 255],
        msg: 'Máº­t kháº©u pháº£i cÃ³ Ã­t nháº¥t 6 kÃ½ tá»±'
      }
    }
  },
  role: {
    type: DataTypes.STRING(45),
    allowNull: true,
    defaultValue: 'user',
    validate: {
      isIn: {
        args: [['user', 'staff', 'manager', 'admin']],
        msg: 'Role khÃ´ng há»£p lá»‡'
      }
    }
  },
  status: {
    type: DataTypes.STRING(45),
    allowNull: true,
    defaultValue: 'active',
    validate: {
      isIn: {
        args: [['active', 'inactive', 'banned']],
        msg: 'Status khÃ´ng há»£p lá»‡'
      }
    }
  },
  fieldId: {
    type: DataTypes.INTEGER,
    allowNull: true,
    references: {
      model: 'fields',
      key: 'field_id'
    }
  }
}, {
  tableName: 'person',
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
      if (person.changed('password')) {
        const salt = await bcrypt.genSalt(10);
        person.password = await bcrypt.hash(person.password, salt);
      }
    }
  }
});

// Instance method to check password
Person.prototype.comparePassword = async function(candidatePassword) {
  return await bcrypt.compare(candidatePassword, this.password);
};

// Instance method to hide password in JSON responses
Person.prototype.toJSON = function() {
  const values = { ...this.get() };
  delete values.password;
  return values;
};

export default Person;
export const User = Person;
