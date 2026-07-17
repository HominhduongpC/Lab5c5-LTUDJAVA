package vn.edu.gdu.springjpalab.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.edu.gdu.springjpalab.entity.*;
import vn.edu.gdu.springjpalab.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Nạp dữ liệu mẫu khi khởi động ứng dụng, để các lệnh cURL trong bài thực hành
 * có sẵn dữ liệu mà chạy. Chỉ chạy khi bảng products còn rỗng nên không ghi trùng
 * dữ liệu mỗi lần restart.
 */
@Component
// Không nạp dữ liệu mẫu khi chạy unit test.
// Dùng tên đầy đủ vì @Profile của Spring trùng tên với entity Profile.
@org.springframework.context.annotation.Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public DataSeeder(ProductRepository productRepository,
                      CategoryRepository categoryRepository,
                      UserRepository userRepository,
                      StudentRepository studentRepository,
                      CourseRepository courseRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            System.out.println("[SEED] Da co du lieu, bo qua buoc nap du lieu mau.");
            return;
        }

        // ── Danh mục + Sản phẩm (quan hệ Một - Nhiều) ──
        Category laptop = categoryRepository.save(new Category("Laptop"));
        Category dienThoai = categoryRepository.save(new Category("Điện thoại"));
        Category phuKien = categoryRepository.save(new Category("Phụ kiện"));

        productRepository.saveAll(List.of(
                new Product("Dell XPS 13", new BigDecimal("32000000"), "DELL-XPS13", laptop),
                new Product("Dell Inspiron 15", new BigDecimal("18500000"), "DELL-INS15", laptop),
                new Product("Dell Latitude 7440", new BigDecimal("38000000"), "DELL-LAT7440", laptop),
                new Product("MacBook Air M3", new BigDecimal("28900000"), "MBA-M3", laptop),
                new Product("MacBook Pro 14", new BigDecimal("48000000"), "MBP-14", laptop),
                new Product("iPhone 15 Pro Max", new BigDecimal("34990000"), "IP15PM", dienThoai),
                new Product("Samsung Galaxy S24", new BigDecimal("22000000"), "SS-S24", dienThoai),
                new Product("Chuột Logitech MX", new BigDecimal("2500000"), "LOGI-MX", phuKien)
        ));

        // ── User + Profile (quan hệ Một - Một) ──
        userRepository.save(new User("minhdao", new Profile("Sinh viên GDU", "https://i.pravatar.cc/150?u=1")));
        userRepository.save(new User("lananh", new Profile("Trợ giảng Java", "https://i.pravatar.cc/150?u=2")));

        // ── Student + Course (quan hệ Nhiều - Nhiều) ──
        Student sv1 = studentRepository.save(new Student(
                "SV001", "Nguyễn Văn An", "an@gdu.edu.vn",
                new BigDecimal("3.20"), LocalDate.of(2023, 9, 5)));
        Student sv2 = studentRepository.save(new Student(
                "SV002", "Trần Thị Bình", "binh@gdu.edu.vn",
                new BigDecimal("3.65"), LocalDate.of(2023, 9, 5)));

        Course courseJava = courseRepository.save(new Course("Lập trình ứng dụng với Java"));
        Course courseCsdl = courseRepository.save(new Course("Cơ sở dữ liệu"));

        sv1.enrollInCourse(courseJava);
        sv1.enrollInCourse(courseCsdl);
        sv2.enrollInCourse(courseJava);
        studentRepository.saveAll(List.of(sv1, sv2));

        System.out.println("[SEED] Da nap xong du lieu mau: "
                + productRepository.count() + " san pham, "
                + studentRepository.count() + " sinh vien, "
                + courseRepository.count() + " mon hoc.");
    }
}
