'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable('payments').catch(() => null);

    if (!tableInfo) {
      await queryInterface.createTable('payments', {
        payment_id: {
          type: Sequelize.INTEGER,
          primaryKey: true,
          autoIncrement: true,
          allowNull: false
        },
        booking_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: 'bookings',
            key: 'booking_id'
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
        field_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: 'fields',
            key: 'field_id'
          },
          onUpdate: 'CASCADE',
          onDelete: 'CASCADE'
        },
        schedule_id: {
          type: Sequelize.INTEGER,
          allowNull: true,
          references: {
            model: 'field_schedules',
            key: 'schedule_id'
          },
          onUpdate: 'CASCADE',
          onDelete: 'SET NULL'
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
        amount: {
          type: Sequelize.DECIMAL(10, 2),
          allowNull: false
        },
        payment_method: {
          type: Sequelize.STRING(50),
          allowNull: false
        },
        payment_date: {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal('CURRENT_TIMESTAMP')
        },
        transaction_code: {
          type: Sequelize.STRING(45),
          allowNull: true
        },
        status: {
          type: Sequelize.STRING(45),
          allowNull: false,
          defaultValue: 'pending'
        }
      });
    } else {
      if (!tableInfo.schedule_id) {
        await queryInterface.addColumn('payments', 'schedule_id', {
          type: Sequelize.INTEGER,
          allowNull: true,
          references: {
            model: 'field_schedules',
            key: 'schedule_id'
          },
          onUpdate: 'CASCADE',
          onDelete: 'SET NULL'
        });
      }
      if (!tableInfo.manager_id) {
        await queryInterface.addColumn('payments', 'manager_id', {
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
      if (!tableInfo.payment_date) {
        await queryInterface.addColumn('payments', 'payment_date', {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal('CURRENT_TIMESTAMP')
        });
      }
      if (!tableInfo.transaction_code) {
        await queryInterface.addColumn('payments', 'transaction_code', {
          type: Sequelize.STRING(45),
          allowNull: true
        });
      }
      if (!tableInfo.status) {
        await queryInterface.addColumn('payments', 'status', {
          type: Sequelize.STRING(45),
          allowNull: false,
          defaultValue: 'pending'
        });
      }
    }

    // Add indexes if missing
    const refreshed = await queryInterface.describeTable('payments');
    const indexes = await queryInterface.showIndex('payments');
    const hasIndex = (name) => indexes.some((idx) => idx.name === name);

    if (refreshed.booking_id && !hasIndex('payments_booking_id')) {
      await queryInterface.addIndex('payments', ['booking_id']);
    }
    if (refreshed.customer_id && !hasIndex('payments_customer_id')) {
      await queryInterface.addIndex('payments', ['customer_id']);
    }
    if (refreshed.field_id && !hasIndex('payments_field_id')) {
      await queryInterface.addIndex('payments', ['field_id']);
    }
    if (refreshed.schedule_id && !hasIndex('payments_schedule_id')) {
      await queryInterface.addIndex('payments', ['schedule_id']);
    }
    if (refreshed.manager_id && !hasIndex('payments_manager_id')) {
      await queryInterface.addIndex('payments', ['manager_id']);
    }
    if (refreshed.status && !hasIndex('payments_status')) {
      await queryInterface.addIndex('payments', ['status']);
    }
    if (refreshed.transaction_code && !hasIndex('payments_transaction_code')) {
      await queryInterface.addIndex('payments', ['transaction_code']);
    }
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.dropTable('payments');
  }
};
