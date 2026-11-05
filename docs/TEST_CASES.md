# Kết quả kiểm thử thực tế

Maven clean verify trên JDK 25, target Java 17: BUILD SUCCESS. 29/29 PASS, không lỗi hoặc bỏ qua.

| ID | Nội dung | Phương thức JUnit | Kết quả |
|---|---|---|---|
| TC01 | Hai buổi sát giờ không trùng | acceptsAdjacentMeetings | PASS |
| TC02 | Đủ tiên quyết được đăng ký | acceptsCompletedPrerequisite | PASS |
| TC03 | Cùng giờ khác học kỳ được chấp nhận | acceptsSameTimeInDifferentSemester | PASS |
| TC04 | Tạo tài khoản, trùng tên và đăng nhập sai | accountCreationAndFailedLoginDoNotGrantPrivileges | PASS |
| TC05 | Admin hủy cho sinh viên theo cùng hạn | adminCanCancelForStudentWithinDeadline | PASS |
| TC06 | Admin sửa sinh viên và xem thống kê | adminCanUpdateStudentAndViewStatistics | PASS |
| TC07 | Admin không đăng ký như Student | adminCannotRegisterAsStudent | PASS |
| TC08 | CRUD Admin giữ tham chiếu và lịch sử | adminCrudPreservesReferencesAndHistory | PASS |
| TC09 | Chưa đăng nhập không đăng ký/hủy/xem lịch sử | anonymousCannotRegisterDropOrReadHistory | PASS |
| TC10 | Observer lỗi không phá kết quả đã ghi | brokenObserverDoesNotUndoCommittedRegistration | PASS |
| TC11 | Đăng ký lại sau hủy tạo bản ghi mới | canRegisterAgainAfterCancellation | PASS |
| TC12 | Hủy trả chỗ, giữ lịch sử và thời điểm | cancellationRestoresSeatAndRetainsHistory | PASS |
| TC13 | Bảo vệ collection và lịch thuộc riêng lớp | collectionViewsAreProtectedAndSchedulesAreOwned | PASS |
| TC14 | Hai Observer thực tế nhận thông báo/làm mới | concreteObserversWork | PASS |
| TC15 | Được hủy đến hết ngày hạn | dropDeadlineIncludesFinalDay | PASS |
| TC16 | Sửa môn sai và chu trình tiên quyết không làm đổi dữ liệu | invalidCourseEditIsAtomic | PASS |
| TC17 | Hai yêu cầu qua hai service tranh chỗ cuối | lastSeatIsAtomicAcrossServices | PASS |
| TC18 | Đọc nhiều buổi học và kiểm tra giờ sai | multipleMeetingsAndSundayParseCorrectly | PASS |
| TC19 | Observer chỉ nhận thành công và gỡ đăng ký được | observersReceiveOnlySuccessfulEventsAndCanUnsubscribe | PASS |
| TC20 | Đăng ký hợp lệ tạo ACTIVE và giảm chỗ | registersSuccessfully | PASS |
| TC21 | Sinh viên khác không được hủy | rejectsCancellationByAnotherStudent | PASS |
| TC22 | Không đăng ký trùng cùng lớp ACTIVE | rejectsDuplicateClass | PASS |
| TC23 | Lớp đầy bị từ chối, dữ liệu không đổi | rejectsFullClassWithoutChangingData | PASS |
| TC24 | Hủy quá hạn bị từ chối | rejectsLateCancellation | PASS |
| TC25 | Thiếu tiên quyết bị từ chối | rejectsMissingPrerequisite | PASS |
| TC26 | Trùng giờ cùng học kỳ bị từ chối | rejectsOverlappingSchedule | PASS |
| TC27 | Không hủy lần hai | rejectsSecondCancellation | PASS |
| TC28 | Tìm mã/tên không phân biệt hoa thường | searchesByCodeAndNameIgnoringCase | PASS |
| TC29 | Student bị chặn các thao tác Admin | studentCannotPerformAnyAdminOperation | PASS |

## Kiểm tra giao diện

Đã khởi tạo và chạy JFrame trên Windows; đã render ba khu vực login, Student, Admin từ component Swing thật và xem ảnh bố cục. Ảnh trong docs/screenshots.
Chưa xác nhận bằng một lượt bấm thủ công tất cả nút trên màn hình do bước cấp quyền điều khiển cửa sổ desktop hết thời gian.

Checklist cần chạy trực tiếp trước buổi thuyết trình:
- Tạo tài khoản mới, đăng nhập sai/đúng, đăng xuất.
- Tìm mã và tên; chọn môn để thấy lớp; xác nhận đăng ký/hủy và nhấn No để kiểm tra không đổi dữ liệu.
- Mở lịch sử và lịch học sau đăng ký/hủy.
- Admin thêm/sửa/xóa một môn và lớp chưa có đăng ký; thử xóa lớp có lịch sử để thấy thông báo từ chối.
- Sửa sinh viên và xem thống kê sau hủy.
- Kiểm tra đọc chữ, tab, hộp thoại ở độ phân giải máy trình chiếu.

Không diễn đạt checklist chưa chạy là kết quả PASS. JUnit ở trên kiểm tra nghiệp vụ và parser lịch, không tự động bấm chuột vào GUI.
