'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const table = await queryInterface.describeTable('payments');

    const addColumnIfMissing = async (name, definition) => {
      if (!table[name]) {
        await queryInterface.addColumn('payments', name, definition);
      }
    };

    await addColumnIfMissing('provider', {
      type: Sequelize.STRING(45),
      allowNull: true
    });

    await addColumnIfMissing('order_id', {
      type: Sequelize.STRING(200),
      allowNull: true
    });

    await addColumnIfMissing('request_id', {
      type: Sequelize.STRING(200),
      allowNull: true
    });

    await addColumnIfMissing('pay_url', {
      type: Sequelize.TEXT,
      allowNull: true
    });

    await addColumnIfMissing('deeplink', {
      type: Sequelize.TEXT,
      allowNull: true
    });

    await addColumnIfMissing('qr_code_url', {
      type: Sequelize.TEXT,
      allowNull: true
    });

    await addColumnIfMissing('raw_response', {
      type: Sequelize.TEXT,
      allowNull: true
    });

    await addColumnIfMissing('failure_reason', {
      type: Sequelize.TEXT,
      allowNull: true
    });

    await queryInterface.addIndex('payments', ['provider'], {
      name: 'idx_payments_provider'
    }).catch(() => {});
    await queryInterface.addIndex('payments', ['order_id'], {
      name: 'idx_payments_order_id'
    }).catch(() => {});
    await queryInterface.addIndex('payments', ['request_id'], {
      name: 'idx_payments_request_id'
    }).catch(() => {});
  },

  async down(queryInterface) {
    const table = await queryInterface.describeTable('payments');

    await queryInterface.removeIndex('payments', 'idx_payments_request_id').catch(() => {});
    await queryInterface.removeIndex('payments', 'idx_payments_order_id').catch(() => {});
    await queryInterface.removeIndex('payments', 'idx_payments_provider').catch(() => {});

    for (const column of [
      'failure_reason',
      'raw_response',
      'qr_code_url',
      'deeplink',
      'pay_url',
      'request_id',
      'order_id',
      'provider'
    ]) {
      if (table[column]) {
        await queryInterface.removeColumn('payments', column);
      }
    }
  }
};
