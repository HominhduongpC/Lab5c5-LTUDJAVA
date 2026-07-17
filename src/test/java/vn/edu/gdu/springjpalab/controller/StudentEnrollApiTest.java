package vn.edu.gdu.springjpalab.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.gdu.springjpalab.entity.Course;
import vn.edu.gdu.springjpalab.entity.Student;
import vn.edu.gdu.springjpalab.repository.CourseRepository;
import vn.edu.gdu.springjpalab.repository.StudentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Kiểm thử Bài tập về nhà: API đăng ký học phần
 * POST /api/students/{studentId}/enroll/{courseId}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentEnrollApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    private Long anId;
    private Long binhId;
    private Long javaId;
    private Long csdlId;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        courseRepository.deleteAll();

        anId = studentRepository.save(new Student("SV001", "Nguyen Van An", "an@gdu.edu.vn",
                new BigDecimal("3.20"), LocalDate.of(2023, 9, 5))).getId();
        binhId = studentRepository.save(new Student("SV002", "Tran Thi Binh", "binh@gdu.edu.vn",
                new BigDecimal("3.65"), LocalDate.of(2023, 9, 5))).getId();
        javaId = courseRepository.save(new Course("Lap trinh ung dung voi Java")).getId();
        csdlId = courseRepository.save(new Course("Co so du lieu")).getId();
    }

    @Test
    @DisplayName("BTVN: POST /api/students/{id}/enroll/{courseId} ghi dữ liệu vào bảng student_course")
    void dangKyHocPhanThanhCong() throws Exception {
        mockMvc.perform(post("/api/students/{studentId}/enroll/{courseId}", anId, javaId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Đăng ký thành công")));

        mockMvc.perform(post("/api/students/{studentId}/enroll/{courseId}", anId, csdlId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/students/{studentId}/enroll/{courseId}", binhId, javaId))
                .andExpect(status().isOk());

        // An đăng ký 2 môn
        mockMvc.perform(get("/api/students/{id}/courses", anId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nguyen Van An"))
                .andExpect(jsonPath("$.studentCode").value("SV001"))
                .andExpect(jsonPath("$.courses.length()").value(2));

        // Bình đăng ký 1 môn
        mockMvc.perform(get("/api/students/{id}/courses", binhId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses.length()").value(1));
    }

    @Test
    @DisplayName("BTVN: đăng ký trùng môn -> 409 Conflict")
    void dangKyTrungMonHoc() throws Exception {
        mockMvc.perform(post("/api/students/{studentId}/enroll/{courseId}", anId, javaId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/students/{studentId}/enroll/{courseId}", anId, javaId))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("BTVN: sinh viên hoặc môn học không tồn tại -> 404 Not Found")
    void dangKyVoiIdKhongTonTai() throws Exception {
        mockMvc.perform(post("/api/students/{studentId}/enroll/{courseId}", 999999L, javaId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("sinh viên")));

        mockMvc.perform(post("/api/students/{studentId}/enroll/{courseId}", anId, 999999L))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("môn học")));
    }

    @Test
    @DisplayName("BTVN: hủy đăng ký -> xóa dòng khỏi bảng student_course")
    void huyDangKyHocPhan() throws Exception {
        mockMvc.perform(post("/api/students/{studentId}/enroll/{courseId}", anId, javaId))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/students/{studentId}/enroll/{courseId}", anId, javaId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/students/{id}/courses", anId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses.length()").value(0));
    }
}
