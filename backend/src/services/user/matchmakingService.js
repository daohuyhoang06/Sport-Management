import sequelize from "../../config/database.js";
import { ACTIVE_BOOKING_STATUS_CONDITION } from "../bookingSlotService.js";
import { sendNotificationPush } from "./pushNotificationService.js";

const MATCH_LOG_PREFIX = "[matchmaking]";

const logMatchInfo = (message, data = {}) => {
  console.log(`${MATCH_LOG_PREFIX} ${message}`, data);
};

const logMatchWarn = (message, data = {}) => {
  console.warn(`${MATCH_LOG_PREFIX} ${message}`, data);
};

const MATCH_LEVELS = {
  BEGINNER: "Mới chơi",
  INTERMEDIATE: "Trung bình",
  ADVANCED: "Khá",
  PRO: "Chuyên nghiệp",
};

const buildInClause = (items) => items.map(() => "?").join(", ");

const normalizeLevel = (value) => {
  const normalized = String(value || "").trim().toUpperCase();
  if (normalized in MATCH_LEVELS) {
    return normalized;
  }

  const aliases = {
    "MỚI CHƠI": "BEGINNER",
    "MOI CHOI": "BEGINNER",
    "TRUNG BÌNH": "INTERMEDIATE",
    "TRUNG BINH": "INTERMEDIATE",
    "KHÁ": "ADVANCED",
    KHA: "ADVANCED",
    "CHUYÊN NGHIỆP": "PRO",
    "CHUYEN NGHIEP": "PRO",
  };

  return aliases[normalized] || null;
};

const formatLevelLabel = (level) => MATCH_LEVELS[level] || MATCH_LEVELS.INTERMEDIATE;

const normalizeText = (value, maxLength) => {
  const text = String(value || "").trim();
  if (!text) return "";
  return text.slice(0, maxLength);
};

export const normalizeMatchPostPayload = (payload) => {
  if (!payload || payload.enabled === false) {
    return null;
  }

  const teamName = normalizeText(payload.team_name ?? payload.teamName, 120);
  const playerCount = Number.parseInt(
    payload.player_count ?? payload.playerCount,
    10,
  );
  const level = normalizeLevel(payload.level);
  const description = normalizeText(payload.description, 1000);

  if (!teamName) {
    const error = new Error("TEAM_NAME_REQUIRED");
    error.code = "TEAM_NAME_REQUIRED";
    throw error;
  }

  if (!Number.isInteger(playerCount) || playerCount <= 0 || playerCount > 100) {
    const error = new Error("INVALID_PLAYER_COUNT");
    error.code = "INVALID_PLAYER_COUNT";
    throw error;
  }

  if (!level) {
    const error = new Error("INVALID_MATCH_LEVEL");
    error.code = "INVALID_MATCH_LEVEL";
    throw error;
  }

  return {
    teamName,
    playerCount,
    level,
    levelLabel: formatLevelLabel(level),
    description,
  };
};

export const createMatchPostForBooking = async ({
  bookingId,
  fieldId,
  ownerUserId,
  payload,
  transaction,
}) => {
  if (!payload) {
    return null;
  }

  await sequelize.query(
    `INSERT INTO match_posts
      (booking_id, field_id, owner_user_id, team_name, player_count, level, description, status, created_at, updated_at)
     VALUES (?, ?, ?, ?, ?, ?, ?, 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)`,
    {
      replacements: [
        bookingId,
        fieldId,
        ownerUserId,
        payload.teamName,
        payload.playerCount,
        payload.level,
        payload.description || null,
      ],
      transaction,
    },
  );

  const [rows] = await sequelize.query(
    `SELECT
      match_post_id,
      booking_id,
      field_id,
      owner_user_id,
      team_name,
      player_count,
      level,
      description,
      status
     FROM match_posts
     WHERE booking_id = ?
     LIMIT 1`,
    { replacements: [bookingId], transaction },
  );

  return rows?.[0] || null;
};

const mapMatchPostPreview = (row) => ({
  matchPostId: Number(row.match_post_id),
  bookingId: Number(row.booking_id),
  fieldId: Number(row.field_id),
  courtId: row.court_id == null ? "" : String(row.court_id),
  startTime: new Date(row.start_time).toISOString().substring(11, 16),
  endTime: new Date(row.end_time).toISOString().substring(11, 16),
  teamName: row.team_name || "",
  playerCount: Number(row.player_count || 0),
  level: row.level || "INTERMEDIATE",
  levelLabel: formatLevelLabel(row.level),
  description: row.description || "",
  status: row.status || "OPEN",
});

