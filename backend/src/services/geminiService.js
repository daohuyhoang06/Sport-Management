import { GoogleGenerativeAI } from "@google/generative-ai";

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY || "");

/**
 * Get field recommendations based on user preferences
 */
export const getFieldRecommendations = async (preferences) => {
  try {
    const { location, budget, time, playerCount } = preferences;
    const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" });

    const prompt = `Ban la chuyen gia tu van dat san the thao da mon.

Thong tin nguoi dung:
- Vi tri mong muon: ${location || "Khong xac dinh"}
- Ngan sach: ${budget || "Linh hoat"}
- Thoi gian choi: ${time || "Chua xac dinh"}
- So nguoi choi: ${playerCount || "Chua biet"}

Hay dua ra 3-5 goi y cu the ve:
1. Mon/san phu hop (bong da, cau long, tennis, bong ro, bong chuyen, pickleball...)
2. Khung gio nen dat de toi uu chi phi
3. Tien ich nen uu tien theo tung mon
4. Luu y khi dat san

Tra loi ngan gon, de hieu, toi da 200 tu.`;

    const result = await model.generateContent(prompt);
    const response = await result.response;
    const text = response.text();

    return {
      success: true,
      recommendation: text,
    };
  } catch (error) {
    console.error("Gemini API Error:", error);
    return {
      success: false,
      message: "Khong the tao goi y luc nay",
      error: error.message,
    };
  }
};

/**
 * Analyze booking pattern for fraud detection
 */
export const detectBookingFraud = async (bookingData) => {
  try {
    const { bookingHistory, currentBooking } = bookingData;
    const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" });

    const prompt = `Phan tich hanh vi dat san de phat hien gian lan:

Lich su dat san (${bookingHistory.length} lan):
${bookingHistory.map((b, i) => `${i + 1}. San: ${b.field_name}, Gia: ${b.price}d, Trang thai: ${b.status}, Ngay: ${b.date}`).join("\n")}

Booking hien tai:
- San: ${currentBooking.field_name}
- Gia: ${currentBooking.price}d
- Thoi gian: ${currentBooking.time}

Danh gia cac dau hieu bat thuong:
1. Dat qua nhieu san cung luc
2. Huy lien tuc
3. Pattern dat gio cao diem roi huy
4. Thay doi bat thuong ve gia tri booking

Tra ve JSON format:
{
  "riskLevel": "low|medium|high",
  "score": 0-100,
  "reasons": ["ly do 1", "ly do 2"],
  "recommendation": "Cho phep/Can xem xet/Tu choi"
}`;

    const result = await model.generateContent(prompt);
    const response = await result.response;
    const text = response.text();

    const jsonMatch = text.match(/\{[\s\S]*\}/);
    if (jsonMatch) {
      const analysis = JSON.parse(jsonMatch[0]);
      return {
        success: true,
        ...analysis,
      };
    }

    return {
      success: true,
      riskLevel: "low",
      score: 10,
      reasons: ["Khong phat hien dau hieu bat thuong"],
      recommendation: "Cho phep",
    };
  } catch (error) {
    console.error("Fraud Detection Error:", error);
    return {
      success: false,
      riskLevel: "low",
      score: 0,
      message: "Khong the phan tich luc nay",
    };
  }
};

/**
 * Suggest best time slots based on data
 */
export const suggestBestTimeSlots = async (fieldData) => {
  try {
    const { fieldName, priceData, bookingStats } = fieldData;
    const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" });

    const prompt = `Phan tich du lieu dat san the thao va dua ra goi y khung gio tot nhat:

San: ${fieldName}

Thong ke dat san:
- Tong so booking: ${bookingStats.total || 0}
- Ty le lap day: ${bookingStats.occupancyRate || 0}%
- Khung gio dong nhat: ${bookingStats.peakHours || "N/A"}

Bang gia theo khung gio:
${priceData && priceData.length > 0 ? priceData.map((p) => `- ${p.timeSlot}: ${p.price}d (${p.availability})`).join("\n") : "Chua co du lieu"}

Hay dua ra:
1. Top 3 khung gio tot nhat (can bang gia va chat luong)
2. Khung gio tiet kiem nhat
3. Khung gio tot nhat cho chat luong san
4. Loi khuyen theo muc dich (luyen tap, thi dau, giai tri)

Tra loi ngan gon, de hieu.`;

    const result = await model.generateContent(prompt);
    const response = await result.response;
    const text = response.text();

    return {
      success: true,
      suggestions: text,
    };
  } catch (error) {
    console.error("Time Slot Suggestion Error:", error);
    return {
      success: false,
      message: "Khong the tao goi y luc nay",
    };
  }
};

/**
 * General AI chatbot
 */
export const chatWithAI = async (userMessage, conversationHistory = []) => {
  try {
    const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" });

    const systemPrompt = `Ban la tro ly AI cua he thong dat san the thao da mon.
Nhiem vu:
- Tu van tim va chon san cho nhieu mon: bong da, cau long, tennis, bong ro, bong chuyen, pickleball va cac mon the thao khac trong he thong.
- Giai dap thac mac ve gia, khung gio, chinh sach, cach dat/huy/doi lich.
- Dua ra goi y dua tren nhu cau nguoi dung (so nguoi, ngan sach, thoi gian, muc dich choi).

Huong dan tra loi:
- Neu nguoi dung hoi ve mon the thao khac bong da nhung van lien quan den dat san the thao, van tra loi binh thuong.
- Neu cau hoi khong lien quan den dat san the thao, lich su dung san, hoac ho tro khach hang trong he thong, phan hoi ngan gon va huong nguoi dung ve noi dung the thao.
- Tra loi than thien, ro rang, ngan gon (toi da 180 tu).
- Uu tien ngon ngu giong nguoi dung dang su dung.`;

    const fullPrompt =
      conversationHistory.length > 0
        ? `${systemPrompt}\n\nLich su hoi thoai:\n${conversationHistory.map((h) => `${h.role}: ${h.message}`).join("\n")}\n\nUser: ${userMessage}\nAI:`
        : `${systemPrompt}\n\nUser: ${userMessage}\nAI:`;

    const result = await model.generateContent(fullPrompt);
    const response = await result.response;
    const text = response.text();

    return {
      success: true,
      message: text,
    };
  } catch (error) {
    console.error("AI Chat Error:", error);
    return {
      success: false,
      message: "Xin loi, toi khong the tra loi luc nay. Vui long thu lai sau.",
    };
  }
};
