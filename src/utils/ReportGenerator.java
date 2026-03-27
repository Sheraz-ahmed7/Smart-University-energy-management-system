package utils;

import Models.Department;
import Models.Device;
import database.Queries;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportGenerator {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String generateDepartmentSummary(Department dept) {
        StringBuilder sb = new StringBuilder();

        sb.append("=".repeat(80)).append("\n");
        sb.append("DEPARTMENT ENERGY SUMMARY REPORT\n");
        sb.append("Generated: ").append(dtf.format(LocalDateTime.now())).append("\n");
        sb.append("=".repeat(80)).append("\n\n");

        if (dept != null) {
            appendDepartmentDetails(sb, dept);
        } else {
            List<Department> departments = Queries.getAllDepartments();
            for (Department d : departments) {
                appendDepartmentDetails(sb, d);
                sb.append("-".repeat(60)).append("\n");
            }
        }

        return sb.toString();
    }

    private static void appendDepartmentDetails(StringBuilder sb, Department dept) {
        double monthlyKWh = Queries.getTotalMonthlyKWhByDepartment(dept.getDeptId());
        double cost = EnergyCalculator.calculateMonthlyCost(monthlyKWh);
        double carbon = CarbonCalculator.calculateCarbonFootprint(monthlyKWh);
        int trees = CarbonCalculator.calculateTreesNeeded(carbon);

        sb.append(String.format("Department: %s\n", dept.getDeptName()));
        sb.append(String.format("Floor: %d\n", dept.getFloorNumber()));
        sb.append(String.format("Contact: %s\n",
                dept.getContactNumber() != null ? dept.getContactNumber() : "N/A"));
        sb.append(String.format("Monthly Consumption: %.2f kWh\n", monthlyKWh));
        sb.append(String.format("Monthly Cost: Rs. %.2f\n", cost));
        sb.append(String.format("Carbon Footprint: %.2f kg CO2\n", carbon));
        sb.append(String.format("Trees needed for offset: %d\n\n", trees));
    }

    public static String generateDeviceDetails(Department dept) {
        StringBuilder sb = new StringBuilder();

        sb.append("=".repeat(80)).append("\n");
        sb.append("DEVICE ENERGY CONSUMPTION DETAILS\n");
        sb.append("Generated: ").append(dtf.format(LocalDateTime.now())).append("\n");
        sb.append("=".repeat(80)).append("\n\n");

        List<Department> departments;
        if (dept != null) {
            departments = List.of(dept);
        } else {
            departments = Queries.getAllDepartments();
        }

        for (Department d : departments) {
            sb.append(String.format("\n%s Department:\n", d.getDeptName()));
            sb.append("-".repeat(60)).append("\n");

            List<Device> devices = Queries.getDevicesByDepartment(d.getDeptId());
            if (devices.isEmpty()) {
                sb.append("No devices registered.\n");
            } else {
                sb.append(String.format("%-4s %-20s %8s %6s %10s %12s %12s\n",
                        "ID", "Device Name", "Watts", "Qty", "Hrs/Day", "Daily kWh", "Monthly kWh"));
                sb.append("-".repeat(80)).append("\n");

                for (Device device : devices) {
                    sb.append(String.format("%-4d %-20s %8d %6d %10.1f %12.2f %12.2f\n",
                            device.getDeviceId(),
                            truncate(device.getDeviceName(), 20),
                            device.getWattage(),
                            device.getQuantity(),
                            device.getHoursPerDay(),
                            device.getDailyConsumption(),
                            device.getMonthlyConsumption()));
                }
            }
        }

        return sb.toString();
    }

    public static String generateCarbonReport(Department dept) {
        StringBuilder sb = new StringBuilder();

        sb.append("=".repeat(80)).append("\n");
        sb.append("CARBON FOOTPRINT REPORT\n");
        sb.append("Generated: ").append(dtf.format(LocalDateTime.now())).append("\n");
        sb.append("=".repeat(80)).append("\n\n");

        sb.append("CO2 Emission Factor: ").append(CarbonCalculator.EMISSION_FACTOR).append(" kg CO2 per kWh\n");
        sb.append("Tree Offset: 1 tree absorbs ~").append(CarbonCalculator.TREE_ABSORPTION_PER_YEAR).append(" kg CO2 per year\n\n");

        List<Department> departments;
        if (dept != null) {
            departments = List.of(dept);
        } else {
            departments = Queries.getAllDepartments();
        }

        double totalCarbon = 0;
        int totalTrees = 0;

        sb.append(String.format("%-30s %15s %15s %12s\n", "Department", "Monthly CO2", "Annual CO2", "Trees"));
        sb.append("-".repeat(80)).append("\n");

        for (Department d : departments) {
            double monthlyKWh = Queries.getTotalMonthlyKWhByDepartment(d.getDeptId());
            double carbon = CarbonCalculator.calculateCarbonFootprint(monthlyKWh);
            int trees = CarbonCalculator.calculateTreesNeeded(carbon);

            sb.append(String.format("%-30s %15.2f %15.2f %12d\n",
                    d.getDeptName(), carbon, carbon * 12, trees));

            totalCarbon += carbon;
            totalTrees += trees;
        }

        sb.append("-".repeat(80)).append("\n");
        sb.append(String.format("%-30s %15.2f %15.2f %12d\n",
                "TOTAL", totalCarbon, totalCarbon * 12, totalTrees));

        return sb.toString();
    }

    public static boolean exportReport(String content, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.print(content);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String truncate(String str, int length) {
        if (str.length() <= length) return str;
        return str.substring(0, length - 3) + "...";
    }
}