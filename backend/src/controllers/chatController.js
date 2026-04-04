import {
  getOrCreateChatService,
  getUserChatsService,
  getChatMessagesService,
  sendMessageService,
  getAvailableManagersService
} from '../services/chatService.js';

/**
 * Get or create chat
 * POST /api/chat/create
 */
export const createChat = async (req, res) => {
  try {
    const userId = req.user.id;
    const { managerId } = req.body;

    if (!managerId) {
      return res.status(400).json({
        success: false,
        message: 'Manager ID là bắt buộc'
      });
    }

    const chat = await getOrCreateChatService(userId, managerId);

    res.json({
      success: true,
      data: chat
    });
  } catch (error) {
    console.error('Error creating chat:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Lỗi server khi tạo chat'
    });
  }
};

/**
 * Get user's chats
 * GET /api/chat/list
 */
export const getUserChats = async (req, res) => {
  try {
    const personId = req.user.id;
    const role = req.user.role;

    const chats = await getUserChatsService(personId, role);

    res.json({
      success: true,
      data: chats
    });
  } catch (error) {
    console.error('Error getting chats:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Lỗi server khi lấy danh sách chat'
    });
  }
};

/**
 * Get chat messages
 * GET /api/chat/:chatId/messages
 */
export const getChatMessages = async (req, res) => {
  try {
    const personId = req.user.id;
    const { chatId } = req.params;

    const messages = await getChatMessagesService(chatId, personId);

    res.json({
      success: true,
      data: messages
    });
  } catch (error) {
    console.error('Error getting messages:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Lỗi server khi lấy tin nhắn'
    });
  }
};

/**
 * Send message
 * POST /api/chat/:chatId/send
 */
export const sendMessage = async (req, res) => {
  try {
    const senderId = req.user.id;
    const { chatId } = req.params;
    const { message } = req.body;

    if (!message || !message.trim()) {
      return res.status(400).json({
        success: false,
        message: 'Tin nhắn không được để trống'
      });
    }

    const newMessage = await sendMessageService(chatId, senderId, message.trim());

    res.json({
      success: true,
      data: newMessage
    });
  } catch (error) {
    console.error('Error sending message:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Lỗi server khi gửi tin nhắn'
    });
  }
};

/**
 * Get available managers
 * GET /api/chat/managers
 */
export const getAvailableManagers = async (req, res) => {
  try {
    const managers = await getAvailableManagersService();

    res.json({
      success: true,
      data: managers
    });
  } catch (error) {
    console.error('Error getting managers:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Lỗi server khi lấy danh sách manager'
    });
  }
};
