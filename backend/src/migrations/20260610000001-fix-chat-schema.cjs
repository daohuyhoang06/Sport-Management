'use strict';

/**
 * Migration: Fix chat tables schema
 *
 * messages table was created with only `message_text` but chatService uses
 * `content` and `updated_at`. This migration adds the missing columns and
 * copies existing data.
 *
 * chats table was missing `last_message` and `last_message_at` used by the
 * UPDATE after each message send.
 */
module.exports = {
  up: async (queryInterface, Sequelize) => {
    const msgCols = await queryInterface.describeTable('messages');

    if (!msgCols.content) {
      await queryInterface.addColumn('messages', 'content', {
        type: Sequelize.TEXT,
        allowNull: true,
        after: 'message_text',
      });
      // Backfill content from message_text for existing rows
      await queryInterface.sequelize.query(
        'UPDATE messages SET content = message_text WHERE content IS NULL'
      );
    }

    if (!msgCols.updated_at) {
      await queryInterface.addColumn('messages', 'updated_at', {
        type: Sequelize.DATE,
        allowNull: true,
        defaultValue: Sequelize.literal('CURRENT_TIMESTAMP'),
        after: 'created_at',
      });
    }

    const chatCols = await queryInterface.describeTable('chats');

    if (!chatCols.last_message) {
      await queryInterface.addColumn('chats', 'last_message', {
        type: Sequelize.TEXT,
        allowNull: true,
        after: 'updated_at',
      });
    }

    if (!chatCols.last_message_at) {
      await queryInterface.addColumn('chats', 'last_message_at', {
        type: Sequelize.DATE,
        allowNull: true,
        after: 'last_message',
      });
    }

    // Backfill last_message/last_message_at for existing chats
    await queryInterface.sequelize.query(`
      UPDATE chats c
      JOIN (
        SELECT chat_id,
               COALESCE(content, message_text) AS last_msg,
               created_at AS last_msg_at
        FROM messages
        WHERE (chat_id, created_at) IN (
          SELECT chat_id, MAX(created_at) FROM messages GROUP BY chat_id
        )
      ) latest ON c.chat_id = latest.chat_id
      SET c.last_message    = latest.last_msg,
          c.last_message_at = latest.last_msg_at
      WHERE c.last_message IS NULL
    `);
  },

  down: async (queryInterface) => {
    const msgCols = await queryInterface.describeTable('messages');
    if (msgCols.content)     await queryInterface.removeColumn('messages', 'content');
    if (msgCols.updated_at)  await queryInterface.removeColumn('messages', 'updated_at');

    const chatCols = await queryInterface.describeTable('chats');
    if (chatCols.last_message)    await queryInterface.removeColumn('chats', 'last_message');
    if (chatCols.last_message_at) await queryInterface.removeColumn('chats', 'last_message_at');
  },
};
