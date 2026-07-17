package vn.edu.gdu.springjpalab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.gdu.springjpalab.entity.Course;

import java.util.List;

/**
 * Chương 5 - Bài tập về nhà, mục 1.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByTitleContainingIgnoreCase(String keyword);
}
