import { useCallback, useEffect, useState } from "react";
import { adminFetch } from "../services/adminApi";

function formatDate(value) {
  if (!value) {
    return "-";
  }

  const date = new Date(value);
  return new Intl.DateTimeFormat("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  }).format(date);
}

function formatTime(value) {
  if (!value) {
    return "--:--";
  }

  const date = new Date(value);
  return new Intl.DateTimeFormat("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  }).format(date);
}

function normalizeBookings(rawBookings = []) {
  return rawBookings.map((item) => ({
    id: item.booking_id,
    customer: item.customer_name || "-",
    field: item.field_name || "-",
    slot: `${formatTime(item.start_time)} - ${formatTime(item.end_time)}`,
    date: formatDate(item.start_time),
    status: item.status || "pending",
  }));
}

export default function useAdminBookings() {
  const [bookings, setBookings] = useState([]);
  const [stats, setStats] = useState({
    total: 0,
    pending: 0,
    confirmed: 0,
    completed: 0,
    cancelled: 0,
    today: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadBookings = useCallback(async (signal) => {
    try {
      setLoading(true);
      setError("");

      const [bookingsResponse, statsResponse] = await Promise.all([
        adminFetch("/api/admin/bookings?page=1&limit=200"),
        adminFetch("/api/admin/bookings/stats"),
      ]);

      if (signal.cancelled) {
        return;
      }

      const bookingsData = bookingsResponse?.data?.bookings ?? [];
      const statsData = statsResponse?.data ?? {};

      setBookings(normalizeBookings(bookingsData));
      setStats({
        total: Number(statsData.total ?? bookingsData.length ?? 0),
        pending: Number(statsData.pending ?? 0),
        confirmed: Number(statsData.confirmed ?? 0),
        completed: Number(statsData.completed ?? 0),
        cancelled: Number(statsData.cancelled ?? 0),
        today: Number(statsData.today ?? 0),
      });
    } catch (fetchError) {
      if (signal.cancelled) {
        return;
      }

      setError(fetchError.message || "Unable to load bookings");
    } finally {
      if (!signal.cancelled) {
        setLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    const signal = { cancelled: false };

    loadBookings(signal);

    return () => {
      signal.cancelled = true;
    };
  }, [loadBookings]);

  const reload = useCallback(
    () => loadBookings({ cancelled: false }),
    [loadBookings],
  );

  const updateBookingStatus = useCallback(
    async (bookingId, status, note = "") => {
      await adminFetch(`/api/admin/bookings/${bookingId}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status, note }),
      });
      await reload();
    },
    [reload],
  );

  const cancelBooking = useCallback(
    async (bookingId, reason) => {
      await adminFetch(`/api/admin/bookings/${bookingId}/cancel`, {
        method: "POST",
        body: JSON.stringify({ reason }),
      });
      await reload();
    },
    [reload],
  );

  return {
    bookings,
    stats,
    loading,
    error,
    reload,
    updateBookingStatus,
    cancelBooking,
  };
}
