"use strict";

const tableName = "match_requests";

module.exports = {
  async up(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable(tableName);

    if (!tableInfo.level) {
      await queryInterface.addColumn(tableName, "level", {
        type: Sequelize.STRING(20),
        allowNull: true,
      });
    }
  },

  async down(queryInterface) {
    const tableInfo = await queryInterface.describeTable(tableName);
    if (tableInfo.level) {
      await queryInterface.removeColumn(tableName, "level");
    }
  },
};
