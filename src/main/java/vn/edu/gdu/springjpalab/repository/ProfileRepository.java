package vn.edu.gdu.springjpalab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.gdu.springjpalab.entity.Profile;

/**
 * Chương 5 - Bài tập về nhà, mục 1.
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
