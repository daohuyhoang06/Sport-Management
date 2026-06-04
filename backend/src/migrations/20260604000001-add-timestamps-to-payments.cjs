'use strict';

const tableName = 'payments';

module.exports = {
  async up(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable(tableName);

    if (!tableInfo.created_at) {
      await queryInterface.addColumn(tableName, 'created_at', {
        type: Sequelize.DATE,
        allowNull: false,
        defaultValue: Sequelize.literal('CURRENT_TIMESTAMP'),
      });
    }

    if (!tableInfo.updated_at) {
      await queryInterface.addColumn(tableName, 'updated_at', {
        type: Sequelize.DATE,
        allowNull: false,
        defaultValue: Sequelize.literal('CURRENT_TIMESTAMP'),
      });
    }
  },

  async down(queryInterface) {
    const tableInfo = await queryInterface.describeTable(tableName);

    if (tableInfo.updated_at) {
      await queryInterface.removeColumn(tableName, 'updated_at');
    }

    if (tableInfo.created_at) {
      await queryInterface.removeColumn(tableName, 'created_at');
    }
  },
};
