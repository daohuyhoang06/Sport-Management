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
    logging: console.log // Show SQL queries
  }
);

async function testBookingInsert() {
  try {
    console.log('Testing booking insert...\n');
    
    const customer_id = 1;
    const field_id = 1;
    const start_time = new Date();
    const end_time = new Date(start_time.getTime() + 2 * 60 * 60 * 1000); // +2 hours
    const price = 1200000;
    const note = 'Test booking';
    
    console.log('Data to insert:', {
      customer_id,
      field_id,
      start_time: start_time.toISOString(),
      end_time: end_time.toISOString(),
      price,
      note
    });
    
    await sequelize.query(
      `INSERT INTO bookings (customer_id, field_id, start_time, end_time, price, note, status) VALUES (?, ?, ?, ?, ?, ?, 'pending')`,
      { replacements: [customer_id, field_id, start_time, end_time, price, note] }
    );
    
    console.log('\n✅ Booking inserted successfully!');
    
    const [rows] = await sequelize.query('SELECT * FROM bookings WHERE booking_id = LAST_INSERT_ID() LIMIT 1');
    console.log('\nInserted booking:', rows[0]);
    
    await sequelize.close();
  } catch (error) {
    console.error('\n❌ Error:', error.message);
    console.error('SQL Error:', error.original?.sqlMessage);
    process.exit(1);
  }
}

testBookingInsert();
