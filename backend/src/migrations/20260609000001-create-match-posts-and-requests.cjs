"use strict";

const matchPostTable = "match_posts";
const matchRequestTable = "match_requests";

const tableExists = async (queryInterface, Sequelize, tableName) => {
  const result = await queryInterface.sequelize.query(
    "SELECT COUNT(*) AS count FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
    {
      replacements: [tableName],
      type: Sequelize.QueryTypes.SELECT,
    },
  );
  return Number(result?.[0]?.count || 0) > 0;
};

module.exports = {
  async up(queryInterface, Sequelize) {
    const matchPostExists = await tableExists(
      queryInterface,
      Sequelize,
      matchPostTable,
    );

    if (!matchPostExists) {
      await queryInterface.createTable(matchPostTable, {
        match_post_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          primaryKey: true,
          autoIncrement: true,
        },
        booking_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: "bookings",
            key: "booking_id",
          },
          onDelete: "CASCADE",
          onUpdate: "CASCADE",
          unique: true,
        },
        field_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: "fields",
            key: "field_id",
          },
          onDelete: "CASCADE",
          onUpdate: "CASCADE",
        },
        owner_user_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: "person",
            key: "person_id",
          },
          onDelete: "CASCADE",
          onUpdate: "CASCADE",
        },
        team_name: {
          type: Sequelize.STRING(120),
          allowNull: false,
        },
        player_count: {
          type: Sequelize.INTEGER,
          allowNull: false,
        },
        level: {
          type: Sequelize.STRING(30),
          allowNull: false,
        },
        description: {
          type: Sequelize.TEXT,
          allowNull: true,
        },
        status: {
          type: Sequelize.STRING(20),
          allowNull: false,
          defaultValue: "OPEN",
        },
        matched_request_id: {
          type: Sequelize.INTEGER,
          allowNull: true,
        },
        matched_user_id: {
          type: Sequelize.INTEGER,
          allowNull: true,
          references: {
            model: "person",
            key: "person_id",
          },
          onDelete: "SET NULL",
          onUpdate: "CASCADE",
        },
        created_at: {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal("CURRENT_TIMESTAMP"),
        },
        updated_at: {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal("CURRENT_TIMESTAMP"),
        },
      });

      await queryInterface.addIndex(matchPostTable, ["booking_id"], {
        name: "match_posts_booking_id",
        unique: true,
      });
      await queryInterface.addIndex(matchPostTable, ["field_id", "status"], {
        name: "match_posts_field_status",
      });
      await queryInterface.addIndex(matchPostTable, ["owner_user_id"], {
        name: "match_posts_owner_user_id",
      });
    }

    const matchRequestExists = await tableExists(
      queryInterface,
      Sequelize,
      matchRequestTable,
    );

    if (!matchRequestExists) {
      await queryInterface.createTable(matchRequestTable, {
        match_request_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          primaryKey: true,
          autoIncrement: true,
        },
        match_post_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: matchPostTable,
            key: "match_post_id",
          },
          onDelete: "CASCADE",
          onUpdate: "CASCADE",
        },
        booking_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: "bookings",
            key: "booking_id",
          },
          onDelete: "CASCADE",
          onUpdate: "CASCADE",
        },
        requester_user_id: {
          type: Sequelize.INTEGER,
          allowNull: false,
          references: {
            model: "person",
            key: "person_id",
          },
          onDelete: "CASCADE",
          onUpdate: "CASCADE",
        },
        team_name: {
          type: Sequelize.STRING(120),
          allowNull: false,
        },
        player_count: {
          type: Sequelize.INTEGER,
          allowNull: false,
        },
        message: {
          type: Sequelize.TEXT,
          allowNull: true,
        },
        status: {
          type: Sequelize.STRING(20),
          allowNull: false,
          defaultValue: "PENDING",
        },
        responded_at: {
          type: Sequelize.DATE,
          allowNull: true,
        },
        created_at: {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal("CURRENT_TIMESTAMP"),
        },
        updated_at: {
          type: Sequelize.DATE,
          allowNull: false,
          defaultValue: Sequelize.literal("CURRENT_TIMESTAMP"),
        },
      });

      await queryInterface.addIndex(matchRequestTable, ["match_post_id", "status"], {
        name: "match_requests_post_status",
      });
      await queryInterface.addIndex(matchRequestTable, ["booking_id"], {
        name: "match_requests_booking_id",
      });
      await queryInterface.addIndex(matchRequestTable, ["requester_user_id"], {
        name: "match_requests_requester_user_id",
      });
      await queryInterface.addIndex(
        matchRequestTable,
        ["match_post_id", "requester_user_id"],
        {
          name: "match_requests_unique_requester",
          unique: true,
        },
      );
    }
  },

  async down(queryInterface) {
    await queryInterface.dropTable(matchRequestTable);
    await queryInterface.dropTable(matchPostTable);
  },
};
