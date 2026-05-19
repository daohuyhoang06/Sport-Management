'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable('fields');

    if (!tableInfo.avatar_image_url) {
      await queryInterface.addColumn('fields', 'avatar_image_url', {
        type: Sequelize.STRING(255),
        allowNull: true,
        defaultValue: null,
      });
    }

    if (!tableInfo.card_image_url) {
      await queryInterface.addColumn('fields', 'card_image_url', {
        type: Sequelize.STRING(255),
        allowNull: true,
        defaultValue: null,
      });
    }
  },

  async down(queryInterface) {
    const tableInfo = await queryInterface.describeTable('fields');

    if (tableInfo.card_image_url) {
      await queryInterface.removeColumn('fields', 'card_image_url');
    }

    if (tableInfo.avatar_image_url) {
      await queryInterface.removeColumn('fields', 'avatar_image_url');
    }
  },
};
