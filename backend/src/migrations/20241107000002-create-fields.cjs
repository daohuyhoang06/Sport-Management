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
      status: {
        type: Sequelize.STRING(45),
        allowNull: true,
        defaultValue: 'active'
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
