package vn.edu.gdu.springjpalab.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import vn.edu.gdu.springjpalab.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kiểm thử Bài 3: các mối quan hệ OneToMany, OneToOne, ManyToMany.
 */
@DataJpaTest
@ActiveProfiles("test")
class RelationshipTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Student newStudent(String code, String name, String email) {
        return new Student(code, name, email, new BigDecimal("3.20"), LocalDate.of(2023, 9, 5));
    }

    // ── Bước 1: Một - Nhiều giữa Category và Product ──

    @Test
    @DisplayName("Bài 3.1: Category (Inverse Side) <-> Product (Owning Side, giữ khóa ngoại category_id)")
    void quanHeMotNhieuCategoryProduct() {
        Category laptop = new Category("Laptop");
        laptop.addProduct(new Product("Dell XPS 13", new BigDecimal("32000000"), "DELL-XPS13"));
        laptop.addProduct(new Product("MacBook Air", new BigDecimal("28900000"), "MBA-M3"));

        // cascade = ALL -> lưu Category sẽ lưu luôn các Product con
        categoryRepository.save(laptop);
        entityManager.flush();
        entityManager.clear();

        Category reloaded = categoryRepository.findByName("Laptop").orElseThrow();
        assertThat(reloaded.getProducts()).hasSize(2);
        // Kiểm tra chiều ngược lại: Product trỏ về đúng Category
        assertThat(reloaded.getProducts().get(0).getCategory().getName()).isEqualTo("Laptop");
    }

    @Test
    @DisplayName("Bài 3.1: orphanRemoval = true -> gỡ Product khỏi Category sẽ xóa hẳn bản ghi")
    void orphanRemovalXoaSanPhamMoCoi() {
        Category laptop = new Category("Laptop");
        laptop.addProduct(new Product("Dell XPS 13", new BigDecimal("32000000"), "DELL-XPS13"));
        laptop.addProduct(new Product("MacBook Air", new BigDecimal("28900000"), "MBA-M3"));
        categoryRepository.save(laptop);
        entityManager.flush();

        Category managed = categoryRepository.findByName("Laptop").orElseThrow();
        managed.removeProduct(managed.getProducts().get(0));
        categoryRepository.save(managed);
        entityManager.flush();
        entityManager.clear();

        assertThat(productRepository.count()).isEqualTo(1);
    }

    // ── Bước 2: Một - Một giữa User và Profile ──

    @Test
    @DisplayName("Bài 3.2: User (Owning Side, giữ profile_id) <-> Profile, CascadeType.ALL")
    void quanHeMotMotUserProfile() {
        User user = new User("minhdao", new Profile("Sinh vien GDU", "https://avatar/1.png"));

        // cascade = ALL -> chỉ cần lưu User, Profile tự được lưu theo
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        User reloaded = userRepository.findByUsername("minhdao").orElseThrow();
        assertThat(reloaded.getProfile()).isNotNull();
        assertThat(reloaded.getProfile().getBio()).isEqualTo("Sinh vien GDU");
        // Chiều ngược lại: Profile trỏ về đúng User (mappedBy = "profile")
        assertThat(reloaded.getProfile().getUser().getUsername()).isEqualTo("minhdao");
    }

    // ── Bước 3: Nhiều - Nhiều giữa Student và Course ──

    @Test
    @DisplayName("Bài 3.3: enrollInCourse() đồng bộ cả hai phía và ghi vào bảng student_course")
    void quanHeNhieuNhieuStudentCourse() {
        Student an = studentRepository.save(newStudent("SV001", "Nguyen Van An", "an@gdu.edu.vn"));
        Student binh = studentRepository.save(newStudent("SV002", "Tran Thi Binh", "binh@gdu.edu.vn"));
        Course courseJava = courseRepository.save(new Course("Lap trinh Java"));
        Course courseCsdl = courseRepository.save(new Course("Co so du lieu"));

        an.enrollInCourse(courseJava);
        an.enrollInCourse(courseCsdl);
        binh.enrollInCourse(courseJava);
        studentRepository.saveAll(List.of(an, binh));
        entityManager.flush();
        entityManager.clear();

        // Kiểm tra từ phía Student (Owning Side)
        Student anReloaded = studentRepository.findByEmail("an@gdu.edu.vn").orElseThrow();
        assertThat(anReloaded.getCourses()).hasSize(2);

        // Kiểm tra từ phía Course (Inverse Side, mappedBy = "courses")
        Course javaReloaded = courseRepository.findById(courseJava.getId()).orElseThrow();
        assertThat(javaReloaded.getStudents()).hasSize(2);

        // Kiểm tra trực tiếp bảng trung gian: phải có 3 dòng (An-Java, An-CSDL, Binh-Java)
        Long rows = ((Number) entityManager.getEntityManager()
                .createNativeQuery("SELECT COUNT(*) FROM student_course")
                .getSingleResult()).longValue();
        assertThat(rows).isEqualTo(3);
    }

    @Test
    @DisplayName("Bài 3.3: JOIN FETCH nạp Student kèm Courses trong 1 câu SELECT (tránh lỗi N+1)")
    void joinFetchNapKemDanhSachMonHoc() {
        Student an = studentRepository.save(newStudent("SV001", "Nguyen Van An", "an@gdu.edu.vn"));
        Course courseJava = courseRepository.save(new Course("Lap trinh Java"));
        an.enrollInCourse(courseJava);
        studentRepository.save(an);
        entityManager.flush();
        entityManager.clear();

        Student reloaded = studentRepository.findByIdWithCourses(an.getId()).orElseThrow();

        // Collection đã được nạp sẵn ngay khi query trả về, không cần truy vấn thêm
        assertThat(org.hibernate.Hibernate.isInitialized(reloaded.getCourses())).isTrue();
        assertThat(reloaded.getCourses()).hasSize(1);
    }

    @Test
    @DisplayName("Chương 4: các trường studentCode, gpa, enrollmentDate vẫn được giữ nguyên")
    void cacTruongCuaChuong4VanHoatDong() {
        Student an = studentRepository.save(new Student(
                "SV001", "Nguyen Van An", "an@gdu.edu.vn",
                new BigDecimal("3.75"), LocalDate.of(2023, 9, 5)));
        entityManager.flush();
        entityManager.clear();

        Student reloaded = studentRepository.findByEmail("an@gdu.edu.vn").orElseThrow();
        assertThat(reloaded.getStudentCode()).isEqualTo("SV001");
        assertThat(reloaded.getGpa()).isEqualByComparingTo(new BigDecimal("3.75"));
        assertThat(reloaded.getEnrollmentDate()).isEqualTo(LocalDate.of(2023, 9, 5));
    }
}
