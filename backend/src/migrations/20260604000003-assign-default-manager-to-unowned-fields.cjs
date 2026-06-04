'use strict';

const fieldsTable = 'fields';
const personTable = 'person';

module.exports = {
  async up(queryInterface, Sequelize) {
    const [managers] = await queryInterface.sequelize.query(
      `SELECT person_id, phone
       FROM ${personTable}
       WHERE role = 'manager'
         AND (status IS NULL OR status = 'active')
       ORDER BY person_id ASC
       LIMIT 1`,
      { type: Sequelize.QueryTypes.SELECT },
    );

    const manager = Array.isArray(managers) ? managers[0] : managers;
    if (!manager?.person_id) {
      return;
    }

    await queryInterface.sequelize.query(
      `UPDATE ${fieldsTable}
       SET manager_id = ?
       WHERE manager_id IS NULL`,
      { replacements: [manager.person_id] },
    );

    if (manager.phone) {
      await queryInterface.sequelize.query(
        `UPDATE ${fieldsTable}
         SET phone = ?
         WHERE (phone IS NULL OR phone = '')
           AND manager_id = ?`,
        { replacements: [manager.phone, manager.person_id] },
      );
    }
  },

  async down() {
    // Data migration: keep assigned owners to avoid breaking existing bookings/chats.
  },
};
