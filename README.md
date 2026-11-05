# BTL OOP desktop — Nhóm 6

University Course Registration, đề tài 11. Java Swing + Maven + JUnit 5.
Ứng dụng nhỏ phục vụ học tập và bảo vệ OOP, chạy offline sau khi build.

## Làm việc nhóm qua GitHub

Đọc [CONTRIBUTING.md](CONTRIBUTING.md) để clone bằng IntelliJ, tạo nhánh và mở Pull Request. Mã nguồn và tài liệu được quản lý bằng Git; `target`, cấu hình IDE và JAR không đưa lên repository. Khi clone từ GitHub, chạy `mvn verify` để tạo JAR trước khi chạy ứng dụng.

## Chạy ngay trên máy hiện tại

Nhấp đúp **run.bat** trong thư mục project. Script dùng JAVA_HOME, hoặc tự tìm JDK NetBeans có sẵn trên máy này; không thay đổi biến hệ thống.
File `BTL_OOP_desktop.jar` ở bản local đã được build và không cần Maven để chạy. Cần **Java 17 trở lên**. Với bản clone từ GitHub, build rồi chạy `target/BTL_OOP_desktop-1.0.0.jar`.
`java` mặc định trên máy hiện tại là Java 8; đừng dùng Java 8 để chạy JAR.

Trong PowerShell có thể chạy trực tiếp:

```powershell
& 'C:\Program Files\Apache NetBeans\jdk\bin\java.exe' -jar .\BTL_OOP_desktop.jar
```

## Mở và chạy trong IntelliJ IDEA

1. File → Open → chọn thư mục `BTL_OOP_desktop` hoặc `pom.xml` → Open as Project.
2. Project Structure → Project SDK: chọn JDK 17+; máy hiện tại có JDK 25 tại `C:\Program Files\Apache NetBeans\jdk`.
3. Settings → Build Tools → Maven → Maven home: Bundled Maven; Maven Runner JRE: Project JDK.
4. Reload All Maven Projects. Lần đầu cần mạng để Maven tải JUnit và build plugins.
5. Mở `src/main/java/vn/nhom6/registration/Main.java`, nhấn Run tại `main`.
6. Chạy test bằng nút Run ở `RegistrationTest`, hoặc Maven `verify`.

Không cần GUI Designer hay plugin Swing; giao diện được viết bằng Java thông thường.

## Build trên Windows

```powershell
$env:JAVA_HOME = 'C:\Program Files\Apache NetBeans\jdk'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
mvn clean verify
java -jar target/BTL_OOP_desktop-1.0.0.jar
```

Nếu máy chưa có lệnh `mvn`, dùng Maven tích hợp IntelliJ (đường dẫn của máy này):

```powershell
& 'D:\Downloads\IntelliJ IDEA 2026.2.3\plugins\maven-plugin\lib\maven3\bin\mvn.cmd' clean verify
```

Maven tạo JAR trong `target`. Sau khi sửa code, chạy JAR mới trong target hoặc sao chép nó đè `BTL_OOP_desktop.jar` khi ứng dụng đã đóng.

## Tài khoản và dữ liệu demo

| Vai trò | Tài khoản | Mật khẩu | Dữ liệu |
|---|---|---|---|
| Student | sv01 | student123 | Đã hoàn thành JAVA, có đăng ký JAVA-OLD quá hạn hủy |
| Student | sv02 | student123 | Chưa hoàn thành JAVA, đã chiếm chỗ DB-FULL |
| Admin | admin | admin123 | Quản lý môn/lớp, sửa sinh viên, hủy đăng ký theo hạn, xem thống kê |

Đây là tài khoản giả. Account lưu PBKDF2 có salt, không lưu mật khẩu rõ.
Có 5 môn, 6 lớp, 2 học kỳ và 2 phòng. Hạn hủy dữ liệu demo tính theo ngày chạy: HK1-DEMO còn 30 ngày; HK-CU đã quá hạn 1 ngày.
Ngày trong lịch dùng T2 đến T8, trong đó T8 là Chủ nhật. Lịch học có thể gồm nhiều buổi.

