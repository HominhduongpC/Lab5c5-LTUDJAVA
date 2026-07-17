package vn.edu.gdu.springjpalab.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.gdu.springjpalab.entity.Category;
import vn.edu.gdu.springjpalab.repository.CategoryRepository;

import java.util.List;

/**
 * Chương 5 - Bài 3: API quản lý danh mục (quan hệ Một - Nhiều với Product).
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // ── 1. Lấy danh sách tất cả danh mục ──
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // ── 2. Lấy danh mục theo ID ──
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── 3. Thêm mới danh mục ──
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryRepository.save(category));
    }

    // ── 4. Xóa danh mục (cascade = ALL nên xóa luôn sản phẩm thuộc danh mục) ──
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ── 5. Đếm tổng số danh mục ──
    @GetMapping("/count")
    public long countCategories() {
        return categoryRepository.count();
    }
}
