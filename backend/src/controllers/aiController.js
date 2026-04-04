import {
  getFieldRecommendations,
  detectBookingFraud,
  suggestBestTimeSlots,
  chatWithAI
} from '../services/geminiService.js';
import { getWeatherForecast } from '../services/weatherService.js';
import sequelize from '../config/database.js';

/**
 * AI Chatbot endpoint
 * POST /api/ai/chat
 */
export const aiChat = async (req, res) => {
  try {
    const { message, conversationHistory } = req.body;

    if (!message || !message.trim()) {
      return res.status(400).json({
        success: false,
        message: 'Tin nhắn không được để trống'
      });
    }

    const response = await chatWithAI(message, conversationHistory || []);

    res.json(response);
  } catch (error) {
    console.error('AI Chat Error:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi xử lý tin nhắn'
    });
  }
};

/**
 * Get field recommendations based on preferences
 * POST /api/ai/recommend-fields
 */
export const recommendFields = async (req, res) => {
  try {
    const { location, budget, time, playerCount } = req.body;

    const preferences = { location, budget, time, playerCount };
    const result = await getFieldRecommendations(preferences);

    res.json(result);
  } catch (error) {
    console.error('Field Recommendation Error:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi tạo gợi ý sân'
    });
  }
};

/**
 * Get weather forecast for booking date
 * GET /api/ai/weather?date=YYYY-MM-DD&location=Hanoi
 */
export const getWeather = async (req, res) => {
  try {
    const { date, location } = req.query;

    if (!date) {
      return res.status(400).json({
        success: false,
        message: 'Ngày đặt sân là bắt buộc'
      });
    }

    const result = await getWeatherForecast(date, location || 'Hanoi,VN');

    res.json(result);
  } catch (error) {
    console.error('Weather Forecast Error:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi lấy dự báo thời tiết'
    });
  }
};

/**
 * Suggest best time slots for a field
 * GET /api/ai/suggest-timeslots/:fieldId
 */
export const suggestTimeSlots = async (req, res) => {
  try {
    const { fieldId } = req.params;

    // Get field data
    const [fieldResult] = await sequelize.query(
      `SELECT field_id, field_name, rental_price FROM fields WHERE field_id = ?`,
      { replacements: [fieldId] }
    );

    if (fieldResult.length === 0) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy sân'
      });
    }

    const field = fieldResult[0];

    // Get booking statistics
    const [statsResult] = await sequelize.query(
      `SELECT 
        COUNT(*) as total,
        SUM(CASE WHEN status = 'confirmed' THEN 1 ELSE 0 END) as confirmed,
        HOUR(start_time) as hour,
        COUNT(*) as count
       FROM bookings 
       WHERE field_id = ? AND start_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
       GROUP BY HOUR(start_time)
       ORDER BY count DESC
       LIMIT 5`,
      { replacements: [fieldId] }
    );

    const totalBookings = statsResult.reduce((sum, row) => sum + Number(row.count), 0);
    const peakHours = statsResult.length > 0 
      ? statsResult.map(r => `${r.hour}h`).join(', ')
      : 'Chưa có dữ liệu';

    // Mock price data for different time slots
    const priceData = [
      { timeSlot: '5h-9h', price: Math.round(field.rental_price * 0.7), availability: 'Còn nhiều' },
      { timeSlot: '9h-16h', price: Math.round(field.rental_price * 0.8), availability: 'Còn trống' },
      { timeSlot: '16h-18h', price: Math.round(field.rental_price * 0.9), availability: 'Khá đông' },
      { timeSlot: '18h-21h', price: field.rental_price, availability: 'Đông' },
      { timeSlot: '21h-23h', price: Math.round(field.rental_price * 0.85), availability: 'Trung bình' }
    ];

    const fieldData = {
      fieldId,
      fieldName: field.field_name,
      priceData,
      bookingStats: {
        total: totalBookings,
        occupancyRate: totalBookings > 0 ? Math.round((totalBookings / 30) * 100 / 12) : 0,
        peakHours
      }
    };

    const result = await suggestBestTimeSlots(fieldData);

    res.json(result);
  } catch (error) {
    console.error('Time Slot Suggestion Error:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi tạo gợi ý khung giờ'
    });
  }
};

/**
 * Detect booking fraud
 * POST /api/ai/detect-fraud
 */
export const detectFraud = async (req, res) => {
  try {
    const userId = req.user?.id;
    const { bookingDetails } = req.body;

    if (!userId) {
      return res.status(401).json({
        success: false,
        message: 'Unauthorized'
      });
    }

    // Get user's booking history
    const [bookingHistory] = await sequelize.query(
      `SELECT 
        b.booking_id,
        f.field_name,
        b.total_price as price,
        b.status,
        DATE(b.start_time) as date,
        b.created_at
       FROM bookings b
       JOIN fields f ON b.field_id = f.field_id
       WHERE b.customer_id = ?
       ORDER BY b.created_at DESC
       LIMIT 20`,
      { replacements: [userId] }
    );

    const bookingData = {
      userId,
      bookingHistory,
      currentBooking: bookingDetails
    };

    const result = await detectBookingFraud(bookingData);

    res.json(result);
  } catch (error) {
    console.error('Fraud Detection Error:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi phân tích booking'
    });
  }
};
