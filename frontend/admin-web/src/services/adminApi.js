const TOKEN_KEYS = ["adminToken", "accessToken", "token"];

function readToken() {
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

export async function adminFetch(path, options = {}) {
  const token = readToken();
  const headers = new Headers(options.headers || {});

  if (!headers.has("Content-Type") && options.body) {
    headers.set("Content-Type", "application/json");
  }

  if (token) {
    headers.set(
      "Authorization",
      token.startsWith("Bearer ") ? token : `Bearer ${token}`,
    );
  }

  const response = await fetch(path, {
    ...options,
    headers,
  });

  const contentType = response.headers.get("content-type") || "";
  const data = contentType.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const errorMessage =
      typeof data === "string" ? data : data?.message || "Request failed";
    throw new Error(errorMessage);
  }

  return data;
}
