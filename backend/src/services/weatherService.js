import { GoogleGenerativeAI } from '@google/generative-ai';

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY || '');

/**
 * Get weather forecast for booking date using Gemini AI
 */
export const getWeatherForecast = async (date, location = 'Hanoi,VN') => {
  try {
    const targetDate = new Date(date);
    const today = new Date();
    const daysFromNow = Math.ceil((targetDate - today) / (1000 * 60 * 60 * 24));

    // Use Gemini AI to predict weather
    const model = genAI.getGenerativeModel({ model: 'gemini-2.5-flash' });

    const prompt = `Dự đoán thời tiết cho ngày ${targetDate.toLocaleDateString('vi-VN')} tại ${location}.

Hãy phân tích và đưa ra dự báo chi tiết dựa trên:
- Vị trí: ${location}
- Ngày: ${targetDate.toLocaleDateString('vi-VN')} (${daysFromNow} ngày kể từ hôm nay)
- Mùa hiện tại và xu hướng thời tiết thường gặp

Trả về dự báo dưới dạng JSON format chính xác như sau (không có text thừa):
{
  "temperature": <số nhiệt độ trung bình>,
  "condition": "<mô tả thời tiết bằng tiếng Việt>",
  "humidity": <độ ẩm %>,
  "windSpeed": <tốc độ gió km/h>,
  "rainChance": <khả năng mưa %>,
  "recommendation": "<lời khuyên cho việc chơi bóng>"
}

Lưu ý: 
- Nhiệt độ phải phù hợp với thời tiết Việt Nam (15-38°C)
- Đưa ra dự báo thực tế dựa trên kiến thức về khí hậu khu vực
- Recommendation nên bao gồm emoji phù hợp`;

    const result = await model.generateContent(prompt);
    const response = await result.response;
    let text = response.text();

    // Extract JSON from response
    const jsonMatch = text.match(/\{[\s\S]*\}/);
    let weatherData;

    if (jsonMatch) {
      weatherData = JSON.parse(jsonMatch[0]);
    } else {
      // Fallback if JSON parsing fails
      throw new Error('Cannot parse weather data from AI');
    }

    // Add additional info
    weatherData.date = date;
    weatherData.location = location.split(',')[0];

    // Ensure recommendation has proper format
    if (!weatherData.recommendation.includes('⚠️') && !weatherData.recommendation.includes('🌦️') && 
        !weatherData.recommendation.includes('🌡️') && !weatherData.recommendation.includes('🥶') &&
        !weatherData.recommendation.includes('✅')) {
      // Add emoji based on conditions
      if (weatherData.rainChance > 70) {
        weatherData.recommendation = '⚠️ ' + weatherData.recommendation;
      } else if (weatherData.rainChance > 40) {
        weatherData.recommendation = '🌦️ ' + weatherData.recommendation;
      } else if (weatherData.temperature > 35) {
        weatherData.recommendation = '🌡️ ' + weatherData.recommendation;
      } else if (weatherData.temperature < 15) {
        weatherData.recommendation = '🥶 ' + weatherData.recommendation;
      } else {
        weatherData.recommendation = '✅ ' + weatherData.recommendation;
      }
    }

    return {
      success: true,
      data: weatherData
    };
  } catch (error) {
    console.error('Gemini Weather Forecast Error:', error);
    
    // Return mock data as fallback
    const targetDate = new Date(date);
    return {
      success: true,
      data: {
        date,
        location: location.split(',')[0],
        temperature: 25,
        condition: 'Trời nhiều mây',
        humidity: 70,
        windSpeed: 10,
        rainChance: 20,
        recommendation: '✅ Thời tiết tốt để chơi bóng',
        isMock: true,
        note: 'Dữ liệu mẫu (cần API key Gemini để có dự báo thực)'
      }
    };
  }
};

/**
 * Check if weather is suitable for playing
 */
export const isWeatherSuitable = (weatherData) => {
  if (!weatherData) return true;

  const { rainChance, temperature, windSpeed } = weatherData;

  // Not suitable if high rain chance
  if (rainChance > 70) return false;

  // Not suitable if too hot or too cold
  if (temperature > 38 || temperature < 10) return false;

  // Not suitable if too windy
  if (windSpeed > 40) return false;

  return true;
};
