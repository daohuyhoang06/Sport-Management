'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable('feedbacks').catch(() => null);

    if (!tableInfo) {
      await queryInterface.createTable('feedbacks', {
        feedback_id: {
          type: Sequelize.INTEGER,
          primaryKey: true,
          autoIncrement: true,
          allowNull: false
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
        message: {
          type: Sequelize.TEXT,
          allowNull: false
        },
        created_at: {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal('CURRENT_TIMESTAMP')
        },
        status: {
          type: Sequelize.STRING(45),
          allowNull: false,
          defaultValue: 'pending'
        }
      });
    } else {
      if (!tableInfo.customer_id) {
        await queryInterface.addColumn('feedbacks', 'customer_id', {
          type: Sequelize.INTEGER,
          allowNull: true,
          references: {
            model: 'person',
            key: 'person_id'
          },
          onUpdate: 'CASCADE',
          onDelete: 'CASCADE'
        });

        if (tableInfo.person_id) {
          await queryInterface.sequelize.query(
            'UPDATE feedbacks SET customer_id = person_id WHERE customer_id IS NULL',
          );
        }
      }

      if (!tableInfo.manager_id) {
        await queryInterface.addColumn('feedbacks', 'manager_id', {
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
    const refreshed = await queryInterface.describeTable('feedbacks');
    const indexes = await queryInterface.showIndex('feedbacks');
    const hasIndex = (name) => indexes.some((idx) => idx.name === name);

    if (refreshed.customer_id && !hasIndex('feedbacks_customer_id')) {
      await queryInterface.addIndex('feedbacks', ['customer_id']);
    }
    if (refreshed.manager_id && !hasIndex('feedbacks_manager_id')) {
      await queryInterface.addIndex('feedbacks', ['manager_id']);
    }
    if (refreshed.status && !hasIndex('feedbacks_status')) {
      await queryInterface.addIndex('feedbacks', ['status']);
    }
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.dropTable('feedbacks');
  }
};
