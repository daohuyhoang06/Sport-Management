'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    await queryInterface.bulkInsert('reviews', [
      {
        field_id: 1,
        customer_id: 1,
        rating: 5,
        comment: 'Sân đẹp, cỏ mượt, dịch vụ tốt. Mình rất hài lòng khi đá ở đây!',
        images: JSON.stringify([]),
        created_at: new Date('2024-11-10'),
        updated_at: new Date('2024-11-10')
      },
      {
        field_id: 1,
        customer_id: 2,
        rating: 4,
        comment: 'Sân khá tốt, giá cả hợp lý. Tuy nhiên giờ cao điểm hơi đông.',
        images: JSON.stringify([]),
        created_at: new Date('2024-11-08'),
        updated_at: new Date('2024-11-08')
      },
      {
        field_id: 1,
        customer_id: 3,
        rating: 5,
        comment: 'Chất lượng sân rất tốt, nhân viên thân thiện, giá cả phải chăng. Sẽ quay lại!',
        images: JSON.stringify([]),
        created_at: new Date('2024-11-05'),
        updated_at: new Date('2024-11-05')
      },
      {
        field_id: 2,
        customer_id: 1,
        rating: 4,
        comment: 'Sân rộng rãi, thoáng mát. Bãi đỗ xe tiện lợi.',
        images: JSON.stringify([]),
        created_at: new Date('2024-11-07'),
        updated_at: new Date('2024-11-07')
      },
      {
        field_id: 2,
        customer_id: 4,
        rating: 3,
        comment: 'Sân ổn nhưng cỏ hơi thưa một số chỗ. Mong cải thiện thêm.',
        images: JSON.stringify([]),
        created_at: new Date('2024-11-03'),
        updated_at: new Date('2024-11-03')
      },
      {
        field_id: 3,
        customer_id: 2,
        rating: 5,
        comment: 'Sân tuyệt vời! Cỏ mới, chiếu sáng tốt, căng tin đầy đủ. Highly recommended!',
        images: JSON.stringify([]),
        created_at: new Date('2024-11-12'),
        updated_at: new Date('2024-11-12')
      }
    ], {});
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.bulkDelete('reviews', null, {});
  }
};