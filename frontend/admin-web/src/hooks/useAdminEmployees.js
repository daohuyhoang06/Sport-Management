import { useCallback, useEffect, useState } from "react";
import { adminFetch } from "../services/adminApi";

function normalizeEmployees(rawEmployees = []) {
  return rawEmployees.map((item) => ({
    id: item.person_id,
    name: item.person_name || "-",
    email: item.email || "-",
    phone: item.phone || "-",
    role: item.role || "manager",
    status: item.status || "inactive",
    assignedField:
      item.field_names ||
      (item.field_count ? `${item.field_count} field(s)` : "Unassigned"),
  }));
}

export default function useAdminEmployees() {
  const [employees, setEmployees] = useState([]);
  const [stats, setStats] = useState({ total: 0, active: 0, inactive: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadEmployees = useCallback(async (signal) => {
    try {
      setLoading(true);
      setError("");

      const [employeesResponse, statsResponse] = await Promise.all([
        adminFetch("/api/admin/employees?page=1&limit=200"),
        adminFetch("/api/admin/employees/stats"),
      ]);

      if (signal.cancelled) {
        return;
      }

      const employeesData = employeesResponse?.data?.employees ?? [];
      const statsData = statsResponse?.data ?? {};

      setEmployees(normalizeEmployees(employeesData));
      setStats({
        total: Number(statsData.total ?? employeesData.length ?? 0),
        active: Number(statsData.active ?? 0),
        inactive: Number(statsData.inactive ?? 0),
      });
    } catch (fetchError) {
      if (signal.cancelled) {
        return;
      }

      setError(fetchError.message || "Unable to load employees");
    } finally {
      if (!signal.cancelled) {
        setLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    const signal = { cancelled: false };

    loadEmployees(signal);

    return () => {
      signal.cancelled = true;
    };
  }, [loadEmployees]);

  const reload = useCallback(
    () => loadEmployees({ cancelled: false }),
    [loadEmployees],
  );

  const createEmployee = useCallback(
    async (employeeData) => {
      await adminFetch("/api/admin/employees", {
        method: "POST",
        body: JSON.stringify(employeeData),
      });
      await reload();
    },
    [reload],
  );

  const deleteEmployee = useCallback(
    async (employeeId) => {
      await adminFetch(`/api/admin/employees/${employeeId}`, {
        method: "DELETE",
      });
      await reload();
    },
    [reload],
  );

  return {
    employees,
    stats,
    loading,
    error,
    reload,
    createEmployee,
    deleteEmployee,
  };
}
