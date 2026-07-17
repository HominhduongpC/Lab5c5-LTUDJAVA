package vn.edu.gdu.springjpalab.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

/**
 * Chương 5 - Bài 3, Bước 2: quan hệ Một - Một giữa User và Profile.
 * Profile là Inverse Side (khóa ngoại profile_id nằm bên bảng users).
 */
@Entity
@Table(name = "profiles")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "avatar_url")
    private String avatarUrl;

    // @JsonIgnore để tránh vòng lặp vô tận: User -> profile -> user -> ...
    @JsonIgnore
    @OneToOne(mappedBy = "profile")
    private User user;

    // ── Constructor không tham số (BẮT BUỘC cho JPA) ──
    protected Profile() {
    }

    // ── Constructor tiện lợi ──
    public Profile(String bio, String avatarUrl) {
        this.bio = bio;
        this.avatarUrl = avatarUrl;
    }

    // ── Getter và Setter ──
    public Long getId() {
        return id;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Profile{id=" + id + ", bio='" + bio + "', avatarUrl='" + avatarUrl + "'}";
    }
}
