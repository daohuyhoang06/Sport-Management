import sequelize from './src/config/database.js';
import Field from './src/models/Field.js';
import FieldCourt from './src/models/FieldCourt.js';

async function seedData() {
  try {
    await sequelize.authenticate();
    console.log('✅ Kết nối database thành công');

    // 1. Find or create a manager using raw queries to avoid model mismatch
    const [managers] = await sequelize.query("SELECT * FROM person WHERE role = 'manager' LIMIT 1");
    let managerId;
    
    if (managers.length === 0) {
      console.log('⚠️ Không tìm thấy manager, đang tạo một manager mặc định...');
      // find which columns exist in person
      const [columns] = await sequelize.query("SHOW COLUMNS FROM person");
      const colNames = columns.map(c => c.Field);
      
      const nameCol = colNames.includes('full_name') ? 'full_name' : 'name';
      
      const [result] = await sequelize.query(
        `INSERT INTO person (${nameCol}, username, email, password, phone, role, status) VALUES (?, ?, ?, ?, ?, ?, ?)`,
        {
          replacements: ['Default Manager', 'manager_admin2', 'manager2@test.com', 'password123', '0901234567', 'manager', 'active']
        }
      );
      managerId = result;
      console.log(`✅ Đã tạo manager với ID: ${managerId}`);
    } else {
      managerId = managers[0].person_id;
      console.log(`✅ Tìm thấy manager với ID: ${managerId}`);
    }

    // 2. Update manager_id for all existing fields
    const fields = await Field.findAll();
    console.log(`📊 Tìm thấy ${fields.length} sân trong database`);
    
    let updateCount = 0;
    for (const field of fields) {
      if (field.manager_id !== managerId) {
        field.manager_id = managerId;
        await field.save();
        updateCount++;
      }
    }
    console.log(`✅ Đã cập nhật manager_id cho ${updateCount} sân`);

    // 3. Create field_courts for each field
    let courtsCreatedCount = 0;
    for (const field of fields) {
      // Check if courts already exist
      const existingCourts = await FieldCourt.findAll({ where: { field_id: field.field_id } });
      if (existingCourts.length === 0) {
        // Create 3 courts for each field
        const courtsToCreate = [
          { field_id: field.field_id, court_code: 'S1', court_name: 'Sân 1', sort_order: 1 },
          { field_id: field.field_id, court_code: 'S2', court_name: 'Sân 2', sort_order: 2 },
          { field_id: field.field_id, court_code: 'S3', court_name: 'Sân 3', sort_order: 3 },
        ];
        await FieldCourt.bulkCreate(courtsToCreate);
        courtsCreatedCount += 3;
      }
    }
    console.log(`✅ Đã tạo ${courtsCreatedCount} sân con (courts)`);

    console.log('🎉 Quá trình cập nhật dữ liệu hoàn tất!');
    process.exit(0);
  } catch (error) {
    console.error('❌ Lỗi:', error.message);
    console.error(error);
    process.exit(1);
  }
}

seedData();
