import "./config/dotenv.js";
import sequelize from "./config/database.js"; // Initialize database connection
import express from "express";
import morgan from "morgan";
import cors from "cors";
import path from "path";
import swaggerUi from "swagger-ui-express";
import { fileURLToPath } from "url";
import swaggerSpec from "./config/swagger.js";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
import adminRoutes from "./routes/admin/adminRoutes.js";
import managerRoutes from "./routes/manager/managerRoutes.js";
import userRoutes from "./routes/user/userRoutes.js";
import authRoutes from "./routes/user/authRoutes.js";
import chatRoutes from "./routes/chatRoutes.js";
import aiRoutes from "./routes/aiRoutes.js";

const app = express();
app.use(cors());
app.use(express.json());
app.use(morgan("dev"));

app.use("/api/docs", swaggerUi.serve, swaggerUi.setup(swaggerSpec));
app.get("/api/docs-json", (_, res) => res.json(swaggerSpec));

// Serve uploaded files statically
app.use("/uploads", express.static(path.join(__dirname, "../public/uploads")));

app.use("/api/admin", adminRoutes);
app.use("/api/manager", managerRoutes);
app.use("/api/user", userRoutes);
app.use("/api/auth", authRoutes);
app.use("/api/users", userRoutes);
app.use("/api/chat", chatRoutes);
app.use("/api/ai", aiRoutes);

app.get("/", (_, res) =>
  res.json({
    name: "sport-management-backend",
    version: "0.1.0",
    status: "running",
    endpoints: {
      auth: "/api/auth",
      user: "/api/user",
      admin: "/api/admin",
      manager: "/api/manager",
      docs: "/api/docs",
    },
  }),
);

// Health check endpoint with database status
app.get("/health", (req, res) => {
  res.json({
    status: "OK",
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
  });
});

app.get("/api/health", async (req, res) => {
  try {
    await sequelize.authenticate();
    res.json({
      status: "OK",
      database: "Connected",
      timestamp: new Date().toISOString(),
      uptime: process.uptime(),
    });
  } catch (error) {
    res.status(500).json({
      status: "ERROR",
      database: "Disconnected",
      error: error.message,
      timestamp: new Date().toISOString(),
    });
  }
});

export default app;
