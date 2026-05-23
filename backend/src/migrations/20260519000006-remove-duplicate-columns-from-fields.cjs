'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface) {
    const tableInfo = await queryInterface.describeTable('fields');
    const indexes = await queryInterface.showIndex('fields');

    const hasSportTypeIdx = indexes.some((idx) => idx.name === 'idx_sport_type_id_fields');
    if (hasSportTypeIdx) {
      await queryInterface.removeIndex('fields', 'idx_sport_type_id_fields');
    }

    if (tableInfo.id) {
      await queryInterface.removeColumn('fields', 'id');
    }
    if (tableInfo.name) {
      await queryInterface.removeColumn('fields', 'name');
    }
    if (tableInfo.sport_type_id) {
      await queryInterface.removeColumn('fields', 'sport_type_id');
    }
    if (tableInfo.address) {
      await queryInterface.removeColumn('fields', 'address');
    }
  },

  async down(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable('fields');

    if (!tableInfo.id) {
      await queryInterface.addColumn('fields', 'id', {
        type: Sequelize.INTEGER,
        allowNull: true,
      });
    }
    if (!tableInfo.name) {
      await queryInterface.addColumn('fields', 'name', {
        type: Sequelize.STRING(100),
        allowNull: true,
      });
    }
    if (!tableInfo.sport_type_id) {
      await queryInterface.addColumn('fields', 'sport_type_id', {
        type: Sequelize.INTEGER,
        allowNull: true,
      });
    }
    if (!tableInfo.address) {
      await queryInterface.addColumn('fields', 'address', {
        type: Sequelize.STRING(255),
        allowNull: true,
      });
    }
  },
};

