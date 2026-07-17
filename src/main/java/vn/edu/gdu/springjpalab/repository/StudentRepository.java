package vn.edu.gdu.springjpalab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.gdu.springjpalab.entity.Student;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    // Kế thừa toàn bộ CRUD của JpaRepository: save, findById, findAll, deleteById, count, existsById...

    // ══════════ Bổ sung cho Chương 5 ══════════

    // Derived Query Method: tìm sinh viên theo email
    Optional<Student> findByEmail(String email);

    // JOIN FETCH: nạp sinh viên kèm danh sách môn học trong 1 câu SELECT duy nhất
    // -> tránh lỗi N+1 query khi duyệt student.getCourses()
    @Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.courses WHERE s.id = :id")
    Optional<Student> findByIdWithCourses(@Param("id") Long id);
}
