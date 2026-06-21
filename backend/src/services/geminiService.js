import { GoogleGenerativeAI } from "@google/generative-ai";
import sequelize from "../config/database.js";

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY || "");
const FIELD_SEARCH_LIMIT = 3;
const DEFAULT_TIME_LABEL = "thời gian bạn chọn";

const SPORT_ALIASES = {
  FOOTBALL: ["bong da", "san bong da", "football", "futsal", "bongda"],
  BADMINTON: ["cau long", "badminton", "caulong"],
  TENNIS: ["tennis"],
  PICKLEBALL: ["pickleball", "picke ball"],
  VOLLEYBALL: ["bong chuyen", "volleyball", "bongchuyen"],
};

const SPORT_DISPLAY_LABEL = {
  FOOTBALL: "bóng đá",
  BADMINTON: "cầu lông",
  TENNIS: "tennis",
  PICKLEBALL: "pickleball",
  VOLLEYBALL: "bóng chuyền",
};

const normalizeVietnamese = (value) =>
  String(value || "")
    .trim()
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/\u0111/g, "d")
    .replace(/\u0110/g, "d");

const compactForSearch = (value) =>
  normalizeVietnamese(value).replace(/[\s,.\-_]/g, "");

const compactWhitespace = (value) =>
  String(value || "")
    .replace(/\s+/g, " ")
    .trim();

const NON_SEARCH_SMALL_TALK = new Set([
  "hello",
  "hi",
  "helo",
  "alo",
  "xin chao",
  "chao",
  "ok",
  "oke",
  "okay",
  "cam on",
  "thanks",
  "thank you",
  "yes",
  "no",
]);

const compactSqlExpression = (expression) =>
  `REPLACE(REPLACE(REPLACE(REPLACE(LOWER(COALESCE(${expression}, '')) COLLATE utf8mb4_unicode_ci, 'đ', 'd'), ' ', ''), ',', ''), '.', '')`;

const buildTextSearchClause = (columns, value, replacements) => {
  const normalized = normalizeVietnamese(value);
  if (!normalized) return null;

  const tokens = normalized.split(/\s+/).filter(Boolean);
  if (tokens.length === 0) return null;

  const perTokenClauses = tokens.map((token) => {
    const tokenCompact = compactForSearch(token);
    const tokenParts = [];

    columns.forEach((column) => {
      tokenParts.push(
        `LOWER(COALESCE(${column}, '')) COLLATE utf8mb4_unicode_ci LIKE ?`,
      );
      replacements.push(`%${token}%`);

      if (tokenCompact) {
        tokenParts.push(`${compactSqlExpression(column)} LIKE ?`);
        replacements.push(`%${tokenCompact}%`);
      }
    });

    return `(${tokenParts.join(" OR ")})`;
  });

  const fullCompact = compactForSearch(value);
  if (fullCompact && tokens.length > 1) {
    const compactPhraseParts = [];

    columns.forEach((column) => {
      compactPhraseParts.push(`${compactSqlExpression(column)} LIKE ?`);
      replacements.push(`%${fullCompact}%`);
    });

    return `((${perTokenClauses.join(" AND ")}) OR (${compactPhraseParts.join(" OR ")}))`;
  }

  return `(${perTokenClauses.join(" AND ")})`;
};

const sportTypeAliases = (sportType) => {
  const normalized = compactForSearch(sportType);
  const aliases = {
    football: ["football", "bongda", "soccer", "futsal"],
    bongda: ["football", "bongda", "soccer", "futsal"],
    soccer: ["football", "bongda", "soccer", "futsal"],
    futsal: ["football", "bongda", "soccer", "futsal"],
    badminton: ["badminton", "caulong"],
    caulong: ["badminton", "caulong"],
    tennis: ["tennis"],
    pickleball: ["pickleball"],
    volleyball: ["volleyball", "bongchuyen"],
    bongchuyen: ["volleyball", "bongchuyen"],
  };

  return aliases[normalized] || [normalized];
};

