'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const tableInfo = await queryInterface.describeTable('fields');

    const addColumnIfMissing = async (name, definition) => {
      if (!tableInfo[name]) {
        await queryInterface.addColumn('fields', name, definition);
      }
    };

    await addColumnIfMissing('latitude', {
      type: Sequelize.DECIMAL(10, 7),
      allowNull: true,
    });

    await addColumnIfMissing('longitude', {
      type: Sequelize.DECIMAL(10, 7),
      allowNull: true,
    });

    await addColumnIfMissing('phone', {
      type: Sequelize.STRING(20),
      allowNull: true,
    });

    await addColumnIfMissing('open_time', {
      type: Sequelize.TIME,
      allowNull: true,
    });

    await addColumnIfMissing('close_time', {
      type: Sequelize.TIME,
      allowNull: true,
    });

    await addColumnIfMissing('created_at', {
      type: Sequelize.DATE,
      allowNull: false,
      defaultValue: Sequelize.literal('CURRENT_TIMESTAMP'),
    });

    await addColumnIfMissing('updated_at', {
      type: Sequelize.DATE,
      allowNull: false,
      defaultValue: Sequelize.literal('CURRENT_TIMESTAMP'),
    });

  },

  async down(queryInterface) {
    const tableInfo = await queryInterface.describeTable('fields');
    const removeColumnIfExists = async (name) => {
      if (tableInfo[name]) {
        await queryInterface.removeColumn('fields', name);
      }
    };

    await removeColumnIfExists('latitude');
    await removeColumnIfExists('longitude');
    await removeColumnIfExists('phone');
    await removeColumnIfExists('open_time');
    await removeColumnIfExists('close_time');
    await removeColumnIfExists('created_at');
    await removeColumnIfExists('updated_at');
  },
};
