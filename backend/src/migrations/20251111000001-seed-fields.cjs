'use strict';

/**
 * Seed fields table with ~10 sample rows.
 * This file is a sequelize-cli style migration/seeder in .cjs format.
 */

module.exports = {
  up: async (queryInterface, Sequelize) => {
    const now = new Date();
    return queryInterface.bulkInsert('fields', [
      { manager_id: null, field_name: 'Sunrise Stadium', location: 'District 1', status: 'active' },
      { manager_id: null, field_name: 'Green Park Arena', location: 'District 3', status: 'active' },
      { manager_id: null, field_name: 'Riverside Field', location: 'District 7', status: 'active' },
      { manager_id: null, field_name: 'Maple Leaf Pitch', location: 'District 5', status: 'active' },
      { manager_id: null, field_name: 'Central Sports Ground', location: 'District 2', status: 'active' },
      { manager_id: null, field_name: 'Victory Turf', location: 'District 4', status: 'active' },
      { manager_id: null, field_name: 'Starlight Arena', location: 'District 6', status: 'active' },
      { manager_id: null, field_name: 'Harbor Field', location: 'District 8', status: 'active' },
      { manager_id: null, field_name: 'Cityside Stadium', location: 'District 9', status: 'active' },
      { manager_id: null, field_name: 'Lakeside Pitch', location: 'District 10', status: 'active' },
    ], {});
  },

  down: async (queryInterface, Sequelize) => {
    // Remove seeded rows by name
    return queryInterface.bulkDelete('fields', {
      field_name: [
        'Sunrise Stadium','Green Park Arena','Riverside Field','Maple Leaf Pitch','Central Sports Ground',
        'Victory Turf','Starlight Arena','Harbor Field','Cityside Stadium','Lakeside Pitch'
      ]
    }, {});
  }
};