const formatCurrencyLabel = (value) => {
  const amount = Number(value);
  if (!Number.isFinite(amount) || amount <= 0) return "Liên hệ";
  return `${amount.toLocaleString("vi-VN")}đ/giờ`;
};

const formatHourLabel = (value) => String(value).padStart(2, "0");

const titleCaseWords = (value) =>
  compactWhitespace(value)
    .split(" ")
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");

const formatTimeRange = (openTime, closeTime) => {
  const formatPart = (raw) => {
    const match = String(raw || "").match(/(\d{2}):(\d{2})/);
    return match ? `${match[1]}:${match[2]}` : "";
  };

  const open = formatPart(openTime);
  const close = formatPart(closeTime);
  if (!open || !close) return "Chưa cập nhật";
  return `${open} - ${close}`;
};

const detectSportKey = (text) => {
  const normalized = compactWhitespace(normalizeVietnamese(text));
  if (/\btim\b.*\bsan bong\b|\bsan bong\b.*\b(o|tai|gan|khu\s*vuc|quan|huyen|phuong|xa)\b/i.test(normalized)) {
    return "FOOTBALL";
  }
  return (
    Object.entries(SPORT_ALIASES).find(([, aliases]) =>
      aliases.some((alias) => normalized.includes(alias)),
    )?.[0] || null
  );
};

const extractLocation = (text) => {
  const normalized = normalizeVietnamese(compactWhitespace(text));
  const patterns = [
    /\b(?:o|tai|gan)\b\s+khu\s*vuc\s+([^,.!?]+)/i,
    /\b(?:o|tai|gan)\b\s+(?:quan|huyen|phuong|xa)\s+([^,.!?]+)/i,
    /\b(?:o|tai|gan)\b\s+([^,.!?]+)/i,
    /khu\s*vuc\s+([^,.!?]+)/i,
    /quan\s+([^,.!?]+)/i,
    /huyen\s+([^,.!?]+)/i,
  ];

  for (const pattern of patterns) {
    const match = normalized.match(pattern);
    if (!match?.[1]) continue;

    const cleaned = compactWhitespace(match[1])
      .replace(/\b(luc|vao|tam|khoang)\b.*$/, "")
      .trim();

    if (cleaned) return cleaned;
  }

  return null;
};

const extractImplicitLocation = (text) => {
  const raw = compactWhitespace(text);
  const normalized = normalizeVietnamese(raw);
  if (!normalized || NON_SEARCH_SMALL_TALK.has(normalized)) return null;
  if (isFieldSearchIntent(text)) return null;
  if (detectSportKey(text) || extractPreferredTime(text)) return null;

  const wordCount = normalized.split(/\s+/).filter(Boolean).length;
  if (wordCount < 1 || wordCount > 4) return null;

  return normalized;
};

const extractPreferredTime = (text) => {
  const normalized = normalizeVietnamese(text);
  if (/\b\d{1,2}\s*(nguoi|ng)\b/.test(normalized)) {
    return null;
  }

  const explicitTimeMatch = normalized.match(
    /\b(\d{1,2})(?:[:hg](\d{1,2}))\s*(sang|trua|chieu|toi|dem)?\b/,
  );
  const contextualTimeMatch = normalized.match(
    /\b(?:luc|vao|khoang|tam)\s+(\d{1,2})(?:[:hg](\d{1,2}))?\s*(sang|trua|chieu|toi|dem)?\b/,
  );
  const partOfDayMatch = normalized.match(
    /\b(\d{1,2})\s*(sang|trua|chieu|toi|dem)\b/,
  );

  const match = explicitTimeMatch || contextualTimeMatch || partOfDayMatch;
  if (!match) return null;

  let hour = Number.parseInt(match[1], 10);
  const minute = Number.parseInt(match[2] || "0", 10);
  const part = match[3] || "";

  if (!Number.isInteger(hour) || hour > 23 || minute > 59) {
    return null;
  }

  if (["chieu", "toi", "dem"].includes(part) && hour < 12) {
    hour += 12;
  }
  if (part === "trua" && hour < 11) {
    hour += 12;
  }
  if (part === "sang" && hour === 12) {
    hour = 0;
  }

  const label = `${formatHourLabel(hour)}:${formatHourLabel(minute)}`;
  return { hour, minute, label };
};

