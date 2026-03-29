import GUI.LoginScreen;
import GUI.Theme;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        Theme.apply();  // sets system LAF only, no Nimbus
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}