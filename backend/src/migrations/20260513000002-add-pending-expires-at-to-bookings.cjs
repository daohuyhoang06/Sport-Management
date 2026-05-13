'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    await queryInterface.addColumn('bookings', 'pending_expires_at', {
      type: Sequelize.DATE,
      allowNull: true,
      after: 'status',
    });

    await queryInterface.addIndex('bookings', ['status', 'pending_expires_at'], {
      name: 'bookings_status_pending_expires_idx',
    });
  },

  async down(queryInterface) {
    await queryInterface.removeIndex('bookings', 'bookings_status_pending_expires_idx');
    await queryInterface.removeColumn('bookings', 'pending_expires_at');
  },
};