const isFieldSearchIntent = (text) => {
  const normalized = normalizeVietnamese(text);
  const hasSport = Boolean(detectSportKey(text));
  const hasLocationSignal =
    /\b(o|tai|gan|khu\s*vuc|quan|huyen|phuong|xa)\b/i.test(normalized);

  return (
    [
      "tim san",
      "tim toi san",
      "dat san",
      "muon choi",
      "muon tim",
      "can tim",
      "tim cho toi",
      "goi y san",
      "san cau long",
      "san tennis",
      "san pickleball",
      "san bong chuyen",
    ].some((keyword) => normalized.includes(keyword)) ||
    (/\btim\b.*\bsan\b/.test(normalized) && (hasSport || hasLocationSignal)) ||
    (hasSport && hasLocationSignal)
  );
};

const hasRecentFieldSearchContext = (conversationHistory = []) =>
  (Array.isArray(conversationHistory) ? conversationHistory : [])
    .filter((item) => item?.role === "user")
    .slice(-3)
    .some((item) => isFieldSearchIntent(item?.message || ""));

const isFieldSearchFollowUp = (userMessage, conversationHistory = []) => {
  const hasSearchContext = hasRecentFieldSearchContext(conversationHistory);
  if (!hasSearchContext) return false;

  return Boolean(
    detectSportKey(userMessage) ||
      extractLocation(userMessage) ||
      extractPreferredTime(userMessage) ||
      extractImplicitLocation(userMessage),
  );
};

const collectFieldSearchCriteria = (userMessage, conversationHistory = []) => {
  const currentMessageIsSearchIntent = isFieldSearchIntent(userMessage);
  const currentMessageIsFollowUp = isFieldSearchFollowUp(
    userMessage,
    conversationHistory,
  );
  const shouldUseSearchShortcut =
    currentMessageIsSearchIntent || currentMessageIsFollowUp;

  if (!shouldUseSearchShortcut) {
    return {
      sportKey: null,
      location: null,
      preferredTime: null,
      isSearchIntent: false,
    };
  }

  const messages = [
    ...(Array.isArray(conversationHistory) ? conversationHistory : [])
      .filter((item) => item?.role === "user")
      .map((item) => item?.message || ""),
    userMessage,
  ];

  const criteria = {
    sportKey: null,
    location: null,
    preferredTime: null,
    isSearchIntent: shouldUseSearchShortcut,
  };

  for (const message of messages) {
    if (!criteria.sportKey) {
      criteria.sportKey = detectSportKey(message);
    }
    if (!criteria.location) {
      criteria.location =
        extractLocation(message) ||
        (message === userMessage ? extractImplicitLocation(message) : null);
    }
    if (!criteria.preferredTime) {
      criteria.preferredTime = extractPreferredTime(message);
    }
  }

  if (!criteria.isSearchIntent && criteria.sportKey && criteria.location) {
    criteria.isSearchIntent = true;
  }

  return criteria;
};

