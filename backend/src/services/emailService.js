import nodemailer from 'nodemailer';

// Create reusable transporter
let transporter = null;

const createTransporter = () => {
  if (transporter) return transporter;

  // For development, use ethereal email (fake SMTP)
  // For production, use real SMTP credentials
  if (process.env.NODE_ENV === 'production' && process.env.EMAIL_HOST) {
    transporter = nodemailer.createTransport({
      host: process.env.EMAIL_HOST,
      port: process.env.EMAIL_PORT || 587,
      secure: false,
      auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASSWORD
      }
    });
  } else {
    // Development mode - sử dụng Gmail hoặc console log
    console.log('📧 Email service running in DEV mode (emails will be logged to console)');
    transporter = nodemailer.createTransport({
      host: 'smtp.gmail.com',
      port: 587,
      secure: false,
      auth: {
        user: process.env.EMAIL_USER || 'demo@example.com',
        pass: process.env.EMAIL_PASSWORD || 'demo_password'
      },
      // Tạm thời ignore SSL errors cho dev
      tls: {
        rejectUnauthorized: false
      }
    });
  }

  return transporter;
};

// Send OTP email
export const sendOTPEmail = async (email, otpCode, userName) => {
  try {
    const transport = createTransporter();

    // Email template
    const mailOptions = {
      from: process.env.EMAIL_FROM || 'Sport Booking <noreply@sportbooking.com>',
      to: email,
      subject: 'Mã xác nhận đặt lại mật khẩu',
      html: `
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <style>
            body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
            .container { max-width: 600px; margin: 0 auto; padding: 20px; }
            .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
            .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
            .otp-box { background: white; border: 2px dashed #667eea; padding: 20px; text-align: center; margin: 20px 0; border-radius: 8px; }
            .otp-code { font-size: 36px; font-weight: bold; color: #667eea; letter-spacing: 8px; }
            .warning { color: #e74c3c; font-size: 14px; margin-top: 20px; }
            .footer { text-align: center; margin-top: 20px; color: #777; font-size: 12px; }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="header">
              <h1>⚽ Đặt lại mật khẩu</h1>
            </div>
            <div class="content">
              <p>Xin chào <strong>${userName || 'bạn'}</strong>,</p>
              <p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của mình. Sử dụng mã OTP bên dưới để xác nhận:</p>
              
              <div class="otp-box">
                <div class="otp-code">${otpCode}</div>
                <p style="margin-top: 10px; color: #666;">Mã này có hiệu lực trong <strong>10 phút</strong></p>
              </div>

              <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>
              
              <div class="warning">
                ⚠️ <strong>Lưu ý:</strong> Không chia sẻ mã này với bất kỳ ai!
              </div>
            </div>
            <div class="footer">
              <p>© 2025 Sport Field Booking System</p>
              <p>Email này được gửi tự động, vui lòng không trả lời.</p>
            </div>
          </div>
        </body>
        </html>
      `
    };

    // In development mode, just log the OTP
    if (process.env.NODE_ENV !== 'production' || !process.env.EMAIL_HOST) {
      console.log('═══════════════════════════════════════════');
      console.log('📧 [DEV MODE] Email OTP');
      console.log('═══════════════════════════════════════════');
      console.log('To:', email);
      console.log('Subject:', mailOptions.subject);
      console.log('OTP Code:', otpCode);
      console.log('Valid for: 10 minutes');
      console.log('═══════════════════════════════════════════');
      
      // Return success without actually sending
      return {
        success: true,
        messageId: 'dev-mode-' + Date.now(),
        message: 'Email logged to console (DEV mode)'
      };
    }

    // Send email in production
    const info = await transport.sendMail(mailOptions);

    console.log('✅ Email sent successfully:', info.messageId);
    return {
      success: true,
      messageId: info.messageId,
      message: 'Email sent successfully'
    };

  } catch (error) {
    console.error('❌ Error sending email:', error);
    throw new Error('Failed to send email: ' + error.message);
  }
};

// Send password reset success notification
export const sendPasswordResetSuccessEmail = async (email, userName) => {
  try {
    const transport = createTransporter();

    const mailOptions = {
      from: process.env.EMAIL_FROM || 'Sport Booking <noreply@sportbooking.com>',
      to: email,
      subject: 'Mật khẩu đã được đặt lại thành công',
      html: `
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <style>
            body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
            .container { max-width: 600px; margin: 0 auto; padding: 20px; }
            .header { background: linear-gradient(135deg, #28a745 0%, #20c997 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
            .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
            .footer { text-align: center; margin-top: 20px; color: #777; font-size: 12px; }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="header">
              <h1>✅ Mật khẩu đã được cập nhật</h1>
            </div>
            <div class="content">
              <p>Xin chào <strong>${userName || 'bạn'}</strong>,</p>
              <p>Mật khẩu tài khoản của bạn đã được đặt lại thành công vào lúc <strong>${new Date().toLocaleString('vi-VN')}</strong>.</p>
              <p>Bây giờ bạn có thể đăng nhập với mật khẩu mới.</p>
              <p>Nếu bạn không thực hiện thay đổi này, vui lòng liên hệ với chúng tôi ngay lập tức.</p>
            </div>
            <div class="footer">
              <p>© 2025 Sport Field Booking System</p>
            </div>
          </div>
        </body>
        </html>
      `
    };

    if (process.env.NODE_ENV !== 'production' || !process.env.EMAIL_HOST) {
      console.log('📧 [DEV MODE] Password reset success email to:', email);
      return { success: true, messageId: 'dev-mode-success' };
    }

    const info = await transport.sendMail(mailOptions);
    return { success: true, messageId: info.messageId };

  } catch (error) {
    console.error('❌ Error sending success email:', error);
    // Don't throw error for notification emails
    return { success: false, error: error.message };
  }
};

export default {
  sendOTPEmail,
  sendPasswordResetSuccessEmail
};
