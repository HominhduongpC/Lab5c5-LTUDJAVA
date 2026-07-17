-- ===================================================================
-- Bước 4: Tạo cơ sở dữ liệu MySQL cho bài thực hành
-- ===================================================================

-- Tạo database mới cho bài thực hành
CREATE DATABASE IF NOT EXISTS gdu_jpa_lab
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Kiểm tra database đã tạo thành công
SHOW DATABASES LIKE 'gdu_jpa_lab';
