import { Router } from 'express';
import {
  register,
  login,
  getMe,
  refreshToken,
  logout
} from '../../controllers/user/authController.js';
import {
  forgotPassword,
  verifyOTP,
  resetPassword,
  resendOTP
} from '../../controllers/user/passwordResetController.js';
import { protect } from '../../middleware/auth.js';

const router = Router();

// Public routes
router.post('/register', register);
router.post('/login', login);
router.post('/refresh', refreshToken);

// Password reset routes (public)
router.post('/forgot-password', forgotPassword);
router.post('/verify-otp', verifyOTP);
router.post('/reset-password', resetPassword);
router.post('/resend-otp', resendOTP);

// Protected routes
router.get('/me', protect, getMe);
router.post('/logout', protect, logout);

export default router;