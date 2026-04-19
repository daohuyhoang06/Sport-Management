import { useEffect, useState } from "react";
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

  useEffect(() => {
    let active = true;

    async function loadFields() {
      try {
        setLoading(true);
        setError("");

        const [fieldsResponse, statsResponse] = await Promise.all([
          adminFetch("/api/admin/fields?page=1&limit=200"),
          adminFetch("/api/admin/fields/stats"),
        ]);

        if (!active) {
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
        if (!active) {
          return;
        }

        setError(fetchError.message || "Unable to load fields");
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadFields();

    return () => {
      active = false;
    };
  }, []);

  return {
    fields,
    stats,
    loading,
    error,
  };
}
