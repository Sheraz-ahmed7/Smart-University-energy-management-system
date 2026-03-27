package utils;

public class CarbonCalculator {
    public static final double EMISSION_FACTOR = 0.5;
    public static final double TREE_ABSORPTION_PER_YEAR = 22.0;

    public static double calculateCarbonFootprint(double kwh) {
        return kwh * EMISSION_FACTOR;
    }

    public static int calculateTreesNeeded(double carbonKg) {
        return (int) Math.ceil(carbonKg / TREE_ABSORPTION_PER_YEAR);
    }

    public static double calculateMonthlyCarbonFromDaily(double dailyKwh) {
        return calculateCarbonFootprint(dailyKwh * 30);
    }

    public static String getCarbonFootprintString(double kwh) {
        return String.format("%.2f kg CO2", calculateCarbonFootprint(kwh));
    }

    public static String getComparison(double kwh) {
        double carbon = calculateCarbonFootprint(kwh);
        double average = 500;

        if (carbon < average * 0.7) {
            return "Below Average (Good!)";
        } else if (carbon < average * 1.3) {
            return "Average";
        } else {
            return "Above Average (Needs Improvement)";
        }
    }
}