'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
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
        references: {
          model: 'fields',
          key: 'field_id',
        },
        onUpdate: 'CASCADE',
        onDelete: 'CASCADE',
      },
      court_id: {
        type: Sequelize.INTEGER,
        allowNull: true,
        references: {
          model: 'field_courts',
          key: 'court_id',
        },
        onUpdate: 'CASCADE',
        onDelete: 'SET NULL',
      },
      block_date: {
        type: Sequelize.DATEONLY,
        allowNull: false,
      },
      start_time: {
        type: Sequelize.TIME,
        allowNull: false,
      },
      end_time: {
        type: Sequelize.TIME,
        allowNull: false,
      },
      reason: {
        type: Sequelize.TEXT,
        allowNull: true,
      },
      block_type: {
        type: Sequelize.STRING(50),
        allowNull: false,
        defaultValue: 'maintenance',
      },
      created_at: {
        type: Sequelize.DATE,
        allowNull: false,
        defaultValue: Sequelize.literal('CURRENT_TIMESTAMP'),
      },
      updated_at: {
        type: Sequelize.DATE,
        allowNull: false,
        defaultValue: Sequelize.literal('CURRENT_TIMESTAMP'),
      },
    });

    await queryInterface.addIndex('field_blocked_slots', ['court_id'], {
      name: 'idx_field_blocked_slots_court_id',
    });
    await queryInterface.addIndex('field_blocked_slots', ['block_date'], {
      name: 'idx_field_blocked_slots_block_date',
    });
    await queryInterface.addIndex('field_blocked_slots', ['field_id', 'block_date'], {
      name: 'idx_field_blocked_slots_field_date',
    });
  },

  async down(queryInterface) {
    await queryInterface.dropTable('field_blocked_slots');
  },
};
