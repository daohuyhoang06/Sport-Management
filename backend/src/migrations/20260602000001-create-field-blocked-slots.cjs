'use strict';

module.exports = {
  up: async (queryInterface, Sequelize) => {
    await queryInterface.createTable('field_blocked_slots', {
      slot_id: {
        type: Sequelize.INTEGER,
        primaryKey: true,
        autoIncrement: true,
        allowNull: false,
      },
      field_id: {
        type: Sequelize.INTEGER,
        allowNull: false,
        references: { model: 'fields', key: 'field_id' },
        onDelete: 'CASCADE',
      },
      court_id: {
        type: Sequelize.INTEGER,
        allowNull: true,
        references: { model: 'field_courts', key: 'court_id' },
        onDelete: 'SET NULL',
      },
      block_date: { type: Sequelize.DATEONLY, allowNull: false },
      start_time: { type: Sequelize.TIME, allowNull: false },
      end_time:   { type: Sequelize.TIME, allowNull: false },
      reason:     { type: Sequelize.STRING(255), allowNull: true },
      block_type: {
        type: Sequelize.ENUM('maintenance', 'event', 'other'),
        defaultValue: 'maintenance',
      },
      created_at: {
        type: Sequelize.DATE,
        defaultValue: Sequelize.literal('CURRENT_TIMESTAMP'),
      },
    });

    await queryInterface.addIndex('field_blocked_slots', ['field_id']);
    await queryInterface.addIndex('field_blocked_slots', ['block_date']);
  },

  down: async (queryInterface) => {
    await queryInterface.dropTable('field_blocked_slots');
  },
};
