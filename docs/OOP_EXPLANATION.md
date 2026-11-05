# Giải thích OOP theo mã nguồn thực tế

## Đóng gói và thiết kế lớp

Fields đều private. `Course.update`, `CourseSection.update`, `Schedule` kiểm tra dữ liệu trước khi gán để tránh cập nhật một phần khi lỗi. List/Set trả về là bản sao bất biến; không thể gọi clear để phá store hay kết quả môn đã hoàn thành. Model có public behavior; kiểm tra quyền và điều kiện toàn cục thuộc service.

## Các quan hệ

- Association: Registration giữ Student và CourseSection, mỗi registration thuộc đúng một sinh viên và một lớp.
- CourseSection tham chiếu Course, Semester, Classroom; các đối tượng này dùng chung, không bị xóa khi xóa lớp.
- Composition: CourseSection tạo bản sao các Schedule truyền vào. Lịch là giá trị bất biến thuộc riêng lớp đó, danh sách không bị chỉnh từ ngoài. Xóa lớp không có lịch sử làm các lịch của nó không còn được giữ bởi store; Java thu gom khi không còn tham chiếu. Không dùng delete thủ công như C++.
- Inheritance: Student và Admin extends abstract User. Không dùng Student extends Account vì sinh viên không phải tài khoản.
- Polymorphism: các User trong store được gọi getRole; Java chọn override của Student/Admin. Hai strategy và các observer cũng được gọi qua interface.

## SOLID

| Nguyên lý | Vị trí | Giải thích |
|---|---|---|
| SRP | model, service, ui | Model giữ dữ liệu và invariant; RegistrationService điều phối đăng ký; DesktopApp trình bày và nhận thao tác |
| OCP | CourseCatalogService.search | Vòng lọc gọi strategy.matches; thêm chiến lược mới không sửa vòng lọc |
| LSP | User, Student, Admin | Cả hai lớp con thực hiện đúng hợp đồng getRole; không ghi đè phương thức bằng lỗi UnsupportedOperation |
| ISP | CourseSearchStrategy, RegistrationObserver | Mỗi interface chỉ có một thao tác phù hợp; lớp tìm kiếm không phải cài thông báo |
| DIP | CourseCatalogService, RegistrationService | Phụ thuộc CourseSearchStrategy và RegistrationObserver, không gọi implementation cụ thể trong service |

DIP được minh họa ở các điểm biến đổi trên; bản nhỏ vẫn phụ thuộc InMemoryStore cụ thể, không tuyên bố mọi dependency trong ứng dụng đều đã trừu tượng hóa.

## Strategy và Observer

`CourseSearchStrategy.matches(Course,String)` có SearchByCodeStrategy và SearchByNameStrategy. ComboBox chọn strategy, catalog chỉ biết interface. Một if ở nơi chọn đối tượng là bình thường; vòng lọc tìm kiếm không chứa if theo loại thuật toán.

`RegistrationService` giữ danh sách RegistrationObserver, có addObserver/removeObserver. Sau khi ghi đăng ký ACTIVE hoặc chuyển CANCELLED, publish phát RegistrationEvent. NotificationObserver cập nhật nội dung thông báo, StatisticsObserver gọi callback làm mới thống kê. Lỗi điều kiện không phát sự kiện. Observer lỗi được ghi stderr và không làm giao dịch đã commit bị báo thất bại; observer sau vẫn được gọi.

## Collections generics exceptions

`List<Course>`, `List<Registration>`, `Set<String> completedCourseIds`, `Map<String,Integer>` thống kê. Generic giúp compiler kiểm tra kiểu. RegistrationException biểu diễn lớp đầy/trùng/thiếu tiên quyết/quá hạn; SecurityException cho đăng nhập và phân quyền; IllegalArgumentException cho dữ liệu biểu mẫu sai. Swing bắt lỗi nghiệp vụ và hiển thị JOptionPane.

## Nhất quán và thời gian

RegistrationService kiểm tra và ghi trong synchronized(store). Hai service trên cùng store vẫn dùng chung khóa, nên không tranh chỗ cuối vượt capacity. availableSeats = capacity - activeCount, không có enrolled độc lập. Clock được truyền vào service; JUnit dùng ngày cố định để kiểm thử hạn hủy ổn định. Hai lịch giao nhau nếu cùng thứ và start1 < end2 && start2 < end1; CourseSection kiểm tra thêm học kỳ.

## Refactoring có bằng chứng

Đã thực hiện Extract Method từ register sang validateRegistration sau lần chạy 29 test thành công; chạy lại 29 test vẫn thành công. `docs/refactoring-before.txt` là bản trước; `docs/refactoring.patch` ghi thay đổi. Patch được lưu trước bước định dạng toàn bộ code. Không coi thiết kế đề xuất trong tài liệu cũ là code đã từng chạy.

## Câu hỏi cần trả lời được

1. Vì sao Course khác CourseSection? Môn là danh mục; một môn có nhiều lớp theo học kỳ, lịch và sĩ số khác nhau.
2. Vì sao giữ CANCELLED? Để giữ lịch sử và chỉ đếm ACTIVE khi tính chỗ.
3. Vì sao cần service? Điều kiện liên quan nhiều entity cần một nơi điều phối, GUI không tự tạo đăng ký.
4. Vì sao không chỉ kiểm tra quyền bằng ẩn nút? Vì service còn có thể được gọi trực tiếp; test kiểm tra Student bị chặn tại service.
5. Vì sao Clock tốt hơn LocalDate.now trực tiếp trong test? Test không đổi kết quả theo ngày chạy.
6. Vì sao SOLID không có nghĩa càng nhiều interface càng tốt? Chỉ trừu tượng hóa nơi có nhu cầu thay đổi cụ thể.
7. synchronized có hỗ trợ nhiều máy không? Không; chỉ bảo vệ các luồng cùng JVM và cùng store. Đây là app desktop dữ liệu trong bộ nhớ.
