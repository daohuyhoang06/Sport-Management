"use strict";

const normalizeTableName = (table) => {
  if (typeof table === "string") return table;
  if (table?.tableName) return table.tableName;
  if (table?.table_name) return table.table_name;
  if (table?.name) return table.name;
  return String(table || "");
};

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tables = (await queryInterface.showAllTables()).map(normalizeTableName);

    if (!tables.includes("favorite_fields")) {
      await queryInterface.createTable("favorite_fields", {
        id: {
          type: Sequelize.INTEGER,
          primaryKey: true,
          autoIncrement: true,
          allowNull: false,
        },
        user_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: "person",
            key: "person_id",
          },
          onUpdate: "CASCADE",
          onDelete: "CASCADE",
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
        created_at: {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal("CURRENT_TIMESTAMP"),
        },
      });
    }

    await queryInterface.addIndex("favorite_fields", ["user_id", "field_id"], {
      unique: true,
      name: "uq_favorite_fields_user_field",
    });
    await queryInterface.addIndex("favorite_fields", ["user_id"], {
      name: "idx_favorite_fields_user_id",
    });
    await queryInterface.addIndex("favorite_fields", ["field_id"], {
      name: "idx_favorite_fields_field_id",
    });
  },

  async down(queryInterface) {
    const tables = (await queryInterface.showAllTables()).map(normalizeTableName);
    if (tables.includes("favorite_fields")) {
      await queryInterface.dropTable("favorite_fields");
    }
  },
};
