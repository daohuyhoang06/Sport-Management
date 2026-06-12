import sequelize from "../../config/database.js";
import {
  getAvailableSlots,
  releaseExpiredPendingBookings,
} from "../../services/user/scheduleService.js";
import {
  buildBookingShareResponse,
  getBookingShareDetailByBookingId,
} from "../../services/bookingShareService.js";
import {
  ACTIVE_BOOKING_STATUS_CONDITION,
  deriveAggregateBookingValues,
} from "../../services/bookingSlotService.js";
import {
  createMatchPostForBooking,
  listOpenMatchPostPreviewsForFieldDate,
  normalizeMatchPostPayload,
} from "../../services/user/matchmakingService.js";

const PENDING_HOLD_MINUTES = 6;
const SPORT_NAME_TO_ICON = {
  "Bóng đá": "FOOTBALL",
  "Bóng chuyền": "VOLLEYBALL",
  Pickleball: "PICKLEBALL",
  "Cầu lông": "BADMINTON",
  Tennis: "TENNIS",
};
const DEFAULT_IMAGE_URL = "/images/fields/placeholder.svg";
const EARTH_RADIUS_KM = 6371;
const VN_TIME_ZONE = "Asia/Ho_Chi_Minh";

const formatPriceLabel = (slotPrice) => {
  if (slotPrice === null || slotPrice === undefined || Number(slotPrice) <= 0) {
    return "Lien he";
  }
  const value = Number(slotPrice);
  return `${value.toLocaleString("vi-VN")}đ/h`;
};

const formatHoursLabel = (openTime, closeTime) => {
  if (!openTime || !closeTime) return "00:00 - 24:00";
  const open = String(openTime).slice(0, 5);
  const close = String(closeTime).slice(0, 5);
  return `${open} - ${close}`;
};

const parseCoordinate = (value) => {
  if (value === undefined || value === null || value === "") return null;
  const parsed = Number.parseFloat(value);
  if (!Number.isFinite(parsed)) return null;
  return parsed;
};

const formatDistanceLabel = (distanceKm) => {
  if (distanceKm === null || distanceKm === undefined) return "";
  const numeric = Number(distanceKm);
  if (!Number.isFinite(numeric)) return "";
  if (numeric < 1) {
    return `${Math.round(numeric * 1000)} m`;
  }
  return `${numeric.toFixed(1)} km`;
};

const getVnDateString = (date = new Date()) =>
  (() => {
    const parts = new Intl.DateTimeFormat("en-GB", {
      timeZone: VN_TIME_ZONE,
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    }).formatToParts(date);
    const year = parts.find((item) => item.type === "year")?.value || "0000";
    const month = parts.find((item) => item.type === "month")?.value || "00";
    const day = parts.find((item) => item.type === "day")?.value || "00";
    return `${year}-${month}-${day}`;
  })();

const getVnTimeMinutes = (date = new Date()) => {
  const parts = new Intl.DateTimeFormat("en-GB", {
    timeZone: VN_TIME_ZONE,
    hour: "2-digit",
    minute: "2-digit",
    hourCycle: "h23",
  }).formatToParts(date);

  const hours = Number(parts.find((item) => item.type === "hour")?.value || 0);
  const minutes = Number(
    parts.find((item) => item.type === "minute")?.value || 0,
  );
  return hours * 60 + minutes;
};

const formatBookingDatetime = (value) => {
  const raw = String(value || "").trim();
  const localMatch = raw.match(
    /^(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2})(?::(\d{2}))?$/,
  );
  if (localMatch) {
    return `${localMatch[1]}-${localMatch[2]}-${localMatch[3]} ${localMatch[4]}:${localMatch[5]}:${localMatch[6] || "00"}`;
  }

  const date = new Date(raw);
  if (Number.isNaN(date.getTime())) {
    const error = new Error("INVALID_SLOT_RANGE");
    error.code = "INVALID_SLOT_RANGE";
    throw error;
  }

  const parts = new Intl.DateTimeFormat("en-GB", {
    timeZone: VN_TIME_ZONE,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hourCycle: "h23",
  }).formatToParts(date);
  const pick = (type) => parts.find((item) => item.type === type)?.value || "00";

  return `${pick("year")}-${pick("month")}-${pick("day")} ${pick("hour")}:${pick("minute")}:${pick("second")}`;
};

const formatDbTimeLabel = (value) => {
  if (value instanceof Date) {
    return `${String(value.getUTCHours()).padStart(2, "0")}:${String(value.getUTCMinutes()).padStart(2, "0")}`;
  }

  const raw = String(value || "").trim();
  const match = raw.match(/\b(\d{2}):(\d{2})(?::\d{2})?/);
  if (match) return `${match[1]}:${match[2]}`;

  return "";
};

const roundMinutesUpToHalfHour = (minutes) => {
  const normalizedMinutes = Math.max(0, Number(minutes) || 0);
  const remainder = normalizedMinutes % 30;
  return remainder === 0
    ? normalizedMinutes
    : normalizedMinutes + (30 - remainder);
};

const roundMinutesToNearestStep = (minutes, stepMinutes) => {
  const normalizedMinutes = Math.max(0, Number(minutes) || 0);
  const normalizedStep = Math.max(1, Number(stepMinutes) || 1);
  const remainder = normalizedMinutes % normalizedStep;
  if (remainder === 0) return normalizedMinutes;
  return remainder * 2 < normalizedStep
    ? normalizedMinutes - remainder
    : normalizedMinutes + (normalizedStep - remainder);
};

const buildBookingNote = (note, customerName, customerPhone) => {
  if (customerName || customerPhone) {
    let finalNote = `Ten: ${customerName || "N/A"}, SDT: ${customerPhone || "N/A"}`;
    if (note) {
      finalNote += ` - Ghi chu: ${note}`;
    }
    return finalNote;
  }
  return note || "";
};

const slotsOverlap = (left, right) =>
  new Date(left.start_time).getTime() < new Date(right.end_time).getTime() &&
  new Date(left.end_time).getTime() > new Date(right.start_time).getTime();

