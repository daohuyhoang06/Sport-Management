import "./config/dotenv.js";
import sequelize from "./config/database.js"; // Initialize database connection
import express from "express";
import morgan from "morgan";
import cors from "cors";
import path from "path";
import swaggerUi from "swagger-ui-express";
import { fileURLToPath } from "url";
import swaggerSpec from "./config/swagger.js";
import { releaseExpiredPendingBookings } from "./services/user/scheduleService.js";
import { startUpcomingBookingReminderJob } from "./services/user/bookingReminderService.js";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
import adminRoutes from "./routes/admin/adminRoutes.js";
import managerRoutes from "./routes/manager/managerRoutes.js";
import userRoutes from "./routes/user/userRoutes.js";
import authRoutes from "./routes/user/authRoutes.js";
import chatRoutes from "./routes/chatRoutes.js";
import aiRoutes from "./routes/aiRoutes.js";
import paymentRoutes from "./routes/paymentRoutes.js";
import { searchFields } from "./controllers/user/fieldController.js";
import {
  getPublicBookingShare,
  renderPublicBookingSharePage,
} from "./controllers/public/bookingShareController.js";

const app = express();
app.use(cors());
app.use(express.json());
app.use(morgan("dev"));

app.use("/api/docs", swaggerUi.serve, swaggerUi.setup(swaggerSpec));
app.get("/api/docs-json", (_, res) => res.json(swaggerSpec));

// Serve uploaded files statically
app.use("/uploads", express.static(path.join(__dirname, "../public/uploads")));
app.use("/images", express.static(path.join(__dirname, "../public/images")));

app.use("/api/admin", adminRoutes);
app.use("/api/manager", managerRoutes);
app.use("/api/user", userRoutes);
app.use("/api/auth", authRoutes);
app.use("/api/users", userRoutes);
app.get("/api/fields/search", searchFields);
app.use("/api/chat", chatRoutes);
app.use("/api/ai", aiRoutes);
app.use("/api/payments", paymentRoutes);
app.get("/api/public/bookings/:shareToken", getPublicBookingShare);

app.get("/l/field/:fieldId", (req, res) => {
  const fieldId = Number.parseInt(req.params.fieldId, 10);
  if (!Number.isInteger(fieldId) || fieldId <= 0) {
    res.status(400).send("Invalid field id");
    return;
  }

  const deepLinkScheme = process.env.APP_DEEP_LINK_SCHEME || "sportmanagement";
  const downloadUrl =
    process.env.APP_DOWNLOAD_URL ||
    "https://play.google.com/store/apps/details?id=com.sportmanagement.user";
  const deepLink = `${deepLinkScheme}://field/${fieldId}`;

  res.setHeader("Content-Type", "text/html; charset=utf-8");
  res.send(`<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Open Sport Management</title>
    <style>
      body { font-family: Arial, sans-serif; padding: 24px; background: #f7f9fc; color: #0f172a; }
      .card { max-width: 520px; margin: 40px auto; background: #fff; border-radius: 16px; padding: 20px; box-shadow: 0 8px 24px rgba(15,23,42,0.08); }
      .btn { display: inline-block; margin-right: 10px; margin-top: 12px; padding: 10px 14px; border-radius: 10px; text-decoration: none; }
      .btn-primary { background: #1d4ed8; color: #fff; }
      .btn-secondary { border: 1px solid #cbd5e1; color: #1e293b; }
      p { line-height: 1.5; }
    </style>
    <script>
      (function () {
        var deepLink = ${JSON.stringify(deepLink)};
        var fallback = ${JSON.stringify(downloadUrl)};
        window.location.href = deepLink;
        setTimeout(function () {
          window.location.href = fallback;
        }, 1200);
      })();
    </script>
  </head>
  <body>
    <div class="card">
      <h2>Opening app...</h2>
      <p>If the app is installed, this page will open field detail directly.</p>
      <p>If it is not installed, tap Download to install the app.</p>
      <a class="btn btn-primary" href="${deepLink}">Open app</a>
      <a class="btn btn-secondary" href="${downloadUrl}">Download app</a>
    </div>
  </body>
</html>`);
});

app.get("/l/booking/:shareToken", renderPublicBookingSharePage);

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

// Auto-release expired pending bookings every 30 seconds.
setInterval(async () => {
  try {
    await releaseExpiredPendingBookings();
  } catch (error) {
    console.error("releaseExpiredPendingBookings interval error:", error.message);
  }
}, 30 * 1000);

// Create reminders for user bookings starting within the next 60 minutes.
startUpcomingBookingReminderJob({
  intervalMs: 60 * 1000,
  runImmediately: true,
});

export default app;
