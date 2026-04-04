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

async function checkReviewsData() {
  try {
    const [reviews] = await sequelize.query('SELECT * FROM reviews');
    
    console.log('Total reviews:', reviews.length);
    console.log('\nReview data:');
    
    reviews.forEach((r, idx) => {
      console.log(`\n--- Review ${idx + 1} ---`);
      console.log('review_id:', r.review_id);
      console.log('field_id:', r.field_id);
      console.log('images type:', typeof r.images);
      console.log('images value:', r.images);
      console.log('images repr:', JSON.stringify(r.images));
    });
    
    await sequelize.close();
  } catch (error) {
    console.error('Error:', error.message);
    process.exit(1);
  }
}

checkReviewsData();
