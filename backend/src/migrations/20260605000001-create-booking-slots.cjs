'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable('booking_slots').catch(() => null);

    if (!tableInfo) {
      await queryInterface.createTable('booking_slots', {
        booking_slot_id: {
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
        court_id: {
          type: Sequelize.INTEGER,
          allowNull: true,
          references: {
            model: 'field_courts',
            key: 'court_id'
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
        price: {
          type: Sequelize.DECIMAL(10, 2),
          allowNull: false
        }
      });
    }

    const refreshed = await queryInterface.describeTable('booking_slots');
    const indexes = await queryInterface.showIndex('booking_slots');
    const hasIndex = (name) => indexes.some((idx) => idx.name === name);

    if (refreshed.booking_id && !hasIndex('booking_slots_booking_id')) {
      await queryInterface.addIndex('booking_slots', ['booking_id'], {
        name: 'booking_slots_booking_id'
      });
    }
    if (refreshed.field_id && !hasIndex('booking_slots_field_id')) {
      await queryInterface.addIndex('booking_slots', ['field_id'], {
        name: 'booking_slots_field_id'
      });
    }
    if (refreshed.court_id && !hasIndex('booking_slots_court_id')) {
      await queryInterface.addIndex('booking_slots', ['court_id'], {
        name: 'booking_slots_court_id'
      });
    }
    if (refreshed.start_time && refreshed.end_time && !hasIndex('booking_slots_time_range')) {
      await queryInterface.addIndex('booking_slots', ['start_time', 'end_time'], {
        name: 'booking_slots_time_range'
      });
    }
    if (
      refreshed.booking_id &&
      refreshed.court_id &&
      refreshed.start_time &&
      refreshed.end_time &&
      !hasIndex('booking_slots_unique_slot')
    ) {
      await queryInterface.addIndex(
        'booking_slots',
        ['booking_id', 'court_id', 'start_time', 'end_time'],
        {
          name: 'booking_slots_unique_slot',
          unique: true
        }
      );
    }

    await queryInterface.sequelize.query(`
      INSERT IGNORE INTO booking_slots
        (booking_id, field_id, court_id, start_time, end_time, price)
      SELECT
        b.booking_id,
        b.field_id,
        b.court_id,
        b.start_time,
        b.end_time,
        b.price
      FROM bookings b
    `);
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.dropTable('booking_slots');
  }
};
