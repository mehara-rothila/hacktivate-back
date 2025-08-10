// File Path: src/main/java/com/edulink/backend/dto/response/UserProfileResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class UserProfileResponse {
    
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String department;
    private String avatar;
    private String phone;
    private boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Personal Information Fields
    private String bio;
    private String dateOfBirth;
    private String gender;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String emergencyContact;
    private String emergencyPhone;

    // Social/Professional Links
    private String linkedIn;
    private String github;
    private String portfolio;
    private String website;
    private String researchGate;
    private String orcid;

    // Student-specific fields
    private String studentId;
    private String year;
    private String major;
    private String minor;
    private String program;
    private String gpa;
    private String expectedGraduation;
    private String enrollmentStatus;
    private String academicStanding;
    
    // Academic Records
    private List<AcademicRecordResponse> academicRecords = new ArrayList<>();
    private Double calculatedGPA;
    private Integer totalCompletedCredits;
    private Integer completedCoursesCount;

    // Lecturer-specific fields
    private String employeeId;
    private String office;
    private String title;
    private String position;
    private String qualification;
    private String experience;
    private String employmentType;
    private String status;
    private String officeAddress;
    private String officeHours;
    private String campus;
    private String building;
    private String room;

    // Academic Record Response DTO
    public static class AcademicRecordResponse {
        private String id;
        private String courseCode;
        private String courseName;
        private String semester;
        private Integer credits;
        private String grade;
        private String year;
        private String status;
        private LocalDateTime recordedAt;
        
        // Constructors
        public AcademicRecordResponse() {}
        
        public static AcademicRecordResponse fromAcademicRecord(User.AcademicRecord record) {
            if (record == null) return null;
            
            AcademicRecordResponse response = new AcademicRecordResponse();
            response.id = record.getId();
            response.courseCode = record.getCourseCode();
            response.courseName = record.getCourseName();
            response.semester = record.getSemester();
            response.credits = record.getCredits();
            response.grade = record.getGrade();
            response.year = record.getYear();
            response.status = record.getStatus();
            response.recordedAt = record.getRecordedAt();
            return response;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getCourseCode() { return courseCode; }
        public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
        
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        
        public String getSemester() { return semester; }
        public void setSemester(String semester) { this.semester = semester; }
        
        public Integer getCredits() { return credits; }
        public void setCredits(Integer credits) { this.credits = credits; }
        
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
        
        public String getYear() { return year; }
        public void setYear(String year) { this.year = year; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getRecordedAt() { return recordedAt; }
        public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    }

    // Constructors
    public UserProfileResponse() {}

    // Static factory method
    public static UserProfileResponse fromUser(User user) {
        if (user == null) return null;
        
        UserProfileResponse response = new UserProfileResponse();
        User.UserProfile profile = user.getProfile();
        
        // Basic fields
        response.id = user.getId();
        response.email = user.getEmail();
        response.firstName = profile != null ? profile.getFirstName() : null;
        response.lastName = profile != null ? profile.getLastName() : null;
        response.role = user.getRole() != null ? user.getRole().toString() : null;
        response.department = profile != null ? profile.getDepartment() : null;
        response.avatar = profile != null ? profile.getAvatar() : null;
        response.phone = profile != null ? profile.getPhone() : null;
        response.isActive = user.isActive();
        response.lastLogin = user.getLastLogin();
        response.createdAt = user.getCreatedAt();
        response.updatedAt = user.getUpdatedAt();
        
        if (profile != null) {
            // Personal Information
            response.bio = profile.getBio();
            response.dateOfBirth = profile.getDateOfBirth();
            response.gender = profile.getGender();
            response.address = profile.getAddress();
            response.city = profile.getCity();
            response.country = profile.getCountry();
            response.postalCode = profile.getPostalCode();
            response.emergencyContact = profile.getEmergencyContact();
            response.emergencyPhone = profile.getEmergencyPhone();
            
            // Social Links
            response.linkedIn = profile.getLinkedIn();
            response.github = profile.getGithub();
            response.portfolio = profile.getPortfolio();
            response.website = profile.getWebsite();
            response.researchGate = profile.getResearchGate();
            response.orcid = profile.getOrcid();
            
            // Student-specific fields
            response.studentId = profile.getStudentId();
            response.year = profile.getYear();
            response.major = profile.getMajor();
            response.minor = profile.getMinor();
            response.program = profile.getProgram();
            response.gpa = profile.getGpa();
            response.expectedGraduation = profile.getExpectedGraduation();
            response.enrollmentStatus = profile.getEnrollmentStatus();
            response.academicStanding = profile.getAcademicStanding();
            
            // Academic Records
            if (profile.getAcademicRecords() != null) {
                response.academicRecords = profile.getAcademicRecords().stream()
                        .map(AcademicRecordResponse::fromAcademicRecord)
                        .toList();
            }
            response.calculatedGPA = profile.calculateGPA();
            response.totalCompletedCredits = profile.getTotalCompletedCredits();
            response.completedCoursesCount = profile.getCompletedCoursesCount();
            
            // Lecturer-specific fields
            response.employeeId = profile.getEmployeeId();
            response.office = profile.getOffice();
            response.title = profile.getTitle();
            response.position = profile.getPosition();
            response.qualification = profile.getQualification();
            response.experience = profile.getExperience();
            response.employmentType = profile.getEmploymentType();
            response.status = profile.getStatus();
            response.officeAddress = profile.getOfficeAddress();
            response.officeHours = profile.getOfficeHours();
            response.campus = profile.getCampus();
            response.building = profile.getBuilding();
            response.room = profile.getRoom();
        }
        
        return response;
    }

    // Basic Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Personal Information Getters and Setters
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getEmergencyPhone() { return emergencyPhone; }
    public void setEmergencyPhone(String emergencyPhone) { this.emergencyPhone = emergencyPhone; }

    // Social/Professional Links Getters and Setters
    public String getLinkedIn() { return linkedIn; }
    public void setLinkedIn(String linkedIn) { this.linkedIn = linkedIn; }

    public String getGithub() { return github; }
    public void setGithub(String github) { this.github = github; }

    public String getPortfolio() { return portfolio; }
    public void setPortfolio(String portfolio) { this.portfolio = portfolio; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getResearchGate() { return researchGate; }
    public void setResearchGate(String researchGate) { this.researchGate = researchGate; }

    public String getOrcid() { return orcid; }
    public void setOrcid(String orcid) { this.orcid = orcid; }

    // Student-specific Getters and Setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getMinor() { return minor; }
    public void setMinor(String minor) { this.minor = minor; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public String getGpa() { return gpa; }
    public void setGpa(String gpa) { this.gpa = gpa; }

    public String getExpectedGraduation() { return expectedGraduation; }
    public void setExpectedGraduation(String expectedGraduation) { this.expectedGraduation = expectedGraduation; }

    public String getEnrollmentStatus() { return enrollmentStatus; }
    public void setEnrollmentStatus(String enrollmentStatus) { this.enrollmentStatus = enrollmentStatus; }

    public String getAcademicStanding() { return academicStanding; }
    public void setAcademicStanding(String academicStanding) { this.academicStanding = academicStanding; }

    // Academic Records Getters and Setters
    public List<AcademicRecordResponse> getAcademicRecords() { return academicRecords; }
    public void setAcademicRecords(List<AcademicRecordResponse> academicRecords) { this.academicRecords = academicRecords; }

    public Double getCalculatedGPA() { return calculatedGPA; }
    public void setCalculatedGPA(Double calculatedGPA) { this.calculatedGPA = calculatedGPA; }

    public Integer getTotalCompletedCredits() { return totalCompletedCredits; }
    public void setTotalCompletedCredits(Integer totalCompletedCredits) { this.totalCompletedCredits = totalCompletedCredits; }

    public Integer getCompletedCoursesCount() { return completedCoursesCount; }
    public void setCompletedCoursesCount(Integer completedCoursesCount) { this.completedCoursesCount = completedCoursesCount; }

    // Lecturer-specific Getters and Setters
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getOffice() { return office; }
    public void setOffice(String office) { this.office = office; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOfficeAddress() { return officeAddress; }
    public void setOfficeAddress(String officeAddress) { this.officeAddress = officeAddress; }

    public String getOfficeHours() { return officeHours; }
    public void setOfficeHours(String officeHours) { this.officeHours = officeHours; }

    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
}