const requestedSlotsConflict = (left, right) => {
  const leftCourtId = Number.parseInt(left?.court_id, 10);
  const rightCourtId = Number.parseInt(right?.court_id, 10);
  const isDifferentSpecificCourt =
    Number.isInteger(leftCourtId) &&
    Number.isInteger(rightCourtId) &&
    leftCourtId !== rightCourtId;

  if (isDifferentSpecificCourt) {
    return false;
  }

  return slotsOverlap(left, right);
};

const normalizeBookingRequests = (bookings) => {
  const normalizedBookings = [];

  for (const item of bookings) {
    const normalizedCourtId = Number.parseInt(item?.court_id, 10);
    if (
      item?.court_id !== undefined &&
      item?.court_id !== null &&
      !Number.isInteger(normalizedCourtId)
    ) {
      const error = new Error("INVALID_COURT_ID");
      error.code = "INVALID_COURT_ID";
      throw error;
    }
    if (!item?.start_time || !item?.end_time) {
      const error = new Error("INVALID_SLOT_RANGE");
      error.code = "INVALID_SLOT_RANGE";
      throw error;
    }

    const startTime = formatBookingDatetime(item.start_time);
    const endTime = formatBookingDatetime(item.end_time);
    if (new Date(startTime).getTime() >= new Date(endTime).getTime()) {
      const error = new Error("INVALID_SLOT_RANGE");
      error.code = "INVALID_SLOT_RANGE";
      throw error;
    }

    const priceValue = Number(item?.price || 0);
    normalizedBookings.push({
      court_id: Number.isInteger(normalizedCourtId) ? normalizedCourtId : null,
      start_time: startTime,
      end_time: endTime,
      price: Number.isFinite(priceValue) ? priceValue : 0,
    });
  }

  for (let leftIndex = 0; leftIndex < normalizedBookings.length; leftIndex += 1) {
    for (
      let rightIndex = leftIndex + 1;
      rightIndex < normalizedBookings.length;
      rightIndex += 1
    ) {
      if (requestedSlotsConflict(normalizedBookings[leftIndex], normalizedBookings[rightIndex])) {
        const error = new Error("DUPLICATE_BOOKING_SLOT");
        error.code = "DUPLICATE_BOOKING_SLOT";
        throw error;
      }
    }
  }

  return normalizedBookings;
};

const validateFieldCourts = async (fieldId, normalizedBookings) => {
  const distinctCourtIds = [
    ...new Set(
      normalizedBookings
        .map((item) => item.court_id)
        .filter((item) => Number.isInteger(item)),
    ),
  ];

  if (distinctCourtIds.length === 0) {
    return;
  }

  const [courtRows] = await sequelize.query(
    `SELECT court_id
     FROM field_courts
     WHERE field_id = ? AND court_id IN (${distinctCourtIds.map(() => "?").join(", ")})`,
    { replacements: [fieldId, ...distinctCourtIds] },
  );
  const validCourtIds = new Set(
    courtRows.map((item) => Number.parseInt(item.court_id, 10)),
  );
  const invalidCourtId = distinctCourtIds.find((item) => !validCourtIds.has(item));
  if (invalidCourtId) {
    const error = new Error("INVALID_COURT_ID");
    error.code = "INVALID_COURT_ID";
    throw error;
  }
};

const createAggregateBookingWithSlots = async ({
  customerId,
  fieldId,
  managerId,
  normalizedBookings,
  finalNote,
  customerName,
  customerPhone,
  matchPostPayload = null,
}) => {
  let booking = null;

  await sequelize.transaction(async (transaction) => {
    await releaseExpiredPendingBookings(transaction);

    const [lockedField] = await sequelize.query(
      `SELECT field_id FROM fields WHERE field_id = ? FOR UPDATE`,
      { replacements: [fieldId], transaction },
    );
    if (!lockedField || lockedField.length === 0) {
      const invalidFieldError = new Error("INVALID_FIELD_ID");
      invalidFieldError.code = "INVALID_FIELD_ID";
      throw invalidFieldError;
    }

    for (const item of normalizedBookings) {
      const [blockedConflicts] = await sequelize.query(
        `SELECT slot_id
         FROM field_blocked_slots
         WHERE field_id = ?
           AND block_date = DATE(?)
           AND start_time < TIME(?)
           AND end_time > TIME(?)
         FOR UPDATE`,
        {
          replacements: [fieldId, item.start_time, item.end_time, item.start_time],
          transaction,
        },
      );

      if (blockedConflicts && blockedConflicts.length > 0) {
        const conflictError = new Error("SLOT_NOT_AVAILABLE");
        conflictError.code = "SLOT_NOT_AVAILABLE";
        throw conflictError;
      }

      const [conflicts] = await sequelize.query(
        `SELECT bs.booking_slot_id
         FROM booking_slots bs
         INNER JOIN bookings b ON b.booking_id = bs.booking_id
         WHERE bs.field_id = ?
           AND (? IS NULL OR bs.court_id = ? OR bs.court_id IS NULL)
           AND bs.start_time < ?
           AND bs.end_time > ?
           AND ${ACTIVE_BOOKING_STATUS_CONDITION}
         FOR UPDATE`,
        {
          replacements: [
            fieldId,
            item.court_id,
            item.court_id,
            item.end_time,
            item.start_time,
          ],
          transaction,
        },
      );

      if (conflicts && conflicts.length > 0) {
        const conflictError = new Error("SLOT_NOT_AVAILABLE");
        conflictError.code = "SLOT_NOT_AVAILABLE";
        throw conflictError;
      }
    }

    const aggregate = deriveAggregateBookingValues(normalizedBookings);

    await sequelize.query(
      `INSERT INTO bookings (
        customer_id, field_id, court_id, manager_id, start_time, end_time, price, note, customer_name, customer_phone, status, pending_expires_at
      )
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'pending', DATE_ADD(NOW(), INTERVAL ? MINUTE))`,
      {
        replacements: [
          customerId,
          fieldId,
          aggregate.courtId,
          managerId || null,
          aggregate.startTime,
          aggregate.endTime,
          aggregate.totalPrice,
          finalNote,
          customerName,
          customerPhone,
          PENDING_HOLD_MINUTES,
        ],
        transaction,
      },
    );

    const [[bookingRow]] = await sequelize.query(
      `SELECT * FROM bookings WHERE booking_id = LAST_INSERT_ID()`,
      { transaction },
    );
    const bookingId = bookingRow?.booking_id;
    if (!bookingId) {
      throw new Error("BOOKING_CREATE_FAILED");
    }

    for (const item of normalizedBookings) {
      await sequelize.query(
        `INSERT INTO booking_slots (
          booking_id, field_id, court_id, start_time, end_time, price
        )
         VALUES (?, ?, ?, ?, ?, ?)`,
        {
          replacements: [
            bookingId,
            fieldId,
            item.court_id,
            item.start_time,
            item.end_time,
            item.price,
          ],
          transaction,
        },
      );
    }

    if (matchPostPayload) {
      await createMatchPostForBooking({
        bookingId,
        fieldId,
        ownerUserId: customerId,
        payload: matchPostPayload,
        transaction,
      });
    }

    booking = bookingRow;
  });

  return booking;
};

