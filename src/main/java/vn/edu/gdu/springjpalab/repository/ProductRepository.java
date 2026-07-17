package vn.edu.gdu.springjpalab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.gdu.springjpalab.entity.Product;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Spring Data JPA tự động cung cấp các phương thức CRUD:
    // - save(entity)   : Thêm mới hoặc cập nhật
    // - findById(id)   : Tìm theo khóa chính
    // - findAll()      : Lấy tất cả bản ghi
    // - deleteById(id) : Xóa theo khóa chính
    // - count()        : Đếm tổng số bản ghi
    // - existsById(id) : Kiểm tra tồn tại
    //
    // JpaRepository kế thừa gián tiếp PagingAndSortingRepository nên findAll(Pageable)
    // và findAll(Sort) đã có sẵn, không cần khai báo thêm (Chương 5 - Bài 1).

    // ══════════ CÁC TRUY VẤN NÂNG CAO (Chương 5 - Bài 2) ══════════

    // 1. Derived Query Method: Tìm kiếm theo tên sản phẩm (không phân biệt hoa thường)
    List<Product> findByNameContainingIgnoreCase(String keyword);

    // 2. Derived Query Method: Tìm sản phẩm có giá nằm trong khoảng [min, max]
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // 3. Custom Query (JPQL): Tìm sản phẩm có giá lớn hơn giá trị tối thiểu
    @Query("SELECT p FROM Product p WHERE p.price > :minPrice")
    List<Product> findExpensiveProducts(@Param("minPrice") BigDecimal minPrice);

    // 4. Custom Query (Native SQL): Lấy 3 sản phẩm có giá cao nhất
    @Query(value = "SELECT * FROM products ORDER BY price DESC LIMIT 3", nativeQuery = true)
    List<Product> findTop3ExpensiveProducts();

    // 5. Modifying & Custom Query: Cập nhật tăng giá sản phẩm hàng loạt theo tỷ lệ
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.price = p.price * :rate WHERE p.id = :id")
    int updateProductPrice(@Param("id") Long id, @Param("rate") BigDecimal rate);

    // 6. Custom Query (JPQL): Tìm sản phẩm theo tên danh mục (đi qua quan hệ @ManyToOne)
    @Query("SELECT p FROM Product p WHERE p.category.name = :categoryName")
    List<Product> findByCategoryName(@Param("categoryName") String categoryName);
}