## Phạm vi chức năng

Student: tạo tài khoản, đăng nhập, tìm mã/tên môn, xem môn/lớp, đăng ký/hủy, xem lịch và lịch sử.
Admin: thêm/sửa/xóa môn và lớp; xem/sửa tên, email, mã môn hoàn thành của sinh viên; xem/hủy đăng ký theo cùng hạn hủy; thống kê ACTIVE theo lớp.
Lớp đã có lịch sử đăng ký không được sửa hoặc xóa. Môn còn lớp, đang là tiên quyết hoặc có kết quả hoàn thành không được xóa.
Học kỳ và phòng là dữ liệu mẫu chỉ đọc; không có phân hệ quản lý mở rộng.

Quy tắc: đăng nhập đúng quyền; không trùng lớp ACTIVE; còn chỗ; đủ tiên quyết; không chồng giờ trong cùng học kỳ. Hủy chuyển CANCELLED, không xóa lịch sử, không hủy lần hai hoặc quá hạn.
Đăng ký/hủy và thay đổi quản trị dùng chung khóa store để giữ dữ liệu nhất quán. Số chỗ được tính từ ACTIVE.

## Cấu trúc

```text
src/main/java/vn/nhom6/registration/
  Main.java
  model/       10 entity, User, Checks và enum
  store/       InMemoryStore, DemoData
  service/     AuthService, CourseCatalogService, RegistrationService, AdminService
  strategy/    CourseSearchStrategy, SearchByCodeStrategy, SearchByNameStrategy
  observer/    RegistrationObserver, RegistrationEvent, NotificationObserver, StatisticsObserver
  exception/   RegistrationException
  ui/          DesktopApp
src/test/java/vn/nhom6/registration/RegistrationTest.java
docs/          UML, ảnh giao diện, log kiểm thử, bằng chứng refactor
```

## Kết quả đã chạy

- `30` file Java ứng dụng, `1858` dòng vật lý kể cả dòng trắng/chú thích.
- `1` file Java kiểm thử, `440` dòng.
- JDK 25 biên dịch `--release 17`; Maven `clean verify`: **BUILD SUCCESS**.
- JUnit: **29 tests, 0 failures, 0 errors, 0 skipped**.
- Đã mở project Maven trong IntelliJ, chạy `Main.main()` bằng JDK 25 và xác nhận màn hình đăng nhập xuất hiện. Sau khi dọn thư mục tạm, Maven dùng cache chuẩn vẫn `verify` thành công.
- Có log và XML gốc trong `docs/`. Dòng `Observer: test observer` trong log là tình huống test chủ động làm observer lỗi, không phải build lỗi.
- Đã khởi tạo/chạy ứng dụng và render ba màn hình từ component thật. Chưa hoàn tất lượt bấm thử thủ công tất cả nút qua công cụ desktop; xem checklist trong [docs/TEST_CASES.md](docs/TEST_CASES.md).

## Giới hạn

Dữ liệu chỉ tồn tại trong phiên chạy, không DB/lưu file. Không quản lý điểm số, học phí, email thật, đăng ký nhiều máy hoặc quy tắc trường đại học ngoài phạm vi. Không có giới hạn tín chỉ hay chặn hai lớp khác nhau của cùng môn. Bản demo chỉ kiểm tra trùng đúng lớp như thiết kế đã chốt.
JUnit không thay thế kiểm tra GUI thủ công trên máy thuyết trình. Không đảm bảo điểm số; nhóm cần hiểu code và tự bảo vệ.

Tất cả tài liệu hỗ trợ được gom trong `docs`:

- [Báo cáo Google Docs](docs/BAO_CAO_GOOGLE_DOCS.md)
- [Giải thích OOP và SOLID](docs/OOP_EXPLANATION.md)
- [Kết quả kiểm thử](docs/TEST_CASES.md)
- [Kịch bản demo](docs/DEMO_GUIDE.md)
- [Sơ đồ UML](docs/UML.md)
