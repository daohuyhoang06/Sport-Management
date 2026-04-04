'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    // Add images column if not exists
    const tableInfo = await queryInterface.describeTable('reviews');
    
    if (!tableInfo.images) {
      await queryInterface.addColumn('reviews', 'images', {
        type: Sequelize.JSON,
        allowNull: true,
        after: 'comment'
      });
    }
    
    if (!tableInfo.updated_at) {
      await queryInterface.addColumn('reviews', 'updated_at', {
        type: Sequelize.DATE,
        allowNull: false,
        defaultValue: Sequelize.literal('CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP'),
        after: 'created_at'
      });
    }
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.removeColumn('reviews', 'images');
    await queryInterface.removeColumn('reviews', 'updated_at');
  }
};