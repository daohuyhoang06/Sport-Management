import sequelize from "../../config/database.js";
import { getFirebaseMessaging } from "../../config/firebaseAdmin.js";

const TOKEN_INVALID_CODES = new Set([
  "messaging/invalid-registration-token",
  "messaging/registration-token-not-registered",
]);

const chunk = (items, size) => {
  const chunks = [];
  for (let index = 0; index < items.length; index += size) {
    chunks.push(items.slice(index, index + size));
  }
  return chunks;
};

const normalizePayloadValue = (value) => {
  if (value === null || value === undefined) return "";
  if (typeof value === "object") return JSON.stringify(value);
  return String(value);
};

export const registerDeviceToken = async ({
  userId,
  fcmToken,
  platform = "android",
  appVersion = null,
  deviceId = null,
}) => {
  const normalizedUserId = Number.parseInt(userId, 10);
  const token = String(fcmToken || "").trim();

  if (!Number.isInteger(normalizedUserId) || normalizedUserId <= 0 || !token) {
    const error = new Error("INVALID_DEVICE_TOKEN");
    error.code = "INVALID_DEVICE_TOKEN";
    throw error;
  }

  await sequelize.query(
    `INSERT INTO user_device_tokens
      (user_id, fcm_token, platform, app_version, device_id, last_seen_at, created_at, updated_at)
     VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
     ON DUPLICATE KEY UPDATE
       user_id = VALUES(user_id),
       platform = VALUES(platform),
       app_version = VALUES(app_version),
       device_id = VALUES(device_id),
       last_seen_at = CURRENT_TIMESTAMP,
       updated_at = CURRENT_TIMESTAMP`,
    {
      replacements: [
        normalizedUserId,
        token,
        String(platform || "android").slice(0, 32),
        appVersion ? String(appVersion).slice(0, 64) : null,
        deviceId ? String(deviceId).slice(0, 128) : null,
      ],
    },
  );
};

export const removeDeviceToken = async ({ userId, fcmToken }) => {
  const normalizedUserId = Number.parseInt(userId, 10);
  const token = String(fcmToken || "").trim();
  if (!Number.isInteger(normalizedUserId) || normalizedUserId <= 0 || !token) {
    return;
  }

  await sequelize.query(
    "DELETE FROM user_device_tokens WHERE user_id = ? AND fcm_token = ?",
    { replacements: [normalizedUserId, token] },
  );
};

const listUserTokens = async (userId) => {
  const [rows] = await sequelize.query(
    `SELECT fcm_token
     FROM user_device_tokens
     WHERE user_id = ?
     ORDER BY last_seen_at DESC`,
    { replacements: [userId] },
  );
  return (rows || []).map((row) => row.fcm_token).filter(Boolean);
};

const deleteInvalidTokens = async (tokens) => {
  if (!tokens.length) return;
  await sequelize.query(
    `DELETE FROM user_device_tokens
     WHERE fcm_token IN (${tokens.map(() => "?").join(", ")})`,
    { replacements: tokens },
  );
};

export const sendPushToUser = async ({
  userId,
  title,
  body,
  data = {},
}) => {
  const messaging = getFirebaseMessaging();
  if (!messaging) {
    return { sent: 0, skipped: true };
  }

  const normalizedUserId = Number.parseInt(userId, 10);
  if (!Number.isInteger(normalizedUserId) || normalizedUserId <= 0) {
    return { sent: 0, skipped: true };
  }

  const tokens = await listUserTokens(normalizedUserId);
  if (!tokens.length) {
    return { sent: 0, skipped: true };
  }

  let sent = 0;
  const invalidTokens = [];
  const normalizedData = Object.fromEntries(
    Object.entries(data).map(([key, value]) => [key, normalizePayloadValue(value)]),
  );

  for (const tokenChunk of chunk(tokens, 500)) {
    const response = await messaging.sendEachForMulticast({
      tokens: tokenChunk,
      notification: {
        title: title || "Sport Management",
        body: body || "",
      },
      data: normalizedData,
      android: {
        priority: "high",
        notification: {
          channelId: "sport_updates",
          sound: "default",
          clickAction: "OPEN_NOTIFICATION",
        },
      },
    });

    sent += response.successCount || 0;
    response.responses?.forEach((item, index) => {
      const code = item.error?.code;
      if (code && TOKEN_INVALID_CODES.has(code)) {
        invalidTokens.push(tokenChunk[index]);
      }
    });
  }

  await deleteInvalidTokens(invalidTokens);
  return { sent, skipped: false };
};

export const sendNotificationPush = async ({
  userId,
  type,
  title,
  subtitle,
  content,
  targetType = null,
  targetId = null,
  bookingId = null,
  fieldId = null,
  notificationId = null,
}) => {
  return sendPushToUser({
    userId,
    title,
    body: subtitle || content || "",
    data: {
      type,
      targetType,
      targetId,
      bookingId,
      fieldId,
      notificationId,
    },
  }).catch((error) => {
    console.error("send notification push error:", error.message);
    return { sent: 0, skipped: true, error: error.message };
  });
};
