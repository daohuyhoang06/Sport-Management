import './src/config/dotenv.js';
import sequelize from './src/config/database.js';

const checkManagers = async () => {
  try {
    console.log('🔍 Checking managers in database...');
    
    const [managers] = await sequelize.query(
      `SELECT person_id, person_name, email, role, status FROM person WHERE role = 'manager'`
    );
    
    console.log(`\n✅ Found ${managers.length} managers:`);
    console.log(JSON.stringify(managers, null, 2));
    
    // Check fields with manager_id
    const [fields] = await sequelize.query(
      `SELECT field_id, field_name, manager_id FROM fields LIMIT 10`
    );
    
    console.log(`\n📍 Sample fields with manager assignment:`);
    console.log(JSON.stringify(fields, null, 2));
    
    process.exit(0);
  } catch (error) {
    console.error('❌ Error:', error);
    process.exit(1);
  }
};

checkManagers();
