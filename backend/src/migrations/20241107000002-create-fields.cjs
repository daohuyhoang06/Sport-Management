'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    await queryInterface.createTable('fields', {
      field_id: {
        type: Sequelize.INTEGER,
        primaryKey: true,
        autoIncrement: true,
        allowNull: false
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
      field_name: {
        type: Sequelize.STRING(50),
        allowNull: false
      },
      location: {
        type: Sequelize.STRING(100),
        allowNull: true
      },
      latitude: {
        type: Sequelize.DECIMAL(10, 7),
        allowNull: true
      },
      longitude: {
        type: Sequelize.DECIMAL(10, 7),
        allowNull: true
      },
      phone: {
        type: Sequelize.STRING(20),
        allowNull: true
      },
      open_time: {
        type: Sequelize.TIME,
        allowNull: true
      },
      close_time: {
        type: Sequelize.TIME,
        allowNull: true
      },
      slot_minutes: {
        type: Sequelize.INTEGER,
        allowNull: false,
        defaultValue: 60
      },
      slot_price: {
        type: Sequelize.DECIMAL(10, 2),
        allowNull: true,
        defaultValue: null
      },
      status: {
        type: Sequelize.STRING(45),
        allowNull: true,
        defaultValue: 'active'
      },
      created_at: {
        type: Sequelize.DATE,
        allowNull: false,
        defaultValue: Sequelize.literal('CURRENT_TIMESTAMP')
      },
      updated_at: {
        type: Sequelize.DATE,
        allowNull: false,
        defaultValue: Sequelize.literal('CURRENT_TIMESTAMP')
      }
    });

    // Add indexes
    await queryInterface.addIndex('fields', ['manager_id']);
    await queryInterface.addIndex('fields', ['status']);
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.dropTable('fields');
  }
};