export const listOpenMatchPostPreviewsForFieldDate = async (
  fieldId,
  date,
) => {
  const [rows] = await sequelize.query(
    `SELECT
      mp.match_post_id,
      mp.booking_id,
      mp.field_id,
      mp.team_name,
      mp.player_count,
      mp.level,
      mp.description,
      mp.status,
      bs.court_id,
      bs.start_time,
      bs.end_time
     FROM match_posts mp
     INNER JOIN bookings b ON b.booking_id = mp.booking_id
     INNER JOIN booking_slots bs ON bs.booking_id = mp.booking_id
     WHERE mp.field_id = ?
       AND DATE(bs.start_time) = ?
       AND mp.status = 'OPEN'
       AND ${ACTIVE_BOOKING_STATUS_CONDITION}
     ORDER BY bs.start_time ASC, bs.end_time ASC, mp.match_post_id ASC`,
    { replacements: [fieldId, date] },
  );

  return rows.map(mapMatchPostPreview);
};

const insertNotification = async ({
  userId,
  type,
  title,
  subtitle,
  content,
  targetType = null,
  targetId = null,
  bookingId = null,
  fieldId = null,
  metadata = null,
  transaction = null,
}) => {
  const [result] = await sequelize.query(
    `INSERT INTO notifications
      (user_id, type, section, title, subtitle, content, target_type, target_id, booking_id, field_id, is_read, metadata, created_at, updated_at)
     VALUES (?, ?, 'activity', ?, ?, ?, ?, ?, ?, ?, 0, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)`,
    {
      replacements: [
        userId,
        type,
        title,
        subtitle,
        content,
        targetType,
        targetId,
        bookingId,
        fieldId,
        metadata ? JSON.stringify(metadata) : null,
      ],
      transaction,
    },
  );

  if (Number(result?.affectedRows || 0) <= 0) {
    return;
  }

  const sendPush = () =>
    sendNotificationPush({
      userId,
      type,
      title,
      subtitle,
      content,
      targetType,
      targetId,
      bookingId,
      fieldId,
    });

  if (transaction?.afterCommit) {
    transaction.afterCommit(() => {
      sendPush().catch((error) => {
        console.warn(`${MATCH_LOG_PREFIX} push notification failed`, {
          type,
          userId,
          error: error.message,
        });
      });
    });
    return;
  }

  await sendPush();
};

const getMatchPostForAction = async ({
  matchPostId,
  lock = false,
  transaction = null,
}) => {
  const [rows] = await sequelize.query(
    `SELECT
      mp.match_post_id,
      mp.booking_id,
      mp.field_id,
      mp.owner_user_id,
      mp.team_name,
      mp.player_count,
      mp.level,
      mp.description,
      mp.status,
      b.status AS booking_status,
      b.pending_expires_at
     FROM match_posts mp
     INNER JOIN bookings b ON b.booking_id = mp.booking_id
     WHERE mp.match_post_id = ?
     LIMIT 1${lock ? " FOR UPDATE" : ""}`,
    { replacements: [matchPostId], transaction },
  );
  return rows?.[0] || null;
};

