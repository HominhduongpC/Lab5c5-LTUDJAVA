# Chương 5 — Lập trình CSDL với Spring Data JPA (Nâng cao)

- **Học phần:** Lập trình ứng dụng với Java (14113014)
- **Project:** `ch5-lab-spring-jpa-advanced` — package gốc `vn.edu.gdu.springjpalab`
- **Kế thừa từ:** dự án `ch4-lab-spring-jpa` của Chương 4 ([Lab4-UDJAVA](https://github.com/HominhduongpC/Lab4-UDJAVA))

Toàn bộ mã nguồn Chương 4 được giữ nguyên (entity `Product` với trường `description` của
câu 6 bổ sung, entity `Student` với `studentCode`/`gpa`/`enrollmentDate`, các API CRUD 1–6,
cấu hình MySQL). Chương 5 chỉ **bổ sung** lên trên.

## 1. Những gì Chương 5 thêm vào

| Tệp | Thay đổi |
|---|---|
| `entity/Product.java` | + `@ManyToOne` tới `Category`, + constructor có Category, + `@PrePersist` |
| `entity/Student.java` | + `@ManyToMany` tới `Course` qua bảng `student_course`, + `enrollInCourse()` |
| `entity/Category.java` | **mới** — Một-Nhiều (Inverse Side) |
| `entity/User.java`, `entity/Profile.java` | **mới** — Một-Một |
| `entity/Course.java` | **mới** — Nhiều-Nhiều (Inverse Side) |
| `repository/ProductRepository.java` | + Derived Query, JPQL, Native SQL, `@Modifying` |
| `repository/StudentRepository.java` | + `findByEmail`, + `findByIdWithCourses` (JOIN FETCH) |
| `repository/` (4 tệp mới) | `CategoryRepository`, `UserRepository`, `ProfileRepository`, `CourseRepository` |
| `controller/ProductController.java` | + `/page` và API 7–12 |
| `controller/StudentController.java` | + API 7–9 (đăng ký / hủy đăng ký học phần) |
| `controller/` (3 tệp mới) | `CategoryController`, `CourseController`, `UserController` |
| `config/DataSeeder.java` | **mới** — nạp dữ liệu mẫu để các lệnh cURL trong bài chạy được ngay |

Bảng sinh ra dưới CSDL `gdu_jpa_lab`: `products`, `students` (đã có từ Chương 4, `products`
được bổ sung cột `category_id`) cùng 5 bảng mới `categories`, `users`, `profiles`, `courses`,
`student_course`.

## 2. Chạy dự án

CSDL dùng lại đúng container MySQL của Chương 4 (`gdu_jpa_mysql`, cổng 3306, CSDL `gdu_jpa_lab`).

```powershell
# 1. Bật Docker Desktop rồi dựng MySQL (nếu container chưa chạy)
docker compose up -d

# 2. Chạy ứng dụng bằng Maven wrapper có sẵn trong dự án
.\mvnw.cmd spring-boot:run
```

Ứng dụng chạy ở http://localhost:8080. Hibernate tự sinh/bổ sung bảng nhờ
`spring.jpa.hibernate.ddl-auto=update`, nên dữ liệu Chương 4 không bị mất.

Nếu `JAVA_HOME` chưa được đặt:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
```

**Chạy test** (dùng CSDL H2 trong bộ nhớ, **không cần MySQL, không cần Docker**):

```powershell
.\mvnw.cmd test
```

Xem CSDL trực tiếp:

```powershell
docker exec -it gdu_jpa_mysql mysql -uroot -pDao666999 -D gdu_jpa_lab
```

> Nếu chạy `mysql` trong PowerShell mà tiếng Việt hiển thị vỡ (`Tr?n Th? B�nh`), đó chỉ là do
> bảng mã của cửa sổ console, **không phải dữ liệu lưu sai** — bảng đang dùng
> `utf8mb4_unicode_ci` và API trả JSON tiếng Việt hoàn toàn bình thường.

## 3. Bài 1 — Phân trang và Sắp xếp

`GET /api/products/page`

| Tham số | Mặc định | Ý nghĩa |
|---|---|---|
| `page` | 0 | Số trang, bắt đầu từ 0 |
| `size` | 10 | Số bản ghi mỗi trang |
| `sortBy` | price | Tên thuộc tính dùng để sắp xếp |
| `sortDir` | asc | `asc` hoặc `desc` |

```bash
curl "http://localhost:8080/api/products/page?page=0&size=2&sortBy=price&sortDir=desc"
```

JSON trả về ngoài `content` còn có metadata: `totalPages`, `totalElements`, `size`, `number`.

`ProductRepository` kế thừa `JpaRepository`, vốn đã gián tiếp kế thừa
`PagingAndSortingRepository`, nên `findAll(Pageable)` có sẵn mà không cần khai báo thêm.

## 4. Bài 2 — Derived Query và @Query

| API | Kỹ thuật | Lệnh thử |
|---|---|---|
| `GET /api/products/search?keyword=Dell` | Derived Query | `curl "http://localhost:8080/api/products/search?keyword=Dell"` |
| `GET /api/products/price-range?min=&max=` | Derived Query | `curl "http://localhost:8080/api/products/price-range?min=30000000&max=40000000"` |
| `GET /api/products/expensive?minPrice=` | @Query JPQL | `curl "http://localhost:8080/api/products/expensive?minPrice=30000000"` |
| `GET /api/products/top3` | @Query Native SQL | `curl "http://localhost:8080/api/products/top3"` |
| `PUT /api/products/{id}/increase-price?rate=` | @Modifying + @Transactional | `curl -X PUT "http://localhost:8080/api/products/1/increase-price?rate=1.1"` |
| `GET /api/products/by-category?categoryName=` | JPQL qua quan hệ | `curl "http://localhost:8080/api/products/by-category?categoryName=Laptop"` |

**Quan sát Console log:** với `/expensive` Hibernate sinh SQL dịch từ JPQL; với `/top3` Hibernate
giữ nguyên câu Native SQL `SELECT * FROM products ORDER BY price DESC LIMIT 3`.

## 5. Bài 3 — Các mối quan hệ bảng

| Quan hệ | Owning Side (giữ khóa ngoại) | Inverse Side (`mappedBy`) | Fetch |
|---|---|---|---|
| Một-Nhiều | `Product.category` → cột `category_id` | `Category.products` | LAZY |
| Một-Một | `User.profile` → cột `profile_id` | `Profile.user` | LAZY |
| Nhiều-Nhiều | `Student.courses` → bảng `student_course` | `Course.students` | LAZY |

```sql
USE gdu_jpa_lab;
SHOW TABLES;
DESCRIBE student_course;
```

**Mọi quan hệ đều đặt `FetchType.LAZY`** theo đúng khuyến cáo hiệu năng của bài lab, kể cả
`@ManyToOne`/`@OneToOne` (vốn mặc định là EAGER).

## 6. Bài tập về nhà — Đăng ký học phần

```bash
# Tạo sinh viên
curl -X POST http://localhost:8080/api/students -H "Content-Type: application/json" \
     -d '{"studentCode":"SV003","fullName":"Le Van Cuong","email":"cuong@gdu.edu.vn","gpa":3.1,"enrollmentDate":"2023-09-05"}'

# Tạo môn học
curl -X POST http://localhost:8080/api/courses -H "Content-Type: application/json" \
     -d '{"title":"Lập trình ứng dụng với Java"}'

# Đăng ký học phần
curl -X POST http://localhost:8080/api/students/1/enroll/1

# Xem sinh viên kèm môn đã đăng ký (JOIN FETCH, tránh N+1)
curl http://localhost:8080/api/students/1/courses

# Hủy đăng ký
curl -X DELETE http://localhost:8080/api/students/1/enroll/1
```

Kiểm tra bảng trung gian:

```sql
SELECT s.full_name, c.course_title
FROM student_course sc
JOIN students s ON s.id = sc.student_id
JOIN courses  c ON c.id = sc.course_id;
```

Đăng ký trùng trả về **409 Conflict**; sinh viên/môn học không tồn tại trả về **404 Not Found**.

## 7. Câu hỏi ôn tập

Xem [DAP-AN-CAU-HOI-ON-TAP.md](DAP-AN-CAU-HOI-ON-TAP.md).

## 8. Kiểm thử tự động

33 test chạy trên CSDL H2 trong bộ nhớ (không cần MySQL):

| Lớp test | Phủ |
|---|---|
| `ProductRepositoryTest` | Phân trang, metadata, sắp xếp, derived query, JPQL, Native SQL, `@Modifying`, trường `description` của Ch4 |
| `RelationshipTest` | Một-Nhiều + `orphanRemoval`, Một-Một + cascade, Nhiều-Nhiều + bảng `student_course`, `JOIN FETCH`, các trường Ch4 của Student |
| `ProductApiTest` | Toàn bộ REST API Bài 1 & Bài 2 qua tầng HTTP + CRUD Ch4 |
| `StudentEnrollApiTest` | API đăng ký/hủy học phần, 409 khi trùng, 404 khi sai ID |
| `SpringJpaLabApplicationTests` | Context Spring nạp được |

`DataSeeder` bị tắt khi chạy test (`@Profile("!test")`) để dữ liệu mẫu không ảnh hưởng kết quả.
