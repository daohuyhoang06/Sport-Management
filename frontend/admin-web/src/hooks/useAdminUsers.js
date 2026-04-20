import { useCallback, useEffect, useState } from "react";
import { adminFetch } from "../services/adminApi";

function normalizeUsers(rawUsers = []) {
  return rawUsers.map((item) => ({
    id: item.person_id,
    name: item.person_name || "-",
    email: item.email || "-",
    role: item.role || "user",
    status: item.status || "inactive",
  }));
}

export default function useAdminUsers() {
  const [users, setUsers] = useState([]);
  const [stats, setStats] = useState({ total: 0, active: 0, inactive: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadUsers = useCallback(async (signal) => {
    try {
      setLoading(true);
      setError("");

      const [usersResponse, statsResponse] = await Promise.all([
        adminFetch("/api/admin/users?page=1&limit=200"),
        adminFetch("/api/admin/users/stats"),
      ]);

      if (signal.cancelled) {
        return;
      }

      const usersData = usersResponse?.data?.users ?? [];
      const statsData = statsResponse?.data ?? {};

      setUsers(normalizeUsers(usersData));
      setStats({
        total: Number(statsData.total ?? usersData.length ?? 0),
        active: Number(statsData.active ?? 0),
        inactive: Number(statsData.inactive ?? 0),
      });
    } catch (fetchError) {
      if (signal.cancelled) {
        return;
      }

      setError(fetchError.message || "Unable to load users");
    } finally {
      if (!signal.cancelled) {
        setLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    const signal = { cancelled: false };

    loadUsers(signal);

    return () => {
      signal.cancelled = true;
    };
  }, [loadUsers]);

  const reload = useCallback(
    () => loadUsers({ cancelled: false }),
    [loadUsers],
  );

  const toggleUserStatus = useCallback(
    async (userId) => {
      await adminFetch(`/api/admin/users/${userId}/status`, {
        method: "PATCH",
      });
      await reload();
    },
    [reload],
  );

  const createUser = useCallback(
    async (userData) => {
      await adminFetch("/api/admin/users", {
        method: "POST",
        body: JSON.stringify(userData),
      });
      await reload();
    },
    [reload],
  );

  return {
    users,
    stats,
    loading,
    error,
    reload,
    toggleUserStatus,
    createUser,
  };
}
