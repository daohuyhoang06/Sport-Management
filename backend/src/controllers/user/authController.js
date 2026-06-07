import jwt from "jsonwebtoken";
import { randomUUID } from "crypto";
import Person from "../../models/Person.js";
import { Op, QueryTypes } from "sequelize";
import {
  createFirebaseUser,
  deleteFirebaseUser,
  FirebaseAuthServiceError,
  isFirebaseAuthFlowEnabled,
  signInWithFirebaseIdp,
  signInWithFirebasePassword,
  updateFirebaseUserPassword,
  verifyFirebaseIdToken,
} from "../../services/firebaseAuthService.js";

const DEFAULT_MEMBERSHIP_LEVEL = "\u0110\u1ed3ng";
const INVALID_LOGIN_MESSAGE =
  "Th\u00f4ng tin \u0111\u0103ng nh\u1eadp kh\u00f4ng ch\u00ednh x\u00e1c.";
const ACCOUNT_INACTIVE_MESSAGE =
  "T\u00e0i kho\u1ea3n \u0111\u00e3 b\u1ecb kh\u00f3a ho\u1eb7c v\u00f4 hi\u1ec7u h\u00f3a.";
const SPORT_NAME_BY_KEY = {
  FOOTBALL: "Bóng đá",
  VOLLEYBALL: "Bóng chuyền",
  PICKLEBALL: "Pickleball",
  BADMINTON: "Cầu lông",
  TENNIS: "Tennis",
};

// Generate JWT Token
const generateToken = (user) => {
  return jwt.sign(
    {
      id: user.person_id,
      username: user.username,
      role: user.role,
    },
    process.env.JWT_SECRET,
    {
      expiresIn: process.env.JWT_EXPIRE || "7d",
    },
  );
};

// Generate Refresh Token
const generateRefreshToken = (user) => {
  return jwt.sign(
    {
      id: user.person_id,
      username: user.username,
      role: user.role,
    },
    process.env.JWT_REFRESH_SECRET || process.env.JWT_SECRET,
    {
      expiresIn: process.env.JWT_REFRESH_EXPIRE || "30d",
    },
  );
};

const mapFirebaseCreateUserErrorMessage = (error) => {
  if (!error || !(error instanceof Error)) {
    return "Khong the tao tai khoan Firebase";
  }

  const errorCode = error.code || "";

  if (
    errorCode === "auth/email-already-exists" ||
    errorCode === "EMAIL_EXISTS"
  ) {
    return "Email da duoc su dung";
  }

  if (errorCode === "auth/invalid-password") {
    return "Mat khau khong hop le theo yeu cau Firebase";
  }

  if (errorCode === "auth/invalid-email") {
    return "Email khong hop le";
  }

  return "Khong the tao tai khoan Firebase";
};

const sanitizeUsernameBase = (value) =>
  (value || "")
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^a-z0-9]+/g, "")
    .trim();

const generateUniqueUsername = async (candidateSources = []) => {
  const fallbackBase = "user";
  const seed =
    candidateSources
      .map((item) => sanitizeUsernameBase(item))
      .find((item) => item.length >= 3) || fallbackBase;

  let usernameCandidate = seed.slice(0, 24);
  let suffix = 0;

  while (true) {
    const exists = await Person.findOne({
      where: { username: usernameCandidate },
    });

    if (!exists) {
      return usernameCandidate;
    }

    suffix += 1;
    usernameCandidate = `${seed.slice(0, 20)}${suffix}`;
  }
};

const findUserByLoginIdentifier = async (loginIdentifier) =>
  Person.findOne({
    where: {
      [Op.or]: [
        { username: loginIdentifier },
        { email: loginIdentifier },
        { phone: loginIdentifier },
      ],
    },
  });

const resolvePublicAssetUrl = (req, assetPath) => {
  if (!assetPath) {
    return null;
  }
  if (/^https?:\/\//i.test(assetPath)) {
    return assetPath;
  }
  if (!req) {
    return assetPath;
  }
  const normalizedPath = assetPath.startsWith("/") ? assetPath : `/${assetPath}`;
  return `${req.protocol}://${req.get("host")}${normalizedPath}`;
};

