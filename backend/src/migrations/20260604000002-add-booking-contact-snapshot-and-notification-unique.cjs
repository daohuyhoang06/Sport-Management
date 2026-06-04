'use strict';

const bookingsTable = 'bookings';
const notificationsTable = 'notifications';
const notificationUniqueIndex = 'notifications_user_type_booking_unique';

const hasIndex = async (queryInterface, Sequelize, tableName, indexName) => {
  const rows = await queryInterface.sequelize.query(
    `SELECT COUNT(*) AS count
     FROM information_schema.statistics
     WHERE table_schema = DATABASE()
       AND table_name = ?
       AND index_name = ?`,
    {
      replacements: [tableName, indexName],
      type: Sequelize.QueryTypes.SELECT,
    },
  );
  return Number(rows?.[0]?.count || 0) > 0;
};

module.exports = {
  async up(queryInterface, Sequelize) {
    const bookingInfo = await queryInterface.describeTable(bookingsTable);

    if (!bookingInfo.customer_name) {
      await queryInterface.addColumn(bookingsTable, 'customer_name', {
        type: Sequelize.STRING(255),
        allowNull: true,
      });
    }

    if (!bookingInfo.customer_phone) {
      await queryInterface.addColumn(bookingsTable, 'customer_phone', {
        type: Sequelize.STRING(30),
        allowNull: true,
      });
    }

    if (!(await hasIndex(queryInterface, Sequelize, notificationsTable, notificationUniqueIndex))) {
      await queryInterface.sequelize.query(
        `DELETE n1
         FROM ${notificationsTable} n1
         INNER JOIN ${notificationsTable} n2
           ON n1.user_id = n2.user_id
          AND n1.type = n2.type
          AND n1.booking_id = n2.booking_id
          AND n1.id > n2.id
         WHERE n1.type = 'booking_success'
           AND n1.booking_id IS NOT NULL`,
      );

      await queryInterface.addIndex(
        notificationsTable,
        ['user_id', 'type', 'booking_id'],
        {
          name: notificationUniqueIndex,
          unique: true,
        },
      );
    }
  },

  async down(queryInterface, Sequelize) {
    if (await hasIndex(queryInterface, Sequelize, notificationsTable, notificationUniqueIndex)) {
      await queryInterface.removeIndex(notificationsTable, notificationUniqueIndex);
    }

    const bookingInfo = await queryInterface.describeTable(bookingsTable);

    if (bookingInfo.customer_phone) {
      await queryInterface.removeColumn(bookingsTable, 'customer_phone');
    }

    if (bookingInfo.customer_name) {
      await queryInterface.removeColumn(bookingsTable, 'customer_name');
    }
  },
};
