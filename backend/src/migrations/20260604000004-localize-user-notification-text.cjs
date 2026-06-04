'use strict';

const tableName = 'notifications';

module.exports = {
  async up(queryInterface) {
    await queryInterface.sequelize.query(
      `UPDATE ${tableName}
       SET
         title = CASE
           WHEN title = 'Thong bao he thong' THEN 'Thông báo hệ thống'
           WHEN title = 'Dat san thanh cong' THEN 'Đặt sân thành công'
           WHEN title = 'Sunrise Stadium da phan hoi' THEN 'Sunrise Stadium đã phản hồi'
           ELSE title
         END,
         subtitle = CASE
           WHEN subtitle = 'Tinh nang hop thu da san sang' THEN 'Tính năng hộp thư đã sẵn sàng'
           WHEN subtitle = 'Ban co 1 tin nhan moi tu chu san.' THEN 'Bạn có 1 tin nhắn mới từ chủ sân.'
           WHEN subtitle = 'Sunrise Stadium - 18:00 hom nay' THEN 'Sunrise Stadium - 18:00 hôm nay'
           ELSE subtitle
         END,
         content = CASE
           WHEN content = 'Ban co the nhan tin truc tiep voi chu san ngay trong app.' THEN 'Bạn có thể nhắn tin trực tiếp với chủ sân ngay trong app.'
           WHEN content = 'Nhan de mo hoi thoai voi chu san.' THEN 'Nhấn để mở hội thoại với chủ sân.'
           WHEN content = 'Don dat san cua ban da duoc xac nhan.' THEN 'Đơn đặt sân của bạn đã được xác nhận.'
           ELSE content
         END
       WHERE type IN ('system_notice', 'system', 'message', 'booking_success')`,
    );

    await queryInterface.sequelize.query(
      `UPDATE ${tableName}
       SET subtitle = REPLACE(subtitle, 'Ma dat san', 'Mã đặt sân')
       WHERE type = 'booking_success'
         AND subtitle LIKE 'Ma dat san%'`,
    );

    await queryInterface.sequelize.query(
      `UPDATE ${tableName}
       SET content = REPLACE(REPLACE(content, 'San: ', 'Sân: '), '. Thoi gian: ', '. Thời gian: ')
       WHERE type = 'booking_success'
         AND (content LIKE 'San:%' OR content LIKE '%. Thoi gian:%')`,
    );
  },

  async down() {
    // Text-only localization migration; keep Vietnamese display text.
  },
};