const resolveUserProfileStats = async (personId) => {
  if (!personId) {
    return {
      bookingCount: "0",
      rating: "0.0",
    };
  }

  const [[bookingRow], [reviewRow]] = await Promise.all([
    Person.sequelize.query(
      `SELECT COUNT(*) AS booking_count
       FROM bookings
       WHERE customer_id = ?
         AND status IN ('confirmed', 'approved', 'completed')`,
      {
        replacements: [personId],
        type: QueryTypes.SELECT,
      },
    ),
    Person.sequelize.query(
      `SELECT ROUND(AVG(rating), 1) AS average_rating
       FROM reviews
       WHERE customer_id = ?`,
      {
        replacements: [personId],
        type: QueryTypes.SELECT,
      },
    ),
  ]);

  const bookingCount = Number.parseInt(bookingRow?.booking_count, 10);
  const averageRating = Number(reviewRow?.average_rating);

  return {
    bookingCount: Number.isFinite(bookingCount) ? String(bookingCount) : "0",
    rating: Number.isFinite(averageRating) ? averageRating.toFixed(1) : "0.0",
  };
};

const serializeUser = async (user, req) => {
  const raw = user.toJSON();
  const membership = raw.membership_level || DEFAULT_MEMBERSHIP_LEVEL;
  const avatarUrl = resolvePublicAssetUrl(req, raw.avatar_url);
  const [favoriteSportsData, profileStats] = await Promise.all([
    resolveFavoriteSports(raw.favorite_sport_ids),
    resolveUserProfileStats(raw.person_id),
  ]);
  return {
    ...raw,
    membership,
    avatarUrl,
    ...profileStats,
    ...favoriteSportsData,
  };
};

const parseBirthdayInput = (value) => {
  if (!value || typeof value !== "string") {
    return null;
  }

  const trimmed = value.trim();
  if (!trimmed) {
    return null;
  }

  const slashMatch = trimmed.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
  if (slashMatch) {
    const [, dd, mm, yyyy] = slashMatch;
    const iso = `${yyyy}-${mm}-${dd}`;
    const date = new Date(iso);
    if (!Number.isNaN(date.getTime())) {
      return iso;
    }
  }

  const isoMatch = trimmed.match(/^(\d{4})-(\d{2})-(\d{2})$/);
  if (isoMatch) {
    const date = new Date(trimmed);
    if (!Number.isNaN(date.getTime())) {
      return trimmed;
    }
  }

  return null;
};

const parseFavoriteSportIds = (value) => {
  if (!value) {
    return [];
  }

  if (Array.isArray(value)) {
    return value
      .map((item) => Number(item))
      .filter((item) => Number.isInteger(item) && item > 0);
  }

  if (typeof value === "string") {
    try {
      const parsed = JSON.parse(value);
      if (Array.isArray(parsed)) {
        return parsed
          .map((item) => Number(item))
          .filter((item) => Number.isInteger(item) && item > 0);
      }
    } catch (_error) {
      return [];
    }
  }

  return [];
};

const parseFavoriteSportKeys = (value) => {
  if (!value) {
    return [];
  }
  if (!Array.isArray(value)) {
    return [];
  }
  return value
    .map((item) => `${item || ""}`.trim().toUpperCase())
    .filter((item) => item in SPORT_NAME_BY_KEY);
};

const resolveSportIdsByNames = async (sportNames = []) => {
  if (!sportNames.length) {
    return [];
  }

  const rows = await Person.sequelize.query(
    `SELECT sport_id, sport_name FROM sport_types WHERE sport_name IN (:sportNames)`,
    {
      replacements: { sportNames },
      type: QueryTypes.SELECT,
    },
  );

  return rows
    .map((row) => Number(row.sport_id))
    .filter((item) => Number.isInteger(item) && item > 0);
};