export const createMatchRequestForPost = async ({
  matchPostId,
  requesterUserId,
  teamName,
  playerCount,
  level,
  message,
}) => {
  const normalizedTeamName = normalizeText(teamName, 120);
  const normalizedPlayerCount = Number.parseInt(playerCount, 10);
  const normalizedLevel = normalizeLevel(level);
  const normalizedMessage = normalizeText(message, 1000);

  if (!normalizedTeamName) {
    const error = new Error("REQUEST_TEAM_NAME_REQUIRED");
    error.code = "REQUEST_TEAM_NAME_REQUIRED";
    throw error;
  }

  if (
    !Number.isInteger(normalizedPlayerCount) ||
    normalizedPlayerCount <= 0 ||
    normalizedPlayerCount > 100
  ) {
    const error = new Error("INVALID_REQUEST_PLAYER_COUNT");
    error.code = "INVALID_REQUEST_PLAYER_COUNT";
    throw error;
  }

  if (!normalizedLevel) {
    const error = new Error("INVALID_MATCH_LEVEL");
    error.code = "INVALID_MATCH_LEVEL";
    throw error;
  }

  return sequelize.transaction(async (transaction) => {
    const post = await getMatchPostForAction({
      matchPostId,
      lock: true,
      transaction,
    });

    if (!post) {
      const error = new Error("MATCH_POST_NOT_FOUND");
      error.code = "MATCH_POST_NOT_FOUND";
      throw error;
    }

    if (Number(post.owner_user_id) === Number(requesterUserId)) {
      const error = new Error("CANNOT_REQUEST_OWN_POST");
      error.code = "CANNOT_REQUEST_OWN_POST";
      throw error;
    }

    if (String(post.status || "").toUpperCase() !== "OPEN") {
      const error = new Error("MATCH_POST_NOT_OPEN");
      error.code = "MATCH_POST_NOT_OPEN";
      throw error;
    }

    const [existingRows] = await sequelize.query(
      `SELECT match_request_id, status
       FROM match_requests
       WHERE match_post_id = ? AND requester_user_id = ?
       LIMIT 1`,
      {
        replacements: [matchPostId, requesterUserId],
        transaction,
      },
    );

    const existing = existingRows?.[0];
    if (existing && String(existing.status || "").toUpperCase() === "PENDING") {
      const error = new Error("MATCH_REQUEST_ALREADY_PENDING");
      error.code = "MATCH_REQUEST_ALREADY_PENDING";
      throw error;
    }

    if (existing) {
      await sequelize.query(
        `UPDATE match_requests
         SET team_name = ?, player_count = ?, level = ?, message = ?, status = 'PENDING', updated_at = CURRENT_TIMESTAMP
         WHERE match_request_id = ?`,
        {
          replacements: [
            normalizedTeamName,
            normalizedPlayerCount,
            normalizedLevel,
            normalizedMessage || null,
            existing.match_request_id,
          ],
          transaction,
        },
      );
    } else {
      await sequelize.query(
        `INSERT INTO match_requests
          (match_post_id, booking_id, requester_user_id, team_name, player_count, level, message, status, created_at, updated_at)
         VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)`,
        {
          replacements: [
            matchPostId,
            post.booking_id,
            requesterUserId,
            normalizedTeamName,
            normalizedPlayerCount,
            normalizedLevel,
            normalizedMessage || null,
          ],
          transaction,
        },
      );
    }

    const [requestRows] = await sequelize.query(
      `SELECT
        mr.match_request_id,
        mr.match_post_id,
        mr.booking_id,
        mr.requester_user_id,
        mr.team_name,
        mr.player_count,
        mr.level,
        mr.message,
        mr.status,
        mr.created_at,
        p.username AS requester_username
       FROM match_requests mr
       LEFT JOIN person p ON p.person_id = mr.requester_user_id
       WHERE mr.match_post_id = ? AND mr.requester_user_id = ?
       LIMIT 1`,
      {
        replacements: [matchPostId, requesterUserId],
        transaction,
      },
    );

    await insertNotification({
      userId: Number(post.owner_user_id),
      type: "match_request_received",
      title: `${normalizedTeamName} muốn ghép trận với bạn`,
      subtitle: post.team_name || "Tìm đối thủ",
      content: normalizedMessage || `Đội ${normalizedTeamName} muốn giao lưu với bạn.`,
      targetType: "match_request",
      targetId: Number(requestRows?.[0]?.match_request_id || 0),
      bookingId: Number(post.booking_id),
      fieldId: Number(post.field_id),
      metadata: {
        icon: "match_request_received",
        matchRequestId: Number(requestRows?.[0]?.match_request_id || 0),
        peerUserId: Number(requesterUserId),
      },
      transaction,
    });

    return requestRows?.[0] || null;
  });
};

const mapMatchRequestRow = (row, statusOverride = null) => ({
  matchRequestId: Number(row.match_request_id),
  matchPostId: Number(row.match_post_id),
  bookingId: Number(row.booking_id),
  requesterUserId: Number(row.requester_user_id),
  requesterUsername: row.requester_username || "",
  teamName: row.team_name || "",
  playerCount: Number(row.player_count || 0),
  level: row.level || "INTERMEDIATE",
  levelLabel: formatLevelLabel(row.level),
  message: row.message || "",
  status: statusOverride || row.status || "PENDING",
  createdAt: row.created_at,
});

