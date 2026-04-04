'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    // Add rental_price column to fields table
    await queryInterface.addColumn('fields', 'rental_price', {
      type: Sequelize.DECIMAL(10, 2),
      allowNull: true,
      defaultValue: null,
      comment: 'Rental price per hour in VND'
    });

    // Update existing fields with random prices between 100,000 and 300,000
    const [fields] = await queryInterface.sequelize.query(
      'SELECT field_id FROM fields'
    );

    for (const field of fields) {
      // Generate random price between 100,000 and 300,000
      const randomPrice = Math.floor(Math.random() * (300000 - 100000 + 1)) + 100000;
      await queryInterface.sequelize.query(
        `UPDATE fields SET rental_price = ${randomPrice} WHERE field_id = ${field.field_id}`
      );
    }
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.removeColumn('fields', 'rental_price');
  }
};
