import { cert, getApps, initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getMessaging } from "firebase-admin/messaging";

const FIREBASE_PROJECT_ID = process.env.FIREBASE_PROJECT_ID;
const FIREBASE_CLIENT_EMAIL = process.env.FIREBASE_CLIENT_EMAIL;
const FIREBASE_PRIVATE_KEY = process.env.FIREBASE_PRIVATE_KEY;

let warnedMissingConfig = false;

const hasFirebaseAdminConfig = () =>
  Boolean(FIREBASE_PROJECT_ID && FIREBASE_CLIENT_EMAIL && FIREBASE_PRIVATE_KEY);

const normalizePrivateKey = (key) => key.replace(/\\n/g, "\n");

export const isFirebaseAuthEnabled = () => hasFirebaseAdminConfig();

const getFirebaseApp = () => {
  if (!hasFirebaseAdminConfig()) {
    if (!warnedMissingConfig) {
      warnedMissingConfig = true;
      console.warn(
        "Firebase disabled: missing FIREBASE_PROJECT_ID/FIREBASE_CLIENT_EMAIL/FIREBASE_PRIVATE_KEY",
      );
    }
    return null;
  }

  if (!getApps().length) {
    return initializeApp({
      credential: cert({
        projectId: FIREBASE_PROJECT_ID,
        clientEmail: FIREBASE_CLIENT_EMAIL,
        privateKey: normalizePrivateKey(FIREBASE_PRIVATE_KEY),
      }),
    });
  }

  return getApps()[0];
};

export const getFirebaseAuth = () => {
  const app = getFirebaseApp();
  return app ? getAuth(app) : null;
};

export const getFirebaseMessaging = () => {
  const app = getFirebaseApp();
  return app ? getMessaging(app) : null;
};

