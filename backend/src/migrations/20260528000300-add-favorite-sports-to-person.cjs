"use strict";

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable("person");

    if (!tableInfo.favorite_sport_ids) {
      await queryInterface.addColumn("person", "favorite_sport_ids", {
        type: Sequelize.TEXT,
        allowNull: true,
      });
    }
  },

  async down(queryInterface) {
    const tableInfo = await queryInterface.describeTable("person");

    if (tableInfo.favorite_sport_ids) {
      await queryInterface.removeColumn("person", "favorite_sport_ids");
    }
  },
};

