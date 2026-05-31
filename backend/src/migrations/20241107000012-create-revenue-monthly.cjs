'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tables = await queryInterface.showAllTables();
    const hasTable = tables
      .map((t) => (typeof t === "string" ? t : t.tableName || t.table_name))
      .includes("revenue_monthly");

    if (!hasTable) {
      await queryInterface.createTable('revenue_monthly', {
        revenue_monthly_id: {
          type: Sequelize.INTEGER,
          primaryKey: true,
          autoIncrement: true,
          allowNull: false
        },
        month: {
          type: Sequelize.INTEGER,
          allowNull: false
        },
        year: {
          type: Sequelize.INTEGER,
          allowNull: false
        },
        total_amount: {
          type: Sequelize.DECIMAL(10, 2),
          allowNull: false,
          defaultValue: 0
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

    const columns = await queryInterface.describeTable('revenue_monthly');
    const indexes = await queryInterface.showIndex('revenue_monthly');
    const hasMonthYearIndex = indexes.some((idx) => {
      const attrs = (idx.fields || []).map((field) => field.attribute);
      return attrs.includes('month') && attrs.includes('year');
    });

    if (columns.month && columns.year && !hasMonthYearIndex) {
      await queryInterface.addIndex('revenue_monthly', ['month', 'year'], {
        unique: true
      });
    }
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.dropTable('revenue_monthly');
  }
};
