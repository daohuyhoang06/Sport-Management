"use strict";

const tableName = "notifications";

const tableExists = async (queryInterface, Sequelize, name) => {
  const result = await queryInterface.sequelize.query(
    "SELECT COUNT(*) AS count FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
    {
      replacements: [name],
      type: Sequelize.QueryTypes.SELECT,
    },
  );
  return Number(result?.[0]?.count || 0) > 0;
};

const addColumnIfMissing = async (
  queryInterface,
  tableInfo,
  name,
  definition,
) => {
  if (!tableInfo[name]) {
    await queryInterface.addColumn(tableName, name, definition);
  }
};

module.exports = {
  async up(queryInterface, Sequelize) {
    const exists = await tableExists(queryInterface, Sequelize, tableName);

    if (!exists) {
      await queryInterface.createTable(tableName, {
        id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          primaryKey: true,
          autoIncrement: true,
        },
        user_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: "person",
            key: "person_id",
          },
          onDelete: "CASCADE",
          onUpdate: "CASCADE",
        },
        type: {
          type: Sequelize.STRING(50),
          allowNull: false,
        },
        section: {
          type: Sequelize.STRING(50),
          allowNull: true,
        },
        title: {
          type: Sequelize.STRING(255),
          allowNull: false,
        },
        subtitle: {
          type: Sequelize.STRING(255),
          allowNull: true,
        },
        content: {
          type: Sequelize.TEXT,
          allowNull: true,
        },
        target_type: {
          type: Sequelize.STRING(50),
          allowNull: true,
        },
        target_id: {
          type: Sequelize.INTEGER,
          allowNull: true,
        },
        booking_id: {
          type: Sequelize.INTEGER,
          allowNull: true,
          references: {
            model: "bookings",
            key: "booking_id",
          },
          onDelete: "SET NULL",
          onUpdate: "CASCADE",
        },
        field_id: {
          type: Sequelize.INTEGER,
          allowNull: true,
          references: {
            model: "fields",
            key: "field_id",
          },
          onDelete: "SET NULL",
          onUpdate: "CASCADE",
        },
        is_read: {
          type: Sequelize.BOOLEAN,
          allowNull: false,
          defaultValue: false,
        },
        metadata: {
          type: Sequelize.JSON,
          allowNull: true,
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

      await queryInterface.addIndex(tableName, ["user_id"]);
      await queryInterface.addIndex(tableName, ["type"]);
      await queryInterface.addIndex(tableName, ["section"]);
      await queryInterface.addIndex(tableName, ["is_read"]);
      await queryInterface.addIndex(tableName, ["created_at"]);
      return;
    }

    const tableInfo = await queryInterface.describeTable(tableName);
    await addColumnIfMissing(queryInterface, tableInfo, "user_id", {
      type: Sequelize.INTEGER,
      allowNull: false,
    });
    await addColumnIfMissing(queryInterface, tableInfo, "type", {
      type: Sequelize.STRING(50),
      allowNull: false,
    });
    await addColumnIfMissing(queryInterface, tableInfo, "section", {
      type: Sequelize.STRING(50),
      allowNull: true,
    });
    await addColumnIfMissing(queryInterface, tableInfo, "title", {
      type: Sequelize.STRING(255),
      allowNull: false,
    });
    await addColumnIfMissing(queryInterface, tableInfo, "subtitle", {
      type: Sequelize.STRING(255),
      allowNull: true,
    });
    await addColumnIfMissing(queryInterface, tableInfo, "content", {
      type: Sequelize.TEXT,
      allowNull: true,
    });
    await addColumnIfMissing(queryInterface, tableInfo, "target_type", {
      type: Sequelize.STRING(50),
      allowNull: true,
    });
    await addColumnIfMissing(queryInterface, tableInfo, "target_id", {
      type: Sequelize.INTEGER,
      allowNull: true,
    });
    await addColumnIfMissing(queryInterface, tableInfo, "booking_id", {
      type: Sequelize.INTEGER,
      allowNull: true,
    });
    await addColumnIfMissing(queryInterface, tableInfo, "field_id", {
      type: Sequelize.INTEGER,
      allowNull: true,
    });
    await addColumnIfMissing(queryInterface, tableInfo, "is_read", {
      type: Sequelize.BOOLEAN,
      allowNull: false,
      defaultValue: false,
    });
    await addColumnIfMissing(queryInterface, tableInfo, "metadata", {
      type: Sequelize.JSON,
      allowNull: true,
    });
    await addColumnIfMissing(queryInterface, tableInfo, "created_at", {
      type: Sequelize.DATE,
      allowNull: false,
      defaultValue: Sequelize.literal("CURRENT_TIMESTAMP"),
    });
    await addColumnIfMissing(queryInterface, tableInfo, "updated_at", {
      type: Sequelize.DATE,
      allowNull: false,
      defaultValue: Sequelize.literal("CURRENT_TIMESTAMP"),
    });
  },

  async down(queryInterface, Sequelize) {
    const exists = await tableExists(queryInterface, Sequelize, tableName);
    if (exists) {
      await queryInterface.dropTable(tableName);
    }
  },
};
