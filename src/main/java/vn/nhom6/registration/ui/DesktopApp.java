package vn.nhom6.registration.ui;

import vn.nhom6.registration.model.*;
import vn.nhom6.registration.observer.*;
import vn.nhom6.registration.service.*;
import vn.nhom6.registration.store.InMemoryStore;
import vn.nhom6.registration.strategy.*;

import java.awt.*;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Small Swing view: rules and authorization remain in the services. */
public final class DesktopApp extends JFrame {
    private final InMemoryStore store;
    private final AuthService auth;
    private final RegistrationService registrations;
    private final CourseCatalogService catalog;
    private final AdminService admin;
    private final JLabel message = new JLabel(" ");
    private final List<Runnable> refreshers = new ArrayList<>();
    private Runnable refreshStatistics = () -> {};
    private JTextField keyword;
    private JComboBox<String> searchMode;
    private JTable coursesTable, sectionsTable;
    private List<Course> shownCourses = List.of();
    private List<CourseSection> shownSections = List.of();

    public DesktopApp(InMemoryStore store, Clock clock) {
        super("BTL OOP | Nhóm 6 | Đăng ký học phần");
        this.store = store;
        auth = new AuthService(store);
        registrations = new RegistrationService(store, auth, clock);
        catalog = new CourseCatalogService(store);
        admin = new AdminService(store, auth, registrations);
        registrations.addObserver(
                new NotificationObserver(text -> onEdt(() -> message.setText(text))));
        registrations.addObserver(
                new StatisticsObserver(() -> onEdt(() -> refreshStatistics.run())));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        setSize(1180, 760);
        setLocationRelativeTo(null);
        showLogin();
    }

