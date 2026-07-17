package vn.edu.gdu.springjpalab.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import vn.edu.gdu.springjpalab.entity.Category;
import vn.edu.gdu.springjpalab.entity.Product;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kiểm thử Bài 1 (Phân trang & Sắp xếp) và Bài 2 (Derived Query, @Query, @Modifying).
 */
@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        Category laptop = categoryRepository.save(new Category("Laptop"));
        Category dienThoai = categoryRepository.save(new Category("Dien thoai"));

        productRepository.saveAll(List.of(
                new Product("Dell XPS 13", new BigDecimal("32000000"), "DELL-XPS13", laptop),
                new Product("Dell Inspiron 15", new BigDecimal("18500000"), "DELL-INS15", laptop),
                new Product("Dell Latitude 7440", new BigDecimal("38000000"), "DELL-LAT7440", laptop),
                new Product("MacBook Pro 14", new BigDecimal("48000000"), "MBP-14", laptop),
                new Product("iPhone 15 Pro Max", new BigDecimal("34990000"), "IP15PM", dienThoai)
        ));
    }

    // ── Bài 1: Phân trang và sắp xếp ──

    @Test
    @DisplayName("Bài 1: findAll(Pageable) trả về đúng trang, đúng kích thước và metadata")
    void phanTrangTraVeDungMetadata() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("price").descending());

        Page<Product> result = productRepository.findAll(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3); // 5 bản ghi / 2 mỗi trang = 3 trang
        assertThat(result.getNumber()).isZero();
        assertThat(result.hasNext()).isTrue();
        // Sắp xếp giá giảm dần -> trang 0 phải là 2 sản phẩm đắt nhất (48tr và 38tr)
        assertThat(result.getContent().get(0).getName()).isEqualTo("MacBook Pro 14");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Dell Latitude 7440");
    }

    @Test
    @DisplayName("Bài 1: trang cuối chỉ còn 1 bản ghi và hasNext() = false")
    void trangCuoiCungKhongConTrangKeTiep() {
        Page<Product> lastPage = productRepository.findAll(
                PageRequest.of(2, 2, Sort.by("price").descending()));

        assertThat(lastPage.getContent()).hasSize(1);
        assertThat(lastPage.hasNext()).isFalse();
        assertThat(lastPage.isLast()).isTrue();
    }

    @Test
    @DisplayName("Bài 1: Sort.by(\"price\").ascending() sắp xếp tăng dần")
    void sapXepGiaTangDan() {
        List<Product> sorted = productRepository.findAll(Sort.by("price").ascending());

        assertThat(sorted).extracting(Product::getName)
                .containsExactly("Dell Inspiron 15", "Dell XPS 13", "iPhone 15 Pro Max",
                        "Dell Latitude 7440", "MacBook Pro 14");
    }

    // ── Bài 2: Derived Query Methods ──

    @Test
    @DisplayName("Bài 2.1: findByNameContainingIgnoreCase tìm được cả chữ hoa lẫn chữ thường")
    void timKiemTheoTenKhongPhanBietHoaThuong() {
        assertThat(productRepository.findByNameContainingIgnoreCase("Dell")).hasSize(3);
        assertThat(productRepository.findByNameContainingIgnoreCase("dell")).hasSize(3);
        assertThat(productRepository.findByNameContainingIgnoreCase("DELL")).hasSize(3);
        assertThat(productRepository.findByNameContainingIgnoreCase("Nokia")).isEmpty();
    }

    @Test
    @DisplayName("Bài 2.2: findByPriceBetween lọc đúng khoảng giá [min, max]")
    void locTheoKhoangGia() {
        List<Product> result = productRepository.findByPriceBetween(
                new BigDecimal("30000000"), new BigDecimal("40000000"));

        assertThat(result).extracting(Product::getName)
                .containsExactlyInAnyOrder("Dell XPS 13", "Dell Latitude 7440", "iPhone 15 Pro Max");
    }

    // ── Bài 2: @Query JPQL & Native SQL ──

    @Test
    @DisplayName("Bài 2.3: @Query JPQL findExpensiveProducts chỉ lấy sản phẩm giá > minPrice")
    void jpqlTimSanPhamDatTien() {
        List<Product> result = productRepository.findExpensiveProducts(new BigDecimal("34000000"));

        // 34.99tr, 38tr, 48tr > 34tr; loại Dell XPS (32tr) và Dell Inspiron (18.5tr)
        assertThat(result).extracting(Product::getName)
                .containsExactlyInAnyOrder("MacBook Pro 14", "Dell Latitude 7440", "iPhone 15 Pro Max");
    }

    @Test
    @DisplayName("Bài 2.4: @Query Native SQL findTop3ExpensiveProducts lấy đúng 3 sản phẩm đắt nhất")
    void nativeQueryTop3SanPhamDatNhat() {
        List<Product> result = productRepository.findTop3ExpensiveProducts();

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Product::getName)
                .containsExactly("MacBook Pro 14", "Dell Latitude 7440", "iPhone 15 Pro Max");
    }

    // ── Bài 2: @Modifying ──

    @Test
    @DisplayName("Bài 2.5: @Modifying updateProductPrice tăng giá đúng tỷ lệ và trả về số dòng bị tác động")
    void modifyingCapNhatTangGia() {
        Product xps = productRepository.findByNameContainingIgnoreCase("Dell XPS 13").get(0);
        Long id = xps.getId();

        int updatedRows = productRepository.updateProductPrice(id, new BigDecimal("1.1"));

        assertThat(updatedRows).isEqualTo(1);

        // @Modifying ghi thẳng xuống DB, không đồng bộ Persistence Context.
        // Phải clear() cache trước, nếu không findById() sẽ trả về bản ghi cũ trong bộ nhớ.
        entityManager.flush();
        entityManager.clear();
        Product reloaded = productRepository.findById(id).orElseThrow();
        assertThat(reloaded.getPrice()).isEqualByComparingTo(new BigDecimal("35200000")); // 32tr * 1.1
    }

    @Test
    @DisplayName("Bài 2.5: updateProductPrice trả về 0 khi ID không tồn tại")
    void modifyingTraVe0KhiKhongTimThayId() {
        assertThat(productRepository.updateProductPrice(999999L, new BigDecimal("1.1"))).isZero();
    }

    // ── Bài 3: quan hệ ManyToOne ──

    @Test
    @DisplayName("Bài 3: JPQL đi qua quan hệ ManyToOne p.category.name")
    void timSanPhamTheoTenDanhMuc() {
        assertThat(productRepository.findByCategoryName("Laptop")).hasSize(4);
        assertThat(productRepository.findByCategoryName("Dien thoai")).hasSize(1);
    }

    // ── Kế thừa từ Chương 4: trường description vẫn hoạt động ──

    @Test
    @DisplayName("Chương 4: trường description (câu 6 bổ sung) vẫn lưu và đọc được")
    void truongDescriptionVanHoatDong() {
        Product p = productRepository.findByNameContainingIgnoreCase("MacBook Pro 14").get(0);
        p.setDescription("Laptop cao cấp dành cho lập trình viên");
        productRepository.save(p);
        entityManager.flush();
        entityManager.clear();

        Product reloaded = productRepository.findById(p.getId()).orElseThrow();
        assertThat(reloaded.getDescription()).isEqualTo("Laptop cao cấp dành cho lập trình viên");
    }
}
