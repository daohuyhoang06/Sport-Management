import sequelize from './src/config/database.js';

const seedReviews = async () => {
  try {
    console.log('🌱 Starting to seed reviews...');

    // Sample comments for reviews
    const comments = [
      'Sân đẹp, cỏ xanh tốt, tiện nghi đầy đủ. Tôi rất hài lòng!',
      'Chất lượng tốt, giá cả hợp lý. Sẽ quay lại lần sau.',
      'Sân rộng rãi, có chỗ đỗ xe tiện lợi. Nhân viên thân thiện.',
      'Mặt sân phẳng, cỏ được chăm sóc tốt. Phòng thay đồ sạch sẽ.',
      'Vị trí thuận tiện, dễ tìm. Có căng tin phục vụ đồ uống.',
      'Sân bóng chất lượng cao, ánh sáng tốt vào buổi tối.',
      'Giá thuê hợp lý, dịch vụ chu đáo. Rất đáng để trải nghiệm.',
      'Không gian thoáng mát, cơ sở vật chất hiện đại.',
      'Sân được bảo trì thường xuyên, luôn trong tình trạng tốt.',
      'Đội ngũ nhân viên nhiệt tình, hỗ trợ tận tình.',
      'Sân bóng đạt tiêu chuẩn, phù hợp cho các trận đấu nghiêm túc.',
      'Có wifi miễn phí, camera an ninh đầy đủ. An toàn và tiện lợi.',
      'Phòng thay đồ rộng rãi, có điều hòa mát mẻ.',
      'Sân có mái che, không lo trời mưa hay nắng gắt.',
      'Giá cả cạnh tranh, chất lượng vượt mong đợi.',
      'Booking online dễ dàng, thanh toán linh hoạt.',
      'Sân sạch sẽ, được vệ sinh thường xuyên.',
      'Có nhiều khung giờ linh hoạt, dễ dàng đặt lịch.',
      'Manager nhiệt tình, hỗ trợ khách hàng tốt.',
      'Tổng thể rất hài lòng, sẽ giới thiệu cho bạn bè.',
    ];

    const customerNames = [
      'Nguyễn Văn An', 'Trần Thị Bình', 'Lê Minh Cường', 'Phạm Thu Dung',
      'Hoàng Văn Em', 'Đặng Thị Phượng', 'Vũ Quang Hùng', 'Bùi Thị Hà',
      'Ngô Minh Khải', 'Đinh Thị Lan', 'Trương Văn Minh', 'Phan Thị Nga',
      'Lý Quang Oai', 'Đỗ Thị Phương', 'Mai Văn Quân', 'Tô Thị Rượu',
      'Dương Minh Sơn', 'Cao Thị Tâm', 'Hồ Văn Uy', 'Võ Thị Vân',
      'Châu Minh Xuân', 'Lưu Thị Yến', 'Huỳnh Văn Zên', 'Tạ Thị Ánh',
      'Lâm Minh Bảo', 'Ninh Thị Chi', 'Quách Văn Đức', 'La Thị Ê',
    ];

    // Get all fields
    const [fields] = await sequelize.query('SELECT field_id FROM fields WHERE status = "active"');
    console.log(`📊 Found ${fields.length} active fields`);

    // Get all customers (users)
    const [customers] = await sequelize.query('SELECT person_id, person_name FROM person LIMIT 20');
    console.log(`👥 Found ${customers.length} people in database`);

    if (customers.length === 0) {
      console.log('⚠️  No people found in database. Cannot create reviews.');
      return;
    }

    let totalReviews = 0;

    // For each field, create 3-8 random reviews
    for (const field of fields) {
      const numReviews = Math.floor(Math.random() * 6) + 3; // 3-8 reviews per field
      
      for (let i = 0; i < numReviews; i++) {
        const customer = customers[Math.floor(Math.random() * customers.length)];
        const rating = Math.floor(Math.random() * 2) + 4; // 4-5 stars mostly
        const comment = comments[Math.floor(Math.random() * comments.length)];
        const daysAgo = Math.floor(Math.random() * 90); // Reviews from last 90 days
        const createdAt = new Date();
        createdAt.setDate(createdAt.getDate() - daysAgo);

        await sequelize.query(
          `INSERT INTO reviews (customer_id, field_id, rating, comment, images, created_at, updated_at)
           VALUES (?, ?, ?, ?, ?, ?, ?)`,
          {
            replacements: [
              customer.person_id,
              field.field_id,
              rating,
              comment,
              JSON.stringify([]), // Empty images array
              createdAt,
              createdAt
            ]
          }
        );

        totalReviews++;
      }
    }

    console.log(`✅ Successfully seeded ${totalReviews} reviews for ${fields.length} fields!`);
    console.log(`📈 Average: ${(totalReviews / fields.length).toFixed(1)} reviews per field`);
    
  } catch (error) {
    console.error('❌ Error seeding reviews:', error);
  } finally {
    await sequelize.close();
  }
};

seedReviews();