const ensurePeerConversationForAcceptedMatch = async ({
  bookingId,
  ownerUserId,
  requesterUserId,
  transaction,
}) => {
  const normalizedBookingId = Number(bookingId);
  const normalizedOwnerUserId = Number(ownerUserId);
  const normalizedRequesterUserId = Number(requesterUserId);

  if (
    !Number.isInteger(normalizedBookingId) ||
    !Number.isInteger(normalizedOwnerUserId) ||
    !Number.isInteger(normalizedRequesterUserId)
  ) {
    return null;
  }

  const [existingRows] = await sequelize.query(
    `SELECT chat_id
     FROM chats
     WHERE booking_id = ?
       AND field_id IS NULL
       AND (
         (user_id = ? AND manager_id = ?)
         OR (user_id = ? AND manager_id = ?)
       )
     ORDER BY chat_id DESC
     LIMIT 1`,
    {
      replacements: [
        normalizedBookingId,
        normalizedOwnerUserId,
        normalizedRequesterUserId,
        normalizedRequesterUserId,
        normalizedOwnerUserId,
      ],
      transaction,
    },
  );

  if (existingRows?.[0]?.chat_id) {
    return Number(existingRows[0].chat_id);
  }

  const [insertResult] = await sequelize.query(
    `INSERT INTO chats
      (user_id, manager_id, field_id, booking_id, created_at, updated_at)
     VALUES (?, ?, NULL, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)`,
    {
      replacements: [
        normalizedOwnerUserId,
        normalizedRequesterUserId,
        normalizedBookingId,
      ],
      transaction,
    },
  );

  return insertResult?.insertId ? Number(insertResult.insertId) : null;
};

const getMatchRequestForOwnerAction = async ({
  matchRequestId,
  ownerUserId,
  transaction,
}) => {
  const [rows] = await sequelize.query(
    `SELECT
      mr.match_request_id,
      mr.match_post_id,
      mr.booking_id,
      mr.requester_user_id,
      mr.team_name,
      mr.player_count,
      mr.level,
      mr.message,
      mr.status,
      mr.created_at,
      p.username AS requester_username,
      mp.field_id,
      mp.owner_user_id,
      mp.team_name AS owner_team_name,
      mp.status AS post_status
     FROM match_requests mr
     INNER JOIN match_posts mp ON mp.match_post_id = mr.match_post_id
     INNER JOIN bookings b ON b.booking_id = mp.booking_id
     LEFT JOIN person p ON p.person_id = mr.requester_user_id
     WHERE mr.match_request_id = ?
       AND mp.owner_user_id = ?
       AND ${ACTIVE_BOOKING_STATUS_CONDITION}
     FOR UPDATE`,
    { replacements: [matchRequestId, ownerUserId], transaction },
  );

  const request = rows?.[0] || null;

  logMatchInfo("owner action lookup", {
    matchRequestId: Number(matchRequestId),
    ownerUserId: Number(ownerUserId),
    found: Boolean(request),
    requestStatus: request?.status || null,
    postStatus: request?.post_status || null,
    bookingId: request?.booking_id != null ? Number(request.booking_id) : null,
    matchPostId:
      request?.match_post_id != null ? Number(request.match_post_id) : null,
    requesterUserId:
      request?.requester_user_id != null
        ? Number(request.requester_user_id)
        : null,
  });

  return request;
};

const notifyRejectedRequests = async ({
  requests,
  fieldId,
  ownerTeamName,
  ownerUserId,
  transaction,
}) => {
  for (const request of requests) {
    await insertNotification({
      userId: Number(request.requester_user_id),
      type: "match_request_rejected",
      title: "Yêu cầu ghép trận chưa được chấp nhận",
      subtitle: ownerTeamName || "Đội chủ sân",
      content: `Đội ${ownerTeamName || "chủ sân"} đã từ chối hoặc đóng bài tìm đối thủ.`,
      targetType: "match_request",
      targetId: Number(request.match_request_id),
      bookingId: Number(request.booking_id),
      fieldId: Number(fieldId),
      metadata: {
        icon: "match_request_rejected",
        peerUserId: Number(ownerUserId),
      },
      transaction,
    });
  }
};

