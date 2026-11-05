# Kịch bản demo và ôn bảo vệ

Đây là tài liệu hỗ trợ tập luyện; báo cáo nộp là một Google Docs tổng hợp duy nhất.

## Demo 10 đến 15 phút

1. Chạy run.bat; nói phạm vi Student/Admin, dữ liệu trong bộ nhớ (1 phút).
2. Đăng nhập sv01/student123. Tìm JAVA theo mã, chọn JAVA-01 và đăng ký (1 phút).
3. Tìm MATH, chọn MATH-01 để thấy lỗi trùng lịch. Tìm NET, đăng ký NET-01 để chứng minh hai buổi sát nhau được phép (2 phút).
4. Tìm DB, đăng ký DB-FULL để thấy lớp đầy. Với sv02, thử OOP-01 để thấy thiếu JAVA; sv01 đã có JAVA nên đăng ký OOP-01 được (2 phút).
5. Vào lịch sử và lịch học. Hủy JAVA-01; xem CANCELLED, chỗ được trả; thử hủy JAVA-OLD để thấy quá hạn. Hủy lần hai bị từ chối (2 phút).
6. Đăng xuất rồi vào admin/admin123. Xem thống kê; thêm môn TMP, thêm lớp TMP-01, sửa rồi xóa lớp và môn khi chưa có lịch sử (2 phút).
7. Chỉ vào User/Student/Admin, CourseSearchStrategy, RegistrationObserver và RegistrationService.validateRegistration. Giải thích SOLID và test lastSeatIsAtomicAcrossServices (2 phút).

Giữ nguyên một phiên chạy khi đổi tài khoản để quan sát dữ liệu. Khởi động lại ứng dụng sẽ đặt lại dữ liệu demo.

## Phân phần ôn tập cho bốn người

Đây là gợi ý phân phần thuyết trình, không phải bảng khẳng định ai đã viết code.
- Người 1: bài toán, use case, Course/CourseSection/Registration và dữ liệu mẫu.
- Người 2: User, Account, encapsulation, association/composition, generics.
- Người 3: RegistrationService, Schedule, prerequisite, Clock, khóa store và kiểm thử.
- Người 4: Swing, Strategy, Observer, SOLID và demo Admin.

Mỗi người vẫn cần chạy được toàn bộ demo và giải thích vì sao đăng ký thất bại không làm thay đổi số chỗ.
