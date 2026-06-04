'use strict';

module.exports = {
  async up(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable('payments');

    const addColumnIfMissing = async (name, definition) => {
      if (!tableInfo[name]) {
        await queryInterface.addColumn('payments', name, definition);
      }
    };

    await addColumnIfMissing('payment_status', {
      type: Sequelize.STRING(45),
      allowNull: true,
    });

    await addColumnIfMissing('transaction_id', {
      type: Sequelize.STRING(100),
      allowNull: true,
    });

    await addColumnIfMissing('paid_at', {
      type: Sequelize.DATE,
      allowNull: true,
    });

    await queryInterface.addIndex('payments', ['payment_status'], {
      name: 'idx_payments_payment_status',
    }).catch(() => {});

    await queryInterface.addIndex('payments', ['transaction_id'], {
      name: 'idx_payments_transaction_id',
    }).catch(() => {});
  },

  async down(queryInterface) {
    const tableInfo = await queryInterface.describeTable('payments');

    await queryInterface.removeIndex('payments', 'idx_payments_transaction_id').catch(() => {});
    await queryInterface.removeIndex('payments', 'idx_payments_payment_status').catch(() => {});

    for (const column of ['paid_at', 'transaction_id', 'payment_status']) {
      if (tableInfo[column]) {
        await queryInterface.removeColumn('payments', column);
      }
    }
  },
};
