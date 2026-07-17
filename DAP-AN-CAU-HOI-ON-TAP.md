# Đáp án câu hỏi ôn tập — Chương 5

## Câu 1. `JpaRepository` kế thừa các interface nào? Khác biệt lớn nhất giữa `findAll()` của `CrudRepository` và của `JpaRepository`?

Theo tài liệu học phần: `JpaRepository` kế thừa `CrudRepository` và `PagingAndSortingRepository`
→ có đủ CRUD cơ bản cộng phân trang/sắp xếp, và bổ sung các phương thức đặc thù JPA
(`flush()`, `saveAndFlush()`, `deleteAllInBatch()`, `getReferenceById()`...).

Chính xác hơn ở phiên bản đang dùng (Spring Data JPA 3.5.x), khai báo thực tế là:

```java
public interface JpaRepository<T, ID>
        extends ListCrudRepository<T, ID>,
                ListPagingAndSortingRepository<T, ID>,
                QueryByExampleExecutor<T>
```

Trong đó `ListCrudRepository` kế thừa `CrudRepository` và `ListPagingAndSortingRepository`
kế thừa `PagingAndSortingRepository` — nên hai interface trong sách vẫn nằm trong cây kế thừa,
chỉ là qua một tầng trung gian mới.

**Khác biệt lớn nhất giữa hai `findAll()`:** `CrudRepository.findAll()` trả về `Iterable<T>` —
chỉ duyệt tuần tự được; còn `JpaRepository.findAll()` trả về `List<T>`, dùng được ngay
`size()`, `get(i)`, `stream()`, `sort()`... mà không phải chuyển đổi thủ công. Chính tầng
`ListCrudRepository` là nơi ghi đè kiểu trả về này.

## Câu 2. Viết câu SQL mô phỏng có `LIMIT` và `OFFSET` khi client yêu cầu `page = 2`, `size = 5`

Trang đánh số từ 0, nên `OFFSET = page * size = 2 * 5 = 10`:

```sql
SELECT * FROM products ORDER BY price ASC LIMIT 5 OFFSET 10;
```

Tức là bỏ qua 10 bản ghi đầu, lấy 5 bản ghi tiếp theo (bản ghi thứ 11 đến 15).

## Câu 3. Lỗi N+1 query là gì? Tại sao `FetchType.EAGER` cho `@OneToMany` là nguyên nhân chính?

**N+1** là tình huống để lấy dữ liệu cha kèm con, CSDL phải chạy **1** câu SELECT lấy N bản ghi
cha, rồi thêm **N** câu SELECT phụ — mỗi câu lấy collection con của một cha → tổng cộng N+1 câu.
Với 100 danh mục là 101 lượt gọi DB thay vì 1.

`@OneToMany` để EAGER buộc Hibernate phải nạp collection con **ngay tại thời điểm nạp cha**.
Khi câu truy vấn cha trả về nhiều dòng (ví dụ `findAll()`), Hibernate không thể gộp tất cả vào
một JOIN cho mọi trường hợp, nên mặc định phát sinh một câu SELECT riêng cho collection của
từng đối tượng cha. Tai hại hơn: EAGER là cấu hình **tĩnh** ở tầng entity — mọi truy vấn đều
gánh chi phí này kể cả khi ta không hề đụng tới dữ liệu con.

**Cách xử lý đúng:** luôn để LAZY, và khi thật sự cần dữ liệu con thì nạp có chủ đích bằng
`JOIN FETCH` hoặc `@EntityGraph` — gộp tất cả về 1 câu SELECT. Xem
`StudentRepository.findByIdWithCourses()` trong dự án này:

```java
@Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.courses WHERE s.id = :id")
Optional<Student> findByIdWithCourses(@Param("id") Long id);
```

## Câu 4. `mappedBy` có tác dụng gì? Quên khai báo thì Hibernate xử lý thế nào?

`mappedBy` khai báo rằng phía này là **Inverse Side** — nó chỉ *soi chiếu* lại một quan hệ đã
được ánh xạ sẵn ở phía kia, chứ không tự quản lý khóa ngoại. Giá trị truyền vào là tên thuộc
tính bên Owning Side (ví dụ `mappedBy = "category"` trỏ tới trường `category` của `Product`).

Nếu **quên** `mappedBy`, JPA sẽ hiểu nhầm rằng đây là **hai quan hệ một chiều độc lập** thay vì
một quan hệ hai chiều. Hậu quả dưới CSDL: Hibernate sinh thêm một **bảng trung gian** (ví dụ
`categories_products`) để chứa quan hệ của phía `@OneToMany`, song song với cột khóa ngoại
`category_id` đã có bên `products`. Dữ liệu bị lưu ở hai nơi, schema dư thừa, và các thao tác
ghi phát sinh thêm câu INSERT/DELETE không cần thiết vào bảng trung gian đó.
