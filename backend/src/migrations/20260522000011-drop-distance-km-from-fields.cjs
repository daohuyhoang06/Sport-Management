'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface) {
    const tableInfo = await queryInterface.describeTable('fields');

    if (tableInfo.distance_km) {
      await queryInterface.removeColumn('fields', 'distance_km');
    }
  },

  async down(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable('fields');

    if (!tableInfo.distance_km) {
      await queryInterface.addColumn('fields', 'distance_km', {
        type: Sequelize.DECIMAL(6, 2),
        allowNull: true,
        defaultValue: null,
      });
    }
  },
};
