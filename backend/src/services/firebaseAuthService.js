import { getFirebaseAuth, isFirebaseAuthEnabled } from "../config/firebaseAdmin.js";
import fetch from "node-fetch";

const FIREBASE_WEB_API_KEY = process.env.FIREBASE_WEB_API_KEY;
const FIREBASE_SIGN_IN_ENDPOINT =
  "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword";
const FIREBASE_SIGN_IN_WITH_IDP_ENDPOINT =
  "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp";

const isFirebaseWebApiConfigured = () => Boolean(FIREBASE_WEB_API_KEY);

export class FirebaseAuthServiceError extends Error {
  constructor(message, { code, status = 500, details } = {}) {
    super(message);
    this.name = "FirebaseAuthServiceError";
    this.code = code;
    this.status = status;
    this.details = details;
  }
}

export const isFirebaseAuthFlowEnabled = () =>
  isFirebaseAuthEnabled() && isFirebaseWebApiConfigured();

export const createFirebaseUser = async ({
  email,
  password,
  displayName,
  disabled = false,
}) => {
  const auth = getFirebaseAuth();
  if (!auth) {
    throw new FirebaseAuthServiceError(
      "\u0110\u0103ng nh\u1eadp kh\u00f4ng kh\u1ea3 d\u1ee5ng. Vui l\u00f2ng th\u1eed l\u1ea1i sau.",
      { code: "FIREBASE_NOT_CONFIGURED", status: 500 },
    );
  }

  return auth.createUser({
    email,
    password,
    displayName,
    disabled,
  });
};

export const deleteFirebaseUser = async (uid) => {
  if (!uid) {
    return;
  }
  const auth = getFirebaseAuth();
  if (!auth) {
    return;
  }
  await auth.deleteUser(uid);
};

export const verifyFirebaseIdToken = async (idToken) => {
  const auth = getFirebaseAuth();
  if (!auth) {
    throw new FirebaseAuthServiceError(
      "\u0110\u0103ng nh\u1eadp kh\u00f4ng kh\u1ea3 d\u1ee5ng. Vui l\u00f2ng th\u1eed l\u1ea1i sau.",
      { code: "FIREBASE_NOT_CONFIGURED", status: 500 },
    );
  }
  return auth.verifyIdToken(idToken);
};

export const signInWithFirebasePassword = async ({ email, password }) => {
  if (!isFirebaseWebApiConfigured()) {
    throw new FirebaseAuthServiceError(
      "\u0110\u0103ng nh\u1eadp kh\u00f4ng kh\u1ea3 d\u1ee5ng. Vui l\u00f2ng th\u1eed l\u1ea1i sau.",
      { code: "FIREBASE_WEB_API_KEY_MISSING", status: 500 },
    );
  }

  const url = `${FIREBASE_SIGN_IN_ENDPOINT}?key=${FIREBASE_WEB_API_KEY}`;
  const response = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      email,
      password,
      returnSecureToken: true,
    }),
  });

  const payload = await response.json().catch(() => null);

  if (!response.ok) {
    const firebaseCode = payload?.error?.message;
    if (
      firebaseCode === "INVALID_PASSWORD" ||
      firebaseCode === "INVALID_LOGIN_CREDENTIALS" ||
      firebaseCode === "EMAIL_NOT_FOUND"
    ) {
      throw new FirebaseAuthServiceError(
        "Th\u00f4ng tin \u0111\u0103ng nh\u1eadp kh\u00f4ng ch\u00ednh x\u00e1c.",
        {
          code: firebaseCode,
          status: 401,
        },
      );
    }

    if (firebaseCode === "USER_DISABLED") {
      throw new FirebaseAuthServiceError(
        "T\u00e0i kho\u1ea3n c\u1ee7a b\u1ea1n \u0111\u00e3 b\u1ecb v\u00f4 hi\u1ec7u h\u00f3a.",
        {
          code: firebaseCode,
          status: 403,
        },
      );
    }

    throw new FirebaseAuthServiceError(
      "\u0110\u0103ng nh\u1eadp kh\u00f4ng th\u00e0nh c\u00f4ng. Vui l\u00f2ng th\u1eed l\u1ea1i.",
      {
        code: firebaseCode || "FIREBASE_SIGNIN_FAILED",
        status: 500,
        details: payload,
      },
    );
  }

  return payload;
};

export const signInWithFirebaseIdp = async ({
  providerId,
  idToken,
  accessToken,
}) => {
  if (!isFirebaseWebApiConfigured()) {
    throw new FirebaseAuthServiceError(
      "\u0110\u0103ng nh\u1eadp kh\u00f4ng kh\u1ea3 d\u1ee5ng. Vui l\u00f2ng th\u1eed l\u1ea1i sau.",
      { code: "FIREBASE_WEB_API_KEY_MISSING", status: 500 },
    );
  }

  if (!providerId || (!idToken && !accessToken)) {
    throw new FirebaseAuthServiceError(
      "Thi\u1ebfu th\u00f4ng tin \u0111\u0103ng nh\u1eadp Google.",
      { code: "SOCIAL_LOGIN_INPUT_INVALID", status: 400 },
    );
  }

  const postBodyParts = mutablePostBodyParts(providerId, idToken, accessToken);
  const url = `${FIREBASE_SIGN_IN_WITH_IDP_ENDPOINT}?key=${FIREBASE_WEB_API_KEY}`;
  const response = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      requestUri: "http://localhost",
      returnSecureToken: true,
      returnIdpCredential: true,
      postBody: postBodyParts.join("&"),
    }),
  });

  const payload = await response.json().catch(() => null);

  if (!response.ok) {
    throw new FirebaseAuthServiceError(
      "\u0110\u0103ng nh\u1eadp Google kh\u00f4ng th\u00e0nh c\u00f4ng. Vui l\u00f2ng th\u1eed l\u1ea1i.",
      {
        code: payload?.error?.message || "FIREBASE_IDP_SIGNIN_FAILED",
        status: 401,
        details: payload,
      },
    );
  }

  return payload;
};

const mutablePostBodyParts = (providerId, idToken, accessToken) => {
  const parts = [`providerId=${encodeURIComponent(providerId)}`];
  if (idToken) {
    parts.push(`id_token=${encodeURIComponent(idToken)}`);
  }
  if (accessToken) {
    parts.push(`access_token=${encodeURIComponent(accessToken)}`);
  }
  return parts;
};
