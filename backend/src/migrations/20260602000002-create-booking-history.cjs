'use strict';

module.exports = {
  up: async (queryInterface, Sequelize) => {
    await queryInterface.createTable('booking_history', {
      history_id: {
        type: Sequelize.INTEGER,
        primaryKey: true,
        autoIncrement: true,
        allowNull: false,
      },
      booking_id: {
        type: Sequelize.INTEGER,
        allowNull: false,
        references: { model: 'bookings', key: 'booking_id' },
        onDelete: 'CASCADE',
      },
      action:      { type: Sequelize.STRING(100), allowNull: false },
      from_status: { type: Sequelize.STRING(50), allowNull: true },
      to_status:   { type: Sequelize.STRING(50), allowNull: true },
      note:        { type: Sequelize.TEXT, allowNull: true },
      author:      { type: Sequelize.STRING(100), allowNull: true },
      created_at: {
        type: Sequelize.DATE,
        defaultValue: Sequelize.literal('CURRENT_TIMESTAMP'),
      },
    });

    await queryInterface.addIndex('booking_history', ['booking_id']);
  },

  down: async (queryInterface) => {
    await queryInterface.dropTable('booking_history');
  },
};
