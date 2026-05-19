'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable('fields');

    if (tableInfo.min_booking_minutes && !tableInfo.slot_minutes) {
      await queryInterface.renameColumn('fields', 'min_booking_minutes', 'slot_minutes');
    } else if (!tableInfo.slot_minutes) {
      await queryInterface.addColumn('fields', 'slot_minutes', {
        type: Sequelize.INTEGER,
        allowNull: false,
        defaultValue: 60,
      });
    }

    if (!tableInfo.slot_price) {
      await queryInterface.addColumn('fields', 'slot_price', {
        type: Sequelize.DECIMAL(10, 2),
        allowNull: true,
        defaultValue: null,
      });
    }

    const refreshed = await queryInterface.describeTable('fields');
    if (refreshed.rental_price && refreshed.slot_price) {
      await queryInterface.sequelize.query(
        'UPDATE fields SET slot_price = rental_price WHERE slot_price IS NULL AND rental_price IS NOT NULL',
      );
      await queryInterface.removeColumn('fields', 'rental_price');
    }
  },

  async down(queryInterface) {
    const tableInfo = await queryInterface.describeTable('fields');

    if (tableInfo.slot_minutes && !tableInfo.min_booking_minutes) {
      await queryInterface.renameColumn('fields', 'slot_minutes', 'min_booking_minutes');
    }

    if (tableInfo.slot_price) {
      await queryInterface.removeColumn('fields', 'slot_price');
    }

    const refreshed = await queryInterface.describeTable('fields');
    if (refreshed.min_booking_minutes) {
      await queryInterface.removeColumn('fields', 'min_booking_minutes');
    }
  },
};
