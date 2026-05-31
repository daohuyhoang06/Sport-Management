import sequelize from './src/config/database.js';

async function checkData() {
  try {
    await sequelize.authenticate();
    const [bookings] = await sequelize.query('SELECT COUNT(*) as count FROM bookings');
    const [schedules] = await sequelize.query('SELECT COUNT(*) as count FROM field_schedules');
    console.log('Bookings count:', bookings[0].count);
    console.log('Schedules count:', schedules[0].count);
    process.exit(0);
  } catch (error) {
    console.error('Error:', error.message);
    process.exit(1);
  }
}

checkData();
