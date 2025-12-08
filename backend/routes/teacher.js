/**
 * Teacher Routes - Teacher Dashboard Operations
 */

const express = require('express');
const router = express.Router();

// Import controllers
const {
    getTeacherProfile,
    getTeacherDashboard,
    getTeacherCourses,
    getTeacherSchedule,
    getTodaySchedule,
    getTeacherAnnouncements,
    getTeacherNotifications,
    getUnreadNotifications,
    markNotificationAsRead,
    getCourseStudents,
    getCourseEvaluations,
    getEvaluationTypes,
    createEvaluation,
    markAttendance,
    markStudentAssessment,
    getCourseAttendanceSummary,
    postAnnouncement
} = require('../controllers/teacherController');

// Profile endpoints
router.get('/profile', getTeacherProfile);
router.get('/dashboard', getTeacherDashboard);

// Course endpoints
router.get('/courses', getTeacherCourses);
router.get('/course/:courseId/students', getCourseStudents);
router.get('/course/:courseId/evaluations', getCourseEvaluations);
router.get('/course/:courseId/attendance-summary', getCourseAttendanceSummary);

// Schedule endpoints
router.get('/schedule', getTeacherSchedule);
router.get('/schedule/today', getTodaySchedule);

// Announcement endpoints
router.get('/announcements', getTeacherAnnouncements);
router.post('/post-announcement', postAnnouncement);

// Notification endpoints
router.get('/notifications', getTeacherNotifications);
router.get('/notifications/unread', getUnreadNotifications);
router.post('/notifications/:notificationId/read', markNotificationAsRead);

// Evaluation endpoints
router.get('/evaluation-types', getEvaluationTypes);
router.post('/create-evaluation', createEvaluation);

// Attendance endpoints
router.post('/mark-attendance', markAttendance);

// Marks endpoints
router.post('/mark-student-assessment', markStudentAssessment);

module.exports = router;
