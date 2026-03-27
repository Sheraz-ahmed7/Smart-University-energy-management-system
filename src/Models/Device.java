package Models;

public class Device {
    private int deviceId;
    private int deptId;
    private String deviceName;
    private int wattage;
    private int quantity;
    private double hoursPerDay;

    public Device() {}

    public Device(int deptId, String deviceName, int wattage, int quantity, double hoursPerDay) {
        this.deptId = deptId;
        this.deviceName = deviceName;
        this.wattage = wattage;
        this.quantity = quantity;
        this.hoursPerDay = hoursPerDay;
    }

    public int getDeviceId() { return deviceId; }
    public void setDeviceId(int deviceId) { this.deviceId = deviceId; }

    public int getDeptId() { return deptId; }
    public void setDeptId(int deptId) { this.deptId = deptId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public int getWattage() { return wattage; }
    public void setWattage(int wattage) { this.wattage = wattage; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getHoursPerDay() { return hoursPerDay; }
    public void setHoursPerDay(double hoursPerDay) { this.hoursPerDay = hoursPerDay; }

    public double getDailyConsumption() {
        return (wattage * quantity * hoursPerDay) / 1000.0;
    }

    public double getMonthlyConsumption() {
        return getDailyConsumption() * 30;
    }

    public double calculateDailyKWh() {
        return getDailyConsumption();
    }

    public double calculateMonthlyKWh() {
        return getMonthlyConsumption();
    }
}