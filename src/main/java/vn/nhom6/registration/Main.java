package vn.nhom6.registration;

import vn.nhom6.registration.store.DemoData;
import vn.nhom6.registration.ui.DesktopApp;

import java.time.Clock;

import javax.swing.*;

public final class Main {
    private Main() {}

    public static void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.err.println("Dùng giao diện Swing mặc định: " + ex.getMessage());
        }
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource font)
                UIManager.put(key, new javax.swing.plaf.FontUIResource(font.deriveFont(14f)));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configureLookAndFeel();
            Clock clock = Clock.systemDefaultZone();
            new DesktopApp(DemoData.create(clock), clock).setVisible(true);
        });
    }
}
