'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    await queryInterface.createTable('person', {
      person_id: {
        type: Sequelize.INTEGER,
        primaryKey: true,
        autoIncrement: true,
        allowNull: false
      },
      person_name: {
        type: Sequelize.STRING(50),
        allowNull: false
      },
      birthday: {
        type: Sequelize.DATEONLY,
        allowNull: true
      },
      sex: {
        type: Sequelize.STRING(10),
        allowNull: true
      },
      address: {
        type: Sequelize.STRING(45),
        allowNull: true
      },
      email: {
        type: Sequelize.STRING(45),
        allowNull: true,
        unique: true
      },
      phone: {
        type: Sequelize.STRING(10),
        allowNull: true
      },
      username: {
        type: Sequelize.STRING(45),
        allowNull: false,
        unique: true
      },
      password: {
        type: Sequelize.STRING(255),
        allowNull: false
      },
      role: {
        type: Sequelize.STRING(45),
        allowNull: true,
        defaultValue: 'user'
      },
      status: {
        type: Sequelize.STRING(45),
        allowNull: true,
        defaultValue: 'active'
      }
    });

    // Add indexes
    await queryInterface.addIndex('person', ['email']);
    await queryInterface.addIndex('person', ['username']);
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.dropTable('person');
  }
};
