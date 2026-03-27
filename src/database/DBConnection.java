package database;

import java.sql.*;
import javax.swing.JOptionPane;

/**
 * Singleton database connection with auto-reconnect.
 *
 * BUG FIXED: Original version never checked if the connection
 * was closed/stale — once broken it stayed null forever.
 * Now: getConnection() validates with isClosed() + isValid()
 * and reconnects automatically.
 */
public class DBConnection {

    // ── Change these to match your MySQL setup ─────────────────
    private static final String URL      = "jdbc:mysql://localhost:3306/university_energy_mgt"
                                         + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER  = "root";
    private static final String DB_PASS  = "abc123";
    // ──────────────────────────────────────────────────────────

    private static Connection connection = null;
    private static boolean    errorShown = false;   // show dialog only once per session

    /** Returns a valid, open Connection — reconnects if needed. */
    public static synchronized Connection getConnection() {
        try {
            // Reconnect if null, closed, or stale (isValid waits 2 s for server ping)
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                connect();
            }
        } catch (SQLException e) {
            connect();  // isValid() itself threw — try fresh connect
        }
        return connection;
    }

    private static void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, DB_USER, DB_PASS);
            errorShown = false;   // reset so future errors show again
            System.out.println("[DB] Connected to " + URL);
        } catch (ClassNotFoundException e) {
            showError("MySQL JDBC Driver not found!\n"
                    + "Add mysql-connector-j-*.jar to your classpath.\n\n"
                    + e.getMessage());
            connection = null;
        } catch (SQLException e) {
            showError("Database connection failed:\n" + e.getMessage()
                    + "\n\nCheck that MySQL is running and credentials are correct.");
            connection = null;
        }
    }

    /** Closes the connection (call on app exit). */
    public static synchronized void closeConnection() {
        if (connection != null) {
            try { connection.close(); System.out.println("[DB] Connection closed."); }
            catch (SQLException e) { e.printStackTrace(); }
            finally { connection = null; }
        }
    }

    private static void showError(String msg) {
        if (!errorShown) {
            errorShown = true;
            JOptionPane.showMessageDialog(null, msg, "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}