const queryMatchingFields = async ({ sportKey, location, preferredTime }) => {
  const whereClauses = ["f.status = 'active'"];
  const replacements = [];

  if (sportKey) {
    const aliases = sportTypeAliases(sportKey);
    whereClauses.push(
      `(${aliases.map(() => `${compactSqlExpression("st.sport_name")} LIKE ?`).join(" OR ")})`,
    );
    aliases.forEach((alias) => replacements.push(`%${alias}%`));
  }

  if (location) {
    const locationClause = buildTextSearchClause(
      ["f.location", "f.district", "f.province", "f.region"],
      location,
      replacements,
    );
    if (locationClause) {
      whereClauses.push(locationClause);
    }
  }

  if (preferredTime) {
    whereClauses.push(
      `TIME(COALESCE(f.open_time, '00:00:00')) <= MAKETIME(?, ?, 0)
       AND TIME(COALESCE(f.close_time, '23:59:59')) >= MAKETIME(?, ?, 0)`,
    );
    replacements.push(
      preferredTime.hour,
      preferredTime.minute,
      preferredTime.hour,
      preferredTime.minute,
    );
  }

  const [rows] = await sequelize.query(
    `
    SELECT
      f.field_id,
      f.field_name,
      f.location,
      f.slot_price,
      f.open_time,
      f.close_time,
      f.phone,
      COALESCE(f.display_rating, AVG(r.rating), 0) AS rating_value,
      st.sport_name
    FROM fields f
    LEFT JOIN sport_types st ON st.sport_id = f.sport_id
    LEFT JOIN reviews r ON r.field_id = f.field_id
    WHERE ${whereClauses.join(" AND ")}
    GROUP BY
      f.field_id, f.field_name, f.location, f.slot_price, f.open_time,
      f.close_time, f.phone, f.display_rating, st.sport_name
    ORDER BY rating_value DESC, f.field_id ASC
    LIMIT ?
    `,
    {
      replacements: [...replacements, FIELD_SEARCH_LIMIT],
    },
  );

  return rows;
};

const buildMissingCriteriaReply = ({ sportKey, location, preferredTime }) => {
  if (!sportKey) {
    const locationPart = location ? " ở khu vực bạn đã nhập" : "";
    const timePart = preferredTime?.label ? ` lúc ${preferredTime.label}` : "";
    return `Mình có thể tìm sân ngay cho bạn${locationPart}${timePart}. Bạn chỉ cần cho mình biết môn thể thao muốn chơi: bóng đá, cầu lông, tennis, pickleball hay bóng chuyền.`;
  }

  if (!location) {
    return `Bạn muốn tìm sân ${SPORT_DISPLAY_LABEL[sportKey] || "thể thao"} ở khu vực nào? Chỉ cần thêm khu vực là mình sẽ đưa kết quả ngay.`;
  }

  return null;
};

const resolveLocationLabel = (location, fields) => {
  const locations = (Array.isArray(fields) ? fields : [])
    .map((field) => compactWhitespace(field?.location || ""))
    .filter(Boolean);

  if (locations.length === 1) {
    return locations[0];
  }

  if (locations.length > 1) {
    const splitLocations = locations.map((entry) =>
      entry
        .split(",")
        .map((part) => compactWhitespace(part))
        .filter(Boolean),
    );
    const reversed = splitLocations.map((parts) => [...parts].reverse());
    const shared = [];

    for (let index = 0; index < reversed[0].length; index += 1) {
      const candidate = normalizeVietnamese(reversed[0][index]);
      if (
        reversed.every(
          (parts) =>
            index < parts.length &&
            normalizeVietnamese(parts[index]) === candidate,
        )
      ) {
        shared.push(reversed[0][index]);
      } else {
        break;
      }
    }

    if (shared.length > 0) {
      return shared.reverse().join(", ");
    }
  }

  return location ? titleCaseWords(location) : "khu vực bạn yêu cầu";
};

const buildFieldSearchReply = ({ sportKey, location, preferredTime, fields }) => {
  const sportLabel = SPORT_DISPLAY_LABEL[sportKey] || "thể thao";
  const locationLabel = resolveLocationLabel(location, fields);
  const timeLabel = preferredTime?.label || DEFAULT_TIME_LABEL;

  if (!Array.isArray(fields) || fields.length === 0) {
    return `Mình chưa tìm thấy sân ${sportLabel} phù hợp ở ${locationLabel}${preferredTime ? ` lúc ${timeLabel}` : ""}. Bạn có thể thử mở rộng sang khu vực lân cận hoặc đổi khung giờ khác, mình sẽ lọc lại ngay.`;
  }

  const lines = fields.map((field, index) => {
    const rating = Number(field.rating_value || 0);
    const ratingLabel = rating > 0 ? `${rating.toFixed(1)} sao` : "chưa có đánh giá";
    return `${index + 1}. ${field.field_name} - ${field.location || "Chưa cập nhật địa chỉ"} - ${formatCurrencyLabel(field.slot_price)} - ${formatTimeRange(field.open_time, field.close_time)} - ${ratingLabel}`;
  });

  return `Mình đã lọc nhanh ${fields.length} sân ${sportLabel} ở ${locationLabel}${preferredTime ? ` có thể chơi quanh ${timeLabel}` : ""}:\n${lines.join("\n")}\n\nNếu cần, bạn nhắn tiếp để mình lọc sâu hơn theo giá hoặc khu vực gần nhất.`;
};

