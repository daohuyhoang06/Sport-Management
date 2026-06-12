import { Sequelize } from "sequelize";
import dotenv from "dotenv";

dotenv.config();

// FIX: force MySQL correctly (no auto postgres detect)
const dbDialect = (process.env.DB_DIALECT || "mysql").toLowerCase();

const defaultPort = 3306;

// MySQL normally does NOT need SSL in local/dev
const useSsl = process.env.NODE_ENV === "production";

// Support both direct connection string and individual parameters
const sequelize = process.env.DATABASE_URL
  ? new Sequelize(process.env.DATABASE_URL, {
      dialect: dbDialect,
      dialectOptions: {
        charset: "utf8mb4",
      },
      logging: false,
      pool: {
        max: 5,
        min: 0,
        acquire: 30000,
        idle: 10000,
      },
      define: {
        timestamps: false,
        underscored: false,
      },
    })
  : new Sequelize(
      process.env.DB_NAME || "sport_management",
      process.env.DB_USER || "root",
      process.env.DB_PASSWORD || "",
      {
        host: process.env.DB_HOST || "localhost",
        port: Number(process.env.DB_PORT) || defaultPort,
        dialect: dbDialect,
        dialectOptions: {
          charset: "utf8mb4",
          ...(useSsl
            ? {
                ssl: {
                  require: true,
                  rejectUnauthorized: false,
                },
              }
            : {}),
        },

        logging: false,

        pool: {
          max: 5,
          min: 0,
          acquire: 30000,
          idle: 10000,
        },

        define: {
          timestamps: false,
          underscored: false,
        },
      },
    );

// Test connection
sequelize
  .authenticate()
  .then(() => {
    console.log("✅ Database connection established successfully.");
  })
  .catch((err) => {
    console.error("❌ Unable to connect to the database:", err);
  });

export default sequelize;
