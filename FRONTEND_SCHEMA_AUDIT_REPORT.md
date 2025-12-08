# 🔍 FRONTEND SCHEMA AUDIT REPORT

**Date:** December 8, 2025  
**Status:** ⚠️ **MULTIPLE ISSUES FOUND** - Frontend needs updates to match backend schema

---

## 📊 SUMMARY
Your frontend has **7 critical schema mismatches** with the updated backend API. These will cause API failures and data display issues.

---

## 🔴 CRITICAL ISSUES FOUND

### 1. **TeacherInfo Model - Wrong Field Name**
**File:** `models/Teacher.kt` (Line 29)  
**Issue:** Using `profile_image` instead of `profile_picture_url`
```kotlin
// ❌ WRONG
data class TeacherInfo(
    val profile_image: String?  // <-- Should be profile_picture_url
)

// ✅ CORRECT
data class TeacherInfo(
    val profile_picture_url: String?
)
```
**Impact:** Profile pictures won't load for teachers; API will return null/error

**Backend Schema:** Teachers table now uses `profile_picture_url`

---

### 2. **TeacherDashboard.kt - Incorrect Field Reference**
**File:** `TeacherDashboard.kt` (Line 175)  
**Issue:** Trying to access `profile_image` that doesn't exist in new schema
```kotlin
// ❌ WRONG
it.teacher.profile_image?.let { url ->

// ✅ CORRECT
it.teacher.profile_picture_url?.let { url ->
```
**Impact:** Teacher profile pictures won't display

---

### 3. **Attendance Status - Not Capitalized**
**File:** `MarkAttendance.kt` (Line 160)  
**Issue:** Sending lowercase status values; backend expects capitalized
```kotlin
// ❌ WRONG
status = status.lowercase()  // sends "present", "absent"

// ✅ CORRECT - Send capitalized values
status: 'Present' | 'Absent' | 'Late' | 'Excused'
```
**Affected Code:**
- `MarkAttendance.kt` line 55: `"present"` → `"Present"`
- `MarkAttendance.kt` line 55: `"absent"` → `"Absent"`
- Backend validates: `['Present', 'Absent', 'Late', 'Excused']`

**Impact:** Attendance marking will fail; backend rejects lowercase values

---

### 4. **AttendanceSummary Model - Old Field Names**
**File:** `models/Attendance.kt` (Lines 5-6)  
**Issue:** Using `first_name` and `last_name` instead of `full_name`
```kotlin
// ❌ WRONG
data class AttendanceSummary(
    val first_name: String,
    val last_name: String,
)

// ✅ CORRECT
data class AttendanceSummary(
    val full_name: String,
)
```
**Affected Code:**
- `adapters/AttendanceAdapter.kt` line 34: `"${attendance.first_name} ${attendance.last_name}"`
  Should be: `attendance.full_name`

**Impact:** Attendance summary view will crash when displaying names

---

### 5. **Course Model - Duplicate Credits Fields**
**File:** `models/Course.kt` (Lines 8-9)  
**Issue:** Has both `credits` and `credit_hours` - should only have `credit_hours`
```kotlin
// ❌ WRONG
data class Course(
    val credits: Int,
    val credit_hours: Int,
)

// ✅ CORRECT
data class Course(
    val credit_hours: Int,
)
```
**Affected Code:**
- `adapters/CourseAdapter.kt` line 35: `"Credits: ${course.credits}"`
  Should be: `"Credit Hours: ${course.credit_hours}"`

**Impact:** Credits display might show wrong value; potential confusion

---

### 6. **PostAnnouncementRequest - Missing announcement_type**
**File:** `models/Announcement.kt` (PostAnnouncementRequest)  
**Issue:** Missing required `announcement_type` field
```kotlin
// ❌ WRONG
data class PostAnnouncementRequest(
    val courseId: Int?,
    val title: String,
    val content: String
    // Missing: announcement_type
)

// ✅ CORRECT
data class PostAnnouncementRequest(
    val courseId: Int?,
    val title: String,
    val content: String,
    val announcement_type: String  // Required: 'General', 'Urgent', 'Event', 'Academic', 'Administrative'
)
```
**Affected Code:**
- `PostAnnouncement.kt` line 76: Has a commented variable but doesn't send it
- Backend schema requires: `announcement_type` ENUM with default `'General'`

**Impact:** Announcements may fail to post; default type handling undefined

---

### 7. **StudentMark Model - Old Field Names**
**File:** `models/Marks.kt` (Lines 51-52)  
**Issue:** Using `first_name` and `last_name` instead of `full_name`
```kotlin
// ❌ WRONG
data class StudentMark(
    val first_name: String,
    val last_name: String,
)

// ✅ CORRECT
data class StudentMark(
    val full_name: String,
)
```
**Impact:** Mark entry screens will crash when displaying student names

---

## 🟡 ADDITIONAL OBSERVATIONS

### ✅ What's Correct:
- ✓ Student model uses `full_name` correctly
- ✓ Teacher model has `employee_id`, `department`, `designation`, `specialization` fields
- ✓ Course model has `is_required`, `description` fields
- ✓ Announcement model has `announcement_type` field
- ✓ Fee model structure is correct
- ✓ ApiService endpoints are well-defined

### ⚠️ Missing Enum Validation:
- PostAnnouncement.kt should validate: `'General', 'Urgent', 'Event', 'Academic', 'Administrative'`
- Attendance status should validate: `'Present', 'Absent', 'Late', 'Excused'`

---

## 📋 FIX CHECKLIST

### Models to Update:
- [ ] `models/Teacher.kt` - Change `profile_image` → `profile_picture_url` in TeacherInfo
- [ ] `models/Attendance.kt` - Change `first_name, last_name` → `full_name` in AttendanceSummary
- [ ] `models/Course.kt` - Remove `credits` field, keep only `credit_hours`
- [ ] `models/Announcement.kt` - Add `announcement_type` to PostAnnouncementRequest
- [ ] `models/Marks.kt` - Change `first_name, last_name` → `full_name` in StudentMark

### Activity/Adapter Code to Update:
- [ ] `TeacherDashboard.kt` - Line 175: `profile_image` → `profile_picture_url`
- [ ] `MarkAttendance.kt` - Line 160: Remove `.lowercase()`, use capitalized values
- [ ] `AttendanceAdapter.kt` - Line 34: Update name display logic
- [ ] `CourseAdapter.kt` - Line 35: `credits` → `credit_hours`
- [ ] `PostAnnouncement.kt` - Add announcement_type field handling

---

## 🔗 BACKEND SCHEMA REFERENCE

| Entity | Old Field | New Field | Type |
|--------|-----------|-----------|------|
| Teachers | `profile_image` | `profile_picture_url` | VARCHAR(500) |
| Students | `first_name` + `last_name` | `full_name` | VARCHAR(100) |
| Courses | `credits` | `credit_hours` | INT |
| Announcements | N/A | `announcement_type` | ENUM |
| Attendance | `status: 'present'` | `status: 'Present'` | ENUM |

---

## ✅ NEXT STEPS

1. **Update Models** (5 files) - Change field names and types
2. **Update Activities** (2 files) - Fix field references and value formatting
3. **Update Adapters** (2 files) - Adjust display logic
4. **Test API Calls** - Verify all endpoints work with new schema
5. **Verify UI** - Ensure data displays correctly

---

**Generated:** 2025-12-08  
**Recommendation:** Apply all fixes immediately before testing with new backend
