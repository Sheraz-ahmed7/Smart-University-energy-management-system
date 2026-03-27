package Models;

import java.time.LocalDateTime;

public class EnergyUsage {
    private int usageId;
    private int deptId;
    private LocalDateTime timestamp;
    private double kwh;
    private double cost;
    private double carbonFootprint;

    public EnergyUsage() {}

    public EnergyUsage(int usageId, int deptId, LocalDateTime timestamp,
                       double kwh, double cost, double carbonFootprint) {
        this.usageId = usageId;
        this.deptId = deptId;
        this.timestamp = timestamp;
        this.kwh = kwh;
        this.cost = cost;
        this.carbonFootprint = carbonFootprint;
    }

    public int getUsageId() { return usageId; }
    public void setUsageId(int usageId) { this.usageId = usageId; }

    public int getDeptId() { return deptId; }
    public void setDeptId(int deptId) { this.deptId = deptId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public double getKWh() { return kwh; }
    public void setKWh(double kwh) { this.kwh = kwh; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    public double getCarbonFootprint() { return carbonFootprint; }
    public void setCarbonFootprint(double carbonFootprint) { this.carbonFootprint = carbonFootprint; }
}