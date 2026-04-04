'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    // Add fieldId column to person table
    await queryInterface.addColumn('person', 'fieldId', {
      type: Sequelize.INTEGER,
      allowNull: true,
      references: {
        model: 'fields',
        key: 'field_id'
      },
      onUpdate: 'CASCADE',
      onDelete: 'SET NULL'
    });

    // Add index for fieldId
    await queryInterface.addIndex('person', ['fieldId']);
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.removeColumn('person', 'fieldId');
  }
};
