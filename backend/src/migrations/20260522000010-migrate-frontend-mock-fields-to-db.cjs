'use strict';

const fs = require('fs');
const path = require('path');

const FRONTEND_MOCK_FIELDS_PATH = path.resolve(
  __dirname,
  '../data/frontend-mock-fields.json',
);

const FIELD_ROWS = JSON.parse(fs.readFileSync(FRONTEND_MOCK_FIELDS_PATH, 'utf8'));

const DEFAULT_PRICE_BY_SPORT_ICON = {
  FOOTBALL: 320000,
  VOLLEYBALL: 220000,
  PICKLEBALL: 280000,
  BADMINTON: 180000,
  TENNIS: 380000,
};

const OPEN_CLOSE_BY_SPORT_ICON = {
  FOOTBALL: ['06:00:00', '23:00:00'],
  VOLLEYBALL: ['06:00:00', '22:30:00'],
  PICKLEBALL: ['05:30:00', '22:00:00'],
  BADMINTON: ['05:00:00', '23:00:00'],
  TENNIS: ['06:00:00', '22:00:00'],
};

const TAGS_BY_SPORT_ICON = {
  FOOTBALL: ['7 ng\u01b0\u1eddi', 'C\u1ecf nh\u00e2n t\u1ea1o'],
  VOLLEYBALL: ['Trong nh\u00e0', 'S\u00e0n g\u1ed7'],
  PICKLEBALL: ['Indoor', '\u0110\u00e8n LED'],
  BADMINTON: ['Ti\u00eau chu\u1ea9n', '\u0110i\u1ec1u h\u00f2a'],
  TENNIS: ['Hard court', 'Hu\u1ea5n luy\u1ec7n'],
};

const DISTANCE_BY_ORDER = [0.4, 0.8, 1.2, 2.5, 4.0, 6.5, 9.0, 14.0, 22.0];

const SPORT_NAME_BY_ICON = {
  FOOTBALL: 'Bóng đá',
  VOLLEYBALL: 'Bóng chuyền',
  PICKLEBALL: 'Pickleball',
  BADMINTON: 'Cầu lông',
  TENNIS: 'Tennis',
};

const REGION_BY_PROVINCE = {
  'Hà Nội': 'Miền Bắc',
  'Hải Phòng': 'Miền Bắc',
  'Đà Nẵng': 'Miền Trung',
  'TP Hồ Chí Minh': 'Miền Nam',
  'Cần Thơ': 'Miền Nam',
};

const normalizeTableName = (table) => {
  if (typeof table === 'string') return table;
  if (table?.tableName) return table.tableName;
  if (table?.table_name) return table.table_name;
  if (table?.name) return table.name;
  return String(table || '');
};

const parsePrice = (priceText, sportIconType) => {
  if (!priceText || String(priceText).toLowerCase().includes('liên hệ')) {
    return DEFAULT_PRICE_BY_SPORT_ICON[sportIconType] || null;
  }

  const digits = String(priceText).replace(/[^0-9]/g, '');
  if (!digits) {
    return DEFAULT_PRICE_BY_SPORT_ICON[sportIconType] || null;
  }

  return Number(digits);
};

const splitLocation = (location) => {
  const parts = String(location || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);

  const province = parts.length > 0 ? parts[parts.length - 1] : '';
  const district = parts.length > 1 ? parts[parts.length - 2] : '';
  const region = REGION_BY_PROVINCE[province] || 'Toàn quốc';

  return { region, province, district };
};

const ensureSportTypesTable = async (queryInterface, Sequelize) => {
  const tables = (await queryInterface.showAllTables()).map(normalizeTableName);

  if (!tables.includes('sport_types')) {
    await queryInterface.createTable('sport_types', {
      sport_id: {
        type: Sequelize.INTEGER,
        primaryKey: true,
        autoIncrement: true,
        allowNull: false,
      },
      sport_name: {
        type: Sequelize.STRING(100),
        allowNull: false,
        unique: true,
      },
      created_at: {
        type: Sequelize.DATE,
        allowNull: false,
        defaultValue: Sequelize.literal('CURRENT_TIMESTAMP'),
      },
      updated_at: {
        type: Sequelize.DATE,
        allowNull: false,
        defaultValue: Sequelize.literal('CURRENT_TIMESTAMP'),
      },
    });

    await queryInterface.addIndex('sport_types', ['sport_name'], {
      unique: true,
      name: 'uq_sport_types_sport_name',
    });
  }
};

