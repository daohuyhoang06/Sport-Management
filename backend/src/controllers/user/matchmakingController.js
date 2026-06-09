import {
  acceptMatchRequest,
  createMatchRequestForPost,
  rejectMatchRequest,
} from "../../services/user/matchmakingService.js";

const MATCH_LOG_PREFIX = "[matchmaking-controller]";

const logControllerInfo = (message, data = {}) => {
  console.log(`${MATCH_LOG_PREFIX} ${message}`, data);
};

const logControllerError = (message, data = {}) => {
  console.error(`${MATCH_LOG_PREFIX} ${message}`, data);
};

export const createMatchRequest = async (req, res) => {
  try {
    const requesterUserId = req.user?.id;
    const { id } = req.params;
    const { team_name, player_count, message } = req.body || {};

    if (!requesterUserId) {
      return res.status(401).json({
        success: false,
        message: "Unauthorized",
      });
    }

    const matchRequest = await createMatchRequestForPost({
      matchPostId: id,
      requesterUserId,
      teamName: team_name,
      playerCount: player_count,
      message,
    });

    return res.status(201).json({
      success: true,
      data: matchRequest,
    });
  } catch (error) {
    const errorMap = {
      MATCH_POST_NOT_FOUND: [404, "Bài tìm đối thủ không tồn tại"],
      MATCH_POST_NOT_OPEN: [409, "Bài tìm đối thủ không còn mở"],
      CANNOT_REQUEST_OWN_POST: [400, "Không thể gửi yêu cầu cho chính bài đăng của bạn"],
      MATCH_REQUEST_ALREADY_PENDING: [409, "Bạn đã gửi yêu cầu ghép trận cho bài đăng này"],
      REQUEST_TEAM_NAME_REQUIRED: [400, "Thiếu tên đội"],
      INVALID_REQUEST_PLAYER_COUNT: [400, "Số lượng người chơi không hợp lệ"],
    };

    const mapped = errorMap[error?.code];
    if (mapped) {
      return res.status(mapped[0]).json({
        success: false,
        error: error.code,
        message: mapped[1],
      });
    }

    console.error("createMatchRequest error:", error);
    return res.status(500).json({
      success: false,
      message: "Lỗi server khi gửi yêu cầu ghép trận",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};

export const acceptMatchRequestHandler = async (req, res) => {
  try {
    const ownerUserId = req.user?.id;
    const { id } = req.params;

    logControllerInfo("accept endpoint hit", {
      matchRequestId: Number(id),
      ownerUserId: ownerUserId != null ? Number(ownerUserId) : null,
      method: req.method,
      path: req.originalUrl,
    });

    if (!ownerUserId) {
      return res.status(401).json({
        success: false,
        message: "Unauthorized",
      });
    }

    const data = await acceptMatchRequest({
      matchRequestId: id,
      ownerUserId,
    });

    return res.json({
      success: true,
      data,
    });
  } catch (error) {
    logControllerError("accept endpoint failed", {
      matchRequestId: Number(req.params?.id),
      ownerUserId: req.user?.id != null ? Number(req.user.id) : null,
      errorCode: error?.code || null,
      errorMessage: error?.message || "Unknown error",
      stack: process.env.NODE_ENV === "development" ? error?.stack : undefined,
    });
    const errorMap = {
      MATCH_REQUEST_NOT_FOUND: [404, "Yêu cầu ghép trận không tồn tại"],
      MATCH_POST_NOT_OPEN: [409, "Bài tìm đối thủ không còn mở"],
      MATCH_REQUEST_NOT_PENDING: [409, "Yêu cầu ghép trận không còn ở trạng thái chờ"],
    };

    const mapped = errorMap[error?.code];
    if (mapped) {
      return res.status(mapped[0]).json({
        success: false,
        error: error.code,
        message: mapped[1],
      });
    }

    console.error("acceptMatchRequestHandler error:", error);
    return res.status(500).json({
      success: false,
      message: "Lỗi server khi chấp nhận yêu cầu ghép trận",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};

export const rejectMatchRequestHandler = async (req, res) => {
  try {
    const ownerUserId = req.user?.id;
    const { id } = req.params;

    logControllerInfo("reject endpoint hit", {
      matchRequestId: Number(id),
      ownerUserId: ownerUserId != null ? Number(ownerUserId) : null,
      method: req.method,
      path: req.originalUrl,
    });

    if (!ownerUserId) {
      return res.status(401).json({
        success: false,
        message: "Unauthorized",
      });
    }

    const data = await rejectMatchRequest({
      matchRequestId: id,
      ownerUserId,
    });

    return res.json({
      success: true,
      data,
    });
  } catch (error) {
    logControllerError("reject endpoint failed", {
      matchRequestId: Number(req.params?.id),
      ownerUserId: req.user?.id != null ? Number(req.user.id) : null,
      errorCode: error?.code || null,
      errorMessage: error?.message || "Unknown error",
      stack: process.env.NODE_ENV === "development" ? error?.stack : undefined,
    });
    const errorMap = {
      MATCH_REQUEST_NOT_FOUND: [404, "Yêu cầu ghép trận không tồn tại"],
      MATCH_REQUEST_NOT_PENDING: [409, "Yêu cầu ghép trận không còn ở trạng thái chờ"],
    };

    const mapped = errorMap[error?.code];
    if (mapped) {
      return res.status(mapped[0]).json({
        success: false,
        error: error.code,
        message: mapped[1],
      });
    }

    console.error("rejectMatchRequestHandler error:", error);
    return res.status(500).json({
      success: false,
      message: "Lỗi server khi từ chối yêu cầu ghép trận",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};
