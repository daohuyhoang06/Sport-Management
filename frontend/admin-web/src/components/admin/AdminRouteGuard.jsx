import { Navigate, Outlet, useLocation } from "react-router-dom";
import { getTokenRole, readStoredAuthToken } from "../../services/authStorage";

export default function AdminRouteGuard({ children }) {
  const location = useLocation();
  const token = readStoredAuthToken();
  const role = getTokenRole(token);

  if (!token || role !== "admin") {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return children ?? <Outlet />;
}
