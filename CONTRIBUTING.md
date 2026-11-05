# Hướng dẫn làm việc nhóm

Mỗi thành viên cần được mời vào repository với quyền ghi và chấp nhận lời mời trên GitHub. Sau đó clone repository bằng IntelliJ: File → New → Project from Version Control, dán URL repository và chọn Clone.

## Chạy project

Chọn JDK 17 trở lên, nạp Maven và chạy `Main.main()`. Xem README để biết tài khoản demo. Repository lưu mã nguồn; JAR và thư mục target được tạo khi build, không đưa lên Git.

## Cách sửa bài

1. Đảm bảo đã commit hoặc cất thay đổi đang làm, rồi cập nhật nhánh chính:

   ```bash
   git switch main
   git pull --ff-only
   ```

2. Tạo nhánh cho một phần việc, ví dụ:

   ```bash
   git switch -c feat/dang-ky
   ```

3. Sửa code, chạy `mvn verify` và thử giao diện nếu thay đổi liên quan đến Swing.
4. Commit những file đã sửa, push nhánh rồi mở Pull Request vào main để cả nhóm xem trước khi gộp.

   ```bash
   git add src
   git commit -m "Hoan thien chuc nang dang ky"
   git push -u origin feat/dang-ky
   ```

Tên nhánh chỉ là ví dụ; mỗi phần việc dùng tên riêng. Nếu sửa tài liệu, chọn thêm đúng file tài liệu khi commit. Không dùng force push trên main. Khi có xung đột, trao đổi với người đang sửa cùng file trước khi chọn giữ nội dung.

## Chia phần việc

Có thể chia model và UML, service và test, Swing, báo cáo và demo. Vì DesktopApp.java tập trung giao diện, nên chỉ một người sửa file này tại một thời điểm để hạn chế xung đột. Phần đăng ký/hủy phải tiếp tục đi qua RegistrationService; không chuyển kiểm tra nghiệp vụ vào ActionListener.

## Những file không commit

`target`, `.idea`, `.iml`, JAR, log, XML kết quả test có thông tin môi trường và file `.env` đã được bỏ qua. Không đưa token GitHub, mật khẩu thật hoặc dữ liệu sinh viên thật vào repository.