    private static void onEdt(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) action.run();
        else SwingUtilities.invokeLater(action);
    }

    private void safely(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException
                | IllegalStateException
                | SecurityException
                | vn.nhom6.registration.exception.RegistrationException ex) {
            JOptionPane.showMessageDialog(
                    this, ex.getMessage(), "Không thực hiện được", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JButton button(String title, Runnable action) {
        JButton b = new JButton(title);
        b.addActionListener(e -> safely(action));
        return b;
    }

    private JPanel row(Component... components) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        for (Component c : components) p.add(c);
        return p;
    }

    private JTable table(String... headings) {
        JTable t =
                new JTable(
                        new DefaultTableModel(headings, 0) {
                            @Override
                            public boolean isCellEditable(int row, int column) {
                                return false;
                            }
                        });
        t.setRowHeight(27);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    private void fill(JTable t, List<Object[]> rows) {
        DefaultTableModel model = (DefaultTableModel) t.getModel();
        model.setRowCount(0);
        for (Object[] r : rows) model.addRow(r);
    }

    private <T> T selected(JTable table, List<T> items) {
        int row = table.getSelectedRow();
        if (row < 0 || row >= items.size())
            throw new IllegalArgumentException("Hãy chọn một dòng trong bảng.");
        return items.get(row);
    }

    private void display(Container content) {
        setContentPane(content);
        revalidate();
        repaint();
    }

    private void showLogin() {
        refreshers.clear();
        refreshStatistics = () -> {};
        JPanel page = new JPanel(new GridBagLayout());
        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setPreferredSize(new Dimension(440, 340));
        JLabel title = new JLabel("HỆ THỐNG ĐĂNG KÝ HỌC PHẦN", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20));
        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();
        form.add(title);
        form.add(new JLabel("Tài khoản"));
        form.add(username);
        form.add(new JLabel("Mật khẩu"));
        form.add(password);
        JButton login =
                button(
                        "Đăng nhập",
                        () -> {
                            char[] chars = password.getPassword();
                            try {
                                auth.login(username.getText(), chars);
                                showWorkspace();
                            } finally {
                                Arrays.fill(chars, '\0');
                                password.setText("");
                            }
                        });
        form.add(login);
        form.add(button("Tạo tài khoản sinh viên", this::registerAccount));
        form.add(
                new JLabel(
                        "Demo: sv01 / student123   |   admin / admin123", SwingConstants.CENTER));
        page.add(form);
        display(page);
        getRootPane().setDefaultButton(login);
    }

    private boolean form(String title, String[] labels, JComponent[] fields) {
        JPanel p = new JPanel(new GridLayout(0, 2, 10, 10));
        for (int i = 0; i < labels.length; i++) {
            p.add(new JLabel(labels[i]));
            p.add(fields[i]);
        }
        return JOptionPane.showConfirmDialog(
                        this, p, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                == JOptionPane.OK_OPTION;
    }

    private void registerAccount() {
        JTextField username = new JTextField(), name = new JTextField(), email = new JTextField();
        JPasswordField pass = new JPasswordField();
        try {
            if (!form(
                    "Đăng ký tài khoản",
                    new String[] {"Tài khoản", "Mật khẩu (ít nhất 6 ký tự)", "Họ tên", "Email"},
                    new JComponent[] {username, pass, name, email})) return;
            char[] chars = pass.getPassword();
            try {
                auth.register(username.getText(), chars, name.getText(), email.getText());
            } finally {
                Arrays.fill(chars, '\0');
            }
            JOptionPane.showMessageDialog(this, "Đã tạo tài khoản. Bạn có thể đăng nhập.");
        } finally {
            pass.setText("");
        }
    }

    private void showWorkspace() {
        refreshers.clear();
        message.setText("Dữ liệu demo lưu trong phiên chạy; đóng ứng dụng sẽ đặt lại dữ liệu.");
        User user = auth.currentUser();
        JPanel page = new JPanel(new BorderLayout(8, 8));
        page.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel header = new JPanel(new BorderLayout());
        header.add(new JLabel(user.getFullName() + " | " + user.getRole()), BorderLayout.WEST);
        header.add(
                button(
                        "Đăng xuất",
                        () -> {
                            auth.logout();
                            showLogin();
                        }),
                BorderLayout.EAST);
        JTabbedPane tabs = new JTabbedPane();
        if (user.getRole() == UserRole.STUDENT) {
            tabs.addTab("Tìm môn và đăng ký", studentSearch());
            tabs.addTab("Đăng ký và lịch sử", registrationPanel(false));
            tabs.addTab("Lịch học", schedulePanel());
        } else {
            tabs.addTab("Học phần", courseAdminPanel());
            tabs.addTab("Lớp học phần", sectionAdminPanel());
            tabs.addTab("Sinh viên", studentAdminPanel());
            tabs.addTab("Danh sách đăng ký", registrationPanel(true));
            tabs.addTab("Thống kê", statisticsPanel());
            tabs.addTab("Học kỳ và phòng", referencePanel());
        }
        tabs.addChangeListener(e -> refreshAll());
        page.add(header, BorderLayout.NORTH);
        page.add(tabs, BorderLayout.CENTER);
        page.add(message, BorderLayout.SOUTH);
        display(page);
        getRootPane().setDefaultButton(null);
        refreshAll();
    }

    private void refreshAll() {
        for (Runnable refresher : refreshers) refresher.run();
    }

    private JPanel studentSearch() {
        JPanel p = new JPanel(new BorderLayout());
        keyword = new JTextField(20);
        searchMode = new JComboBox<>(new String[] {"Theo mã", "Theo tên"});
        coursesTable = table("Mã", "Học phần", "Tín chỉ", "Tiên quyết", "Mô tả");
        sectionsTable =
                table("Mã lớp", "Học kỳ", "Giảng viên", "Phòng", "Lịch", "Còn chỗ", "Hạn hủy");
        Runnable search =
                () -> {
                    CourseSearchStrategy strategy =
                            searchMode.getSelectedIndex() == 0
                                    ? new SearchByCodeStrategy()
                                    : new SearchByNameStrategy();
                    shownCourses = catalog.search(keyword.getText(), strategy);
                    fill(coursesTable, shownCourses.stream().map(this::courseRow).toList());
                    shownSections = List.of();
                    fill(sectionsTable, List.of());
                };
        coursesTable
                .getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting() && coursesTable.getSelectedRow() >= 0) {
                        shownSections = catalog.sectionsFor(selected(coursesTable, shownCourses));
                        fill(sectionsTable, shownSections.stream().map(this::sectionRow).toList());
                    }
                });
        keyword.addActionListener(e -> safely(search));
        p.add(
                row(new JLabel("Tìm học phần"), keyword, searchMode, button("Tìm kiếm", search)),
                BorderLayout.NORTH);
        JSplitPane split =
                new JSplitPane(
                        JSplitPane.VERTICAL_SPLIT,
                        new JScrollPane(coursesTable),
                        new JScrollPane(sectionsTable));
        split.setResizeWeight(.45);
        p.add(split, BorderLayout.CENTER);
        p.add(
                row(
                        button(
                                "Đăng ký lớp đã chọn",
                                () -> {
                                    CourseSection s = selected(sectionsTable, shownSections);
                                    if (JOptionPane.showConfirmDialog(
                                                    this,
                                                    "Đăng ký lớp " + s.getId() + "?",
                                                    "Xác nhận",
                                                    JOptionPane.YES_NO_OPTION)
                                            == JOptionPane.YES_OPTION) {
                                        registrations.register(s);
                                        refreshAll();
                                    }
                                })),
                BorderLayout.SOUTH);
        refreshers.add(search);
        return p;
    }

    private Object[] courseRow(Course c) {
        return new Object[] {
            c.getId(),
            c.getName(),
            c.getCredits(),
            c.getPrerequisites().stream()
                    .map(Prerequisite::getRequiredCourseId)
                    .collect(Collectors.joining(", ")),
            c.getDescription()
        };
    }

    private String meetings(CourseSection s) {
        return s.getSchedules().stream().map(Object::toString).collect(Collectors.joining("; "));
    }

    private Object[] sectionRow(CourseSection s) {
        return new Object[] {
            s.getId(),
            s.getSemester(),
            s.getLecturer(),
            s.getClassroom(),
            meetings(s),
            registrations.availableSeats(s) + "/" + s.getCapacity(),
            s.getSemester().getDropDeadline()
        };
    }

    private JPanel registrationPanel(boolean adminMode) {
        JPanel p = new JPanel(new BorderLayout());
        JTable t =
                table(
                        "Mã ĐK",
                        "Sinh viên",
                        "Lớp",
                        "Môn",
                        "Học kỳ",
                        "Trạng thái",
                        "Ngày đăng ký",
                        "Ngày hủy");
        List<Registration> items = new ArrayList<>();
        Runnable refresh =
                () -> {
                    items.clear();
                    items.addAll(registrations.visibleRegistrations());
                    fill(
                            t,
                            items.stream()
                                    .map(r ->
                                            new Object[] {
                                                r.getId(),
                                                r.getStudent().getId(),
                                                r.getSection().getId(),
                                                r.getSection().getCourse().getName(),
                                                r.getSection().getSemester(),
                                                r.getStatus(),
                                                r.getRegisteredAt().withNano(0),
                                                r.getCancelledAt() == null
                                                        ? ""
                                                        : r.getCancelledAt().withNano(0)
                                            })
                                    .toList());
                };
        p.add(new JScrollPane(t));
        p.add(
                row(
                        button(
                                adminMode
                                        ? "Hủy đăng ký đã chọn (theo hạn hủy)"
                                        : "Hủy đăng ký đã chọn",
                                () -> {
                                    Registration r = selected(t, items);
                                    if (JOptionPane.showConfirmDialog(
                                                    this,
                                                    "Hủy đăng ký " + r.getId() + "?",
                                                    "Xác nhận",
                                                    JOptionPane.YES_NO_OPTION)
                                            == JOptionPane.YES_OPTION) {
                                        registrations.drop(r);
                                        refreshAll();
                                    }
                                }),
                        button("Làm mới", refresh)),
                BorderLayout.SOUTH);
        refreshers.add(refresh);
        return p;
    }

    private JPanel schedulePanel() {
        JPanel p = new JPanel(new BorderLayout());
        JTable t = table("Học kỳ", "Lớp", "Học phần", "Buổi học", "Phòng", "Giảng viên");
        Runnable refresh =
                () -> {
                    List<Object[]> rows = new ArrayList<>();
                    for (Registration r : registrations.visibleRegistrations())
                        if (r.isActive()) {
                            CourseSection s = r.getSection();
                            for (Schedule schedule : s.getSchedules())
                                rows.add(
                                        new Object[] {
                                            s.getSemester(),
                                            s.getId(),
                                            s.getCourse().getName(),
                                            schedule,
                                            s.getClassroom(),
                                            s.getLecturer()
                                        });
                        }
                    fill(t, rows);
                };
        p.add(new JScrollPane(t));
        refreshers.add(refresh);
        return p;
    }

    private JPanel courseAdminPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JTable t = table("Mã", "Tên học phần", "Tín chỉ", "Tiên quyết", "Mô tả");
        List<Course> items = new ArrayList<>();
        Runnable refresh =
                () -> {
                    items.clear();
                    items.addAll(store.courses());
                    fill(t, items.stream().map(this::courseRow).toList());
                };
        p.add(new JScrollPane(t));
        p.add(
                row(
                        button("Thêm môn", () -> editCourse(null)),
                        button("Sửa môn", () -> editCourse(selected(t, items))),
                        button(
                                "Xóa môn",
                                () -> {
                                    Course c = selected(t, items);
                                    if (JOptionPane.showConfirmDialog(
                                                    this,
                                                    "Xóa môn " + c.getId() + "?",
                                                    "Xác nhận",
                                                    JOptionPane.YES_NO_OPTION)
                                            == JOptionPane.YES_OPTION) {
                                        admin.deleteCourse(c);
                                        refreshAll();
                                    }
                                })),
                BorderLayout.SOUTH);
        refreshers.add(refresh);
        return p;
    }

    private void editCourse(Course old) {
        JTextField id = new JTextField(old == null ? "" : old.getId()),
                name = new JTextField(old == null ? "" : old.getName());
        JTextField credits = new JTextField(old == null ? "3" : "" + old.getCredits()),
                description = new JTextField(old == null ? "" : old.getDescription());
        JTextField prerequisites =
                new JTextField(
                        old == null
                                ? ""
                                : old.getPrerequisites().stream()
                                        .map(Prerequisite::getRequiredCourseId)
                                        .collect(Collectors.joining(",")));
        id.setEditable(old == null);
        if (form(
                "Học phần",
                new String[] {
                    "Mã môn", "Tên môn", "Tín chỉ", "Mô tả", "Mã tiên quyết, phân cách dấu phẩy"
                },
                new JComponent[] {id, name, credits, description, prerequisites})) {
            admin.saveCourse(
                    old,
                    id.getText(),
                    name.getText(),
                    Integer.parseInt(credits.getText().trim()),
                    description.getText(),
                    codes(prerequisites.getText()).stream().map(Prerequisite::new).toList());
            refreshAll();
        }
    }

    private Set<String> codes(String text) {
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private JPanel sectionAdminPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JTable t = table("Mã lớp", "Học kỳ", "Giảng viên", "Phòng", "Lịch", "Còn chỗ", "Hạn hủy");
        List<CourseSection> items = new ArrayList<>();
        Runnable refresh =
                () -> {
                    items.clear();
                    items.addAll(store.sections());
                    fill(t, items.stream().map(this::sectionRow).toList());
                };
        p.add(new JScrollPane(t));
        p.add(
                row(
                        button("Thêm lớp", () -> editSection(null)),
                        button("Sửa lớp", () -> editSection(selected(t, items))),
                        button(
                                "Xóa lớp",
                                () -> {
                                    CourseSection s = selected(t, items);
                                    if (JOptionPane.showConfirmDialog(
                                                    this,
                                                    "Xóa lớp " + s.getId() + "?",
                                                    "Xác nhận",
                                                    JOptionPane.YES_NO_OPTION)
                                            == JOptionPane.YES_OPTION) {
                                        admin.deleteSection(s);
                                        refreshAll();
                                    }
                                })),
                BorderLayout.SOUTH);
        refreshers.add(refresh);
        return p;
    }

    private void editSection(CourseSection old) {
        JTextField id = new JTextField(old == null ? "" : old.getId()),
                lecturer = new JTextField(old == null ? "" : old.getLecturer());
        JTextField capacity = new JTextField(old == null ? "30" : "" + old.getCapacity());
        JComboBox<Course> course = new JComboBox<>(store.courses().toArray(Course[]::new));
        JComboBox<Semester> semester = new JComboBox<>(store.semesters().toArray(Semester[]::new));
        JComboBox<Classroom> room = new JComboBox<>(store.rooms().toArray(Classroom[]::new));
        JTextField schedules =
                new JTextField(
                        old == null
                                ? "2 08:00-10:00"
                                : old.getSchedules().stream()
                                        .map(s ->
                                                (s.getDay().getValue() + 1)
                                                        + " "
                                                        + s.getStart()
                                                        + "-"
                                                        + s.getEnd())
                                        .collect(Collectors.joining("; ")));
        id.setEditable(old == null);
        if (old != null) {
            course.setSelectedItem(old.getCourse());
            semester.setSelectedItem(old.getSemester());
            room.setSelectedItem(old.getClassroom());
        }
        if (form(
                "Lớp học phần",
                new String[] {
                    "Mã lớp",
                    "Học phần",
                    "Học kỳ",
                    "Phòng",
                    "Giảng viên",
                    "Sĩ số",
                    "Lịch: thứ 2..8 HH:mm-HH:mm; ..."
                },
                new JComponent[] {id, course, semester, room, lecturer, capacity, schedules})) {
            List<Schedule> parsed = parseSchedules(schedules.getText());
            admin.saveSection(
                    old,
                    id.getText(),
                    (Course) course.getSelectedItem(),
                    (Semester) semester.getSelectedItem(),
                    (Classroom) room.getSelectedItem(),
                    lecturer.getText(),
                    Integer.parseInt(capacity.getText().trim()),
                    parsed);
            refreshAll();
        }
    }

    public static List<Schedule> parseSchedules(String text) {
        try {
            List<Schedule> result = new ArrayList<>();
            for (String part : text.split(";")) {
                String[] tokens = part.trim().split("\\s+");
                String[] times = tokens[1].split("-");
                result.add(
                        new Schedule(
                                DayOfWeek.of(Integer.parseInt(tokens[0]) - 1),
                                LocalTime.parse(times[0]),
                                LocalTime.parse(times[1])));
            }
            return List.copyOf(result);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(
                    "Lịch không hợp lệ. Ví dụ: 2 08:00-10:00; 4 13:00-15:00");
        }
    }

    private JPanel studentAdminPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JTable t = table("Mã SV", "Họ tên", "Email", "Tài khoản", "Môn đã hoàn thành");
        List<Student> items = new ArrayList<>();
        Runnable refresh =
                () -> {
                    items.clear();
                    items.addAll(admin.students());
                    fill(
                            t,
                            items.stream()
                                    .map(s ->
                                            new Object[] {
                                                s.getId(),
                                                s.getFullName(),
                                                s.getEmail(),
                                                s.getAccount().getUsername(),
                                                String.join(
                                                        ", ",
                                                        new TreeSet<>(s.getCompletedCourseIds()))
                                            })
                                    .toList());
                };
        p.add(new JScrollPane(t));
        p.add(
                row(
                        button(
                                "Sửa thông tin sinh viên",
                                () -> {
                                    Student s = selected(t, items);
                                    JTextField name = new JTextField(s.getFullName()),
                                            email = new JTextField(s.getEmail()),
                                            completed =
                                                    new JTextField(
                                                            String.join(
                                                                    ",",
                                                                    s.getCompletedCourseIds()));
                                    if (form(
                                            "Sinh viên " + s.getId(),
                                            new String[] {
                                                "Họ tên",
                                                "Email",
                                                "Môn hoàn thành, cách bằng dấu phẩy"
                                            },
                                            new JComponent[] {name, email, completed})) {
                                        admin.updateStudent(
                                                s,
                                                name.getText(),
                                                email.getText(),
                                                codes(completed.getText()));
                                        refreshAll();
                                    }
                                })),
                BorderLayout.SOUTH);
        refreshers.add(refresh);
        return p;
    }

    private JPanel statisticsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JTable t = table("Lớp", "Học phần", "Học kỳ", "Sĩ số tối đa", "Đang đăng ký", "Còn chỗ");
        refreshStatistics =
                () -> {
                    Map<String, Integer> counts = admin.statistics();
                    fill(
                            t,
                            store.sections().stream()
                                    .map(s ->
                                            new Object[] {
                                                s.getId(),
                                                s.getCourse().getName(),
                                                s.getSemester(),
                                                s.getCapacity(),
                                                counts.get(s.getId()),
                                                s.getCapacity() - counts.get(s.getId())
                                            })
                                    .toList());
                };
        p.add(
                new JLabel(
                        "Thống kê chỉ tính đăng ký ACTIVE. Observer cập nhật sau đăng ký hoặc hủy"
                            + " thành công."),
                BorderLayout.NORTH);
        p.add(new JScrollPane(t));
        refreshers.add(refreshStatistics);
        return p;
    }

    private JPanel referencePanel() {
        JPanel p = new JPanel(new GridLayout(2, 1, 8, 8));
        JTable semesters = table("Học kỳ", "Bắt đầu", "Kết thúc", "Hạn hủy (bao gồm ngày này)");
        JTable rooms = table("Phòng", "Sức chứa");
        fill(
                semesters,
                store.semesters().stream()
                        .map(s ->
                                new Object[] {
                                    s.getId(), s.getStartDate(), s.getEndDate(), s.getDropDeadline()
                                })
                        .toList());
        fill(
                rooms,
                store.rooms().stream()
                        .map(r -> new Object[] {r.getId(), r.getCapacity()})
                        .toList());
        p.add(new JScrollPane(semesters));
        p.add(new JScrollPane(rooms));
        return p;
    }
}
