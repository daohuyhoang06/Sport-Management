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

async function checkReviewsTable() {
  try {
    const [tables] = await sequelize.query("SHOW TABLES LIKE 'reviews'");
    
    if (tables.length > 0) {
      console.log('✅ Reviews table exists');
      const [cols] = await sequelize.query('DESCRIBE reviews');
      console.table(cols);
    } else {
      console.log('❌ Reviews table does not exist');
    }
    
    await sequelize.close();
  } catch (error) {
    console.error('Error:', error.message);
    process.exit(1);
  }
}

checkReviewsTable();
