package vn.edu.gdu.springjpalab.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

/**
 * Chương 5 - Bài 3, Bước 2: quan hệ Một - Một giữa User và Profile.
 * User là Owning Side (bảng users giữ cột khóa ngoại profile_id).
 */
@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    // CascadeType.ALL: lưu User sẽ tự động lưu Profile đi kèm
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    private Profile profile;

    // ── Constructor không tham số (BẮT BUỘC cho JPA) ──
    protected User() {
    }

    // ── Constructor tiện lợi ──
    public User(String username, Profile profile) {
        this.username = username;
        setProfile(profile);
    }

    // ── Getter và Setter ──
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Profile getProfile() {
        return profile;
    }

    // Đồng bộ luôn chiều ngược lại để Profile.getUser() không bị null
    public void setProfile(Profile profile) {
        this.profile = profile;
        if (profile != null) {
            profile.setUser(this);
        }
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "'}";
    }
}
