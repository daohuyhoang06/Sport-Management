'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tables = await queryInterface.showAllTables();
    const hasTable = tables
      .map((t) => (typeof t === "string" ? t : t.tableName || t.table_name))
      .includes("revenue_weekly");

    if (!hasTable) {
      await queryInterface.createTable('revenue_weekly', {
        revenue_weekly_id: {
          type: Sequelize.INTEGER,
          primaryKey: true,
          autoIncrement: true,
          allowNull: false
        },
        week: {
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

    const columns = await queryInterface.describeTable('revenue_weekly');
    const indexes = await queryInterface.showIndex('revenue_weekly');
    const hasWeekYearIndex = indexes.some((idx) => {
      const attrs = (idx.fields || []).map((field) => field.attribute);
      return attrs.includes('week') && attrs.includes('year');
    });

    if (columns.week && columns.year && !hasWeekYearIndex) {
      await queryInterface.addIndex('revenue_weekly', ['week', 'year'], {
        unique: true
      });
    }
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.dropTable('revenue_weekly');
  }
};
