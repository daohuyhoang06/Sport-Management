"use strict";

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tables = await queryInterface.showAllTables();
    const hasTable = tables
      .map((t) => (typeof t === "string" ? t : t.tableName || t.table_name))
      .includes("reviews");

    if (!hasTable) {
      await queryInterface.createTable("reviews", {
        review_id: {
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
        customer_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: "person",
            key: "person_id",
          },
          onUpdate: "CASCADE",
          onDelete: "CASCADE",
        },
        rating: {
          type: Sequelize.INTEGER,
          allowNull: false,
          validate: {
            min: 1,
            max: 5,
          },
        },
        comment: {
          type: Sequelize.TEXT,
          allowNull: false,
        },
        images: {
          type: Sequelize.JSON,
          allowNull: true,
          comment: "Array of image URLs",
        },
        created_at: {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal("CURRENT_TIMESTAMP"),
        },
        updated_at: {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal(
            "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP",
          ),
        },
      });
    }

    const columns = await queryInterface.describeTable("reviews");
    const indexes = await queryInterface.showIndex("reviews");
    const hasIndexOn = (attr) =>
      indexes.some((idx) => (idx.fields || []).some((field) => field.attribute === attr));

    if (columns.rating && !hasIndexOn("rating")) {
      await queryInterface.addIndex("reviews", ["rating"]);
    }
    if (columns.created_at && !hasIndexOn("created_at")) {
      await queryInterface.addIndex("reviews", ["created_at"]);
    }
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.dropTable("reviews");
  },
};
