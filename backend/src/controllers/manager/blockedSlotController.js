import {
  listBlockedSlotsService,
  createBlockedSlotService,
  deleteBlockedSlotService,
} from '../../services/manager/blockedSlotService.js';

/** GET /api/manager/fields/:id/blocked-slots */
export const listBlockedSlots = async (req, res) => {
  try {
    const managerId = req.user.id;
    const fieldId = parseInt(req.params.id);
    const slots = await listBlockedSlotsService(managerId, fieldId);
    res.json({ success: true, data: slots });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
};

/** POST /api/manager/fields/:id/blocked-slots */
export const createBlockedSlot = async (req, res) => {
  try {
    const managerId = req.user.id;
    const fieldId = parseInt(req.params.id);
    const slot = await createBlockedSlotService(managerId, fieldId, req.body);
    res.status(201).json({ success: true, data: slot });
  } catch (err) {
    const status = err.message.includes('quyền') ? 403 : 400;
    res.status(status).json({ message: err.message });
  }
};

/** DELETE /api/manager/fields/:id/blocked-slots/:slotId */
export const deleteBlockedSlot = async (req, res) => {
  try {
    const managerId = req.user.id;
    const fieldId = parseInt(req.params.id);
    const slotId = parseInt(req.params.slotId);
    await deleteBlockedSlotService(managerId, fieldId, slotId);
    res.json({ success: true, message: 'Xóa blocked slot thành công' });
  } catch (err) {
    const status = err.message.includes('quyền') ? 403 : 404;
    res.status(status).json({ message: err.message });
  }
};
