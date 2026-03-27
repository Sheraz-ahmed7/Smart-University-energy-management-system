package Models;

public class Department {
    private int deptId;
    private String deptName;
    private int floorNumber;
    private String contactNumber;

    public Department() {}

    public Department(String deptName, int floorNumber, String contactNumber) {
        this.deptName = deptName;
        this.floorNumber = floorNumber;
        this.contactNumber = contactNumber;
    }

    public Department(int deptId, String deptName, int floorNumber, String contactNumber) {
        this.deptId = deptId;
        this.deptName = deptName;
        this.floorNumber = floorNumber;
        this.contactNumber = contactNumber;
    }

    public int getDeptId() { return deptId; }
    public void setDeptId(int deptId) { this.deptId = deptId; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    @Override
    public String toString() {
        return deptName;
    }
}