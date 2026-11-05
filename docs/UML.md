# UML khớp mã nguồn

Các sơ đồ chọn thuộc tính/phương thức chính để giữ dễ đọc, không liệt kê mọi getter hay lớp Swing. Không đặt enrolled trong CourseSection; availableSeats ở RegistrationService.

- `users.puml`: User/Student/Admin và Account.
- `domain.puml`: association, composition và bội số nghiệp vụ.
- `strategy.puml`: interface tìm kiếm, hai implementation và dependency từ catalog.
- `observer.puml`: interface thông báo, hai observer và RegistrationService.

Mũi tên tam giác rỗng là kế thừa/implements; hình thoi đen là composition; thoi trắng ở danh sách observer là aggregation. Lịch được tạo bản sao riêng trong constructor/update CourseSection, còn Student và Registration không có quan hệ composition vì lịch sử cần được giữ.

Service nhận phiên đăng nhập qua AuthService, nên register/drop không nhận Student do UI tự truyền. Học kỳ được kiểm tra tại CourseSection.conflictsWith trước khi so từng Schedule.

Để dựng lại PNG, dùng PlantUML hỗ trợ Smetana: `java -jar plantuml.jar docs/*.puml`. Project chạy không cần PlantUML.