const buildTimeRange = (courtId, startMinutes, endMinutes) => ({
  courtId,
  startTime: `${String(Math.floor(startMinutes / 60)).padStart(2, "0")}:${String(startMinutes % 60).padStart(2, "0")}`,
  endTime: `${String(Math.floor(endMinutes / 60)).padStart(2, "0")}:${String(endMinutes % 60).padStart(2, "0")}`,
});

const parseTags = (tagsCsv) => {
  if (!tagsCsv) return [];
  return String(tagsCsv)
    .split("|||")
    .map((tag) => tag.trim())
    .filter(Boolean);
};

const normalizeForSearch = (value) =>
  String(value || "")
    .trim()
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d");

const compactForSearch = (value) =>
  normalizeForSearch(value).replace(/[\s,.\-_]/g, "");

const compactSqlExpression = (expression) =>
  `REPLACE(REPLACE(REPLACE(REPLACE(LOWER(COALESCE(${expression}, '')) COLLATE utf8mb4_unicode_ci, 'đ', 'd'), ' ', ''), ',', ''), '.', '')`;

const buildTextSearchClause = (columns, value, replacements) => {
  const normalized = normalizeForSearch(value);
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
    football: ["football", "bongda", "soccer"],
    bongda: ["football", "bongda", "soccer"],
    soccer: ["football", "bongda", "soccer"],
    badminton: ["badminton", "caulong"],
    caulong: ["badminton", "caulong"],
    tennis: ["tennis"],
    pickleball: ["pickleball"],
    volleyball: ["volleyball", "bongchuyen"],
    bongchuyen: ["volleyball", "bongchuyen"],
    basketball: ["basketball", "bongro"],
    bongro: ["basketball", "bongro"],
  };
  return aliases[normalized] || [normalized];
};

export const mapFieldRowToListPayload = (row) => {
  const ratingValue = Number(row.rating_value || 0);
  const priceLabel = formatPriceLabel(row.slot_price);
  const hoursLabel = formatHoursLabel(row.open_time, row.close_time);
  const tags = parseTags(row.tags_csv);
  const sportName = row.sport_name || "";
  const sportIconType = SPORT_NAME_TO_ICON[sportName] || "FOOTBALL";
  const avatarImageUrl = row.avatar_image_url || null;
  const cardImageUrl = row.card_image_url || null;
  const image = cardImageUrl || avatarImageUrl || DEFAULT_IMAGE_URL;
  const distanceKm =
    row.distance_km === null || row.distance_km === undefined
      ? null
      : Number(row.distance_km);

  return {
    field_id: row.field_id,
    field_name: row.field_name,
    name: row.field_name,
    location: row.location || "Chua cap nhat",
    status: row.status,
    sport_id: row.sport_id,
    sport_name: sportName,
    sport_icon_type: sportIconType,
    latitude: row.latitude,
    longitude: row.longitude,
    slot_price: row.slot_price,
    price_per_hour: row.slot_price,
    price: priceLabel,
    rating: ratingValue.toFixed(1),
    reviews: Number(row.review_count || 0),
    is_pro_league: Boolean(row.featured),
    availability: row.availability_note || "",
    card_type: row.card_type || "LARGE_IMAGE",
    avatar_image_url: avatarImageUrl,
    card_image_url: cardImageUrl,
    tags,
    facilities: tags,
    region: row.region || "",
    province: row.province || "",
    district: row.district || "",
    contact_phone: row.phone || "",
    owner_phone: row.owner_phone || "",
    phone: row.phone || "",
    distance_km: distanceKm,
    distance: formatDistanceLabel(distanceKm),
    hours: hoursLabel,
    openTime: hoursLabel,
    image,
    imageUrl: image,
    isOpen: row.status === "active",
    type: sportName || "San the thao",
  };
};

