const TOKEN_KEYS = ["adminToken", "accessToken", "token"];

function getStorageTargets() {
  if (typeof window === "undefined") {
    return [];
  }

  return [window.localStorage, window.sessionStorage];
}

export function readStoredAuthToken() {
  if (typeof window === "undefined") {
    return null;
  }

  for (const key of TOKEN_KEYS) {
    const localToken = window.localStorage.getItem(key);
    if (localToken) {
      return localToken;
    }

    const sessionToken = window.sessionStorage.getItem(key);
    if (sessionToken) {
      return sessionToken;
    }
  }

  return null;
}

export function clearStoredAuthToken() {
  for (const storage of getStorageTargets()) {
    for (const key of TOKEN_KEYS) {
      storage.removeItem(key);
    }
  }
}

export function persistAuthToken(token, rememberMe = true) {
  if (typeof window === "undefined") {
    return;
  }

  clearStoredAuthToken();

  const storage = rememberMe ? window.localStorage : window.sessionStorage;

  for (const key of TOKEN_KEYS) {
    storage.setItem(key, token);
  }
}

export function decodeJwtPayload(token) {
  if (!token) {
    return null;
  }

  const rawToken = token.startsWith("Bearer ") ? token.slice(7) : token;
  const payloadPart = rawToken.split(".")[1];

  if (!payloadPart) {
    return null;
  }

  try {
    const normalized = payloadPart.replace(/-/g, "+").replace(/_/g, "/");
    const decoded = atob(
      normalized.padEnd(
        normalized.length + ((4 - (normalized.length % 4)) % 4),
        "=",
      ),
    );
    return JSON.parse(decoded);
  } catch {
    return null;
  }
}

export function getTokenRole(token) {
  const payload = decodeJwtPayload(token);
  return payload?.role ?? payload?.user?.role ?? null;
}