const ensureFieldTagsTable = async (queryInterface, Sequelize) => {
  const tables = (await queryInterface.showAllTables()).map(normalizeTableName);

  if (!tables.includes('field_tags')) {
    await queryInterface.createTable('field_tags', {
      tag_id: {
        type: Sequelize.INTEGER,
        primaryKey: true,
        autoIncrement: true,
        allowNull: false,
      },
      field_id: {
        type: Sequelize.INTEGER,
        allowNull: false,
        references: {
          model: 'fields',
          key: 'field_id',
        },
        onUpdate: 'CASCADE',
        onDelete: 'CASCADE',
      },
      tag_name: {
        type: Sequelize.STRING(100),
        allowNull: false,
      },
      sort_order: {
        type: Sequelize.INTEGER,
        allowNull: false,
        defaultValue: 0,
      },
      created_at: {
        type: Sequelize.DATE,
        allowNull: false,
        defaultValue: Sequelize.literal('CURRENT_TIMESTAMP'),
      },
    });

    await queryInterface.addConstraint('field_tags', {
      fields: ['field_id', 'tag_name'],
      type: 'unique',
      name: 'uq_field_tags_field_id_tag_name',
    });

    await queryInterface.addIndex('field_tags', ['field_id'], {
      name: 'idx_field_tags_field_id',
    });
  }
};

const ensureFieldColumns = async (queryInterface, Sequelize) => {
  const tableInfo = await queryInterface.describeTable('fields');

  const addColumnIfMissing = async (name, definition) => {
    if (!tableInfo[name]) {
      await queryInterface.addColumn('fields', name, definition);
    }
  };

  await addColumnIfMissing('sport_id', {
    type: Sequelize.INTEGER,
    allowNull: true,
    references: {
      model: 'sport_types',
      key: 'sport_id',
    },
    onUpdate: 'CASCADE',
    onDelete: 'SET NULL',
  });

  await addColumnIfMissing('display_rating', {
    type: Sequelize.DECIMAL(3, 1),
    allowNull: true,
    defaultValue: null,
  });

  await addColumnIfMissing('featured', {
    type: Sequelize.BOOLEAN,
    allowNull: false,
    defaultValue: false,
  });

  await addColumnIfMissing('availability_note', {
    type: Sequelize.STRING(255),
    allowNull: true,
    defaultValue: null,
  });

  await addColumnIfMissing('card_type', {
    type: Sequelize.STRING(50),
    allowNull: false,
    defaultValue: 'LARGE_IMAGE',
  });

  await addColumnIfMissing('region', {
    type: Sequelize.STRING(100),
    allowNull: true,
    defaultValue: null,
  });

  await addColumnIfMissing('province', {
    type: Sequelize.STRING(100),
    allowNull: true,
    defaultValue: null,
  });

  await addColumnIfMissing('district', {
    type: Sequelize.STRING(100),
    allowNull: true,
    defaultValue: null,
  });

  await addColumnIfMissing('distance_km', {
    type: Sequelize.DECIMAL(6, 2),
    allowNull: true,
    defaultValue: null,
  });

  const indexes = await queryInterface.showIndex('fields');
  const indexNames = new Set(indexes.map((idx) => idx.name));

  if (!indexNames.has('idx_fields_sport_id')) {
    await queryInterface.addIndex('fields', ['sport_id'], {
      name: 'idx_fields_sport_id',
    });
  }

  if (!indexNames.has('idx_fields_province')) {
    await queryInterface.addIndex('fields', ['province'], {
      name: 'idx_fields_province',
    });
  }

  if (!indexNames.has('idx_fields_district')) {
    await queryInterface.addIndex('fields', ['district'], {
      name: 'idx_fields_district',
    });
  }
};

const ensureSportTypeRows = async (queryInterface) => {
  const sportNameToId = {};

  for (const sportName of Object.values(SPORT_NAME_BY_ICON)) {
    const [rows] = await queryInterface.sequelize.query(
      'SELECT sport_id FROM sport_types WHERE sport_name = ? LIMIT 1',
      { replacements: [sportName] },
    );

    if (!rows || rows.length === 0) {
      await queryInterface.sequelize.query(
        'INSERT INTO sport_types (sport_name, created_at, updated_at) VALUES (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)',
        { replacements: [sportName] },
      );

      const [createdRows] = await queryInterface.sequelize.query(
        'SELECT sport_id FROM sport_types WHERE sport_name = ? LIMIT 1',
        { replacements: [sportName] },
      );
      sportNameToId[sportName] = createdRows[0].sport_id;
    } else {
      sportNameToId[sportName] = rows[0].sport_id;
    }
  }

  return sportNameToId;
};