const queryFieldList = async (query, options = {}) => {
  const { requireUserPoint = false, excludeNoCoordinate = false } = options;
  const {
    q,
    keyword,
    location,
    address,
    category,
    sportType,
    lat,
    lng,
    user_lat,
    user_lng,
    radius,
    radius_km,
    sortBy,
    limit = 50,
    page = 1,
  } = query;
  const safeLimit = Math.max(
    1,
    Math.min(Number.parseInt(limit, 10) || 50, 100),
  );
  const safePage = Math.max(1, Number.parseInt(page, 10) || 1);
  const offset = (safePage - 1) * safeLimit;
  const userLat = parseCoordinate(lat ?? user_lat);
  const userLng = parseCoordinate(lng ?? user_lng);
  const hasUserPoint = userLat !== null && userLng !== null;
  const radiusKm = parseCoordinate(radius ?? radius_km);

  if (requireUserPoint && !hasUserPoint) {
    return { error: { status: 400, message: "lat and lng are required" } };
  }

  const whereClauses = ["f.status = ?"];
  const replacements = ["active"];

  if (excludeNoCoordinate) {
    whereClauses.push("f.latitude IS NOT NULL");
    whereClauses.push("f.longitude IS NOT NULL");
  }

  const keywordTerm = String(keyword ?? q ?? "").trim();
  if (keywordTerm) {
    const clause = buildTextSearchClause(
      [
        "f.field_name",
        "f.location",
        "f.province",
        "f.district",
        "st.sport_name",
      ],
      keywordTerm,
      replacements,
    );
    if (clause) whereClauses.push(clause);
  }

  const addressTerm = String(address ?? location ?? "").trim();
  if (addressTerm) {
    const clause = buildTextSearchClause(
      ["f.location", "f.region", "f.province", "f.district"],
      addressTerm,
      replacements,
    );
    if (clause) whereClauses.push(clause);
  }

  const sportTerm = String(sportType ?? category ?? "").trim();
  if (sportTerm) {
    const aliases = sportTypeAliases(sportTerm).filter(Boolean);
    if (aliases.length > 0) {
      whereClauses.push(
        `(${aliases.map(() => `${compactSqlExpression("st.sport_name")} LIKE ?`).join(" OR ")})`,
      );
      aliases.forEach((alias) => replacements.push(`%${alias}%`));
    }
  }

  const distanceSql = hasUserPoint
    ? `(${EARTH_RADIUS_KM} * ACOS(LEAST(1, GREATEST(-1,
        COS(RADIANS(?)) * COS(RADIANS(f.latitude)) * COS(RADIANS(f.longitude) - RADIANS(?)) +
        SIN(RADIANS(?)) * SIN(RADIANS(f.latitude))
    ))))`
    : "NULL";

  const distanceReplacements = hasUserPoint ? [userLat, userLng, userLat] : [];
  const havingClauses = [];
  const havingReplacements = [];
  if (hasUserPoint && radiusKm !== null && radiusKm > 0) {
    havingClauses.push("distance_km <= ?");
    havingReplacements.push(radiusKm);
  }

  let orderBy = hasUserPoint
    ? "(distance_km IS NULL) ASC, distance_km ASC, f.field_id ASC"
    : "f.field_id ASC";
  if (String(sortBy || "").toLowerCase() === "rating") {
    orderBy = "rating_value DESC, f.field_id ASC";
  } else if (String(sortBy || "").toLowerCase() === "name") {
    orderBy = "f.field_name ASC, f.field_id ASC";
  } else if (String(sortBy || "").toLowerCase() === "newest") {
    orderBy = "f.created_at DESC, f.field_id DESC";
  }

  const [rows] = await sequelize.query(
    `
    SELECT
      f.field_id,
      f.field_name,
      f.location,
      f.status,
      f.latitude,
      f.longitude,
      f.open_time,
      f.close_time,
      f.slot_price,
      f.avatar_image_url,
      f.card_image_url,
      f.sport_id,
      f.display_rating,
      f.featured,
      f.availability_note,
      f.card_type,
      f.region,
      f.province,
      f.district,
      mgr.phone AS owner_phone,
      ${distanceSql} AS distance_km,
      st.sport_name,
      COALESCE(f.display_rating, AVG(r.rating), 0) AS rating_value,
      COUNT(DISTINCT r.review_id) AS review_count,
      GROUP_CONCAT(DISTINCT ft.tag_name ORDER BY ft.sort_order SEPARATOR '|||') AS tags_csv
    FROM fields f
    LEFT JOIN person mgr ON mgr.person_id = f.manager_id
    LEFT JOIN sport_types st ON st.sport_id = f.sport_id
    LEFT JOIN reviews r ON r.field_id = f.field_id
    LEFT JOIN field_tags ft ON ft.field_id = f.field_id
    WHERE ${whereClauses.join(" AND ")}
    GROUP BY
      f.field_id, f.field_name, f.location, f.status, f.latitude, f.longitude,
      f.open_time, f.close_time, f.slot_price, f.avatar_image_url, f.card_image_url,
      f.sport_id, f.display_rating, f.featured, f.availability_note, f.card_type,
      f.region, f.province, f.district, mgr.phone, st.sport_name
    ${havingClauses.length > 0 ? `HAVING ${havingClauses.join(" AND ")}` : ""}
    ORDER BY ${orderBy}
    LIMIT ? OFFSET ?
    `,
    {
      replacements: [
        ...distanceReplacements,
        ...replacements,
        ...havingReplacements,
        safeLimit,
        offset,
      ],
    },
  );

  return { rows };
};

// GET /api/user/fields
export const listFields = async (req, res) => {
  try {
    const result = await queryFieldList(req.query);
    if (result.error) {
      return res
        .status(result.error.status)
        .json({ message: result.error.message });
    }
    return res.json(result.rows.map(mapFieldRowToListPayload));
  } catch (error) {
    console.error("List fields error:", error);
    return res
      .status(500)
      .json({ message: "Server error when fetching fields" });
  }
};

// GET /api/user/fields/nearby
export const listNearbyFields = async (req, res) => {
  try {
    const result = await queryFieldList(req.query, {
      requireUserPoint: true,
      excludeNoCoordinate: true,
    });

    if (result.error) {
      return res
        .status(result.error.status)
        .json({ message: result.error.message });
    }
    return res.json(result.rows.map(mapFieldRowToListPayload));
  } catch (error) {
    console.error("List nearby fields error:", error);
    return res
      .status(500)
      .json({ message: "Server error when fetching nearby fields" });
  }
};

// GET /api/fields/search
export const searchFields = async (req, res) => {
  try {
    const result = await queryFieldList(req.query);
    if (result.error) {
      return res
        .status(result.error.status)
        .json({ message: result.error.message });
    }
    return res.json(result.rows.map(mapFieldRowToListPayload));
  } catch (error) {
    console.error("Search fields error:", error);
    return res
      .status(500)
      .json({ message: "Server error when searching fields" });
  }
};

