'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable('bookings').catch(() => null);

    if (!tableInfo) {
      await queryInterface.createTable('bookings', {
        booking_id: {
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
        start_time: {
          type: Sequelize.DATE,
          allowNull: false
        },
        end_time: {
          type: Sequelize.DATE,
          allowNull: false
        },
        status: {
          type: Sequelize.STRING(45),
          allowNull: false,
          defaultValue: 'pending'
        },
        note: {
          type: Sequelize.TEXT,
          allowNull: true
        },
        price: {
          type: Sequelize.DECIMAL(10, 2),
          allowNull: false
        }
      });
    } else if (!tableInfo.schedule_id) {
      await queryInterface.addColumn('bookings', 'schedule_id', {
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

    // Add indexes if missing
    const refreshed = await queryInterface.describeTable('bookings');
    const indexes = await queryInterface.showIndex('bookings');
    const hasIndex = (name) => indexes.some((idx) => idx.name === name);

    if (refreshed.customer_id && !hasIndex('bookings_customer_id')) {
      await queryInterface.addIndex('bookings', ['customer_id']);
    }
    if (refreshed.field_id && !hasIndex('bookings_field_id')) {
      await queryInterface.addIndex('bookings', ['field_id']);
    }
    if (refreshed.schedule_id && !hasIndex('bookings_schedule_id')) {
      await queryInterface.addIndex('bookings', ['schedule_id']);
    }
    if (refreshed.manager_id && !hasIndex('bookings_manager_id')) {
      await queryInterface.addIndex('bookings', ['manager_id']);
    }
    if (refreshed.status && !hasIndex('bookings_status')) {
      await queryInterface.addIndex('bookings', ['status']);
    }
    if (
      refreshed.start_time &&
      refreshed.end_time &&
      !hasIndex('bookings_start_time_end_time')
    ) {
      await queryInterface.addIndex('bookings', ['start_time', 'end_time']);
    }
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.dropTable('bookings');
  }
};
