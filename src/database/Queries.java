package database;

import Models.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Queries {

    // ══════════════════════════════════════════════════════════
    //  USER QUERIES
    // ══════════════════════════════════════════════════════════

    public static User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /** Returns all registered users (for UsersPanel). */
    public static List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapUser(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Insert a new user. Password must already be hashed. */
    public static boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password, role, department_id) VALUES (?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            if (user.getDepartmentId() != null) ps.setInt(4, user.getDepartmentId());
            else ps.setNull(4, Types.INTEGER);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /**
     * Update an existing user.
     * @param newHashedPassword  Pass null to keep the existing password unchanged.
     */
    public static boolean updateUser(int userId, String username, String newHashedPassword,
                                     String role, Integer departmentId) {
        String sql = newHashedPassword != null
            ? "UPDATE users SET username=?, password=?, role=?, department_id=? WHERE user_id=?"
            : "UPDATE users SET username=?, role=?, department_id=? WHERE user_id=?";

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            if (newHashedPassword != null) {
                ps.setString(1, username);
                ps.setString(2, newHashedPassword);
                ps.setString(3, role);
                if (departmentId != null) ps.setInt(4, departmentId); else ps.setNull(4, Types.INTEGER);
                ps.setInt(5, userId);
            } else {
                ps.setString(1, username);
                ps.setString(2, role);
                if (departmentId != null) ps.setInt(3, departmentId); else ps.setNull(3, Types.INTEGER);
                ps.setInt(4, userId);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private static User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        int deptId = rs.getInt("department_id");
        u.setDepartmentId(rs.wasNull() ? null : deptId);
        return u;
    }

    // ══════════════════════════════════════════════════════════
    //  DEPARTMENT QUERIES
    // ══════════════════════════════════════════════════════════

    public static List<Department> getAllDepartments() {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT * FROM departments ORDER BY dept_name";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapDept(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static Department getDepartmentById(int deptId) {
        String sql = "SELECT * FROM departments WHERE dept_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, deptId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapDept(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static boolean addDepartment(Department d) {
        String sql = "INSERT INTO departments (dept_name, floor_number, contact_number) VALUES (?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getDeptName());
            ps.setInt(2, d.getFloorNumber());
            ps.setString(3, d.getContactNumber());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean updateDepartment(Department d) {
        String sql = "UPDATE departments SET dept_name=?, floor_number=?, contact_number=? WHERE dept_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getDeptName());
            ps.setInt(2, d.getFloorNumber());
            ps.setString(3, d.getContactNumber());
            ps.setInt(4, d.getDeptId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean deleteDepartment(int deptId) {
        // Devices will cascade-delete if FK is set with ON DELETE CASCADE in schema
        String sql = "DELETE FROM departments WHERE dept_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, deptId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private static Department mapDept(ResultSet rs) throws SQLException {
        return new Department(
            rs.getInt("dept_id"),
            rs.getString("dept_name"),
            rs.getInt("floor_number"),
            rs.getString("contact_number")
        );
    }

    // ══════════════════════════════════════════════════════════
    //  DEVICE QUERIES
    // ══════════════════════════════════════════════════════════

    public static List<Device> getDevicesByDepartment(int deptId) {
        List<Device> list = new ArrayList<>();
        String sql = "SELECT * FROM devices WHERE dept_id = ? ORDER BY device_name";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, deptId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapDevice(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static boolean addDevice(Device d) {
        String sql = "INSERT INTO devices (dept_id, device_name, wattage, quantity, hours_per_day) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, d.getDeptId());
            ps.setString(2, d.getDeviceName());
            ps.setInt(3, d.getWattage());
            ps.setInt(4, d.getQuantity());
            ps.setDouble(5, d.getHoursPerDay());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean updateDevice(Device d) {
        String sql = "UPDATE devices SET device_name=?, wattage=?, quantity=?, hours_per_day=? WHERE device_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getDeviceName());
            ps.setInt(2, d.getWattage());
            ps.setInt(3, d.getQuantity());
            ps.setDouble(4, d.getHoursPerDay());
            ps.setInt(5, d.getDeviceId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean deleteDevice(int deviceId) {
        String sql = "DELETE FROM devices WHERE device_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, deviceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static double getTotalMonthlyKWhByDepartment(int deptId) {
        double total = 0;
        for (Device d : getDevicesByDepartment(deptId)) total += d.getMonthlyConsumption();
        return total;
    }

    private static Device mapDevice(ResultSet rs) throws SQLException {
        Device d = new Device();
        d.setDeviceId(rs.getInt("device_id"));
        d.setDeptId(rs.getInt("dept_id"));
        d.setDeviceName(rs.getString("device_name"));
        d.setWattage(rs.getInt("wattage"));
        d.setQuantity(rs.getInt("quantity"));
        d.setHoursPerDay(rs.getDouble("hours_per_day"));
        return d;
    }

    // ══════════════════════════════════════════════════════════
    //  ENERGY USAGE QUERIES
    // ══════════════════════════════════════════════════════════

    public static List<EnergyUsage> getEnergyUsageHistory(int deptId, int days) {
        List<EnergyUsage> list = new ArrayList<>();
        String sql = "SELECT * FROM energy_usage "
                   + "WHERE dept_id = ? AND timestamp >= DATE_SUB(NOW(), INTERVAL ? DAY) "
                   + "ORDER BY timestamp";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, deptId);
            ps.setInt(2, days);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                EnergyUsage u = new EnergyUsage();
                u.setUsageId(rs.getInt("usage_id"));
                u.setDeptId(rs.getInt("dept_id"));
                u.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                u.setKWh(rs.getDouble("kwh"));
                u.setCost(rs.getDouble("cost"));
                u.setCarbonFootprint(rs.getDouble("carbon_footprint"));
                list.add(u);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static boolean addEnergyUsage(EnergyUsage u) {
        String sql = "INSERT INTO energy_usage (dept_id, timestamp, kwh, cost, carbon_footprint) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, u.getDeptId());
            ps.setTimestamp(2, Timestamp.valueOf(u.getTimestamp()));
            ps.setDouble(3, u.getKWh());
            ps.setDouble(4, u.getCost());
            ps.setDouble(5, u.getCarbonFootprint());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}