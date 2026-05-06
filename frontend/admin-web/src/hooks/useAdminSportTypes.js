import { useCallback, useEffect, useState } from "react";
import { adminFetch } from "../services/adminApi";

function normalize(list = []) {
  return list.map((item) => ({
    id: item.sport_id,
    name: item.sport_name || "",
  }));
}

export default function useAdminSportTypes() {
  const [types, setTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const resp = await adminFetch("/api/admin/sport-types");
      const payload = resp?.data ?? resp ?? [];

      setTypes(normalize(Array.isArray(payload) ? payload : payload || []));
    } catch (err) {
      setError(err.message || "Không thể tải loại sân");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const reload = useCallback(() => load(), [load]);

  const createType = useCallback(
    async (payload) => {
      await adminFetch("/api/admin/sport-types", {
        method: "POST",
        body: JSON.stringify(payload),
      });
      await reload();
    },
    [reload],
  );

  const updateType = useCallback(
    async (id, payload) => {
      await adminFetch(`/api/admin/sport-types/${id}`, {
        method: "PUT",
        body: JSON.stringify(payload),
      });
      await reload();
    },
    [reload],
  );

  const deleteType = useCallback(
    async (id) => {
      await adminFetch(`/api/admin/sport-types/${id}`, {
        method: "DELETE",
      });
      await reload();
    },
    [reload],
  );

  return {
    types,
    loading,
    error,
    reload,
    createType,
    updateType,
    deleteType,
  };
}
