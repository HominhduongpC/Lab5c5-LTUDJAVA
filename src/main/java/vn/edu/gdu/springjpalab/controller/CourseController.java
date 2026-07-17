package vn.edu.gdu.springjpalab.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.gdu.springjpalab.entity.Course;
import vn.edu.gdu.springjpalab.repository.CourseRepository;

import java.util.List;

/**
 * Chương 5 - Bài 3: API quản lý môn học (quan hệ Nhiều - Nhiều với Student).
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseRepository courseRepository;

    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    // ── 1. Lấy danh sách tất cả môn học ──
    @GetMapping
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // ── 2. Lấy môn học theo ID ──
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return courseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── 3. Thêm mới môn học ──
    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseRepository.save(course));
    }

    // ── 4. Tìm môn học theo từ khóa tên ──
    @GetMapping("/search")
    public List<Course> searchCourses(@RequestParam String keyword) {
        return courseRepository.findByTitleContainingIgnoreCase(keyword);
    }

    // ── 5. Xóa môn học ──
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        if (courseRepository.existsById(id)) {
            courseRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ── 6. Đếm tổng số môn học ──
    @GetMapping("/count")
    public long countCourses() {
        return courseRepository.count();
    }
}
