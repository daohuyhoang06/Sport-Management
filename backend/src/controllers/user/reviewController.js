import sequelize from "../../config/database.js";
import path from "path";
import { fileURLToPath } from "url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// POST /api/user/reviews/upload - Upload images only
export const uploadImages = async (req, res) => {
  try {
    if (!req.files || req.files.length === 0) {
      return res.status(400).json({ message: "No files uploaded" });
    }

    // Return URLs of uploaded files
    const imageUrls = req.files.map(
      (file) => `/uploads/reviews/${file.filename}`,
    );

    res.json({
      message: "Images uploaded successfully",
      images: imageUrls,
    });
  } catch (err) {
    console.error("uploadImages error:", err);
    res.status(500).json({
      message: "Server error when uploading images",
      error: err.message,
    });
  }
};

// GET /api/user/reviews?field_id=1
export const getReviews = async (req, res) => {
  try {
    const { field_id } = req.query;

    if (!field_id) {
      return res.status(400).json({ message: "field_id is required" });
    }

    // Join with person table to get customer name
    const [reviews] = await sequelize.query(
      `SELECT 
        r.review_id,
        r.field_id,
        r.customer_id,
        r.rating,
        r.comment,
        r.images,
        r.created_at,
        p.name as customer_name
      FROM reviews r
      LEFT JOIN person p ON r.customer_id = p.person_id
      WHERE r.field_id = ?
      ORDER BY r.created_at DESC`,
      { replacements: [field_id] },
    );

    // MySQL JSON column is already parsed, just ensure it's an array
    const reviewsWithParsedImages = reviews.map((review) => ({
      ...review,
      images: Array.isArray(review.images) ? review.images : [],
    }));

    res.json(reviewsWithParsedImages);
  } catch (err) {
    console.error("getReviews error:", err);
    res.status(500).json({
      message: "Server error when fetching reviews",
      error: err.message,
    });
  }
};

// POST /api/user/reviews
export const createReview = async (req, res) => {
  try {
    const userId = req.user?.id;
    const { field_id, booking_id, rating, comment, images } = req.body;
    const normalizedRating = Number.parseInt(rating, 10);
    const normalizedComment = String(comment || "").trim();

    if (!userId) {
      return res.status(401).json({ message: "Unauthorized" });
    }

    // Validate
    if (!booking_id || !normalizedRating || !normalizedComment) {
      return res.status(400).json({
        message:
          "Missing required fields: booking_id, rating, comment",
      });
    }

    if (normalizedRating < 1 || normalizedRating > 5) {
      return res
        .status(400)
        .json({ message: "Rating must be between 1 and 5" });
    }

    const [bookingRows] = await sequelize.query(
      `SELECT booking_id, field_id, customer_id, status, end_time
       FROM bookings
       WHERE booking_id = ?
       LIMIT 1`,
      { replacements: [booking_id] },
    );
    const booking = bookingRows?.[0];

    if (!booking) {
      return res.status(404).json({ message: "Booking not found" });
    }

    if (Number(booking.customer_id) !== Number(userId)) {
      return res.status(403).json({ message: "You cannot review this booking" });
    }

    if (field_id && Number(field_id) !== Number(booking.field_id)) {
      return res.status(400).json({ message: "field_id does not match booking" });
    }

    if (!["confirmed", "approved", "completed"].includes(String(booking.status || "").toLowerCase())) {
      return res.status(400).json({ message: "Booking is not eligible for review" });
    }

    const bookingEndTime = new Date(booking.end_time).getTime();
    if (!Number.isFinite(bookingEndTime) || bookingEndTime >= Date.now()) {
      return res.status(400).json({ message: "You can only review after the booking has ended" });
    }

    const [existingReviewRows] = await sequelize.query(
      `SELECT review_id
       FROM reviews
       WHERE booking_id = ? AND customer_id = ?
       LIMIT 1`,
      { replacements: [booking_id, userId] },
    );

    if (existingReviewRows?.[0]) {
      return res.status(409).json({ message: "You have already reviewed this booking" });
    }

    // Insert review
    const imagesJson =
      images && images.length > 0 ? JSON.stringify(images) : null;

    await sequelize.query(
      `INSERT INTO reviews (field_id, customer_id, booking_id, rating, comment, images)
       VALUES (?, ?, ?, ?, ?, ?)`,
      {
        replacements: [
          booking.field_id,
          userId,
          booking_id,
          normalizedRating,
          normalizedComment,
          imagesJson,
        ],
      },
    );

    const [[{ review_id: reviewId }]] = await sequelize.query(
      `SELECT LAST_INSERT_ID() as review_id`,
    );

    // Fetch inserted review
    const [rows] = await sequelize.query(
      `SELECT 
        r.review_id,
        r.field_id,
        r.customer_id,
        r.booking_id,
        r.rating,
        r.comment,
        r.images,
        r.created_at,
        p.name as customer_name
      FROM reviews r
      LEFT JOIN person p ON r.customer_id = p.person_id
      WHERE r.review_id = ?
      LIMIT 1`,
      { replacements: [reviewId] },
    );

    const review = rows?.[0] ?? null;

    res.status(201).json({
      message: "Review created successfully",
      review: review
        ? {
            ...review,
            images: Array.isArray(review.images) ? review.images : [],
          }
        : null,
    });
  } catch (err) {
    console.error("createReview error:", err);
    res.status(500).json({
      message: "Server error when creating review",
      error: err.message,
      sqlError: err.original?.sqlMessage || err.original?.message,
    });
  }
};

// GET /api/user/reviews/stats/:field_id - Get review statistics for a field
export const getReviewStats = async (req, res) => {
  try {
    const { field_id } = req.params;

    const [stats] = await sequelize.query(
      `SELECT 
        COUNT(*) as total_reviews,
        AVG(rating) as average_rating,
        SUM(CASE WHEN rating = 5 THEN 1 ELSE 0 END) as five_star,
        SUM(CASE WHEN rating = 4 THEN 1 ELSE 0 END) as four_star,
        SUM(CASE WHEN rating = 3 THEN 1 ELSE 0 END) as three_star,
        SUM(CASE WHEN rating = 2 THEN 1 ELSE 0 END) as two_star,
        SUM(CASE WHEN rating = 1 THEN 1 ELSE 0 END) as one_star
      FROM reviews
      WHERE field_id = ?`,
      { replacements: [field_id] },
    );

    res.json(
      stats[0] || {
        total_reviews: 0,
        average_rating: 0,
        five_star: 0,
        four_star: 0,
        three_star: 0,
        two_star: 0,
        one_star: 0,
      },
    );
  } catch (err) {
    console.error("getReviewStats error:", err);
    res.status(500).json({
      message: "Server error when fetching review stats",
      error: err.message,
    });
  }
};