const resolveValidSportIds = async ({ favoriteSportIds, favoriteSportKeys }) => {
  const idsFromPayload = parseFavoriteSportIds(favoriteSportIds);
  const keysFromPayload = parseFavoriteSportKeys(favoriteSportKeys);

  const idsFromKeys = await resolveSportIdsByNames(
    keysFromPayload.map((key) => SPORT_NAME_BY_KEY[key]).filter(Boolean),
  );

  const merged = [...new Set([...idsFromPayload, ...idsFromKeys])];
  if (!merged.length) {
    return [];
  }

  const validRows = await Person.sequelize.query(
    `SELECT sport_id FROM sport_types WHERE sport_id IN (:sportIds)`,
    {
      replacements: { sportIds: merged },
      type: QueryTypes.SELECT,
    },
  );

  return validRows
    .map((row) => Number(row.sport_id))
    .filter((item) => Number.isInteger(item) && item > 0);
};

const resolveFavoriteSports = async (favoriteSportIds = []) => {
  const ids = [...new Set(parseFavoriteSportIds(favoriteSportIds))];
  if (!ids.length) {
    return {
      favoriteSportIds: [],
      favoriteSportKeys: [],
      favoriteSports: [],
    };
  }

  const rows = await Person.sequelize.query(
    `SELECT sport_id, sport_name FROM sport_types WHERE sport_id IN (:sportIds) ORDER BY sport_id ASC`,
    {
      replacements: { sportIds: ids },
      type: QueryTypes.SELECT,
    },
  );

  const favoriteSports = rows.map((row) => {
    const sportId = Number(row.sport_id);
    const sportName = `${row.sport_name || ""}`;
    const sportKey =
      Object.entries(SPORT_NAME_BY_KEY).find(([, name]) => name === sportName)?.[0] ||
      null;
    return {
      sportId,
      sportName,
      sportKey,
    };
  });

  return {
    favoriteSportIds: favoriteSports.map((item) => item.sportId),
    favoriteSportKeys: favoriteSports.map((item) => item.sportKey).filter(Boolean),
    favoriteSports,
  };
};

const buildLoginResponse = async (
  user,
  req,
  {
    firebaseToken = null,
    firebaseRefreshToken = null,
  } = {},
) => ({
  success: true,
  message: "\u0110\u0103ng nh\u1eadp th\u00e0nh c\u00f4ng",
  data: {
    user: await serializeUser(user, req),
    token: generateToken(user),
    refreshToken: generateRefreshToken(user),
    firebaseToken,
    firebaseRefreshToken,
  },
});

const ensureActiveUser = (user, res) => {
  if (!user) {
    res.status(401).json({
      success: false,
      message: INVALID_LOGIN_MESSAGE,
    });
    return false;
  }

  if (user.status !== "active") {
    res.status(403).json({
      success: false,
      message: ACCOUNT_INACTIVE_MESSAGE,
    });
    return false;
  }

  return true;
};

const createRandomPassword = () => `${randomUUID()}Aa1!`;

const syncFirebaseUidIfNeeded = async (user, firebaseUid) => {
  if (!firebaseUid || user.firebase_uid === firebaseUid) {
    return user;
  }

  user.firebase_uid = firebaseUid;
  await user.save();
  return user;
};

const resolveSocialProviderId = (provider) => {
  if (provider === "google" || provider === "google.com") {
    return "google.com";
  }

  if (provider === "facebook" || provider === "facebook.com") {
    return "facebook.com";
  }

  return null;
};

const findOrCreateSocialUser = async ({
  firebaseUid,
  email,
  name,
}) => {
  const normalizedEmail = email?.trim().toLowerCase() || null;

  let user = await Person.findOne({
    where: {
      [Op.or]: [
        firebaseUid ? { firebase_uid: firebaseUid } : null,
        normalizedEmail ? { email: normalizedEmail } : null,
      ].filter(Boolean),
    },
  });

  if (user) {
    if (firebaseUid && user.firebase_uid !== firebaseUid) {
      user.firebase_uid = firebaseUid;
      await user.save();
    }
    return user;
  }

  const username = await generateUniqueUsername([
    normalizedEmail?.split("@")?.[0],
    name,
    firebaseUid,
  ]);

  user = await Person.create({
    name: name || username,
    email: normalizedEmail,
    phone: null,
    username,
    password: createRandomPassword(),
    birthday: null,
    sex: null,
    address: null,
    firebase_uid: firebaseUid,
    membership_level: DEFAULT_MEMBERSHIP_LEVEL,
    role: "user",
    status: "active",
  });

  return user;
};

