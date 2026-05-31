'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tables = await queryInterface.showAllTables();
    const hasTable = tables
      .map((t) => (typeof t === "string" ? t : t.tableName || t.table_name))
      .includes("password_resets");

    if (!hasTable) {
      await queryInterface.createTable('password_resets', {
        id: {
          type: Sequelize.INTEGER,
          primaryKey: true,
          autoIncrement: true,
          allowNull: false
        },
        person_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: 'person',
            key: 'person_id'
          },
          onUpdate: 'CASCADE',
          onDelete: 'CASCADE'
        },
        email: {
          type: Sequelize.STRING(45),
          allowNull: false
        },
        otp_code: {
          type: Sequelize.STRING(6),
          allowNull: false
        },
        expires_at: {
          type: Sequelize.DATE,
          allowNull: false
        },
        is_used: {
          type: Sequelize.BOOLEAN,
          defaultValue: false,
          allowNull: false
        },
        created_at: {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal('CURRENT_TIMESTAMP')
        }
      });
    }

    const columns = await queryInterface.describeTable('password_resets');
    const indexes = await queryInterface.showIndex('password_resets');
    const hasIndexOn = (attr) =>
      indexes.some((idx) => (idx.fields || []).some((field) => field.attribute === attr));

    if (columns.email && !hasIndexOn('email')) {
      await queryInterface.addIndex('password_resets', ['email']);
    }
    if (columns.otp_code && !hasIndexOn('otp_code')) {
      await queryInterface.addIndex('password_resets', ['otp_code']);
    }
    if (columns.person_id && !hasIndexOn('person_id')) {
      await queryInterface.addIndex('password_resets', ['person_id']);
    }
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.dropTable('password_resets');
  }
};
