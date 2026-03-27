package utils;

public class SolarSimulator {
    private static final double PEAK_SUN_HOURS = 5.0;
    private static final double PANEL_WATTAGE = 400.0;
    private static final double PANEL_COST = 50000.0;
    private static final double SQUARE_FEET_PER_PANEL = 20.0;

    public static class SolarResult {
        public int panelsNeeded;
        public double installationCost;
        public double annualSavings;
        public double paybackYears;
        public double areaRequired;
        public double monthlyGeneration;

        public SolarResult() {}

        public SolarResult(int panelsNeeded, double installationCost, double annualSavings,
                           double paybackYears, double areaRequired, double monthlyGeneration) {
            this.panelsNeeded = panelsNeeded;
            this.installationCost = installationCost;
            this.annualSavings = annualSavings;
            this.paybackYears = paybackYears;
            this.areaRequired = areaRequired;
            this.monthlyGeneration = monthlyGeneration;
        }

        @Override
        public String toString() {
            return String.format(
                    "Solar Installation Analysis:\n" +
                            "Panels Needed: %d\n" +
                            "Installation Cost: Rs. %.2f\n" +
                            "Monthly Generation: %.2f kWh\n" +
                            "Annual Savings: Rs. %.2f\n" +
                            "Payback Period: %.1f years\n" +
                            "Area Required: %.1f sq ft",
                    panelsNeeded, installationCost, monthlyGeneration,
                    annualSavings, paybackYears, areaRequired
            );
        }
    }

    public static SolarResult simulate(double monthlyConsumptionKWh) {
        SolarResult result = new SolarResult();

        double dailyConsumption = monthlyConsumptionKWh / 30.0;
        double panelOutputKW = PANEL_WATTAGE / 1000.0;
        double dailyGenerationPerPanel = panelOutputKW * PEAK_SUN_HOURS;

        result.panelsNeeded = (int) Math.ceil(dailyConsumption / dailyGenerationPerPanel);
        result.installationCost = result.panelsNeeded * PANEL_COST;
        result.annualSavings = monthlyConsumptionKWh * 12 * EnergyCalculator.COST_PER_KWH;
        result.paybackYears = result.installationCost / result.annualSavings;
        result.areaRequired = result.panelsNeeded * SQUARE_FEET_PER_PANEL;
        result.monthlyGeneration = dailyGenerationPerPanel * result.panelsNeeded * 30;

        return result;
    }

    public static SolarResult simulate(double monthlyConsumptionKWh, double customPanelWattage,
                                       double customPeakHours, double customCostPerPanel) {
        SolarResult result = new SolarResult();

        double dailyConsumption = monthlyConsumptionKWh / 30.0;
        double panelOutputKW = customPanelWattage / 1000.0;
        double dailyGenerationPerPanel = panelOutputKW * customPeakHours;

        result.panelsNeeded = (int) Math.ceil(dailyConsumption / dailyGenerationPerPanel);
        result.installationCost = result.panelsNeeded * customCostPerPanel;
        result.annualSavings = monthlyConsumptionKWh * 12 * EnergyCalculator.COST_PER_KWH;
        result.paybackYears = result.installationCost / result.annualSavings;
        result.areaRequired = result.panelsNeeded * SQUARE_FEET_PER_PANEL;
        result.monthlyGeneration = dailyGenerationPerPanel * result.panelsNeeded * 30;

        return result;
    }

    public static String getRecommendation(SolarResult result) {
        if (result.paybackYears < 3) {
            return "✅ Excellent ROI! Strongly recommended to install solar panels.";
        } else if (result.paybackYears < 5) {
            return "👍 Good ROI. Consider installing solar panels.";
        } else if (result.paybackYears < 7) {
            return "🤔 Moderate ROI. Evaluate based on long-term goals.";
        } else {
            return "⚠️ Long payback period. Consider energy efficiency first, then solar.";
        }
    }
}