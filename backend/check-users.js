import { Sequelize } from 'sequelize';
import dotenv from 'dotenv';

dotenv.config();

const sequelize = new Sequelize(
  process.env.DB_NAME || 'sport_management',
  process.env.DB_USER || 'root',
  process.env.DB_PASSWORD || '',
  {
    host: process.env.DB_HOST || 'localhost',
    dialect: 'mysql',
    logging: false
  }
);

async function checkUsers() {
  try {
    const [users] = await sequelize.query('SELECT person_id, username, person_name, role FROM person ORDER BY person_id');
    console.log('Users in database:');
    console.table(users);
    
    const [fields] = await sequelize.query('SELECT field_id, field_name FROM fields ORDER BY field_id LIMIT 5');
    console.log('\nFields in database:');
    console.table(fields);
    
    await sequelize.close();
  } catch (error) {
    console.error('Error:', error.message);
    process.exit(1);
  }
}

checkUsers();
