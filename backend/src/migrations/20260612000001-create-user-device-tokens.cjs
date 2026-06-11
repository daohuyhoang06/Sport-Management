"use strict";

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    await queryInterface.createTable("user_device_tokens", {
      id: {
        type: Sequelize.INTEGER,
        primaryKey: true,
        autoIncrement: true,
        allowNull: false,
      },
      user_id: {
        type: Sequelize.INTEGER,
        allowNull: false,
      },
      fcm_token: {
        type: Sequelize.STRING(512),
        allowNull: false,
        unique: true,
      },
      platform: {
        type: Sequelize.STRING(32),
        allowNull: false,
        defaultValue: "android",
      },
      app_version: {
        type: Sequelize.STRING(64),
        allowNull: true,
      },
      device_id: {
        type: Sequelize.STRING(128),
        allowNull: true,
      },
      last_seen_at: {
        type: Sequelize.DATE,
        allowNull: false,
        defaultValue: Sequelize.literal("CURRENT_TIMESTAMP"),
      },
      created_at: {
        type: Sequelize.DATE,
        allowNull: false,
        defaultValue: Sequelize.literal("CURRENT_TIMESTAMP"),
      },
      updated_at: {
        type: Sequelize.DATE,
        allowNull: false,
        defaultValue: Sequelize.literal("CURRENT_TIMESTAMP"),
      },
    });

    await queryInterface.addIndex("user_device_tokens", ["user_id"], {
      name: "user_device_tokens_user_id_idx",
    });
  },

  async down(queryInterface) {
    await queryInterface.dropTable("user_device_tokens");
  },
};
