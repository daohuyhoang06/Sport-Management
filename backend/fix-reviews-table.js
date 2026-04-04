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

async function addColumnsToReviews() {
  try {
    // Check if images column exists
    const [cols] = await sequelize.query("SHOW COLUMNS FROM reviews LIKE 'images'");
    
    if (cols.length === 0) {
      console.log('Adding images column...');
      await sequelize.query(`
        ALTER TABLE reviews 
        ADD COLUMN images JSON NULL AFTER comment
      `);
      console.log('✅ Added images column');
    } else {
      console.log('✅ images column already exists');
    }
    
    // Check if updated_at column exists
    const [updatedCols] = await sequelize.query("SHOW COLUMNS FROM reviews LIKE 'updated_at'");
    
    if (updatedCols.length === 0) {
      console.log('Adding updated_at column...');
      await sequelize.query(`
        ALTER TABLE reviews 
        ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at
      `);
      console.log('✅ Added updated_at column');
    } else {
      console.log('✅ updated_at column already exists');
    }
    
    // Mark migration as done
    await sequelize.query(`
      INSERT INTO SequelizeMeta (name) 
      VALUES ('20241114000001-create-reviews.cjs')
      ON DUPLICATE KEY UPDATE name=name
    `);
    console.log('✅ Marked migration as done');
    
    await sequelize.close();
    console.log('\n✅ All done! Reviews table is ready.');
  } catch (error) {
    console.error('Error:', error.message);
    process.exit(1);
  }
}

addColumnsToReviews();
