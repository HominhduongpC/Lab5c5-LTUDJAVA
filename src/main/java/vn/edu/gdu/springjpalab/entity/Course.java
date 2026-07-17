package vn.edu.gdu.springjpalab.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Chương 5 - Bài 3, Bước 3: quan hệ Nhiều - Nhiều giữa Student và Course.
 * Course là Inverse Side (bảng trung gian student_course được khai báo bên Student).
 */
@Entity
@Table(name = "courses")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_title", nullable = false, length = 150)
    private String title;

    // mappedBy = "courses" trỏ tới tên thuộc tính bên Student (Owning Side)
    // @JsonIgnore để tránh vòng lặp vô tận: Student -> courses -> students -> ...
    @JsonIgnore
    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();

    // ── Constructor không tham số (BẮT BUỘC cho JPA) ──
    protected Course() {
    }

    // ── Constructor tiện lợi ──
    public Course(String title) {
        this.title = title;
    }

    // ── Getter và Setter ──
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<Student> getStudents() {
        return students;
    }

    public void setStudents(Set<Student> students) {
        this.students = students;
    }

    @Override
    public String toString() {
        return "Course{id=" + id + ", title='" + title + "'}";
    }
}
