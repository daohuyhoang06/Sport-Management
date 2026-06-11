import { getManagerProfileService, updateManagerProfileService } from '../../services/manager/profileService.js';

export const getProfile = async (req, res) => {
  try {
    const profile = await getManagerProfileService(req.user.id);
    if (!profile) return res.status(404).json({ success: false, message: 'Không tìm thấy hồ sơ' });
    res.json({ success: true, data: profile });
  } catch (error) {
    console.error('Error in getProfile:', error);
    res.status(500).json({ success: false, message: error.message });
  }
};

export const updateProfile = async (req, res) => {
  try {
    const { person_name, phone, email } = req.body;
    const updated = await updateManagerProfileService(req.user.id, { person_name, phone, email });
    res.json({ success: true, data: updated });
  } catch (error) {
    console.error('Error in updateProfile:', error);
    res.status(500).json({ success: false, message: error.message });
  }
};
