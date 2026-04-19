import { useEffect, useState } from "react";
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

  useEffect(() => {
    let active = true;

    async function loadBookings() {
      try {
        setLoading(true);
        setError("");

        const [bookingsResponse, statsResponse] = await Promise.all([
          adminFetch("/api/admin/bookings?page=1&limit=200"),
          adminFetch("/api/admin/bookings/stats"),
        ]);

        if (!active) {
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
        if (!active) {
          return;
        }

        setError(fetchError.message || "Unable to load bookings");
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadBookings();

    return () => {
      active = false;
    };
  }, []);

  return {
    bookings,
    stats,
    loading,
    error,
  };
}
