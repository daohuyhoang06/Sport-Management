import { cert, getApps, initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";

const FIREBASE_PROJECT_ID = process.env.FIREBASE_PROJECT_ID;
const FIREBASE_CLIENT_EMAIL = process.env.FIREBASE_CLIENT_EMAIL;
const FIREBASE_PRIVATE_KEY = process.env.FIREBASE_PRIVATE_KEY;

let warnedMissingConfig = false;

const hasFirebaseAdminConfig = () =>
  Boolean(FIREBASE_PROJECT_ID && FIREBASE_CLIENT_EMAIL && FIREBASE_PRIVATE_KEY);

const normalizePrivateKey = (key) => key.replace(/\\n/g, "\n");

export const isFirebaseAuthEnabled = () => hasFirebaseAdminConfig();

export const getFirebaseAuth = () => {
  if (!hasFirebaseAdminConfig()) {
    if (!warnedMissingConfig) {
      warnedMissingConfig = true;
      console.warn(
        "Firebase Auth disabled: missing FIREBASE_PROJECT_ID/FIREBASE_CLIENT_EMAIL/FIREBASE_PRIVATE_KEY",
      );
    }
    return null;
  }

  if (!getApps().length) {
    initializeApp({
      credential: cert({
        projectId: FIREBASE_PROJECT_ID,
        clientEmail: FIREBASE_CLIENT_EMAIL,
        privateKey: normalizePrivateKey(FIREBASE_PRIVATE_KEY),
      }),
    });
  }

  return getAuth();
};

