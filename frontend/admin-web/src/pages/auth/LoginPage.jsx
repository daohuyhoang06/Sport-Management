import { useEffect, useState } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import {
  clearStoredAuthToken,
  getTokenRole,
  persistAuthToken,
  readStoredAuthToken,
} from "../../services/authStorage";

function getLoginMessage(error) {
  if (!error) {
    return "";
  }

  return error;
}

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [formState, setFormState] = useState({
    identifier: "",
    password: "",
    rememberMe: true,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const token = readStoredAuthToken();
  const role = getTokenRole(token);

  useEffect(() => {
    if (token && role === "admin") {
      navigate("/admin/dashboard", { replace: true });
      return;
    }

    if (token && role !== "admin") {
      clearStoredAuthToken();
    }
  }, [navigate, role, token]);

  const targetPath =
    location.state?.from?.pathname &&
    location.state.from.pathname.startsWith("/admin")
      ? location.state.from.pathname
      : "/admin/dashboard";

  const handleChange = (event) => {
    const { name, type, checked, value } = event.target;

    setFormState((current) => ({
      ...current,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!formState.identifier.trim() || !formState.password.trim()) {
      setError("Vui lòng nhập đầy đủ thông tin đăng nhập.");
      return;
    }

    try {
      setLoading(true);
      setError("");

      const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          identifier: formState.identifier.trim(),
          password: formState.password,
        }),
      });

      const data = await response.json().catch(() => null);

      if (!response.ok) {
        throw new Error(data?.message || "Đăng nhập không thành công.");
      }

      const tokenFromResponse = data?.data?.token || data?.token;

      if (!tokenFromResponse) {
        throw new Error("Máy chủ không trả về token đăng nhập.");
      }

      if (getTokenRole(tokenFromResponse) !== "admin") {
        clearStoredAuthToken();
        throw new Error("Tài khoản này không có quyền quản trị.");
      }

      persistAuthToken(tokenFromResponse, formState.rememberMe);
      navigate(targetPath, { replace: true });
    } catch (submitError) {
      setError(
        submitError?.message ||
          "Không thể đăng nhập lúc này. Vui lòng thử lại.",
      );
    } finally {
      setLoading(false);
    }
  };

  if (token && role === "admin") {
    return <Navigate to="/admin/dashboard" replace />;
  }

  return (
    <main className="login-page">
      <div className="login-orb login-orb-left" />
      <div className="login-orb login-orb-right" />

      <section className="login-card" aria-label="Admin login form">
        <header className="login-header">
          <h1>Đăng nhập</h1>
          <p>Chào mừng bạn quay trở lại!</p>
        </header>

        <form className="login-form" onSubmit={handleSubmit}>
          <label className="login-field">
            <span>Username hoặc Email</span>
            <input
              type="text"
              name="identifier"
              value={formState.identifier}
              onChange={handleChange}
              placeholder="Nhập username hoặc email"
              autoComplete="username"
            />
          </label>

          <label className="login-field">
            <span>Mật khẩu</span>
            <input
              type="password"
              name="password"
              value={formState.password}
              onChange={handleChange}
              placeholder="Nhập mật khẩu"
              autoComplete="current-password"
            />
          </label>

          <div className="login-row">
            <label className="login-remember">
              <input
                type="checkbox"
                name="rememberMe"
                checked={formState.rememberMe}
                onChange={handleChange}
              />
              <span>Ghi nhớ đăng nhập</span>
            </label>

            <a href="/login" onClick={(event) => event.preventDefault()}>
              Quên mật khẩu?
            </a>
          </div>

          {error && <p className="login-error">{getLoginMessage(error)}</p>}

          <button type="submit" className="login-submit" disabled={loading}>
            {loading ? "Đang đăng nhập..." : "Đăng nhập"}
          </button>

          <div className="login-divider">
            <span />
            <p>
              Chưa có tài khoản?{" "}
              <a href="/register" onClick={(event) => event.preventDefault()}>
                Đăng ký ngay
              </a>
            </p>
            <span />
          </div>

          <div className="login-social-label">Hoặc đăng nhập bằng</div>

          <div className="login-social-row">
            <button type="button" className="login-social-btn">
              <span className="login-social-icon google">🔎</span>
              Google
            </button>
            <button type="button" className="login-social-btn">
              <span className="login-social-icon facebook">f</span>
              Facebook
            </button>
          </div>

          <Link to="/admin/dashboard" className="login-back-link">
            ← Quay lại trang quản trị
          </Link>
        </form>
      </section>

      <div className="login-fab-group" aria-hidden="true">
        <div className="login-fab login-fab-ai">🤖</div>
        <div className="login-fab login-fab-chat">💬</div>
      </div>
    </main>
  );
}
