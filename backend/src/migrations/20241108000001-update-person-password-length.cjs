'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    // Increase password field length to support hashed passwords (bcrypt outputs 60 chars)
    await queryInterface.changeColumn('person', 'password', {
      type: Sequelize.STRING(255),
      allowNull: false
    });
  },

  async down(queryInterface, Sequelize) {
    // Revert to original length
    await queryInterface.changeColumn('person', 'password', {
      type: Sequelize.STRING(45),
      allowNull: false
    });
  }
};