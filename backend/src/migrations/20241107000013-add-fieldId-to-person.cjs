"use strict";

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    // Add field_id column to person table
    await queryInterface.addColumn("person", "field_id", {
      type: Sequelize.INTEGER,
      allowNull: true,
      references: {
        model: "fields",
        key: "field_id",
      },
      onUpdate: "CASCADE",
      onDelete: "SET NULL",
    });

    // Add index for field_id
    await queryInterface.addIndex("person", ["field_id"]);
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.removeColumn("person", "field_id");
  },
};
