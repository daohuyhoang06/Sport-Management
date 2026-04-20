import { useCallback, useEffect, useState } from "react";
import { adminFetch } from "../services/adminApi";

function normalizeFields(rawFields = []) {
  return rawFields.map((item) => ({
    id: item.field_id,
    name: item.field_name || "-",
    location: item.location || "-",
    managerName: item.manager_name || "Unassigned",
    pricePerHour: Number(item.rental_price || 0),
    status: item.status || "inactive",
  }));
}

export default function useAdminFields() {
  const [fields, setFields] = useState([]);
  const [stats, setStats] = useState({
    total: 0,
    active: 0,
    inactive: 0,
    maintenance: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadFields = useCallback(async (signal) => {
    try {
      setLoading(true);
      setError("");

      const [fieldsResponse, statsResponse] = await Promise.all([
        adminFetch("/api/admin/fields?page=1&limit=200"),
        adminFetch("/api/admin/fields/stats"),
      ]);

      if (signal.cancelled) {
        return;
      }

      const fieldsData = fieldsResponse?.data?.fields ?? [];
      const statsData = statsResponse?.data ?? {};

      setFields(normalizeFields(fieldsData));
      setStats({
        total: Number(statsData.total ?? fieldsData.length ?? 0),
        active: Number(statsData.active ?? 0),
        inactive: Number(statsData.inactive ?? 0),
        maintenance: Number(statsData.maintenance ?? 0),
      });
    } catch (fetchError) {
      if (signal.cancelled) {
        return;
      }

      setError(fetchError.message || "Unable to load fields");
    } finally {
      if (!signal.cancelled) {
        setLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    const signal = { cancelled: false };

    loadFields(signal);

    return () => {
      signal.cancelled = true;
    };
  }, [loadFields]);

  const reload = useCallback(
    () => loadFields({ cancelled: false }),
    [loadFields],
  );

  const toggleFieldStatus = useCallback(
    async (field_id) => {
      await adminFetch(`/api/admin/fields/${field_id}/status`, {
        method: "PATCH",
      });
      await reload();
    },
    [reload],
  );

  const createField = useCallback(
    async (fieldData) => {
      await adminFetch("/api/admin/fields", {
        method: "POST",
        body: JSON.stringify(fieldData),
      });
      await reload();
    },
    [reload],
  );

  return {
    fields,
    stats,
    loading,
    error,
    reload,
    toggleFieldStatus,
    createField,
  };
}