// @desc    Register new user
// @route   POST /api/auth/register
// @access  Public
export const register = async (req, res) => {
  let createdFirebaseUid = null;

  try {
    const {
      name,
      email,
      phone,
      username,
      password,
      birthday,
      sex,
      address,
      favoriteSportIds,
      favoriteSportKeys,
    } =
      req.body;
    const firebaseAuthEnabled = isFirebaseAuthFlowEnabled();
    const normalizedEmail = email?.trim().toLowerCase() || null;
    const normalizedPhone = phone?.trim() || null;
    const normalizedBirthday = parseBirthdayInput(birthday);
    const resolvedUsername =
      username?.trim() ||
      (await generateUniqueUsername([
        normalizedEmail?.split("@")?.[0],
        name,
        normalizedPhone,
      ]));

    // Validation
    if (!name || !password) {
      return res.status(400).json({
        success: false,
        message:
          "Vui l\u00f2ng \u0111i\u1ec1n \u0111\u1ea7y \u0111\u1ee7 th\u00f4ng tin b\u1eaft bu\u1ed9c (t\u00ean, m\u1eadt kh\u1ea9u)",
      });
    }

    if (birthday && !normalizedBirthday) {
      return res.status(400).json({
        success: false,
        message: "Ng\u00e0y sinh kh\u00f4ng h\u1ee3p l\u1ec7.",
      });
    }

    if (firebaseAuthEnabled && !normalizedEmail) {
      return res.status(400).json({
        success: false,
        message:
          "C\u1ea7n email \u0111\u1ec3 \u0111\u0103ng k\u00fd v\u1edbi Firebase Authentication",
      });
    }

    const resolvedFavoriteSportIds = await resolveValidSportIds({
      favoriteSportIds,
      favoriteSportKeys,
    });

    // Check if user already exists
    const existingUser = await Person.findOne({
      where: {
        [Person.sequelize.Sequelize.Op.or]: [
          { username: resolvedUsername },
          normalizedEmail ? { email: normalizedEmail } : null,
        ].filter(Boolean),
      },
    });

    if (existingUser) {
      if (existingUser.username === resolvedUsername) {
        return res.status(400).json({
          success: false,
          message: "Username da ton tai",
        });
      }
      if (existingUser.email === normalizedEmail) {
        return res.status(400).json({
          success: false,
          message: "Email da duoc su dung",
        });
      }
    }

    if (firebaseAuthEnabled) {
      try {
        const firebaseUser = await createFirebaseUser({
          email: normalizedEmail,
          password,
          displayName: name,
        });
        createdFirebaseUid = firebaseUser.uid;
      } catch (error) {
        return res.status(400).json({
          success: false,
          message: mapFirebaseCreateUserErrorMessage(error),
        });
      }
    }

    // Create user
    const user = await Person.create({
      name,
      email: normalizedEmail,
      phone: normalizedPhone,
      username: resolvedUsername,
      password,
      birthday: normalizedBirthday,
      sex,
      address,
      firebase_uid: createdFirebaseUid,
      favorite_sport_ids: resolvedFavoriteSportIds.length
        ? JSON.stringify(resolvedFavoriteSportIds)
        : null,
      membership_level: DEFAULT_MEMBERSHIP_LEVEL,
      role: "user",
      status: "active",
    });

    // Generate tokens
    const token = generateToken(user);
    const refreshToken = generateRefreshToken(user);

    res.status(201).json({
      success: true,
      message: "\u0110\u0103ng k\u00fd th\u00e0nh c\u00f4ng",
      data: {
        user: await serializeUser(user, req),
        token,
        refreshToken,
        firebaseToken: null,
        firebaseRefreshToken: null,
      },
    });
  } catch (error) {
    console.error("Register error:", error);

    if (createdFirebaseUid) {
      try {
        await deleteFirebaseUser(createdFirebaseUid);
      } catch (rollbackError) {
        console.error("Firebase rollback error:", rollbackError);
      }
    }

    // Handle Sequelize validation errors
    if (error.name === "SequelizeValidationError") {
      const messages = error.errors.map((err) => err.message);
      return res.status(400).json({
        success: false,
        message: messages.join(", "),
      });
    }

    res.status(500).json({
      success: false,
      message: "Loi server khi dang ky",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};

// @desc    Login user
// @route   POST /api/auth/login
// @access  Public
export const login = async (req, res) => {
  try {
    const { username, email, phone, identifier, password } = req.body;
    const loginIdentifier = (identifier || username || email || phone || "").trim();
    const firebaseAuthEnabled = isFirebaseAuthFlowEnabled();

    if (!loginIdentifier || !password) {
      return res.status(400).json({
        success: false,
        message:
          "Vui l\u00f2ng nh\u1eadp email/s\u1ed1 \u0111i\u1ec7n tho\u1ea1i v\u00e0 m\u1eadt kh\u1ea9u.",
      });
    }

    const user = await findUserByLoginIdentifier(loginIdentifier);
    if (!ensureActiveUser(user, res)) {
      return;
    }

    const isPasswordValid = await user.comparePassword(password);
    if (!isPasswordValid) {
      return res.status(401).json({
        success: false,
        message: INVALID_LOGIN_MESSAGE,
      });
    }

    if (user.phone === loginIdentifier && !user.email) {
      return res.status(200).json(await buildLoginResponse(user, req));
    }

    let hydratedUser = user;
    let firebaseIdToken = null;
    let firebaseRefreshToken = null;

    if (firebaseAuthEnabled && user.email) {
      try {
        const firebaseSignIn = await signInWithFirebasePassword({
          email: user.email,
          password,
        });

        firebaseIdToken = firebaseSignIn.idToken || null;
        firebaseRefreshToken = firebaseSignIn.refreshToken || null;

        if (firebaseIdToken) {
          const decodedFirebaseToken = await verifyFirebaseIdToken(firebaseIdToken);
          if (decodedFirebaseToken.uid) {
            hydratedUser = await syncFirebaseUidIfNeeded(hydratedUser, decodedFirebaseToken.uid);
          }
        }
      } catch (error) {
        if (error instanceof FirebaseAuthServiceError) {
          // Keep local login working even when Firebase credential state is out of sync.
          console.warn("Firebase password sign-in skipped:", {
            personId: user.person_id,
            code: error.code,
          });
        } else {
          throw error;
        }
      }
    }

    res.status(200).json(
      await buildLoginResponse(hydratedUser, req, {
        firebaseToken: firebaseIdToken,
        firebaseRefreshToken,
      }),
    );
  } catch (error) {
    console.error("Login error:", error);
    res.status(500).json({
      success: false,
      message:
        "\u0110\u0103ng nh\u1eadp kh\u00f4ng th\u00e0nh c\u00f4ng. Vui l\u00f2ng th\u1eed l\u1ea1i.",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};

// @desc    Login/Register user with Google/Facebook via Firebase
// @route   POST /api/auth/social-login
// @access  Public
export const socialLogin = async (req, res) => {
  try {
    const { provider, idToken, accessToken } = req.body;
    const providerId = resolveSocialProviderId(provider);

    if (!providerId) {
      return res.status(400).json({
        success: false,
        message: "Phương thức đăng nhập không được hỗ trợ.",
      });
    }

    const socialPayload = await signInWithFirebaseIdp({
      providerId,
      idToken,
      accessToken,
    });

    const firebaseToken = socialPayload.idToken || null;
    const firebaseRefreshToken = socialPayload.refreshToken || null;
    const decodedFirebaseToken = firebaseToken
      ? await verifyFirebaseIdToken(firebaseToken)
      : null;

    const user = await findOrCreateSocialUser({
      firebaseUid: decodedFirebaseToken?.uid || socialPayload.localId || null,
      email: socialPayload.email || decodedFirebaseToken?.email || null,
      name:
        socialPayload.displayName ||
        decodedFirebaseToken?.name ||
        socialPayload.rawUserInfo?.name ||
        null,
    });

    if (!ensureActiveUser(user, res)) {
      return;
    }

    res.status(200).json(await buildLoginResponse(user, req, {
      firebaseToken,
      firebaseRefreshToken,
    }));
  } catch (error) {
    console.error("Social login error:", error);

    if (error instanceof FirebaseAuthServiceError) {
      return res.status(error.status || 500).json({
        success: false,
        message: error.message,
      });
    }

    res.status(500).json({
      success: false,
      message: "Đăng nhập Google không thành công. Vui lòng thử lại.",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};

// @desc    Get current logged in user
// @route   GET /api/auth/me
// @access  Private
export const getMe = async (req, res) => {
  try {
    const user = await Person.findByPk(req.user.id);

    if (!user) {
      return res.status(404).json({
        success: false,
        message: "Khong tim thay nguoi dung",
      });
    }

    res.status(200).json({
      success: true,
      data: await serializeUser(user, req),
    });
  } catch (error) {
    console.error("Get me error:", error);
    res.status(500).json({
      success: false,
      message: "Loi server",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};

// @desc    Get available sport types
// @route   GET /api/auth/sport-types
// @access  Private
export const getSportTypes = async (_req, res) => {
  try {
    const sportTypes = await Person.sequelize.query(
      "SELECT sport_id AS sportId, sport_name AS sportName FROM sport_types ORDER BY sport_id ASC",
      { type: QueryTypes.SELECT },
    );

    const mappedSportTypes = sportTypes.map((item) => {
      const sportKey =
        Object.entries(SPORT_NAME_BY_KEY).find(([, name]) => name === item.sportName)?.[0] ||
        null;
      return {
        ...item,
        sportKey,
      };
    });

    return res.status(200).json({
      success: true,
      data: mappedSportTypes,
    });
  } catch (error) {
    console.error("Get sport types error:", error);
    return res.status(500).json({
      success: false,
      message: "Loi server khi lay danh sach mon the thao",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};

// @desc    Update current logged in user profile
// @route   PUT /api/auth/me
// @access  Private
export const updateMe = async (req, res) => {
  try {
    const user = await Person.findByPk(req.user.id);
    if (!user) {
      return res.status(404).json({
        success: false,
        message: "Khong tim thay nguoi dung",
      });
    }

    const {
      name,
      phone,
      birthday,
      sex,
      address,
      favoriteSportIds,
      favoriteSportKeys,
    } = req.body || {};

    if (typeof name === "string") {
      const trimmedName = name.trim();
      if (!trimmedName) {
        return res.status(400).json({
          success: false,
          message: "Ten khong duoc de trong",
        });
      }
      user.name = trimmedName;
    }

    if (typeof phone === "string") {
      const normalizedPhone = phone.trim();
      if (normalizedPhone && !/^\d{9,15}$/.test(normalizedPhone)) {
        return res.status(400).json({
          success: false,
          message: "So dien thoai khong hop le",
        });
      }
      user.phone = normalizedPhone || null;
    }

    if (birthday !== undefined) {
      const normalizedBirthday = parseBirthdayInput(birthday);
      if (birthday && !normalizedBirthday) {
        return res.status(400).json({
          success: false,
          message: "Ngay sinh khong hop le",
        });
      }
      user.birthday = normalizedBirthday;
    }

    if (typeof sex === "string") {
      const normalizedSex = sex.trim();
      user.sex = normalizedSex || null;
    }

    if (typeof address === "string") {
      const normalizedAddress = address.trim();
      user.address = normalizedAddress || null;
    }

    if (favoriteSportIds !== undefined || favoriteSportKeys !== undefined) {
      const resolvedFavoriteSportIds = await resolveValidSportIds({
        favoriteSportIds,
        favoriteSportKeys,
      });
      user.favorite_sport_ids = resolvedFavoriteSportIds.length
        ? JSON.stringify(resolvedFavoriteSportIds)
        : null;
    }

    await user.save();

    return res.status(200).json({
      success: true,
      message: "Cap nhat thong tin ca nhan thanh cong",
      data: await serializeUser(user, req),
    });
  } catch (error) {
    console.error("Update me error:", error);
    return res.status(500).json({
      success: false,
      message: "Loi server khi cap nhat thong tin ca nhan",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};

// @desc    Change current user's password
// @route   POST /api/auth/change-password
// @access  Private
export const changePassword = async (req, res) => {
  try {
    const { currentPassword, newPassword } = req.body || {};

    if (!currentPassword || !newPassword) {
      return res.status(400).json({
        success: false,
        message: "Vui long nhap day du mat khau hien tai va mat khau moi.",
      });
    }

    if (`${newPassword}`.length < 6) {
      return res.status(400).json({
        success: false,
        message: "Mat khau moi phai co it nhat 6 ky tu.",
      });
    }

    const user = await Person.findByPk(req.user.id);
    if (!user) {
      return res.status(404).json({
        success: false,
        message: "Khong tim thay nguoi dung",
      });
    }

    const isPasswordValid = await user.comparePassword(currentPassword);
    if (!isPasswordValid) {
      return res.status(401).json({
        success: false,
        message: "Mat khau hien tai khong chinh xac.",
      });
    }

    const firebaseUid = user.firebase_uid;
    user.password = newPassword;
    await user.save();

    let firebaseSynced = false;
    if (firebaseUid && isFirebaseAuthFlowEnabled()) {
      try {
        await updateFirebaseUserPassword(firebaseUid, newPassword);
        firebaseSynced = true;
      } catch (firebaseError) {
        console.warn("Firebase password sync skipped:", {
          personId: user.person_id,
          uid: firebaseUid,
          error: firebaseError?.message || firebaseError,
        });
      }
    }

    return res.status(200).json({
      success: true,
      message: "Cap nhat mat khau thanh cong",
      data: {
        firebaseSynced,
      },
    });
  } catch (error) {
    console.error("Change password error:", error);
    return res.status(500).json({
      success: false,
      message: "Loi server khi cap nhat mat khau",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};

// @desc    Update current user's avatar
// @route   POST /api/auth/me/avatar
// @access  Private
export const updateMyAvatar = async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({
        success: false,
        message: "Vui long tai len mot anh dai dien",
      });
    }

    const user = await Person.findByPk(req.user.id);
    if (!user) {
      return res.status(404).json({
        success: false,
        message: "Khong tim thay nguoi dung",
      });
    }

    user.avatar_url = `/uploads/avatars/${req.file.filename}`;
    await user.save();

    return res.status(200).json({
      success: true,
      message: "Cap nhat anh dai dien thanh cong",
      data: await serializeUser(user, req),
    });
  } catch (error) {
    console.error("Update avatar error:", error);
    return res.status(500).json({
      success: false,
      message: "Loi server khi cap nhat anh dai dien",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};

// @desc    Refresh token
// @route   POST /api/auth/refresh
// @access  Public
export const refreshToken = async (req, res) => {
  try {
    const { refreshToken } = req.body;

    if (!refreshToken) {
      return res.status(400).json({
        success: false,
        message: "Vui long cung cap refresh token",
      });
    }

    // Verify refresh token
    const decoded = jwt.verify(
      refreshToken,
      process.env.JWT_REFRESH_SECRET || process.env.JWT_SECRET,
    );

    // Get user from database
    const user = await Person.findByPk(decoded.id);
    if (!user) {
      return res.status(401).json({
        success: false,
        message: "User not found",
      });
    }

    // Generate new tokens
    const newToken = generateToken(user);
    const newRefreshToken = generateRefreshToken(user);

    res.status(200).json({
      success: true,
      data: {
        token: newToken,
        refreshToken: newRefreshToken,
      },
    });
  } catch (error) {
    console.error("Refresh token error:", error);
    res.status(401).json({
      success: false,
      message: "Refresh token khong hop le hoac da het han",
    });
  }
};

// @desc    Logout user
// @route   POST /api/auth/logout
// @access  Private
export const logout = async (req, res) => {
  try {
    // In a stateless JWT setup, logout is handled on the client side
    // by removing the token from storage
    // Optionally, you can implement token blacklisting here

    res.status(200).json({
      success: true,
      message: "Dang xuat thanh cong",
    });
  } catch (error) {
    console.error("Logout error:", error);
    res.status(500).json({
      success: false,
      message: "Loi server khi dang xuat",
    });
  }
};
