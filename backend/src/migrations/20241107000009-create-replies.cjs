'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable('replies').catch(() => null);

    if (!tableInfo) {
      await queryInterface.createTable('replies', {
        replies_id: {
          type: Sequelize.INTEGER,
          primaryKey: true,
          autoIncrement: true,
          allowNull: false
        },
        feedback_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: 'feedbacks',
            key: 'feedback_id'
          },
          onUpdate: 'CASCADE',
          onDelete: 'CASCADE'
        },
        customer_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: 'person',
            key: 'person_id'
          },
          onUpdate: 'CASCADE',
          onDelete: 'CASCADE'
        },
        manager_id: {
          type: Sequelize.INTEGER,
          allowNull: true,
          references: {
            model: 'person',
            key: 'person_id'
          },
          onUpdate: 'CASCADE',
          onDelete: 'SET NULL'
        },
        reply_message: {
          type: Sequelize.TEXT,
          allowNull: false
        },
        created_at: {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal('CURRENT_TIMESTAMP')
        }
      });
    } else {
      if (!tableInfo.customer_id) {
        await queryInterface.addColumn('replies', 'customer_id', {
          type: Sequelize.INTEGER,
          allowNull: true,
          references: {
            model: 'person',
            key: 'person_id'
          },
          onUpdate: 'CASCADE',
          onDelete: 'CASCADE'
        });
      }
      if (!tableInfo.manager_id) {
        await queryInterface.addColumn('replies', 'manager_id', {
          type: Sequelize.INTEGER,
          allowNull: true,
          references: {
            model: 'person',
            key: 'person_id'
          },
          onUpdate: 'CASCADE',
          onDelete: 'SET NULL'
        });
      }
    }

    // Add indexes if missing
    const refreshed = await queryInterface.describeTable('replies');
    const indexes = await queryInterface.showIndex('replies');
    const hasIndex = (name) => indexes.some((idx) => idx.name === name);

    if (refreshed.feedback_id && !hasIndex('replies_feedback_id')) {
      await queryInterface.addIndex('replies', ['feedback_id']);
    }
    if (refreshed.customer_id && !hasIndex('replies_customer_id')) {
      await queryInterface.addIndex('replies', ['customer_id']);
    }
    if (refreshed.manager_id && !hasIndex('replies_manager_id')) {
      await queryInterface.addIndex('replies', ['manager_id']);
    }
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.dropTable('replies');
  }
};
