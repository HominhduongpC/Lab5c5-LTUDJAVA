package vn.edu.gdu.springjpalab.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_code", unique = true, nullable = false, length = 20)
    private String studentCode;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", unique = true, nullable = false, length = 150)
    private String email;

    @Column(name = "gpa", precision = 3, scale = 2)
    private BigDecimal gpa; // có thể null

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate; // có thể null

    // ── Bổ sung thuộc tính quan hệ (Chương 5 - Bài 3) ──
    // Owning Side: phía khai báo bảng liên kết trung gian student_course
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "student_course", // Tên bảng liên kết trung gian tự sinh
        joinColumns = @JoinColumn(name = "student_id"), // Khóa ngoại trỏ về bảng hiện tại (students)
        inverseJoinColumns = @JoinColumn(name = "course_id") // Khóa ngoại trỏ về bảng đối diện (courses)
    )
    private Set<Course> courses = new HashSet<>();

    // ── Constructor không tham số (BẮT BUỘC cho JPA) ──
    protected Student() {
    }

    // ── Constructor tiện lợi ──
    public Student(String studentCode, String fullName, String email,
                   BigDecimal gpa, LocalDate enrollmentDate) {
        this.studentCode = studentCode;
        this.fullName = fullName;
        this.email = email;
        this.gpa = gpa;
        this.enrollmentDate = enrollmentDate;
    }

    // ── Getter và Setter ──
    public Long getId() {
        return id;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getGpa() {
        return gpa;
    }

    public void setGpa(BigDecimal gpa) {
        this.gpa = gpa;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    // ── Quan hệ Nhiều-Nhiều với Course (Chương 5) ──
    // Hàm tiện ích hỗ trợ liên kết dữ liệu hai chiều nhanh chóng
    public void enrollInCourse(Course course) {
        this.courses.add(course);
        course.getStudents().add(this);
    }

    public Set<Course> getCourses() {
        return courses;
    }

    public void setCourses(Set<Course> courses) {
        this.courses = courses;
    }

    @Override
    public String toString() {
        return "Student{id=" + id + ", studentCode='" + studentCode + "', fullName='" + fullName
                + "', email='" + email + "', gpa=" + gpa + ", enrollmentDate=" + enrollmentDate + "}";
    }
}