// GET /api/user/fields/:id
export const getField = async (req, res) => {
  try {
    const { id } = req.params;
    const { date, lat, lng, user_lat, user_lng } = req.query;
    const userLat = parseCoordinate(lat ?? user_lat);
    const userLng = parseCoordinate(lng ?? user_lng);
    const hasUserPoint = userLat !== null && userLng !== null;
    const distanceSql = hasUserPoint
      ? `(${EARTH_RADIUS_KM} * ACOS(LEAST(1, GREATEST(-1,
          COS(RADIANS(?)) * COS(RADIANS(f.latitude)) * COS(RADIANS(f.longitude) - RADIANS(?)) +
          SIN(RADIANS(?)) * SIN(RADIANS(f.latitude))
      ))))`
      : "NULL";
    const distanceReplacements = hasUserPoint
      ? [userLat, userLng, userLat]
      : [];

    const [[fieldRow]] = await sequelize.query(
      `
      SELECT
        f.field_id,
        f.manager_id,
        f.field_name,
        f.location,
        f.status,
        f.latitude,
        f.longitude,
        f.phone,
        f.open_time,
        f.close_time,
        f.slot_minutes,
        f.slot_price,
        f.avatar_image_url,
        f.card_image_url,
        f.sport_id,
        f.display_rating,
        f.featured,
        f.availability_note,
        f.card_type,
        f.region,
        f.province,
        f.district,
        mgr.phone AS owner_phone,
        ${distanceSql} AS distance_km,
        st.sport_name,
        COALESCE(f.display_rating, AVG(r.rating), 0) AS rating_value,
        COUNT(DISTINCT r.review_id) AS review_count
      FROM fields f
      LEFT JOIN person mgr ON mgr.person_id = f.manager_id
      LEFT JOIN sport_types st ON st.sport_id = f.sport_id
      LEFT JOIN reviews r ON r.field_id = f.field_id
      WHERE f.field_id = ?
      GROUP BY
        f.field_id, f.manager_id, f.field_name, f.location, f.status, f.latitude, f.longitude,
        f.phone, f.open_time, f.close_time, f.slot_minutes, f.slot_price, f.avatar_image_url,
        f.card_image_url, f.sport_id, f.display_rating, f.featured, f.availability_note,
        f.card_type, f.region, f.province, f.district, mgr.phone, st.sport_name
      LIMIT 1
      `,
      { replacements: [...distanceReplacements, id] },
    );

    if (!fieldRow) {
      return res.status(404).json({ message: "Field not found" });
    }

    const [images] = await sequelize.query(
      "SELECT image_id, image_url, is_primary FROM field_images WHERE field_id = ? ORDER BY is_primary DESC, image_id ASC",
      { replacements: [id] },
    );
    const [services] = await sequelize.query(
      "SELECT id, service_name, description, is_free, price FROM field_services WHERE field_id = ? ORDER BY id ASC",
      { replacements: [id] },
    );
    const [policies] = await sequelize.query(
      "SELECT id, title, content, policy_type FROM field_policies WHERE field_id = ? ORDER BY id ASC",
      { replacements: [id] },
    );
    const [tagRows] = await sequelize.query(
      "SELECT tag_name FROM field_tags WHERE field_id = ? ORDER BY sort_order ASC, tag_name ASC",
      { replacements: [id] },
    );
    const [[reviewStats]] = await sequelize.query(
      `SELECT
        COALESCE(AVG(rating), 0) AS avg_rating,
        COUNT(*) AS total_reviews
      FROM reviews
      WHERE field_id = ?`,
      { replacements: [id] },
    );

    let allSlots = [];
    const now = new Date();

    if (date) {
      const slots = await getAvailableSlots(id, date);
      allSlots = slots.map((slot) => ({
        start_time: slot.start_time.toISOString(),
        end_time: slot.end_time.toISOString(),
        available: slot.available,
        shift_label: slot.shift_label,
        booking_status: slot.booking_status,
      }));
    } else {
      const dates = [];
      for (let d = 0; d < 7; d++) {
        const day = new Date(now);
        day.setDate(now.getDate() + d);
        dates.push(day);
      }

      const slotsByDate = await getAvailableSlots(id, dates);
      Object.values(slotsByDate).forEach((slots) => {
        const daySlots = slots.map((slot) => ({
          start_time: slot.start_time.toISOString(),
          end_time: slot.end_time.toISOString(),
          available: slot.available,
          shift_label: slot.shift_label,
          booking_status: slot.booking_status,
        }));
        allSlots = allSlots.concat(daySlots);
      });
    }

    const mapped = mapFieldRowToListPayload({
      ...fieldRow,
      tags_csv: tagRows.map((item) => item.tag_name).join("|||"),
    });

    const data = {
      ...mapped,
      manager_id: fieldRow.manager_id,
      phone: fieldRow.phone || "",
      slot_minutes: fieldRow.slot_minutes,
      images,
      services,
      policies,
      slots: allSlots,
      review_stats: {
        avg_rating: Number(reviewStats?.avg_rating || mapped.rating || 0),
        total_reviews: Number(
          reviewStats?.total_reviews || mapped.reviews || 0,
        ),
      },
    };

    res.json(data);
  } catch (err) {
    console.error("getField error", err);
    res.status(500).json({ message: "Server error" });
  }
};

