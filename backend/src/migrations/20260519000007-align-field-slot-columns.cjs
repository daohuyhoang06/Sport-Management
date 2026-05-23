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

    const refreshed = await queryInterface.describeTable('fields');
    if (!refreshed.slot_price) {
      await queryInterface.addColumn('fields', 'slot_price', {
        type: Sequelize.DECIMAL(10, 2),
        allowNull: true,
        defaultValue: null,
      });
    }

    const latest = await queryInterface.describeTable('fields');
    if (latest.rental_price) {
      await queryInterface.sequelize.query(
        'UPDATE fields SET slot_price = rental_price WHERE slot_price IS NULL AND rental_price IS NOT NULL',
      );
      await queryInterface.removeColumn('fields', 'rental_price');
    }
  },

  async down(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable('fields');

    if (!tableInfo.rental_price) {
      await queryInterface.addColumn('fields', 'rental_price', {
        type: Sequelize.DECIMAL(10, 2),
        allowNull: true,
        defaultValue: null,
      });
    }

    const refreshed = await queryInterface.describeTable('fields');
    if (refreshed.slot_price) {
      await queryInterface.sequelize.query(
        'UPDATE fields SET rental_price = slot_price WHERE rental_price IS NULL AND slot_price IS NOT NULL',
      );
    }

    const latest = await queryInterface.describeTable('fields');
    if (latest.slot_minutes && !latest.min_booking_minutes) {
      await queryInterface.renameColumn('fields', 'slot_minutes', 'min_booking_minutes');
    }
  },
};
