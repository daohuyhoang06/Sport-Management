import { useEffect, useState } from "react";
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

  useEffect(() => {
    let active = true;

    async function loadUsers() {
      try {
        setLoading(true);
        setError("");

        const [usersResponse, statsResponse] = await Promise.all([
          adminFetch("/api/admin/users?page=1&limit=200"),
          adminFetch("/api/admin/users/stats"),
        ]);

        if (!active) {
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
        if (!active) {
          return;
        }

        setError(fetchError.message || "Unable to load users");
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadUsers();

    return () => {
      active = false;
    };
  }, []);

  return {
    users,
    stats,
    loading,
    error,
  };
}
