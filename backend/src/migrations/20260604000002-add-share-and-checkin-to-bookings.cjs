'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable('bookings');

    if (!tableInfo.share_token) {
      await queryInterface.addColumn('bookings', 'share_token', {
        type: Sequelize.STRING(120),
        allowNull: true,
        after: 'price',
      });
    }

    if (!tableInfo.checkin_code) {
      await queryInterface.addColumn('bookings', 'checkin_code', {
        type: Sequelize.STRING(32),
        allowNull: true,
        after: 'share_token',
      });
    }

    if (!tableInfo.checked_in_at) {
      await queryInterface.addColumn('bookings', 'checked_in_at', {
        type: Sequelize.DATE,
        allowNull: true,
        after: 'checkin_code',
      });
    }

    const indexes = await queryInterface.showIndex('bookings');
    const hasIndex = (name) => indexes.some((item) => item.name === name);

    if (!hasIndex('bookings_share_token_unique')) {
      await queryInterface.addIndex('bookings', ['share_token'], {
        name: 'bookings_share_token_unique',
        unique: true,
      });
    }

    if (!hasIndex('bookings_checkin_code_unique')) {
      await queryInterface.addIndex('bookings', ['checkin_code'], {
        name: 'bookings_checkin_code_unique',
        unique: true,
      });
    }
  },

  async down(queryInterface) {
    const indexes = await queryInterface.showIndex('bookings');
    const hasIndex = (name) => indexes.some((item) => item.name === name);

    if (hasIndex('bookings_checkin_code_unique')) {
      await queryInterface.removeIndex('bookings', 'bookings_checkin_code_unique');
    }

    if (hasIndex('bookings_share_token_unique')) {
      await queryInterface.removeIndex('bookings', 'bookings_share_token_unique');
    }

    const tableInfo = await queryInterface.describeTable('bookings');

    if (tableInfo.checked_in_at) {
      await queryInterface.removeColumn('bookings', 'checked_in_at');
    }

    if (tableInfo.checkin_code) {
      await queryInterface.removeColumn('bookings', 'checkin_code');
    }

    if (tableInfo.share_token) {
      await queryInterface.removeColumn('bookings', 'share_token');
    }
  },
};
