'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tables = await queryInterface.showAllTables();
    const hasTable = tables
      .map((t) => (typeof t === "string" ? t : t.tableName || t.table_name))
      .includes("revenue_daily");

    if (!hasTable) {
      await queryInterface.createTable('revenue_daily', {
        revenue_daily_id: {
          type: Sequelize.INTEGER,
          primaryKey: true,
          autoIncrement: true,
          allowNull: false
        },
        total_amount: {
          type: Sequelize.DECIMAL(10, 2),
          allowNull: false,
          defaultValue: 0
        },
        revenue_date: {
          type: Sequelize.DATEONLY,
          allowNull: false
        },
        created_at: {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal('CURRENT_TIMESTAMP')
        },
        updated_at: {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal('CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP')
        }
      });
    }

    const columns = await queryInterface.describeTable('revenue_daily');
    const indexes = await queryInterface.showIndex('revenue_daily');
    const hasRevenueDateIndex = indexes.some((idx) =>
      (idx.fields || []).some((field) => field.attribute === 'revenue_date')
    );

    if (columns.revenue_date && !hasRevenueDateIndex) {
      await queryInterface.addIndex('revenue_daily', ['revenue_date'], {
        unique: true
      });
    }
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.dropTable('revenue_daily');
  }
};
