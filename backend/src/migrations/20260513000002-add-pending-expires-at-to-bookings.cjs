'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable('bookings');
    if (!tableInfo.pending_expires_at) {
      await queryInterface.addColumn('bookings', 'pending_expires_at', {
        type: Sequelize.DATE,
        allowNull: true,
        after: 'status',
      });
    }

    const indexes = await queryInterface.showIndex('bookings');
    const hasIndex = indexes.some((idx) => idx.name === 'bookings_status_pending_expires_idx');
    if (!hasIndex) {
      await queryInterface.addIndex('bookings', ['status', 'pending_expires_at'], {
        name: 'bookings_status_pending_expires_idx',
      });
    }
  },

  async down(queryInterface) {
    const indexes = await queryInterface.showIndex('bookings');
    const hasIndex = indexes.some((idx) => idx.name === 'bookings_status_pending_expires_idx');
    if (hasIndex) {
      await queryInterface.removeIndex('bookings', 'bookings_status_pending_expires_idx');
    }

    const tableInfo = await queryInterface.describeTable('bookings');
    if (tableInfo.pending_expires_at) {
      await queryInterface.removeColumn('bookings', 'pending_expires_at');
    }
  },
};
