'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tables = await queryInterface.showAllTables();
    const tableNames = tables.map((t) =>
      typeof t === "string" ? t : t.tableName || t.table_name
    );

    if (!tableNames.includes("field_services")) {
      await queryInterface.createTable("field_services", {
        id: {
          type: Sequelize.INTEGER,
          primaryKey: true,
          autoIncrement: true,
          allowNull: false,
        },
        field_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: "fields",
            key: "field_id",
          },
          onUpdate: "CASCADE",
          onDelete: "CASCADE",
        },
        service_name: {
          type: Sequelize.STRING(100),
          allowNull: false,
        },
        description: {
          type: Sequelize.TEXT,
          allowNull: true,
        },
        is_free: {
          type: Sequelize.BOOLEAN,
          allowNull: false,
          defaultValue: false,
        },
        price: {
          type: Sequelize.DECIMAL(10, 2),
          allowNull: true,
          defaultValue: null,
        },
      });
    }

    if (!tableNames.includes("field_policies")) {
      await queryInterface.createTable("field_policies", {
        id: {
          type: Sequelize.INTEGER,
          primaryKey: true,
          autoIncrement: true,
          allowNull: false,
        },
        field_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: "fields",
            key: "field_id",
          },
          onUpdate: "CASCADE",
          onDelete: "CASCADE",
        },
        title: {
          type: Sequelize.STRING(150),
          allowNull: false,
        },
        content: {
          type: Sequelize.TEXT,
          allowNull: false,
        },
        policy_type: {
          type: Sequelize.STRING(50),
          allowNull: false,
        },
      });
    }

    const serviceIndexes = await queryInterface.showIndex("field_services");
    const hasServiceFieldIndex = serviceIndexes.some((idx) =>
      (idx.fields || []).some((f) => f.attribute === "field_id")
    );
    if (!hasServiceFieldIndex) {
      await queryInterface.addIndex("field_services", ["field_id"]);
    }

    const policyIndexes = await queryInterface.showIndex("field_policies");
    const hasPolicyFieldIndex = policyIndexes.some((idx) =>
      (idx.fields || []).some((f) => f.attribute === "field_id")
    );
    const hasPolicyTypeIndex = policyIndexes.some((idx) =>
      (idx.fields || []).some((f) => f.attribute === "policy_type")
    );
    if (!hasPolicyFieldIndex) {
      await queryInterface.addIndex("field_policies", ["field_id"]);
    }
    if (!hasPolicyTypeIndex) {
      await queryInterface.addIndex("field_policies", ["policy_type"]);
    }
  },

  async down(queryInterface) {
    const tables = await queryInterface.showAllTables();
    const tableNames = tables.map((t) =>
      typeof t === "string" ? t : t.tableName || t.table_name
    );

    if (tableNames.includes("field_policies")) {
      await queryInterface.dropTable("field_policies");
    }
    if (tableNames.includes("field_services")) {
      await queryInterface.dropTable("field_services");
    }
  },
};
