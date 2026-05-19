'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tables = await queryInterface.showAllTables();
    const hasTable = tables
      .map((t) => (typeof t === "string" ? t : t.tableName || t.table_name))
      .includes("field_courts");

    if (!hasTable) {
      await queryInterface.createTable('field_courts', {
        court_id: {
          type: Sequelize.INTEGER,
          primaryKey: true,
          autoIncrement: true,
          allowNull: false,
        },
        field_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: 'fields',
            key: 'field_id',
          },
          onUpdate: 'CASCADE',
          onDelete: 'CASCADE',
        },
        // External ID for BookingSubCourt.id (e.g., "court-1")
        court_code: {
          type: Sequelize.STRING(50),
          allowNull: false,
        },
        court_name: {
          type: Sequelize.STRING(100),
          allowNull: false,
        },
        status: {
          type: Sequelize.STRING(45),
          allowNull: false,
          defaultValue: 'active',
        },
        sort_order: {
          type: Sequelize.INTEGER,
          allowNull: false,
          defaultValue: 0,
        },
        created_at: {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal('CURRENT_TIMESTAMP'),
        },
        updated_at: {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal('CURRENT_TIMESTAMP'),
        },
      });
    }

    const columns = await queryInterface.describeTable('field_courts');
    const indexes = await queryInterface.showIndex('field_courts');
    const hasUnique = indexes.some((idx) => idx.name === 'uq_field_courts_field_id_court_code');
    const hasFieldIdIndex = indexes.some((idx) =>
      (idx.fields || []).length === 1 && (idx.fields || [])[0].attribute === 'field_id'
    );
    const hasStatusIndex = indexes.some((idx) =>
      (idx.fields || []).length === 1 && (idx.fields || [])[0].attribute === 'status'
    );

    if (columns.field_id && columns.court_code && !hasUnique) {
      await queryInterface.addConstraint('field_courts', {
        fields: ['field_id', 'court_code'],
        type: 'unique',
        name: 'uq_field_courts_field_id_court_code',
      });
    }
    if (columns.field_id && !hasFieldIdIndex) {
      await queryInterface.addIndex('field_courts', ['field_id']);
    }
    if (columns.status && !hasStatusIndex) {
      await queryInterface.addIndex('field_courts', ['status']);
    }
  },

  async down(queryInterface) {
    await queryInterface.dropTable('field_courts');
  },
};