// GET /api/user/fields/:id/grid - Get booking grid data for a field and date
export const getFieldGrid = async (req, res) => {
  try {
    const { id } = req.params;
    const { date } = req.query;

    if (!id || !date) {
      return res
        .status(400)
        .json({ message: "Field ID and Date are required" });
    }

    const [[fieldRow]] = await sequelize.query(
      `SELECT field_id, field_name, open_time, close_time, slot_minutes, slot_price 
       FROM fields WHERE field_id = ? LIMIT 1`,
      { replacements: [id] },
    );

    if (!fieldRow) {
      return res.status(404).json({ message: "Field not found" });
    }

    await releaseExpiredPendingBookings();

    const [courts] = await sequelize.query(
      `SELECT court_id, court_name 
       FROM field_courts WHERE field_id = ? ORDER BY sort_order ASC`,
      { replacements: [id] },
    );
    const gridCourts =
      courts.length > 0
        ? courts
        : [{ court_id: "default", court_name: "San 1" }];

    const formatTime = (timeStr) =>
      timeStr ? String(timeStr).substring(0, 5) : "";
    const openTime = formatTime(fieldRow.open_time) || "06:00";
    const closeTime = formatTime(fieldRow.close_time) || "22:00";
    const slotMinutes = fieldRow.slot_minutes || 60;
    const normalizedOpenMinutes = roundMinutesUpToHalfHour(
      (() => {
        const [hours, minutes] = String(openTime)
          .split(":")
          .map((item) => Number.parseInt(item, 10) || 0);
        return hours * 60 + minutes;
      })(),
    );
    const rawCloseMinutes = (() => {
      const [hours, minutes] = String(closeTime)
        .split(":")
        .map((item) => Number.parseInt(item, 10) || 0);
      return hours * 60 + minutes;
    })();
    const normalizedCloseMinutes = rawCloseMinutes <= normalizedOpenMinutes
      ? 24 * 60
      : Math.min(roundMinutesToNearestStep(rawCloseMinutes, slotMinutes), 24 * 60);
    const normalizedOpenTime = `${String(Math.floor(normalizedOpenMinutes / 60) % 24).padStart(2, "0")}:${String(normalizedOpenMinutes % 60).padStart(2, "0")}`;
    const normalizedCloseTime = `${String(Math.floor(normalizedCloseMinutes / 60) % 24).padStart(2, "0")}:${String(normalizedCloseMinutes % 60).padStart(2, "0")}`;
    const price = Number(fieldRow.slot_price) || 0;
    const selectedDate = String(date || "").trim();
    const todayVn = getVnDateString();
    const currentVnMinutes = getVnTimeMinutes();
    const isPastDate = selectedDate < todayVn;
    const isToday = selectedDate === todayVn;

    // Fetch booked slots for the date
    const [bookings] = await sequelize.query(
      `SELECT bs.court_id, bs.start_time, bs.end_time
       FROM booking_slots bs
       INNER JOIN bookings b ON b.booking_id = bs.booking_id
       WHERE bs.field_id = ? AND DATE(bs.start_time) = ?
         AND ${ACTIVE_BOOKING_STATUS_CONDITION}`,
      { replacements: [id, date] },
    );
    const matchPosts = await listOpenMatchPostPreviewsForFieldDate(id, date);

    const [blockedRows] = await sequelize.query(
      `SELECT court_id, start_time, end_time
       FROM field_blocked_slots
       WHERE field_id = ? AND block_date = ?`,
      { replacements: [id, date] },
    );

    const bookedSlots = bookings.flatMap((b) => {
      const targetCourts = b.court_id
        ? [String(b.court_id)]
        : gridCourts.map((court) => String(court.court_id));

      const startStr = formatDbTimeLabel(b.start_time);
      const endStr = formatDbTimeLabel(b.end_time);

      return targetCourts.map((courtId) => ({
        courtId,
        startTime: startStr,
        endTime: endStr,
      }));
    });

    const blockedSlots = [];
    if (isPastDate || isToday) {
      const openMinutesValue = normalizedOpenMinutes;
      const closeMinutesValue = normalizedCloseMinutes;
      const blockMinutes = Math.max(1, Number(slotMinutes) || 60);
      const allSlotStarts = [];
      let current = openMinutesValue;
      while (current + blockMinutes <= closeMinutesValue) {
        allSlotStarts.push(current);
        current += blockMinutes;
      }

      const lockedStarts = isPastDate
        ? allSlotStarts
        : allSlotStarts.filter((slotStart) => slotStart < currentVnMinutes);

      blockedSlots.push(
        ...lockedStarts.flatMap((slotStart) =>
          gridCourts.map((court) => ({
            courtId: String(court.court_id),
            startTime: `${String(Math.floor(slotStart / 60)).padStart(2, "0")}:${String(slotStart % 60).padStart(2, "0")}`,
            endTime: `${String(Math.floor((slotStart + blockMinutes) / 60)).padStart(2, "0")}:${String((slotStart + blockMinutes) % 60).padStart(2, "0")}`,
          })),
        ),
      );
    }

    blockedRows.forEach((row) => {
      const startStr = formatDbTimeLabel(row.start_time);
      const endStr = formatDbTimeLabel(row.end_time);
      const targetCourts = row.court_id
        ? [String(row.court_id)]
        : gridCourts.map((court) => String(court.court_id));

      blockedSlots.push(
        ...targetCourts.map((courtId) => ({
          courtId,
          startTime: startStr,
          endTime: endStr,
        })),
      );
    });

    const responseData = {
      selectedDate: date,
      grid: {
        openTime: normalizedOpenTime,
        closeTime: normalizedCloseTime,
        gridStepMinutes: slotMinutes,
        minBookingMinutes: slotMinutes,
        courts: gridCourts.map((c) => ({
          id: String(c.court_id),
          name: c.court_name,
        })),
        bookedSlots: bookedSlots,
        blockedSlots: blockedSlots,
        matchPosts,
      },
      pricePerHour: price,
      estimatedPrice:
        price > 0 ? `${price.toLocaleString("vi-VN")}đ` : "Liên hệ",
    };

    res.json(responseData);
  } catch (err) {
    console.error("getFieldGrid error", err);
    res.status(500).json({ message: "Server error when fetching field grid" });
  }
};

