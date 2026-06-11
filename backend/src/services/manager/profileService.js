import sequelize from '../../config/database.js';
import Person from '../../models/Person.js';

export const getManagerProfileService = async (managerId) => {
  const [[person]] = await sequelize.query(
    'SELECT person_id, person_name, email, phone, avatar_url FROM person WHERE person_id = ?',
    { replacements: [managerId] }
  );
  return person || null;
};

export const updateManagerProfileService = async (managerId, { person_name, phone, email }) => {
  const fields = [];
  const values = [];

  if (person_name !== undefined && person_name !== null) {
    fields.push('person_name = ?');
    values.push(person_name.trim());
  }
  if (phone !== undefined && phone !== null) {
    fields.push('phone = ?');
    values.push(phone.trim());
  }
  if (email !== undefined && email !== null) {
    fields.push('email = ?');
    values.push(email.trim());
  }

  if (fields.length === 0) return getManagerProfileService(managerId);

  fields.push('updated_at = NOW()');
  values.push(managerId);

  await sequelize.query(
    `UPDATE person SET ${fields.join(', ')} WHERE person_id = ?`,
    { replacements: values }
  );

  return getManagerProfileService(managerId);
};
