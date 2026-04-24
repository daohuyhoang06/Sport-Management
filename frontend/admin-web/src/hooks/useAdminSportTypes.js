import { useCallback, useEffect, useState } from "react";
import { adminFetch } from "../services/adminApi";

function normalizeSportTypes(rawSportTypes = []) {
  return rawSportTypes
    .map((item) => ({
      sport_id: item.sport_id,
      sport_name: item.sport_name || "",
    }))
    .sort((left, right) => Number(left.sport_id) - Number(right.sport_id));
}

export default function useAdminSportTypes() {
  const [sportTypes, setSportTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadSportTypes = useCallback(async (signal) => {
    try {
      setLoading(true);
      setError("");

      const response = await adminFetch("/api/admin/sport-types");
      if (signal.cancelled) {
        return;
      }

      const sportTypesData = response?.data ?? [];
      setSportTypes(normalizeSportTypes(sportTypesData));
    } catch (fetchError) {
      if (signal.cancelled) {
        return;
      }
      setError(fetchError.message || "Unable to load sport types");
    } finally {
      if (!signal.cancelled) {
        setLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    const signal = { cancelled: false };
    loadSportTypes(signal);

    return () => {
      signal.cancelled = true;
    };
  }, [loadSportTypes]);

  const reload = useCallback(
    () => loadSportTypes({ cancelled: false }),
    [loadSportTypes],
  );

  const createSportType = useCallback(
    async (sport_name) => {
      await adminFetch("/api/admin/sport-types", {
        method: "POST",
        body: JSON.stringify({ sport_name }),
      });
      await reload();
    },
    [reload],
  );

  const updateSportType = useCallback(
    async (sport_id, sport_name) => {
      await adminFetch(`/api/admin/sport-types/${sport_id}`, {
        method: "PUT",
        body: JSON.stringify({ sport_name }),
      });
      await reload();
    },
    [reload],
  );

  const deleteSportType = useCallback(
    async (sport_id) => {
      await adminFetch(`/api/admin/sport-types/${sport_id}`, {
        method: "DELETE",
      });
      await reload();
    },
    [reload],
  );

  return {
    sportTypes,
    loading,
    error,
    reload,
    createSportType,
    updateSportType,
    deleteSportType,
  };
}
