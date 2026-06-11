import {
  registerDeviceToken,
  removeDeviceToken,
} from "../../services/user/pushNotificationService.js";

export const registerMyDeviceToken = async (req, res) => {
  try {
    const userId = req.user?.id;
    const {
      token,
      fcmToken,
      platform = "android",
      appVersion = null,
      deviceId = null,
    } = req.body || {};

    await registerDeviceToken({
      userId,
      fcmToken: fcmToken || token,
      platform,
      appVersion,
      deviceId,
    });

    return res.json({
      success: true,
      message: "Device token registered",
    });
  } catch (error) {
    const status = error.code === "INVALID_DEVICE_TOKEN" ? 400 : 500;
    return res.status(status).json({
      success: false,
      message:
        status === 400 ? "Invalid device token" : "Unable to register device token",
    });
  }
};

export const unregisterMyDeviceToken = async (req, res) => {
  try {
    const userId = req.user?.id;
    const { token, fcmToken } = req.body || {};
    await removeDeviceToken({ userId, fcmToken: fcmToken || token });
    return res.json({
      success: true,
      message: "Device token removed",
    });
  } catch (_error) {
    return res.status(500).json({
      success: false,
      message: "Unable to remove device token",
    });
  }
};
