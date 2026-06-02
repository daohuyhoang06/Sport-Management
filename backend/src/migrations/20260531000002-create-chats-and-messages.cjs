"use strict";

const chatTable = "chats";
const messageTable = "messages";

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
  tableName,
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
    const chatExists = await tableExists(queryInterface, Sequelize, chatTable);
    if (!chatExists) {
      await queryInterface.createTable(chatTable, {
        chat_id: {
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
        manager_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: "person",
            key: "person_id",
          },
          onDelete: "CASCADE",
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
        last_message: {
          type: Sequelize.TEXT,
          allowNull: true,
        },
        last_message_at: {
          type: Sequelize.DATE,
          allowNull: true,
        },
        user_unread_count: {
          type: Sequelize.INTEGER,
          allowNull: false,
          defaultValue: 0,
        },
        owner_unread_count: {
          type: Sequelize.INTEGER,
          allowNull: false,
          defaultValue: 0,
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

      await queryInterface.addIndex(chatTable, ["user_id"]);
      await queryInterface.addIndex(chatTable, ["manager_id"]);
      await queryInterface.addIndex(chatTable, ["field_id"]);
      await queryInterface.addIndex(chatTable, ["booking_id"]);
    } else {
      const chatInfo = await queryInterface.describeTable(chatTable);
      await addColumnIfMissing(
        queryInterface,
        chatTable,
        chatInfo,
        "field_id",
        {
          type: Sequelize.INTEGER,
          allowNull: true,
        },
      );
      await addColumnIfMissing(
        queryInterface,
        chatTable,
        chatInfo,
        "booking_id",
        {
          type: Sequelize.INTEGER,
          allowNull: true,
        },
      );
      await addColumnIfMissing(
        queryInterface,
        chatTable,
        chatInfo,
        "last_message",
        {
          type: Sequelize.TEXT,
          allowNull: true,
        },
      );
      await addColumnIfMissing(
        queryInterface,
        chatTable,
        chatInfo,
        "last_message_at",
        {
          type: Sequelize.DATE,
          allowNull: true,
        },
      );
      await addColumnIfMissing(
        queryInterface,
        chatTable,
        chatInfo,
        "user_unread_count",
        {
          type: Sequelize.INTEGER,
          allowNull: false,
          defaultValue: 0,
        },
      );
      await addColumnIfMissing(
        queryInterface,
        chatTable,
        chatInfo,
        "owner_unread_count",
        {
          type: Sequelize.INTEGER,
          allowNull: false,
          defaultValue: 0,
        },
      );
      await addColumnIfMissing(
        queryInterface,
        chatTable,
        chatInfo,
        "created_at",
        {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal("CURRENT_TIMESTAMP"),
        },
      );
      await addColumnIfMissing(
        queryInterface,
        chatTable,
        chatInfo,
        "updated_at",
        {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal("CURRENT_TIMESTAMP"),
        },
      );
    }

    const messageExists = await tableExists(
      queryInterface,
      Sequelize,
      messageTable,
    );
    if (!messageExists) {
      await queryInterface.createTable(messageTable, {
        message_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          primaryKey: true,
          autoIncrement: true,
        },
        chat_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: chatTable,
            key: "chat_id",
          },
          onDelete: "CASCADE",
          onUpdate: "CASCADE",
        },
        sender_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: "person",
            key: "person_id",
          },
          onDelete: "CASCADE",
          onUpdate: "CASCADE",
        },
        sender_type: {
          type: Sequelize.STRING(20),
          allowNull: true,
        },
        message_type: {
          type: Sequelize.STRING(20),
          allowNull: false,
          defaultValue: "text",
        },
        message_text: {
          type: Sequelize.TEXT,
          allowNull: true,
        },
        content: {
          type: Sequelize.TEXT,
          allowNull: true,
        },
        image_url: {
          type: Sequelize.STRING(255),
          allowNull: true,
        },
        metadata: {
          type: Sequelize.JSON,
          allowNull: true,
        },
        is_read: {
          type: Sequelize.BOOLEAN,
          allowNull: false,
          defaultValue: false,
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

      await queryInterface.addIndex(messageTable, ["chat_id"]);
      await queryInterface.addIndex(messageTable, ["sender_id"]);
      await queryInterface.addIndex(messageTable, ["is_read"]);
      await queryInterface.addIndex(messageTable, ["created_at"]);
    } else {
      const messageInfo = await queryInterface.describeTable(messageTable);
      await addColumnIfMissing(
        queryInterface,
        messageTable,
        messageInfo,
        "sender_type",
        {
          type: Sequelize.STRING(20),
          allowNull: true,
        },
      );
      await addColumnIfMissing(
        queryInterface,
        messageTable,
        messageInfo,
        "message_type",
        {
          type: Sequelize.STRING(20),
          allowNull: false,
          defaultValue: "text",
        },
      );
      await addColumnIfMissing(
        queryInterface,
        messageTable,
        messageInfo,
        "message_text",
        {
          type: Sequelize.TEXT,
          allowNull: true,
        },
      );
      await addColumnIfMissing(
        queryInterface,
        messageTable,
        messageInfo,
        "content",
        {
          type: Sequelize.TEXT,
          allowNull: true,
        },
      );
      await addColumnIfMissing(
        queryInterface,
        messageTable,
        messageInfo,
        "image_url",
        {
          type: Sequelize.STRING(255),
          allowNull: true,
        },
      );
      await addColumnIfMissing(
        queryInterface,
        messageTable,
        messageInfo,
        "metadata",
        {
          type: Sequelize.JSON,
          allowNull: true,
        },
      );
      await addColumnIfMissing(
        queryInterface,
        messageTable,
        messageInfo,
        "is_read",
        {
          type: Sequelize.BOOLEAN,
          allowNull: false,
          defaultValue: false,
        },
      );
      await addColumnIfMissing(
        queryInterface,
        messageTable,
        messageInfo,
        "created_at",
        {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal("CURRENT_TIMESTAMP"),
        },
      );
      await addColumnIfMissing(
        queryInterface,
        messageTable,
        messageInfo,
        "updated_at",
        {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal("CURRENT_TIMESTAMP"),
        },
      );
    }
  },

  async down(queryInterface, Sequelize) {
    const chatExists = await tableExists(queryInterface, Sequelize, chatTable);
    if (chatExists) {
      const chatInfo = await queryInterface.describeTable(chatTable);
      if (chatInfo.owner_unread_count) {
        await queryInterface.removeColumn(chatTable, "owner_unread_count");
      }
      if (chatInfo.user_unread_count) {
        await queryInterface.removeColumn(chatTable, "user_unread_count");
      }
      if (chatInfo.last_message_at) {
        await queryInterface.removeColumn(chatTable, "last_message_at");
      }
      if (chatInfo.last_message) {
        await queryInterface.removeColumn(chatTable, "last_message");
      }
      if (chatInfo.booking_id) {
        await queryInterface.removeColumn(chatTable, "booking_id");
      }
      if (chatInfo.field_id) {
        await queryInterface.removeColumn(chatTable, "field_id");
      }
    }

    const messageExists = await tableExists(
      queryInterface,
      Sequelize,
      messageTable,
    );
    if (messageExists) {
      const messageInfo = await queryInterface.describeTable(messageTable);
      if (messageInfo.metadata) {
        await queryInterface.removeColumn(messageTable, "metadata");
      }
      if (messageInfo.image_url) {
        await queryInterface.removeColumn(messageTable, "image_url");
      }
      if (messageInfo.content) {
        await queryInterface.removeColumn(messageTable, "content");
      }
      if (messageInfo.message_text) {
        await queryInterface.removeColumn(messageTable, "message_text");
      }
      if (messageInfo.message_type) {
        await queryInterface.removeColumn(messageTable, "message_type");
      }
      if (messageInfo.sender_type) {
        await queryInterface.removeColumn(messageTable, "sender_type");
      }
      if (messageInfo.updated_at) {
        await queryInterface.removeColumn(messageTable, "updated_at");
      }
      if (messageInfo.created_at) {
        await queryInterface.removeColumn(messageTable, "created_at");
      }
      if (messageInfo.is_read) {
        await queryInterface.removeColumn(messageTable, "is_read");
      }
    }
  },
};
