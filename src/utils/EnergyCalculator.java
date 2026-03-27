package utils;

import Models.Device;
import java.util.List;

public class EnergyCalculator {
    public static final double COST_PER_KWH = 25.0;

    public static double calculateMonthlyConsumption(List<Device> devices) {
        double total = 0;
        for (Device device : devices) {
            total += device.getMonthlyConsumption();
        }
        return total;
    }

    public static double calculateMonthlyCost(double totalKWh) {
        return totalKWh * COST_PER_KWH;
    }

    public static double calculateCarbonFootprint(double totalKWh) {
        return CarbonCalculator.calculateCarbonFootprint(totalKWh);
    }

    public static int calculateTreesNeeded(double carbonKg) {
        return CarbonCalculator.calculateTreesNeeded(carbonKg);
    }

    public static int calculateSustainabilityScore(double totalKWh, double areaSqFt) {
        double benchmark = areaSqFt * 1.5;
        // Deterministic — no Math.random()
        if (totalKWh <= benchmark * 0.5)  return 95;
        if (totalKWh <= benchmark * 0.8)  return 80;
        if (totalKWh <= benchmark * 1.2)  return 65;
        if (totalKWh <= benchmark * 1.5)  return 50;
        return 35;
    }

    public static String getEnergySummary(double totalKWh) {
        double cost = calculateMonthlyCost(totalKWh);
        double carbon = calculateCarbonFootprint(totalKWh);
        int trees = calculateTreesNeeded(carbon);

        return String.format(
                "Monthly Summary:\n" +
                        "Energy Consumption: %.2f kWh\n" +
                        "Electricity Cost: Rs. %.2f\n" +
                        "Carbon Footprint: %.2f kg CO2\n" +
                        "Trees needed to offset: %d",
                totalKWh, cost, carbon, trees
        );
    }
}