export const acceptMatchRequest = async ({ matchRequestId, ownerUserId }) =>
  sequelize.transaction(async (transaction) => {
    logMatchInfo("accept request started", {
      matchRequestId: Number(matchRequestId),
      ownerUserId: Number(ownerUserId),
    });

    const request = await getMatchRequestForOwnerAction({
      matchRequestId,
      ownerUserId,
      transaction,
    });

    if (!request) {
      logMatchWarn("accept request failed: not found", {
        matchRequestId: Number(matchRequestId),
        ownerUserId: Number(ownerUserId),
      });
      const error = new Error("MATCH_REQUEST_NOT_FOUND");
      error.code = "MATCH_REQUEST_NOT_FOUND";
      throw error;
    }

    if (String(request.post_status || "").toUpperCase() !== "OPEN") {
      logMatchWarn("accept request failed: post not open", {
        matchRequestId: Number(matchRequestId),
        ownerUserId: Number(ownerUserId),
        postStatus: request.post_status || null,
        matchPostId: Number(request.match_post_id),
      });
      const error = new Error("MATCH_POST_NOT_OPEN");
      error.code = "MATCH_POST_NOT_OPEN";
      throw error;
    }

    if (String(request.status || "").toUpperCase() !== "PENDING") {
      logMatchWarn("accept request failed: request not pending", {
        matchRequestId: Number(matchRequestId),
        ownerUserId: Number(ownerUserId),
        requestStatus: request.status || null,
        matchPostId: Number(request.match_post_id),
      });
      const error = new Error("MATCH_REQUEST_NOT_PENDING");
      error.code = "MATCH_REQUEST_NOT_PENDING";
      throw error;
    }

    await sequelize.query(
      `UPDATE match_requests
       SET status = 'ACCEPTED', updated_at = CURRENT_TIMESTAMP
       WHERE match_request_id = ?`,
      { replacements: [matchRequestId], transaction },
    );

    await sequelize.query(
      `UPDATE match_posts
       SET status = 'MATCHED',
           matched_request_id = ?,
           matched_user_id = ?,
           updated_at = CURRENT_TIMESTAMP
       WHERE match_post_id = ?`,
      {
        replacements: [
          Number(matchRequestId),
          Number(request.requester_user_id),
          Number(request.match_post_id),
        ],
        transaction,
      },
    );

    const [otherRows] = await sequelize.query(
      `SELECT match_request_id, booking_id, requester_user_id
       FROM match_requests
       WHERE match_post_id = ?
         AND match_request_id <> ?
         AND status = 'PENDING'
       FOR UPDATE`,
      {
        replacements: [Number(request.match_post_id), matchRequestId],
        transaction,
      },
    );

    if (otherRows.length > 0) {
      await sequelize.query(
        `UPDATE match_requests
         SET status = 'REJECTED', updated_at = CURRENT_TIMESTAMP
         WHERE match_request_id IN (${buildInClause(otherRows.map((row) => row.match_request_id))})`,
        {
          replacements: otherRows.map((row) => row.match_request_id),
          transaction,
        },
      );
      await notifyRejectedRequests({
        requests: otherRows,
        fieldId: request.field_id,
        ownerTeamName: request.owner_team_name,
        ownerUserId: request.owner_user_id,
        transaction,
      });
    }

    logMatchInfo("accept request success", {
      matchRequestId: Number(matchRequestId),
      ownerUserId: Number(ownerUserId),
      matchPostId: Number(request.match_post_id),
      bookingId: Number(request.booking_id),
      requesterUserId: Number(request.requester_user_id),
      autoRejectedCount: otherRows.length,
    });

    await ensurePeerConversationForAcceptedMatch({
      bookingId: Number(request.booking_id),
      ownerUserId: Number(ownerUserId),
      requesterUserId: Number(request.requester_user_id),
      transaction,
    });

    await insertNotification({
      userId: Number(request.requester_user_id),
      type: "match_request_accepted",
      title: "Yêu cầu ghép trận đã được chấp nhận",
      subtitle: request.owner_team_name || "Đội chủ sân",
      content: `Đội ${request.owner_team_name || "chủ sân"} đã đồng ý ghép trận với bạn.`,
      targetType: "match_request",
      targetId: Number(matchRequestId),
      bookingId: Number(request.booking_id),
      fieldId: Number(request.field_id),
      metadata: {
        icon: "match_request_accepted",
        matchRequestId: Number(matchRequestId),
        peerUserId: Number(request.owner_user_id),
      },
      transaction,
    });

    return mapMatchRequestRow(request, "ACCEPTED");
  });

