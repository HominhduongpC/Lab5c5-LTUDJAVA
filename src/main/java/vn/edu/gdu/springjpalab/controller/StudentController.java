package vn.edu.gdu.springjpalab.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.edu.gdu.springjpalab.entity.Course;
import vn.edu.gdu.springjpalab.entity.Student;
import vn.edu.gdu.springjpalab.repository.CourseRepository;
import vn.edu.gdu.springjpalab.repository.StudentRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository; // Bổ sung cho Chương 5

    public StudentController(StudentRepository studentRepository,
                             CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    // ── 1. Lấy danh sách tất cả sinh viên ──
    @GetMapping
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // ── 2. Lấy sinh viên theo ID ──
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Optional<Student> student = studentRepository.findById(id);
        return student.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── 3. Thêm mới sinh viên ──
    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        Student saved = studentRepository.save(student);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ── 4. Cập nhật sinh viên ──
    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(
            @PathVariable Long id,
            @RequestBody Student details) {
        Optional<Student> existing = studentRepository.findById(id);
        if (existing.isPresent()) {
            Student student = existing.get();
            student.setStudentCode(details.getStudentCode());
            student.setFullName(details.getFullName());
            student.setEmail(details.getEmail());
            student.setGpa(details.getGpa());
            student.setEnrollmentDate(details.getEnrollmentDate());
            return ResponseEntity.ok(studentRepository.save(student));
        }
        return ResponseEntity.notFound().build();
    }

    // ── 5. Xóa sinh viên ──
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        if (studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ── 6. Đếm tổng số sinh viên ──
    @GetMapping("/count")
    public long countStudents() {
        return studentRepository.count();
    }

    // ═══════════════ CÁC API NÂNG CAO (Chương 5) ═══════════════

    // ── 7. Lấy sinh viên kèm danh sách môn học đã đăng ký (JOIN FETCH, tránh N+1) ──
    @GetMapping("/{id}/courses")
    public ResponseEntity<Student> getStudentWithCourses(@PathVariable Long id) {
        return studentRepository.findByIdWithCourses(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── 8. API Đăng ký học phần (Bài tập về nhà - quan hệ ManyToMany) ──
    // Ghi một dòng mới vào bảng trung gian student_course
    @Transactional
    @PostMapping("/{studentId}/enroll/{courseId}")
    public ResponseEntity<String> enroll(@PathVariable Long studentId,
                                         @PathVariable Long courseId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy sinh viên ID: " + studentId);
        }

        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy môn học ID: " + courseId);
        }

        Student student = studentOpt.get();
        Course course = courseOpt.get();

        if (student.getCourses().contains(course)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Sinh viên đã đăng ký môn học này rồi.");
        }

        // Hàm tiện ích đồng bộ cả hai phía của quan hệ hai chiều
        student.enrollInCourse(course);
        studentRepository.save(student);

        return ResponseEntity.ok("Đăng ký thành công: sinh viên '" + student.getFullName()
                + "' -> môn học '" + course.getTitle() + "'");
    }

    // ── 9. API Hủy đăng ký học phần ──
    @Transactional
    @DeleteMapping("/{studentId}/enroll/{courseId}")
    public ResponseEntity<String> unenroll(@PathVariable Long studentId,
                                           @PathVariable Long courseId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        Optional<Course> courseOpt = courseRepository.findById(courseId);

        if (studentOpt.isEmpty() || courseOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy sinh viên hoặc môn học.");
        }

        Student student = studentOpt.get();
        Course course = courseOpt.get();

        student.getCourses().remove(course);
        course.getStudents().remove(student);
        studentRepository.save(student);

        return ResponseEntity.ok("Đã hủy đăng ký môn học ID: " + courseId);
    }
}