// POST /api/user/bookings
export const createBooking = async (req, res) => {
  try {
    // Get customer_id from authenticated user (JWT payload has 'id' field)
    const customer_id = req.user?.id;
    const {
      field_id,
      court_id,
      start_time,
      end_time,
      price,
      note,
      customer_name,
      customer_phone,
      find_opponent,
    } = req.body;

    if (!customer_id) {
      return res.status(400).json({ message: "Missing customer_id" });
    }
    if (!field_id) {
      return res.status(400).json({ message: "Missing field_id" });
    }
    if (!start_time) {
      return res.status(400).json({ message: "Missing start_time" });
    }
    if (!end_time) {
      return res.status(400).json({ message: "Missing end_time" });
    }

    const finalPrice = price || 0;
    const snapshotCustomerName =
      typeof customer_name === "string" && customer_name.trim()
        ? customer_name.trim()
        : null;
    const snapshotCustomerPhone =
      typeof customer_phone === "string" && customer_phone.trim()
        ? customer_phone.trim()
        : null;
    const finalNote = buildBookingNote(note, customer_name, customer_phone);
    const matchPostPayload = normalizeMatchPostPayload(find_opponent);

    const [customerCheck] = await sequelize.query(
      "SELECT person_id FROM person WHERE person_id = ? LIMIT 1",
      { replacements: [customer_id] },
    );

    if (!customerCheck || customerCheck.length === 0) {
      return res.status(400).json({
        message:
          "Customer ID does not exist. Please create a user account first.",
        error: "INVALID_CUSTOMER_ID",
      });
    }

    const [fieldCheck] = await sequelize.query(
      "SELECT field_id, manager_id FROM fields WHERE field_id = ? LIMIT 1",
      { replacements: [field_id] },
    );

    if (!fieldCheck || fieldCheck.length === 0) {
      return res.status(400).json({
        message: "Field ID does not exist",
        error: "INVALID_FIELD_ID",
      });
    }

    const normalizedBookings = normalizeBookingRequests([
      {
        court_id,
        start_time,
        end_time,
        price: finalPrice,
      },
    ]);
    await validateFieldCourts(field_id, normalizedBookings);

    const booking = await createAggregateBookingWithSlots({
      customerId: customer_id,
      fieldId: field_id,
      managerId: fieldCheck[0].manager_id || null,
      normalizedBookings,
      finalNote,
      customerName: snapshotCustomerName,
      customerPhone: snapshotCustomerPhone,
      matchPostPayload,
    });

    res.status(201).json({
      message: "Booking created",
      booking,
      pending_hold_seconds: PENDING_HOLD_MINUTES * 60,
    });
  } catch (err) {
    if (err?.code === "SLOT_NOT_AVAILABLE") {
      return res.status(409).json({
        message: "Khung gio nay khong kha dung",
        error: "SLOT_NOT_AVAILABLE",
      });
    }
    if (err?.code === "INVALID_FIELD_ID") {
      return res.status(400).json({
        message: "Field ID does not exist",
        error: "INVALID_FIELD_ID",
      });
    }
    if (err?.code === "INVALID_COURT_ID") {
      return res.status(400).json({
        message: "Court ID does not exist for this field",
        error: "INVALID_COURT_ID",
      });
    }
    if (err?.code === "INVALID_SLOT_RANGE" || err?.code === "DUPLICATE_BOOKING_SLOT") {
      return res.status(400).json({
        message: "Khung gio dat san khong hop le",
        error: err.code,
      });
    }
    if (
      err?.code === "TEAM_NAME_REQUIRED" ||
      err?.code === "INVALID_PLAYER_COUNT" ||
      err?.code === "INVALID_MATCH_LEVEL"
    ) {
      return res.status(400).json({
        message: "Thong tin tim doi thu khong hop le",
        error: err.code,
      });
    }

    console.error("createBooking error:", err);
    console.error("Error stack:", err.stack);
    console.error("SQL Error:", err.original?.sqlMessage);
    res.status(500).json({
      message: "Server error when creating booking",
      error: err.message,
      sqlError: err.original?.sqlMessage || err.original?.message,
      details: err.toString(),
    });
  }
};

// POST /api/user/bookings/batch
export const createBatchBookings = async (req, res) => {
  try {
    const customer_id = req.user?.id;
    const { field_id, bookings, note, customer_name, customer_phone } =
      req.body || {};
    const matchPostPayload = normalizeMatchPostPayload(req.body?.find_opponent);

    if (!customer_id) {
      return res.status(400).json({ message: "Missing customer_id" });
    }
    if (!field_id) {
      return res.status(400).json({ message: "Missing field_id" });
    }
    if (!Array.isArray(bookings) || bookings.length === 0) {
      return res
        .status(400)
        .json({ message: "bookings must be a non-empty array" });
    }

    const [customerCheck] = await sequelize.query(
      "SELECT person_id FROM person WHERE person_id = ? LIMIT 1",
      { replacements: [customer_id] },
    );
    if (!customerCheck || customerCheck.length === 0) {
      return res.status(400).json({
        message:
          "Customer ID does not exist. Please create a user account first.",
        error: "INVALID_CUSTOMER_ID",
      });
    }

    const [fieldCheck] = await sequelize.query(
      "SELECT field_id, manager_id FROM fields WHERE field_id = ? LIMIT 1",
      { replacements: [field_id] },
    );
    if (!fieldCheck || fieldCheck.length === 0) {
      return res.status(400).json({
        message: "Field ID does not exist",
        error: "INVALID_FIELD_ID",
      });
    }

    const snapshotCustomerName =
      typeof customer_name === "string" && customer_name.trim()
        ? customer_name.trim()
        : null;
    const snapshotCustomerPhone =
      typeof customer_phone === "string" && customer_phone.trim()
        ? customer_phone.trim()
        : null;
    const finalNote = buildBookingNote(note, customer_name, customer_phone);
    const normalizedBookings = normalizeBookingRequests(bookings);
    await validateFieldCourts(field_id, normalizedBookings);

    const booking = await createAggregateBookingWithSlots({
      customerId: customer_id,
      fieldId: field_id,
      managerId: fieldCheck[0].manager_id || null,
      normalizedBookings,
      finalNote,
      customerName: snapshotCustomerName,
      customerPhone: snapshotCustomerPhone,
      matchPostPayload,
    });

    res.status(201).json({
      message: "Booking created",
      booking,
      bookings: booking ? [booking] : [],
      pending_hold_seconds: PENDING_HOLD_MINUTES * 60,
    });
  } catch (err) {
    if (err?.code === "SLOT_NOT_AVAILABLE") {
      return res.status(409).json({
        message: "Khung gio nay khong kha dung",
        error: "SLOT_NOT_AVAILABLE",
      });
    }
    if (err?.code === "INVALID_FIELD_ID") {
      return res.status(400).json({
        message: "Field ID does not exist",
        error: "INVALID_FIELD_ID",
      });
    }
    if (err?.code === "INVALID_COURT_ID") {
      return res.status(400).json({
        message: "Court ID does not exist for this field",
        error: "INVALID_COURT_ID",
      });
    }
    if (err?.code === "INVALID_SLOT_RANGE" || err?.code === "DUPLICATE_BOOKING_SLOT") {
      return res.status(400).json({
        message: "Danh sach khung gio khong hop le",
        error: err.code,
      });
    }
    if (
      err?.code === "TEAM_NAME_REQUIRED" ||
      err?.code === "INVALID_PLAYER_COUNT" ||
      err?.code === "INVALID_MATCH_LEVEL"
    ) {
      return res.status(400).json({
        message: "Thong tin tim doi thu khong hop le",
        error: err.code,
      });
    }

    console.error("createBatchBookings error:", err);
    res.status(500).json({
      message: "Server error when creating bookings",
      error: err.message,
      sqlError: err.original?.sqlMessage || err.original?.message,
      details: err.toString(),
    });
  }
};

