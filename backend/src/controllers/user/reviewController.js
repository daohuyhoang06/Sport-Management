import sequelize from '../../config/database.js';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// POST /api/user/reviews/upload - Upload images only
export const uploadImages = async (req, res) => {
  try {
    if (!req.files || req.files.length === 0) {
      return res.status(400).json({ message: 'No files uploaded' });
    }

    // Return URLs of uploaded files
    const imageUrls = req.files.map(file => `/uploads/reviews/${file.filename}`);
    
    res.json({ 
      message: 'Images uploaded successfully',
      images: imageUrls 
    });
  } catch (err) {
    console.error('uploadImages error:', err);
    res.status(500).json({ 
      message: 'Server error when uploading images',
      error: err.message 
    });
  }
};

// GET /api/user/reviews?field_id=1
export const getReviews = async (req, res) => {
  try {
    const { field_id } = req.query;
    
    if (!field_id) {
      return res.status(400).json({ message: 'field_id is required' });
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
        p.person_name as customer_name
      FROM reviews r
      LEFT JOIN person p ON r.customer_id = p.person_id
      WHERE r.field_id = ?
      ORDER BY r.created_at DESC`,
      { replacements: [field_id] }
    );

    // MySQL JSON column is already parsed, just ensure it's an array
    const reviewsWithParsedImages = reviews.map(review => ({
      ...review,
      images: Array.isArray(review.images) ? review.images : []
    }));

    res.json(reviewsWithParsedImages);
  } catch (err) {
    console.error('getReviews error:', err);
    res.status(500).json({ 
      message: 'Server error when fetching reviews',
      error: err.message 
    });
  }
};

// POST /api/user/reviews
export const createReview = async (req, res) => {
  try {
    const { field_id, customer_id, rating, comment, images } = req.body;

    // Validate
    if (!field_id || !customer_id || !rating || !comment) {
      return res.status(400).json({ 
        message: 'Missing required fields: field_id, customer_id, rating, comment' 
      });
    }

    if (rating < 1 || rating > 5) {
      return res.status(400).json({ message: 'Rating must be between 1 and 5' });
    }

    // Check if field exists
    const [fieldCheck] = await sequelize.query(
      'SELECT field_id FROM fields WHERE field_id = ? LIMIT 1',
      { replacements: [field_id] }
    );
    
    if (!fieldCheck || fieldCheck.length === 0) {
      return res.status(400).json({ message: 'Field not found' });
    }

    // Check if customer exists
    const [customerCheck] = await sequelize.query(
      'SELECT person_id FROM person WHERE person_id = ? LIMIT 1',
      { replacements: [customer_id] }
    );
    
    if (!customerCheck || customerCheck.length === 0) {
      return res.status(400).json({ message: 'Customer not found' });
    }

    // Insert review
    const imagesJson = images && images.length > 0 ? JSON.stringify(images) : null;
    
    const [insertResult] = await sequelize.query(
      `INSERT INTO reviews (field_id, customer_id, rating, comment, images) 
       VALUES (?, ?, ?, ?, ?)
       RETURNING review_id`,
      { replacements: [field_id, customer_id, rating, comment, imagesJson] }
    );

    const reviewId = insertResult[0]?.review_id;

    // Fetch inserted review
    const [rows] = await sequelize.query(
      `SELECT 
        r.review_id,
        r.field_id,
        r.customer_id,
        r.rating,
        r.comment,
        r.images,
        r.created_at,
        p.person_name as customer_name
      FROM reviews r
      LEFT JOIN person p ON r.customer_id = p.person_id
      WHERE r.review_id = ?
      LIMIT 1`,
      { replacements: [reviewId] }
    );

    const review = rows?.[0] ?? null;

    res.status(201).json({ 
      message: 'Review created successfully', 
      review: review ? {
        ...review,
        images: Array.isArray(review.images) ? review.images : []
      } : null
    });
  } catch (err) {
    console.error('createReview error:', err);
    res.status(500).json({ 
      message: 'Server error when creating review',
      error: err.message,
      sqlError: err.original?.sqlMessage || err.original?.message
    });
  }
};

// GET /api/user/reviews/stats/:fieldId - Get review statistics for a field
export const getReviewStats = async (req, res) => {
  try {
    const { fieldId } = req.params;

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
      { replacements: [fieldId] }
    );

    res.json(stats[0] || {
      total_reviews: 0,
      average_rating: 0,
      five_star: 0,
      four_star: 0,
      three_star: 0,
      two_star: 0,
      one_star: 0
    });
  } catch (err) {
    console.error('getReviewStats error:', err);
    res.status(500).json({ 
      message: 'Server error when fetching review stats',
      error: err.message 
    });
  }
};