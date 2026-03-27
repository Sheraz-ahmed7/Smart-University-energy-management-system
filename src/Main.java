import GUI.LoginScreen;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Anti-aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Use system LAF — Nimbus breaks custom text fields
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}