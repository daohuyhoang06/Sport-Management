import { Navigate, Outlet, useLocation } from "react-router-dom";

const TOKEN_KEYS = ["adminToken", "accessToken", "token"];

function readStoredToken() {
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

function decodeJwtPayload(token) {
  const payloadPart = token.split(".")[1];
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

function getTokenRole(token) {
  if (!token) {
    return null;
  }

  const rawToken = token.startsWith("Bearer ") ? token.slice(7) : token;
  const payload = decodeJwtPayload(rawToken);
  return payload?.role ?? payload?.user?.role ?? null;
}

function AccessDenied({ title, description }) {
  return (
    <main className="auth-gate">
      <section className="hero">
        <div className="page-meta">
          <span className="badge">Admin only</span>
          <span className="badge">Authentication required</span>
        </div>
        <h2>{title}</h2>
        <p>{description}</p>
      </section>

      <section className="section-card">
        <div className="section-head">
          <div>
            <h3>What this guard checks</h3>
            <p>Matches the backend admin route requirements.</p>
          </div>
        </div>
        <div className="info-list">
          <div className="info-row">
            <strong>Token</strong>
            <span>Bearer token from localStorage or sessionStorage</span>
          </div>
          <div className="info-row">
            <strong>Role</strong>
            <span>JWT payload must contain role = admin</span>
          </div>
          <div className="info-row">
            <strong>Backend parity</strong>
            <span>Aligns with requireAuth + requireRole("admin")</span>
          </div>
        </div>
      </section>
    </main>
  );
}

export default function AdminRouteGuard({ children }) {
  const location = useLocation();
  const token = readStoredToken();
  const role = getTokenRole(token);

  if (!token || role !== "admin") {
    return (
      <AccessDenied
        title="Admin access required"
        description={`You need a valid admin JWT to open ${location.pathname}. Sign in with an admin account first, then reload this route.`}
      />
    );
  }

  return children ?? <Outlet />;
}