const upsertMockFields = async (queryInterface, sportNameToId) => {
  for (const item of FIELD_ROWS) {
    const sportName = SPORT_NAME_BY_ICON[item.sport_icon_type] || 'Bóng đá';
    const sportId = sportNameToId[sportName] || null;
    const slotPrice = parsePrice(item.price, item.sport_icon_type);
    const [openTime, closeTime] = OPEN_CLOSE_BY_SPORT_ICON[item.sport_icon_type] || [
      '06:00:00',
      '23:00:00',
    ];
    const tags = TAGS_BY_SPORT_ICON[item.sport_icon_type] || [];
    const { region, province, district } = splitLocation(item.location);
    const featured = Number(item.rating) >= 4.7 || item.order % 9 === 0;
    const availabilityNote = item.order % 4 === 0 ? 'Còn sân tối nay' : null;
    const distanceKm = DISTANCE_BY_ORDER[item.order % DISTANCE_BY_ORDER.length];
    const avatarImageUrl = `/images/fields/${item.sport_icon_type.toLowerCase()}-avatar.svg`;
    const cardImageUrl = `/images/fields/${item.sport_icon_type.toLowerCase()}-card.svg`;

    const [existingRows] = await queryInterface.sequelize.query(
      'SELECT field_id FROM fields WHERE field_name = ? LIMIT 1',
      { replacements: [item.name] },
    );

    let fieldId;

    if (!existingRows || existingRows.length === 0) {
      await queryInterface.sequelize.query(
        `
        INSERT INTO fields (
          manager_id,
          field_name,
          location,
          latitude,
          longitude,
          open_time,
          close_time,
          slot_minutes,
          slot_price,
          status,
          sport_id,
          display_rating,
          featured,
          availability_note,
          card_type,
          region,
          province,
          district,
          distance_km,
          avatar_image_url,
          card_image_url,
          created_at,
          updated_at
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        `,
        {
          replacements: [
            null,
            item.name,
            item.location,
            item.latitude,
            item.longitude,
            openTime,
            closeTime,
            60,
            slotPrice,
            'active',
            sportId,
            item.rating,
            featured,
            availabilityNote,
            'LARGE_IMAGE',
            region,
            province,
            district,
            distanceKm,
            avatarImageUrl,
            cardImageUrl,
          ],
        },
      );

      const [createdRows] = await queryInterface.sequelize.query(
        'SELECT field_id FROM fields WHERE field_name = ? LIMIT 1',
        { replacements: [item.name] },
      );
      fieldId = createdRows[0].field_id;
    } else {
      fieldId = existingRows[0].field_id;

      await queryInterface.sequelize.query(
        `
        UPDATE fields
        SET
          location = ?,
          latitude = ?,
          longitude = ?,
          open_time = ?,
          close_time = ?,
          slot_minutes = ?,
          slot_price = ?,
          status = 'active',
          sport_id = ?,
          display_rating = ?,
          featured = ?,
          availability_note = ?,
          card_type = ?,
          region = ?,
          province = ?,
          district = ?,
          distance_km = ?,
          avatar_image_url = COALESCE(avatar_image_url, ?),
          card_image_url = COALESCE(card_image_url, ?),
          updated_at = CURRENT_TIMESTAMP
        WHERE field_id = ?
        `,
        {
          replacements: [
            item.location,
            item.latitude,
            item.longitude,
            openTime,
            closeTime,
            60,
            slotPrice,
            sportId,
            item.rating,
            featured,
            availabilityNote,
            'LARGE_IMAGE',
            region,
            province,
            district,
            distanceKm,
            avatarImageUrl,
            cardImageUrl,
            fieldId,
          ],
        },
      );
    }

    await queryInterface.sequelize.query('DELETE FROM field_tags WHERE field_id = ?', {
      replacements: [fieldId],
    });

    for (let i = 0; i < tags.length; i += 1) {
      await queryInterface.sequelize.query(
        `INSERT INTO field_tags (field_id, tag_name, sort_order, created_at)
         VALUES (?, ?, ?, CURRENT_TIMESTAMP)`,
        {
          replacements: [fieldId, tags[i], i],
        },
      );
    }
  }
};

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    await ensureSportTypesTable(queryInterface, Sequelize);
    await ensureFieldColumns(queryInterface, Sequelize);
    await ensureFieldTagsTable(queryInterface, Sequelize);

    const sportNameToId = await ensureSportTypeRows(queryInterface);
    await upsertMockFields(queryInterface, sportNameToId);
  },

  async down(queryInterface) {
    for (const item of FIELD_ROWS) {
      const [rows] = await queryInterface.sequelize.query(
        'SELECT field_id FROM fields WHERE field_name = ? LIMIT 1',
        { replacements: [item.name] },
      );

      if (!rows || rows.length === 0) {
        continue;
      }

      const fieldId = rows[0].field_id;
      await queryInterface.sequelize.query('DELETE FROM field_tags WHERE field_id = ?', {
        replacements: [fieldId],
      });
      await queryInterface.sequelize.query('DELETE FROM fields WHERE field_id = ?', {
        replacements: [fieldId],
      });
    }
  },
};
