import { GoogleGenerativeAI } from '@google/generative-ai';

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY || '');

/**
 * Get field recommendations based on user preferences
 */
export const getFieldRecommendations = async (preferences) => {
  try {
    const { location, budget, time, playerCount } = preferences;
    
    const model = genAI.getGenerativeModel({ model: 'gemini-2.5-flash' });
    
    const prompt = `Bạn là chuyên gia tư vấn sân bóng đá. Hãy phân tích và đưa ra gợi ý dựa trên thông tin sau:
    
    - Vị trí mong muốn: ${location || 'Không xác định'}
    - Ngân sách: ${budget || 'Linh hoạt'}
    - Thời gian chơi: ${time || 'Chưa xác định'}
    - Số người chơi: ${playerCount || 'Chưa biết'}
    
    Hãy đưa ra 3-5 gợi ý cụ thể về:
    1. Loại sân phù hợp (5 người, 7 người, 11 người)
    2. Khung giờ nên đặt để tối ưu giá
    3. Các tiện ích nên ưu tiên
    4. Lưu ý khi đặt sân
    
    Trả lời ngắn gọn, dễ hiểu, tối đa 200 từ.`;
    
    const result = await model.generateContent(prompt);
    const response = await result.response;
    const text = response.text();
    
    return {
      success: true,
      recommendation: text
    };
  } catch (error) {
    console.error('Gemini API Error:', error);
    return {
      success: false,
      message: 'Không thể tạo gợi ý lúc này',
      error: error.message
    };
  }
};

/**
 * Analyze booking pattern for fraud detection
 */
export const detectBookingFraud = async (bookingData) => {
  try {
    const { userId, bookingHistory, currentBooking } = bookingData;
    
    const model = genAI.getGenerativeModel({ model: 'gemini-2.5-flash' });
    
    const prompt = `Phân tích hành vi đặt sân để phát hiện gian lận:
    
    Lịch sử đặt sân (${bookingHistory.length} lần):
    ${bookingHistory.map((b, i) => `${i + 1}. Sân: ${b.field_name}, Giá: ${b.price}đ, Trạng thái: ${b.status}, Ngày: ${b.date}`).join('\n')}
    
    Booking hiện tại:
    - Sân: ${currentBooking.field_name}
    - Giá: ${currentBooking.price}đ
    - Thời gian: ${currentBooking.time}
    
    Đánh giá các dấu hiệu bất thường:
    1. Đặt quá nhiều sân cùng lúc
    2. Hủy liên tục
    3. Pattern đặt giờ cao điểm rồi hủy
    4. Thay đổi bất thường về giá trị booking
    
    Trả về JSON format:
    {
      "riskLevel": "low|medium|high",
      "score": 0-100,
      "reasons": ["lý do 1", "lý do 2"],
      "recommendation": "Cho phép/Cần xem xét/Từ chối"
    }`;
    
    const result = await model.generateContent(prompt);
    const response = await result.response;
    let text = response.text();
    
    // Extract JSON from response
    const jsonMatch = text.match(/\{[\s\S]*\}/);
    if (jsonMatch) {
      const analysis = JSON.parse(jsonMatch[0]);
      return {
        success: true,
        ...analysis
      };
    }
    
    // Fallback if JSON parsing fails
    return {
      success: true,
      riskLevel: 'low',
      score: 10,
      reasons: ['Không phát hiện dấu hiệu bất thường'],
      recommendation: 'Cho phép'
    };
  } catch (error) {
    console.error('Fraud Detection Error:', error);
    return {
      success: false,
      riskLevel: 'low',
      score: 0,
      message: 'Không thể phân tích lúc này'
    };
  }
};

/**
 * Suggest best time slots based on data
 */
export const suggestBestTimeSlots = async (fieldData) => {
  try {
    const { fieldId, fieldName, priceData, bookingStats } = fieldData;
    
    const model = genAI.getGenerativeModel({ model: 'gemini-2.5-flash' });
    
    const prompt = `Phân tích dữ liệu sân bóng và đưa ra gợi ý khung giờ tốt nhất:
    
    Sân: ${fieldName}
    
    Thống kê đặt sân:
    - Tổng số booking: ${bookingStats.total || 0}
    - Tỷ lệ lấp đầy: ${bookingStats.occupancyRate || 0}%
    - Khung giờ đông nhất: ${bookingStats.peakHours || 'N/A'}
    
    Bảng giá theo khung giờ:
    ${priceData && priceData.length > 0 ? priceData.map(p => `- ${p.timeSlot}: ${p.price}đ (${p.availability})`).join('\n') : 'Chưa có dữ liệu'}
    
    Hãy đưa ra:
    1. Top 3 khung giờ tốt nhất (cân bằng giá và chất lượng)
    2. Khung giờ tiết kiệm nhất
    3. Khung giờ tốt nhất cho chất lượng sân
    4. Lời khuyên cho từng mục đích (luyện tập, thi đấu, giải trí)
    
    Trả lời ngắn gọn, dễ hiểu.`;
    
    const result = await model.generateContent(prompt);
    const response = await result.response;
    const text = response.text();
    
    return {
      success: true,
      suggestions: text
    };
  } catch (error) {
    console.error('Time Slot Suggestion Error:', error);
    return {
      success: false,
      message: 'Không thể tạo gợi ý lúc này'
    };
  }
};

/**
 * General AI chatbot
 */
export const chatWithAI = async (userMessage, conversationHistory = []) => {
  try {
    const model = genAI.getGenerativeModel({ model: 'gemini-2.5-flash' });
    
    const systemPrompt = `Bạn là trợ lý AI thông minh của hệ thống đặt sân bóng đá. 
    Nhiệm vụ: Tư vấn, hỗ trợ khách hàng về:
    - Tìm và gợi ý sân phù hợp
    - Giải đáp thắc mắc về giá, chính sách
    - Hướng dẫn đặt sân
    - Gợi ý thời gian và loại sân
    
    Luôn thân thiện, chuyên nghiệp, trả lời ngắn gọn (< 150 từ).`;
    
    const fullPrompt = conversationHistory.length > 0
      ? `${systemPrompt}\n\nLịch sử hội thoại:\n${conversationHistory.map(h => `${h.role}: ${h.message}`).join('\n')}\n\nUser: ${userMessage}\nAI:`
      : `${systemPrompt}\n\nUser: ${userMessage}\nAI:`;
    
    const result = await model.generateContent(fullPrompt);
    const response = await result.response;
    const text = response.text();
    
    return {
      success: true,
      message: text
    };
  } catch (error) {
    console.error('AI Chat Error:', error);
    return {
      success: false,
      message: 'Xin lỗi, tôi không thể trả lời lúc này. Vui lòng thử lại sau.'
    };
  }
};
