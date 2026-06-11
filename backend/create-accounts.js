import bcrypt from 'bcryptjs';
import sequelize from './src/config/database.js';

async function createAccounts() {
  try {
    await sequelize.authenticate();
    console.log('✅ DB connected');

    const hashPassword = async (pw) => {
      const salt = await bcrypt.genSalt(10);
      return bcrypt.hash(pw, salt);
    };

    const accounts = [
      {
        person_name: 'Admin System',
        username: 'admin',
        email: 'admin@sport.com',
        password: await hashPassword('admin123'),
        role: 'admin',
        status: 'active',
        phone: '0900000001',
      },
      {
        person_name: 'Manager Test',
        username: 'manager1',
        email: 'manager1@sport.com',
        password: await hashPassword('manager123'),
        role: 'manager',
        status: 'active',
        phone: '0900000002',
      },
      {
        person_name: 'Nguyen Van A',
        username: 'user1',
        email: 'user1@sport.com',
        password: await hashPassword('user123'),
        role: 'user',
        status: 'active',
        phone: '0900000003',
      },
    ];

    for (const acc of accounts) {
      const [row, created] = await sequelize.query(
        `INSERT INTO person (person_name, username, email, password, role, status, phone)
         VALUES (:person_name, :username, :email, :password, :role, :status, :phone)
         ON DUPLICATE KEY UPDATE username=username`,
        { replacements: acc }
      );
      console.log(created ? `✅ Created: ${acc.username} (${acc.role})` : `⚠️  Already exists: ${acc.username}`);
    }

    console.log('\n📋 Tài khoản:');
    console.log('  Admin   → username: admin     / password: admin123');
    console.log('  Manager → username: manager1  / password: manager123');
    console.log('  User    → username: user1     / password: user123');

    process.exit(0);
  } catch (err) {
    console.error('❌ Error:', err.message);
    process.exit(1);
  }
}

createAccounts();