export const rejectMatchRequest = async ({ matchRequestId, ownerUserId }) =>
  sequelize.transaction(async (transaction) => {
    logMatchInfo("reject request started", {
      matchRequestId: Number(matchRequestId),
      ownerUserId: Number(ownerUserId),
    });

    const request = await getMatchRequestForOwnerAction({
      matchRequestId,
      ownerUserId,
      transaction,
    });

    if (!request) {
      logMatchWarn("reject request failed: not found", {
        matchRequestId: Number(matchRequestId),
        ownerUserId: Number(ownerUserId),
      });
      const error = new Error("MATCH_REQUEST_NOT_FOUND");
      error.code = "MATCH_REQUEST_NOT_FOUND";
      throw error;
    }

    if (String(request.status || "").toUpperCase() !== "PENDING") {
      logMatchWarn("reject request failed: request not pending", {
        matchRequestId: Number(matchRequestId),
        ownerUserId: Number(ownerUserId),
        requestStatus: request.status || null,
        matchPostId: Number(request.match_post_id),
      });
      const error = new Error("MATCH_REQUEST_NOT_PENDING");
      error.code = "MATCH_REQUEST_NOT_PENDING";
      throw error;
    }

    await sequelize.query(
      `UPDATE match_requests
       SET status = 'REJECTED', updated_at = CURRENT_TIMESTAMP
       WHERE match_request_id = ?`,
      { replacements: [matchRequestId], transaction },
    );

    await insertNotification({
      userId: Number(request.requester_user_id),
      type: "match_request_rejected",
      title: "Yêu cầu ghép trận đã bị từ chối",
      subtitle: request.owner_team_name || "Đội chủ sân",
      content: `Đội ${request.owner_team_name || "chủ sân"} hiện chưa thể ghép trận với bạn.`,
      targetType: "notification",
      targetId: null,
      bookingId: Number(request.booking_id),
      fieldId: Number(request.field_id),
      metadata: {
        icon: "match_request_rejected",
        matchRequestId: Number(matchRequestId),
        peerUserId: Number(request.owner_user_id),
      },
      transaction,
    });

    logMatchInfo("reject request success", {
      matchRequestId: Number(matchRequestId),
      ownerUserId: Number(ownerUserId),
      matchPostId: Number(request.match_post_id),
      bookingId: Number(request.booking_id),
      requesterUserId: Number(request.requester_user_id),
    });

    return mapMatchRequestRow(request, "REJECTED");
  });

export const getBookingMatchContext = async (
  bookingId,
  options = {},
) => {
  const normalizedBookingId = Number.parseInt(bookingId, 10);
  if (!Number.isInteger(normalizedBookingId) || normalizedBookingId <= 0) {
    return null;
  }

  const { viewerUserId = null, transaction = null } = options;
  const [postRows] = await sequelize.query(
    `SELECT
      mp.match_post_id,
      mp.booking_id,
      mp.field_id,
      mp.owner_user_id,
      po.username AS owner_username,
      mp.team_name,
      mp.player_count,
      mp.level,
      mp.description,
      mp.status
     FROM match_posts mp
     LEFT JOIN person po ON po.person_id = mp.owner_user_id
     WHERE mp.booking_id = ?
     LIMIT 1`,
    { replacements: [normalizedBookingId], transaction },
  );

  const post = postRows?.[0];
  if (!post) {
    return null;
  }

  const [requestRows] = await sequelize.query(
    `SELECT
      mr.match_request_id,
      mr.match_post_id,
      mr.booking_id,
      mr.requester_user_id,
      mr.team_name,
      mr.player_count,
      mr.level,
      mr.message,
      mr.status,
      mr.created_at,
      p.username AS requester_username
     FROM match_requests mr
     LEFT JOIN person p ON p.person_id = mr.requester_user_id
     WHERE mr.match_post_id = ?
     ORDER BY
       CASE mr.status
         WHEN 'PENDING' THEN 0
         WHEN 'ACCEPTED' THEN 1
         ELSE 2
       END,
       mr.created_at DESC`,
    {
      replacements: [Number(post.match_post_id)],
      transaction,
    },
  );

  const acceptedRequest = requestRows.find(
    (row) => String(row.status || "").toUpperCase() === "ACCEPTED",
  );
  const isOwner =
    viewerUserId != null && Number(viewerUserId) === Number(post.owner_user_id);

  return {
    matchPost: {
      matchPostId: Number(post.match_post_id),
      bookingId: Number(post.booking_id),
      fieldId: Number(post.field_id),
      ownerUserId: Number(post.owner_user_id),
      ownerUsername: post.owner_username || "",
      teamName: post.team_name || "",
      playerCount: Number(post.player_count || 0),
      level: post.level || "INTERMEDIATE",
      levelLabel: formatLevelLabel(post.level),
      description: post.description || "",
      status: post.status || "OPEN",
      isOwner,
      matchedRequest: acceptedRequest ? mapMatchRequestRow(acceptedRequest) : null,
    },
    matchRequests: isOwner ? requestRows.map((row) => mapMatchRequestRow(row)) : [],
  };
};
