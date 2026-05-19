'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable('fields');
    if (!tableInfo.slot_price) {
      await queryInterface.addColumn('fields', 'slot_price', {
        type: Sequelize.DECIMAL(10, 2),
        allowNull: true,
        defaultValue: null,
        comment: 'Slot price in VND'
      });
    }

    // Update existing fields with random prices between 100,000 and 300,000
    const [fields] = await queryInterface.sequelize.query(
      'SELECT field_id FROM fields'
    );

    for (const field of fields) {
      // Generate random price between 100,000 and 300,000
      const randomPrice = Math.floor(Math.random() * (300000 - 100000 + 1)) + 100000;
      await queryInterface.sequelize.query(
        `UPDATE fields SET slot_price = ${randomPrice} WHERE field_id = ${field.field_id} AND slot_price IS NULL`
      );
    }
  },

  async down(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable('fields');
    if (tableInfo.slot_price) {
      await queryInterface.removeColumn('fields', 'slot_price');
    }
  }
};
