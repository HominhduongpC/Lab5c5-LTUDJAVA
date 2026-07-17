package vn.edu.gdu.springjpalab.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Chương 5 - Bài 3, Bước 1: quan hệ Một - Nhiều giữa Category và Product.
 * Category là Inverse Side (không giữ khóa ngoại).
 */
@Entity
@Table(name = "categories")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_name", nullable = false, unique = true, length = 100)
    private String name;

    // mappedBy = "category" trỏ tới tên thuộc tính bên Product (Owning Side)
    // @JsonIgnore để tránh vòng lặp vô tận khi trả JSON: Category -> products -> category -> ...
    @JsonIgnore
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    // ── Constructor không tham số (BẮT BUỘC cho JPA) ──
    protected Category() {
    }

    // ── Constructor tiện lợi ──
    public Category(String name) {
        this.name = name;
    }

    // ── Getter và Setter ──
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    // ── Hàm tiện ích đồng bộ cả hai phía của quan hệ hai chiều ──
    public void addProduct(Product product) {
        this.products.add(product);
        product.setCategory(this);
    }

    public void removeProduct(Product product) {
        this.products.remove(product);
        product.setCategory(null);
    }

    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "'}";
    }
}
