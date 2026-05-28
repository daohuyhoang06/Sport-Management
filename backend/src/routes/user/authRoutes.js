import { Router } from 'express';
import {
  register,
  login,
  socialLogin,
  getMe,
  getSportTypes,
  updateMe,
  updateMyAvatar,
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
import {
  uploadAvatarImage,
  handleUploadErrors
} from '../../middleware/upload.js';

const router = Router();

// Public routes
router.post('/register', register);
router.post('/login', login);
router.post('/social-login', socialLogin);
router.post('/refresh', refreshToken);

// Password reset routes (public)
router.post('/forgot-password', forgotPassword);
router.post('/verify-otp', verifyOTP);
router.post('/reset-password', resetPassword);
router.post('/resend-otp', resendOTP);

// Protected routes
router.get('/me', protect, getMe);
router.get('/sport-types', protect, getSportTypes);
router.put('/me', protect, updateMe);
router.post('/me/avatar', protect, uploadAvatarImage, handleUploadErrors, updateMyAvatar);
router.post('/logout', protect, logout);

export default router;
