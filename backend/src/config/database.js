import { Sequelize } from "sequelize";
import dotenv from "dotenv";

dotenv.config();

const dbDialect = (
  process.env.DB_DIALECT ||
  process.env.DB_CLIENT ||
  (process.env.DATABASE_URL ? "postgres" : "mysql")
).toLowerCase();
const defaultPort = dbDialect === "postgres" ? 5432 : 3306;
const useSsl =
  process.env.NODE_ENV === "production" && dbDialect === "postgres";

// Support both direct connection string and individual parameters
const sequelize = process.env.DATABASE_URL
  ? new Sequelize(process.env.DATABASE_URL, {
      dialect: dbDialect,
      dialectOptions: useSsl
        ? {
            ssl: {
              require: true,
              rejectUnauthorized: false,
            },
          }
        : {},
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
      process.env.DB_NAME || "postgres",
      process.env.DB_USER || "postgres",
      process.env.DB_PASSWORD || "",
      {
        host: process.env.DB_HOST || "localhost",
        port: Number(process.env.DB_PORT) || defaultPort,
        dialect: dbDialect,
        dialectOptions: useSsl
          ? {
              ssl: {
                require: true,
                rejectUnauthorized: false,
              },
            }
          : {},
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
