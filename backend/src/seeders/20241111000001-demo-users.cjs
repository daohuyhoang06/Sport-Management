'use strict';

const bcrypt = require('bcryptjs');

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const hashedPassword = await bcrypt.hash('password123', 10);
    
    await queryInterface.bulkInsert('person', [
      {
        username: 'testuser',
        password: hashedPassword,
        person_name: 'Test User',
        email: 'testuser@example.com',
        phone: '0123456789',
        role: 'user',
        status: 'active'
      },
      {
        username: 'manager',
        password: hashedPassword,
        person_name: 'Manager User',
        email: 'manager@example.com',
        phone: '0987654321',
        role: 'manager',
        status: 'active'
      }
    ], {});
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.bulkDelete('person', {
      username: ['testuser', 'manager']
    }, {});
  }
};
