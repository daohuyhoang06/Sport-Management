'use strict';

module.exports = {
  async up(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable('payments');
    if (!tableInfo.booking_ids_json) {
      await queryInterface.addColumn('payments', 'booking_ids_json', {
        type: Sequelize.TEXT,
        allowNull: true,
      });
    }
  },

  async down(queryInterface) {
    const tableInfo = await queryInterface.describeTable('payments');
    if (tableInfo.booking_ids_json) {
      await queryInterface.removeColumn('payments', 'booking_ids_json');
    }
  },
};
