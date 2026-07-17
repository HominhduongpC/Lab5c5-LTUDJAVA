package vn.edu.gdu.springjpalab.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity // (1) Đánh dấu đây là Entity JPA
@Table(name = "products") // (2) Ánh xạ đến bảng "products" trong DB
// Bỏ qua 2 field kỹ thuật mà Hibernate gắn vào proxy LAZY, tránh lỗi khi Jackson sinh JSON
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {

    @Id // (3) Đánh dấu trường này là khóa chính
    @GeneratedValue(strategy = GenerationType.IDENTITY) // (4) Tự động tăng
    private Long id;

    @Column(name = "product_name", nullable = false, length = 150)
    private String name; // (5) Ánh xạ đến cột "product_name"

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // (6) Kiểu tiền tệ dùng BigDecimal

    @Column(name = "sku", unique = true, nullable = false, length = 50)
    private String sku; // (7) Mã sản phẩm - duy nhất

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ── Câu 6 (thực hành bổ sung): trường mô tả, tối đa 500 ký tự, có thể null ──
    @Column(name = "description", length = 500)
    private String description;

    // ── Bổ sung thuộc tính quan hệ (Chương 5 - Bài 3) ──
    // Owning Side: phía giữ khóa ngoại category_id.
    // LAZY để tránh lỗi N+1 query (mặc định của @ManyToOne là EAGER).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // ── 1. Constructor không tham số (BẮT BUỘC cho JPA) ──
    protected Product() {
    }

    // ── 2. Constructor có tham số (tiện lợi cho lập trình viên) ──
    public Product(String name, BigDecimal price, String sku) {
        this.name = name;
        this.price = price;
        this.sku = sku;
        this.createdAt = LocalDateTime.now();
    }

    // ── 3. Constructor có tham số để thiết lập Category (Chương 5) ──
    public Product(String name, BigDecimal price, String sku, Category category) {
        this.name = name;
        this.price = price;
        this.sku = sku;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }

    // Khi tạo Product từ JSON (POST /api/products), Jackson dùng constructor rỗng + setter
    // nên createdAt sẽ bị null. @PrePersist đảm bảo luôn có giá trị trước khi INSERT.
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // ── 3. Getter và Setter ──
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // ── Getter/Setter cho quan hệ Category (Chương 5) ──
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=" + price
                + ", sku='" + sku + "', createdAt=" + createdAt + "}";
    }
}
