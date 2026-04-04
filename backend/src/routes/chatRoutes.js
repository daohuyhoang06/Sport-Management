import { Router } from 'express';
import {
  createChat,
  getUserChats,
  getChatMessages,
  sendMessage,
  getAvailableManagers
} from '../controllers/chatController.js';
import { requireAuth } from '../middleware/authMiddleware.js';

const router = Router();

// All routes require authentication
router.use(requireAuth);

// Get available managers (for customers)
router.get('/managers', getAvailableManagers);

// Get user's chats
router.get('/list', getUserChats);

// Create or get chat
router.post('/create', createChat);

// Get messages for a chat
router.get('/:chatId/messages', getChatMessages);

// Send message
router.post('/:chatId/send', sendMessage);

export default router;
