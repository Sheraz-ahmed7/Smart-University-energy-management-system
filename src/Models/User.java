package Models;

public class User {
    private int userId;
    private String username;
    private String password;
    private String role;
    private Integer departmentId;

    public User() {}

    public User(String username, String password, String role, Integer departmentId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.departmentId = departmentId;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }

    public boolean isAdmin() {
        return "admin".equals(role);
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}