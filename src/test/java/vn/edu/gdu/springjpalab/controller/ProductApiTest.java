package vn.edu.gdu.springjpalab.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.gdu.springjpalab.entity.Category;
import vn.edu.gdu.springjpalab.entity.Product;
import vn.edu.gdu.springjpalab.repository.CategoryRepository;
import vn.edu.gdu.springjpalab.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Kiểm thử các REST API của Bài 1 và Bài 2 qua tầng HTTP.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Long xpsId;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        Category laptop = categoryRepository.save(new Category("Laptop"));
        List<Product> saved = productRepository.saveAll(List.of(
                new Product("Dell XPS 13", new BigDecimal("32000000"), "DELL-XPS13", laptop),
                new Product("Dell Inspiron 15", new BigDecimal("18500000"), "DELL-INS15", laptop),
                new Product("Dell Latitude 7440", new BigDecimal("38000000"), "DELL-LAT7440", laptop),
                new Product("MacBook Pro 14", new BigDecimal("48000000"), "MBP-14", laptop)
        ));
        xpsId = saved.get(0).getId();
    }

    @Test
    @DisplayName("Bài 1: GET /api/products/page?page=0&size=2&sortBy=price&sortDir=desc")
    void apiPhanTrangTraVeContentVaMetadata() throws Exception {
        mockMvc.perform(get("/api/products/page")
                        .param("page", "0").param("size", "2")
                        .param("sortBy", "price").param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.content[0].name").value("MacBook Pro 14"));
    }

    @Test
    @DisplayName("Bài 1: /page dùng tham số mặc định khi client không truyền gì")
    void apiPhanTrangDungThamSoMacDinh() throws Exception {
        mockMvc.perform(get("/api/products/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.content[0].name").value("Dell Inspiron 15")); // price asc
    }

    @Test
    @DisplayName("Bài 2: GET /api/products/search?keyword=Dell")
    void apiTimKiemTheoTuKhoa() throws Exception {
        mockMvc.perform(get("/api/products/search").param("keyword", "Dell"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @DisplayName("Bài 2: GET /api/products/price-range?min=30000000&max=40000000")
    void apiLocTheoKhoangGia() throws Exception {
        mockMvc.perform(get("/api/products/price-range")
                        .param("min", "30000000").param("max", "40000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Bài 2: GET /api/products/expensive?minPrice=35000000 (JPQL)")
    void apiSanPhamDatTien() throws Exception {
        mockMvc.perform(get("/api/products/expensive").param("minPrice", "35000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Bài 2: GET /api/products/top3 (Native SQL)")
    void apiTop3SanPhamDatNhat() throws Exception {
        mockMvc.perform(get("/api/products/top3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("MacBook Pro 14"));
    }

    @Test
    @DisplayName("Bài 2: PUT /api/products/{id}/increase-price?rate=1.1 (@Modifying)")
    void apiTangGiaSanPham() throws Exception {
        mockMvc.perform(put("/api/products/{id}/increase-price", xpsId).param("rate", "1.1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("thành công")));

        mockMvc.perform(get("/api/products/{id}", xpsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(35200000.00));
    }

    @Test
    @DisplayName("Bài 2: PUT increase-price trả về 404 khi ID không tồn tại")
    void apiTangGiaSanPhamKhongTonTai() throws Exception {
        mockMvc.perform(put("/api/products/{id}/increase-price", 999999L).param("rate", "1.1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Bài 3: GET /api/products/by-category?categoryName=Laptop")
    void apiLaySanPhamTheoDanhMuc() throws Exception {
        mockMvc.perform(get("/api/products/by-category").param("categoryName", "Laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    @DisplayName("Chương 4: POST tạo sản phẩm mới -> createdAt được @PrePersist tự điền, không null")
    void apiTaoSanPhamTuDienCreatedAt() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"San pham test\",\"price\":9999000,\"sku\":\"TEST-001\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("San pham test"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @DisplayName("Chương 4: CRUD cơ bản vẫn hoạt động (GET all, GET by id, DELETE, count)")
    void apiCrudCoBan() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));

        mockMvc.perform(get("/api/products/{id}", xpsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dell XPS 13"))
                .andExpect(jsonPath("$.sku").value("DELL-XPS13"));

        mockMvc.perform(get("/api/products/{id}", 999999L))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/products/{id}", xpsId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }
}
