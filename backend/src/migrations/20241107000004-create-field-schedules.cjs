'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    await queryInterface.createTable('field_schedules', {
      schedule_id: {
        type: Sequelize.INTEGER,
        primaryKey: true,
        autoIncrement: true,
        allowNull: false
      },
      field_id: {
        type: Sequelize.INTEGER,
        allowNull: false,
        references: {
          model: 'fields',
          key: 'field_id'
        },
        onUpdate: 'CASCADE',
        onDelete: 'CASCADE'
      },
      manager_id: {
        type: Sequelize.INTEGER,
        allowNull: true,
        references: {
          model: 'person',
          key: 'person_id'
        },
        onUpdate: 'CASCADE',
        onDelete: 'SET NULL'
      },
      start_time: {
        type: Sequelize.DATE,
        allowNull: false
      },
      end_time: {
        type: Sequelize.DATE,
        allowNull: false
      },
      is_available: {
        type: Sequelize.BOOLEAN,
        allowNull: false,
        defaultValue: true
      }
    });

    // Add indexes
    await queryInterface.addIndex('field_schedules', ['field_id']);
    await queryInterface.addIndex('field_schedules', ['manager_id']);
    await queryInterface.addIndex('field_schedules', ['start_time', 'end_time']);
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.dropTable('field_schedules');
  }
};