// GET /api/user/fields/:id/bookings - Get bookings for a specific field and date
export const getFieldBookings = async (req, res) => {
  try {
    const { id } = req.params;
    const { date } = req.query;

    if (!id) {
      return res.status(400).json({ message: "Field ID is required" });
    }

    if (!date) {
      return res.status(400).json({ message: "Date is required" });
    }

    await releaseExpiredPendingBookings();

    // Query bookings for the specific field and date
    const [rows] = await sequelize.query(
      `SELECT
        bs.booking_slot_id,
        bs.booking_id,
        b.customer_id,
        bs.field_id,
        bs.court_id,
        bs.start_time,
        bs.end_time,
        b.status,
        bs.price,
        b.note,
        b.pending_expires_at
      FROM booking_slots bs
      INNER JOIN bookings b ON b.booking_id = bs.booking_id
      WHERE bs.field_id = ?
        AND DATE(bs.start_time) = ?
        AND ${ACTIVE_BOOKING_STATUS_CONDITION}
      ORDER BY bs.start_time ASC`,
      { replacements: [id, date] },
    );

    res.json(rows);
  } catch (err) {
    console.error("getFieldBookings error", err);
    res
      .status(500)
      .json({ message: "Server error when fetching field bookings" });
  }
};

// GET /api/user/bookings/history - Get booking history for current user
export const getBookingHistory = async (req, res) => {
  try {
    const { customer_id } = req.query;

    if (!customer_id) {
      return res.status(400).json({ message: "Customer ID is required" });
    }

    const [rows] = await sequelize.query(
      `SELECT
        b.booking_id, b.customer_id, b.field_id, b.start_time, b.end_time,
        b.price, b.status, b.note,
        f.field_name, f.location
      FROM bookings b
      LEFT JOIN fields f ON b.field_id = f.field_id
      WHERE b.customer_id = ?
      ORDER BY b.booking_id DESC`,
      { replacements: [customer_id] },
    );

    res.json(rows);
  } catch (err) {
    console.error("getBookingHistory error", err);
    res
      .status(500)
      .json({ message: "Server error when fetching booking history" });
  }
};

// GET /api/user/bookings/:id - Get booking details with field info
export const getBooking = async (req, res) => {
  try {
    const { id } = req.params;
    const userId = req.user?.id;

    if (!userId) {
      return res.status(401).json({
        success: false,
        message: "Unauthorized",
      });
    }

    if (!id) {
      return res.status(400).json({
        success: false,
        message: "Booking ID is required",
      });
    }

    const booking = await getBookingShareDetailByBookingId(id, { userId });
    if (!booking) {
      return res.status(404).json({
        success: false,
        message: "Booking not found",
      });
    }
    const publicBaseUrl =
      process.env.PUBLIC_WEB_BASE_URL || `${req.protocol}://${req.get("host")}`;
    const responseData = buildBookingShareResponse(booking, publicBaseUrl);

    res.json({
      success: true,
      data: responseData,
    });
  } catch (err) {
    console.error("getBooking error", err);
    res.status(500).json({
      success: false,
      message: "Server error when fetching booking",
      error: process.env.NODE_ENV === "development" ? err.message : undefined,
    });
  }
};

// PUT /api/user/bookings/:id - Update booking (payment method, status)
export const updateBooking = async (req, res) => {
  try {
    const { id } = req.params;
    const { payment_method, status } = req.body;

    // Build dynamic update query
    const updates = [];
    const replacements = [];

    if (payment_method) {
      updates.push("note = CONCAT(COALESCE(note, ''), ' | Payment: ', ?)");
      replacements.push(payment_method);
    }

    if (status) {
      updates.push("status = ?");
      replacements.push(status);
    }

    if (updates.length === 0) {
      return res.status(400).json({ message: "No fields to update" });
    }

    replacements.push(id);

    await sequelize.query(
      `UPDATE bookings SET ${updates.join(", ")} WHERE booking_id = ?`,
      { replacements },
    );

    const [rows] = await sequelize.query(
      "SELECT * FROM bookings WHERE booking_id = ? LIMIT 1",
      { replacements: [id] },
    );

    res.json({ message: "Booking updated", booking: rows?.[0] });
  } catch (err) {
    console.error("updateBooking error", err);
    res.status(500).json({ message: "Server error when updating booking" });
  }
};

// GET /api/manager/bookings - Get all bookings for manager
export const listBookings = async (req, res) => {
  try {
    const [rows] = await sequelize.query(
      `SELECT
        b.booking_id, b.customer_id, b.field_id, b.start_time, b.end_time,
        b.price, b.status, b.note,
        f.field_name, f.location,
        p.name as customer_name, p.phone as customer_phone
      FROM bookings b
      LEFT JOIN fields f ON b.field_id = f.field_id
      LEFT JOIN person p ON b.customer_id = p.person_id
      ORDER BY b.booking_id DESC`,
    );

    res.json(rows || []);
  } catch (err) {
    console.error("listBookings error", err);
    res.status(500).json({ message: "Server error when fetching bookings" });
  }
};

// PUT /api/manager/bookings/:id/approve - Approve booking
export const approveBooking = async (req, res) => {
  try {
    const { id } = req.params;

    await sequelize.query(
      `UPDATE bookings SET status = 'confirmed', pending_expires_at = NULL WHERE booking_id = ?`,
      { replacements: [id] },
    );

    const [rows] = await sequelize.query(
      "SELECT * FROM bookings WHERE booking_id = ? LIMIT 1",
      { replacements: [id] },
    );

    res.json({ message: "Booking approved", booking: rows?.[0] });
  } catch (err) {
    console.error("approveBooking error", err);
    res.status(500).json({ message: "Server error when approving booking" });
  }
};

// PUT /api/manager/bookings/:id/reject - Reject booking
export const rejectBooking = async (req, res) => {
  try {
    const { id } = req.params;
    const { reason } = req.body;

    const noteUpdate = reason ? ` | Ly do tu choi: ${reason}` : "";

    await sequelize.query(
      `UPDATE bookings SET status = 'rejected', note = CONCAT(COALESCE(note, ''), ?) WHERE booking_id = ?`,
      { replacements: [noteUpdate, id] },
    );

    const [rows] = await sequelize.query(
      "SELECT * FROM bookings WHERE booking_id = ? LIMIT 1",
      { replacements: [id] },
    );

    res.json({ message: "Booking rejected", booking: rows?.[0] });
  } catch (err) {
    console.error("rejectBooking error", err);
    res.status(500).json({ message: "Server error when rejecting booking" });
  }
};
