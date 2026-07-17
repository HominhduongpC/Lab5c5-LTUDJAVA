package vn.edu.gdu.springjpalab.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.gdu.springjpalab.entity.User;
import vn.edu.gdu.springjpalab.repository.UserRepository;

import java.util.List;

/**
 * Chương 5 - Bài 3: API quản lý người dùng (quan hệ Một - Một với Profile).
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ── 1. Lấy danh sách tất cả người dùng ──
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ── 2. Lấy người dùng theo ID (kèm Profile) ──
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── 3. Thêm mới người dùng (CascadeType.ALL nên Profile được lưu kèm) ──
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userRepository.save(user));
    }

    // ── 4. Xóa người dùng ──
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ── 5. Đếm tổng số người dùng ──
    @GetMapping("/count")
    public long countUsers() {
        return userRepository.count();
    }
}
