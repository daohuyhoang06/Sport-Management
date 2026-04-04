import sequelize from './src/config/database.js';

/**
 * Create sample field schedules for testing
 * This generates default time slots for all fields for the next 7 days
 */
async function seedFieldSchedules() {
  try {
    console.log('🔄 Kết nối database...');
    await sequelize.authenticate();
    console.log('✅ Kết nối thành công!');

    // Get all active fields
    const [fields] = await sequelize.query('SELECT field_id FROM fields WHERE status = "active"');
    console.log(`📊 Tìm thấy ${fields.length} sân bóng`);

    // Define default time slots
    const timeSlots = [
      { start: 6, end: 9, label: 'Ca sáng sớm' },
      { start: 9, end: 12, label: 'Ca sáng' },
      { start: 12, end: 14, label: 'Ca trưa' },
      { start: 14, end: 17, label: 'Ca chiều' },
      { start: 17, end: 19, label: 'Ca tối sớm' },
      { start: 19, end: 22, label: 'Ca tối' }
    ];

    let totalCreated = 0;
    const now = new Date();

    for (const field of fields) {
      console.log(`\n⚙️  Tạo lịch cho sân ${field.field_id}...`);
      
      // Create schedules for next 7 days
      for (let d = 0; d < 7; d++) {
        const day = new Date(now);
        day.setDate(now.getDate() + d);
        day.setHours(0, 0, 0, 0);

        for (const slot of timeSlots) {
          const startTime = new Date(day);
          startTime.setHours(slot.start, 0, 0, 0);

          const endTime = new Date(day);
          endTime.setHours(slot.end, 0, 0, 0);

          // Random availability (90% available, 10% unavailable for testing)
          const isAvailable = Math.random() > 0.1;

          await sequelize.query(
            `INSERT INTO field_schedules (field_id, start_time, end_time, is_available) 
             VALUES (?, ?, ?, ?)
             ON DUPLICATE KEY UPDATE is_available = ?`,
            { 
              replacements: [
                field.field_id, 
                startTime.toISOString().slice(0, 19).replace('T', ' '),
                endTime.toISOString().slice(0, 19).replace('T', ' '),
                isAvailable,
                isAvailable
              ] 
            }
          );

          totalCreated++;
        }
      }
      
      console.log(`  ✓ Đã tạo ${7 * timeSlots.length} khung giờ`);
    }

    console.log(`\n🎉 Hoàn tất! Tổng cộng tạo ${totalCreated} khung giờ cho ${fields.length} sân bóng.`);
    console.log(`📅 Khung giờ từ ${new Date().toLocaleDateString('vi-VN')} đến ${new Date(Date.now() + 7*24*60*60*1000).toLocaleDateString('vi-VN')}`);
    
    process.exit(0);
  } catch (error) {
    console.error('❌ Lỗi:', error.message);
    console.error(error);
    process.exit(1);
  }
}

seedFieldSchedules();