/**
 * Get field recommendations based on user preferences
 */
export const getFieldRecommendations = async (preferences) => {
  try {
    const { location, budget, time, playerCount } = preferences;
    const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" });

    const prompt = `Bạn là chuyên gia tư vấn đặt sân thể thao đa môn.

Thông tin người dùng:
- Vị trí mong muốn: ${location || "Chưa xác định"}
- Ngân sách: ${budget || "Linh hoạt"}
- Thời gian chơi: ${time || "Chưa xác định"}
- Số người chơi: ${playerCount || "Chưa biết"}

Hãy đưa ra 3-5 gợi ý cụ thể về:
1. Môn hoặc loại sân phù hợp
2. Khung giờ nên đặt để tối ưu chi phí
3. Tiện ích nên ưu tiên theo từng môn
4. Lưu ý khi đặt sân

Trả lời ngắn gọn, dễ hiểu, tối đa 200 từ.`;

    const result = await model.generateContent(prompt);
    const response = await result.response;
    const text = response.text();

    return {
      success: true,
      recommendation: text,
    };
  } catch (error) {
    console.error("Gemini API Error:", error);
    return {
      success: false,
      message: "Không thể tạo gợi ý lúc này",
      error: error.message,
    };
  }
};

/**
 * Analyze booking pattern for fraud detection
 */
export const detectBookingFraud = async (bookingData) => {
  try {
    const { bookingHistory, currentBooking } = bookingData;
    const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" });

    const prompt = `Phân tích hành vi đặt sân để phát hiện gian lận:

Lịch sử đặt sân (${bookingHistory.length} lần):
${bookingHistory.map((booking, index) => `${index + 1}. Sân: ${booking.field_name}, Giá: ${booking.price}đ, Trạng thái: ${booking.status}, Ngày: ${booking.date}`).join("\n")}

Booking hiện tại:
- Sân: ${currentBooking.field_name}
- Giá: ${currentBooking.price}đ
- Thời gian: ${currentBooking.time}

Đánh giá các dấu hiệu bất thường:
1. Đặt quá nhiều sân cùng lúc
2. Hủy liên tục
3. Đặt giờ cao điểm rồi hủy
4. Thay đổi bất thường về giá trị booking

Trả về JSON:
{
  "riskLevel": "low|medium|high",
  "score": 0-100,
  "reasons": ["lý do 1", "lý do 2"],
  "recommendation": "Cho phép/Cần xem xét/Từ chối"
}`;

    const result = await model.generateContent(prompt);
    const response = await result.response;
    const text = response.text();

    const jsonMatch = text.match(/\{[\s\S]*\}/);
    if (jsonMatch) {
      const analysis = JSON.parse(jsonMatch[0]);
      return {
        success: true,
        ...analysis,
      };
    }

    return {
      success: true,
      riskLevel: "low",
      score: 10,
      reasons: ["Không phát hiện dấu hiệu bất thường"],
      recommendation: "Cho phép",
    };
  } catch (error) {
    console.error("Fraud Detection Error:", error);
    return {
      success: false,
      riskLevel: "low",
      score: 0,
      message: "Không thể phân tích lúc này",
    };
  }
};

/**
 * Suggest best time slots based on data
 */
