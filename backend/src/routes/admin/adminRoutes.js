import { Router } from "express";
import { requireAuth } from "../../middleware/authMiddleware.js";
import { requireRole } from "../../middleware/roleMiddleware.js";

// Controllers
import { getDashboard, getRevenueByDateRange, getRevenueByField, getMonthlyRevenueStats } from "../../controllers/admin/dashboardController.js";
import { getAllUsers, getUserById, createUser, updateUser, deleteUser, toggleUserStatus, getUserStats } from "../../controllers/admin/userManagementController.js";
import { getAllFields, getFieldById, createField, updateField, deleteField, toggleFieldStatus, getFieldStats, uploadFieldImages, deleteFieldImage } from "../../controllers/admin/fieldManagementController.js";
import { getAllBookings, getBookingById, updateBookingStatus, cancelBooking, getBookingStats, getBookingsByDateRange } from "../../controllers/admin/bookingManagementController.js";
import { getAllEmployees, getEmployeeById, createEmployee, updateEmployee, deleteEmployee, assignFieldToEmployee, getEmployeeStats } from "../../controllers/admin/employeeManagementController.js";

const router = Router();

// Apply authentication and admin role check to all routes
router.use(requireAuth);
router.use(requireRole("admin"));

// ========== DASHBOARD ==========
router.get('/dashboard', getDashboard);
router.get('/revenue/date-range', getRevenueByDateRange);
router.get('/revenue/field/:fieldId', getRevenueByField);
router.get('/revenue/monthly', getMonthlyRevenueStats);

// ========== USER MANAGEMENT ==========
router.get('/users', getAllUsers);
router.get('/users/stats', getUserStats);
router.get('/users/:id', getUserById);
router.post('/users', createUser);
router.put('/users/:id', updateUser);
router.delete('/users/:id', deleteUser);
router.patch('/users/:id/status', toggleUserStatus);

// ========== FIELD MANAGEMENT ==========
router.get('/fields', getAllFields);
router.get('/fields/stats', getFieldStats);
router.get('/fields/:id', getFieldById);
router.post('/fields', createField);
router.put('/fields/:id', updateField);
router.delete('/fields/:id', deleteField);
router.patch('/fields/:id/status', toggleFieldStatus);
router.post('/fields/:id/images', uploadFieldImages);
router.delete('/fields/images/:imageId', deleteFieldImage);

// ========== BOOKING MANAGEMENT ==========
router.get('/bookings', getAllBookings);
router.get('/bookings/stats', getBookingStats);
router.get('/bookings/date-range', getBookingsByDateRange);
router.get('/bookings/:id', getBookingById);
router.patch('/bookings/:id/status', updateBookingStatus);
router.post('/bookings/:id/cancel', cancelBooking);

// ========== EMPLOYEE MANAGEMENT ==========
router.get('/employees', getAllEmployees);
router.get('/employees/stats', getEmployeeStats);
router.get('/employees/:id', getEmployeeById);
router.post('/employees', createEmployee);
router.put('/employees/:id', updateEmployee);
router.delete('/employees/:id', deleteEmployee);
router.post('/employees/assign-field', assignFieldToEmployee);

export default router;
