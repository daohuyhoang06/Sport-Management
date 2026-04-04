import Person from '../../models/Person.js';
import PasswordReset from '../../models/PasswordReset.js';
import { sendOTPEmail, sendPasswordResetSuccessEmail } from '../../services/user/emailService.js';
import { Op } from 'sequelize';

// Generate random 6-digit OTP
const generateOTP = () => {
  return Math.floor(100000 + Math.random() * 900000).toString();
};

// @desc    Request password reset - send OTP to email
// @route   POST /api/auth/forgot-password
// @access  Public
export const forgotPassword = async (req, res) => {
  try {
    const { email } = req.body;

    // Validation
    if (!email) {
      return res.status(400).json({
        success: false,
        message: 'Vui lòng nhập email'
      });
    }

    // Find user by email
    const user = await Person.findOne({ where: { email } });

    if (!user) {
      // Don't reveal if email exists or not for security
      return res.status(200).json({
        success: true,
        message: 'Nếu email tồn tại trong hệ thống, bạn sẽ nhận được mã OTP qua email'
      });
    }

    // Check if user is active
    if (user.status !== 'active') {
      return res.status(403).json({
        success: false,
        message: 'Tài khoản đã bị khóa'
      });
    }

    // Invalidate all previous OTPs for this email
    await PasswordReset.update(
      { is_used: true },
      { where: { email, is_used: false } }
    );

    // Generate new OTP
    const otpCode = generateOTP();
    const expiresAt = new Date(Date.now() + 10 * 60 * 1000); // 10 minutes

    // Save OTP to database
    await PasswordReset.create({
      person_id: user.person_id,
      email: user.email,
      otp_code: otpCode,
      expires_at: expiresAt,
      is_used: false
    });

    // Send OTP via email
    await sendOTPEmail(user.email, otpCode, user.person_name);

    res.status(200).json({
      success: true,
      message: 'Mã OTP đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.',
      data: {
        email: user.email,
        expiresIn: 600 // seconds
      }
    });

  } catch (error) {
    console.error('Forgot password error:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi server khi xử lý yêu cầu',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
};

// @desc    Verify OTP code
// @route   POST /api/auth/verify-otp
// @access  Public
export const verifyOTP = async (req, res) => {
  try {
    const { email, otp } = req.body;

    // Validation
    if (!email || !otp) {
      return res.status(400).json({
        success: false,
        message: 'Vui lòng nhập email và mã OTP'
      });
    }

    // Find valid OTP
    const passwordReset = await PasswordReset.findOne({
      where: {
        email,
        otp_code: otp,
        is_used: false,
        expires_at: {
          [Op.gt]: new Date()
        }
      },
      order: [['created_at', 'DESC']]
    });

    if (!passwordReset) {
      return res.status(400).json({
        success: false,
        message: 'Mã OTP không hợp lệ hoặc đã hết hạn'
      });
    }

    // OTP is valid
    res.status(200).json({
      success: true,
      message: 'Mã OTP hợp lệ',
      data: {
        email: passwordReset.email,
        resetId: passwordReset.id
      }
    });

  } catch (error) {
    console.error('Verify OTP error:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi server khi xác thực OTP',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
};

// @desc    Reset password with OTP
// @route   POST /api/auth/reset-password
// @access  Public
export const resetPassword = async (req, res) => {
  try {
    const { email, otp, newPassword } = req.body;

    // Validation
    if (!email || !otp || !newPassword) {
      return res.status(400).json({
        success: false,
        message: 'Vui lòng nhập đầy đủ thông tin'
      });
    }

    if (newPassword.length < 6) {
      return res.status(400).json({
        success: false,
        message: 'Mật khẩu mới phải có ít nhất 6 ký tự'
      });
    }

    // Find valid OTP
    const passwordReset = await PasswordReset.findOne({
      where: {
        email,
        otp_code: otp,
        is_used: false,
        expires_at: {
          [Op.gt]: new Date()
        }
      },
      order: [['created_at', 'DESC']]
    });

    if (!passwordReset) {
      return res.status(400).json({
        success: false,
        message: 'Mã OTP không hợp lệ hoặc đã hết hạn'
      });
    }

    // Find user
    const user = await Person.findOne({ where: { email } });

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy người dùng'
      });
    }

    // Update password
    user.password = newPassword; // Will be hashed by beforeUpdate hook
    await user.save();

    // Mark OTP as used
    passwordReset.is_used = true;
    await passwordReset.save();

    // Send success notification email
    await sendPasswordResetSuccessEmail(user.email, user.person_name);

    res.status(200).json({
      success: true,
      message: 'Đặt lại mật khẩu thành công. Bạn có thể đăng nhập với mật khẩu mới.'
    });

  } catch (error) {
    console.error('Reset password error:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi server khi đặt lại mật khẩu',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
};

// @desc    Resend OTP
// @route   POST /api/auth/resend-otp
// @access  Public
export const resendOTP = async (req, res) => {
  try {
    const { email } = req.body;

    if (!email) {
      return res.status(400).json({
        success: false,
        message: 'Vui lòng nhập email'
      });
    }

    // Find user
    const user = await Person.findOne({ where: { email } });

    if (!user) {
      return res.status(200).json({
        success: true,
        message: 'Nếu email tồn tại, mã OTP mới đã được gửi'
      });
    }

    // Check rate limit - only allow resend after 1 minute
    const recentOTP = await PasswordReset.findOne({
      where: {
        email,
        created_at: {
          [Op.gt]: new Date(Date.now() - 60 * 1000) // Last 1 minute
        }
      },
      order: [['created_at', 'DESC']]
    });

    if (recentOTP) {
      return res.status(429).json({
        success: false,
        message: 'Vui lòng đợi 1 phút trước khi gửi lại mã OTP'
      });
    }

    // Invalidate all previous OTPs
    await PasswordReset.update(
      { is_used: true },
      { where: { email, is_used: false } }
    );

    // Generate and send new OTP
    const otpCode = generateOTP();
    const expiresAt = new Date(Date.now() + 10 * 60 * 1000);

    await PasswordReset.create({
      person_id: user.person_id,
      email: user.email,
      otp_code: otpCode,
      expires_at: expiresAt,
      is_used: false
    });

    await sendOTPEmail(user.email, otpCode, user.person_name);

    res.status(200).json({
      success: true,
      message: 'Mã OTP mới đã được gửi đến email của bạn',
      data: {
        expiresIn: 600
      }
    });

  } catch (error) {
    console.error('Resend OTP error:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi server khi gửi lại OTP',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
};