export const suggestBestTimeSlots = async (fieldData) => {
  try {
    const { fieldName, priceData, bookingStats } = fieldData;
    const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" });

    const prompt = `Phân tích dữ liệu đặt sân thể thao và đưa ra gợi ý khung giờ tốt nhất:

Sân: ${fieldName}

Thống kê đặt sân:
- Tổng số booking: ${bookingStats.total || 0}
- Tỷ lệ lấp đầy: ${bookingStats.occupancyRate || 0}%
- Khung giờ đông nhất: ${bookingStats.peakHours || "N/A"}

Bảng giá theo khung giờ:
${priceData && priceData.length > 0 ? priceData.map((item) => `- ${item.timeSlot}: ${item.price}đ (${item.availability})`).join("\n") : "Chưa có dữ liệu"}

Hãy đưa ra:
1. Top 3 khung giờ tốt nhất
2. Khung giờ tiết kiệm nhất
3. Khung giờ tốt nhất cho chất lượng sân
4. Lời khuyên theo mục đích sử dụng

Trả lời ngắn gọn, dễ hiểu.`;

    const result = await model.generateContent(prompt);
    const response = await result.response;
    const text = response.text();

    return {
      success: true,
      suggestions: text,
    };
  } catch (error) {
    console.error("Time Slot Suggestion Error:", error);
    return {
      success: false,
      message: "Không thể tạo gợi ý lúc này",
    };
  }
};

/**
 * General AI chatbot
 */
export const chatWithAI = async (userMessage, conversationHistory = []) => {
  try {
    const fieldSearchCriteria = collectFieldSearchCriteria(
      userMessage,
      conversationHistory,
    );

    if (fieldSearchCriteria.isSearchIntent) {
      const missingCriteriaReply = buildMissingCriteriaReply(fieldSearchCriteria);
      if (missingCriteriaReply) {
        return {
          success: true,
          message: missingCriteriaReply,
        };
      }

      try {
        const matchedFields = await queryMatchingFields(fieldSearchCriteria);
        return {
          success: true,
          message: buildFieldSearchReply({
            ...fieldSearchCriteria,
            fields: matchedFields,
          }),
        };
      } catch (error) {
        console.error("Field search shortcut error:", error);
      }
    }

    const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" });

    const systemPrompt = `Bạn là trợ lý AI của hệ thống đặt sân thể thao đa môn.
Nhiệm vụ:
- Tư vấn tìm và chọn sân cho nhiều môn: bóng đá, cầu lông, tennis, bóng rổ, bóng chuyền, pickleball và các môn khác trong hệ thống.
- Giải đáp thắc mắc về giá, khung giờ, chính sách, cách đặt, hủy hoặc đổi lịch.
- Đưa ra gợi ý dựa trên nhu cầu người dùng như số người, ngân sách, thời gian và mục đích chơi.

Hướng dẫn trả lời:
- Khi người dùng đang tìm sân, ưu tiên hỏi ít nhất có thể. Chỉ hỏi lại môn thể thao hoặc khu vực nếu thiếu. Không tự động hỏi giá hoặc ngân sách nếu người dùng chưa đề cập.
- Nếu người dùng hỏi về môn khác bóng đá nhưng vẫn liên quan đến đặt sân thể thao, vẫn trả lời bình thường.
- Nếu câu hỏi không liên quan đến đặt sân thể thao, lịch sử dùng sân hoặc hỗ trợ khách hàng trong hệ thống, phản hồi ngắn gọn và hướng người dùng về nội dung thể thao.
- Trả lời thân thiện, rõ ràng, ngắn gọn, tối đa 180 từ.
- Ưu tiên dùng cùng ngôn ngữ với người dùng.`;

    const fullPrompt =
      conversationHistory.length > 0
        ? `${systemPrompt}\n\nLịch sử hội thoại:\n${conversationHistory.map((item) => `${item.role}: ${item.message}`).join("\n")}\n\nUser: ${userMessage}\nAI:`
        : `${systemPrompt}\n\nUser: ${userMessage}\nAI:`;

    const result = await model.generateContent(fullPrompt);
    const response = await result.response;
    const text = response.text();

    return {
      success: true,
      message: text,
    };
  } catch (error) {
    console.error("AI Chat Error:", error);
    return {
      success: false,
      message: "Xin lỗi, tôi không thể trả lời lúc này. Vui lòng thử lại sau.",
    };
  }
};


