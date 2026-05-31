"use strict";

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable("person");

    if (!tableInfo.membership_level) {
      await queryInterface.addColumn("person", "membership_level", {
        type: Sequelize.STRING(20),
        allowNull: false,
        defaultValue: "\u0110\u1ed3ng",
      });
    }

    if (!tableInfo.avatar_url) {
      await queryInterface.addColumn("person", "avatar_url", {
        type: Sequelize.STRING(255),
        allowNull: true,
      });
    }
  },

  async down(queryInterface) {
    const tableInfo = await queryInterface.describeTable("person");

    if (tableInfo.avatar_url) {
      await queryInterface.removeColumn("person", "avatar_url");
    }

    if (tableInfo.membership_level) {
      await queryInterface.removeColumn("person", "membership_level");
    }
  },
};
