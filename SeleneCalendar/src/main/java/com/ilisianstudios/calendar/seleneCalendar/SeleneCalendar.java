package com.ilisianstudios.calendar.seleneCalendar;

import java.util.TimeZone;
import java.util.Calendar;
import java.util.Locale;

public class SeleneCalendar extends Calendar {

    public SeleneCalendar() {
        this(TimeZone.getDefault(), Locale.getDefault(Locale.Category.FORMAT));
    }

    public SeleneCalendar(TimeZone zone) {
        this(zone, Locale.getDefault(Locale.Category.FORMAT));
    }

    public SeleneCalendar(TimeZone zone, Locale locale) {
        super(zone, locale);
        setTimeInMillis(System.currentTimeMillis());
    }

    public static final String[][] LUNATION_INFO = {
            {"Wolf Moon",       "Howling in deep winter, marking the year's start."},
            {"Snow Moon",       "Quiet reflection amid icy calm."},
            {"Storm Moon",      "Heralding fierce, late-winter storms."},
            {"Worm Moon",       "Signaling the first stirrings of renewal."},
            {"Seed Moon",       "When hope is sown and new growth begins."},
            {"Flower Moon",     "Celebrating blooming life in spring."},
            {"Honey Moon",      "Early summer warmth and fruitful days."},
            {"Thunder Moon",    "The intense, stormy heart of summer."},
            {"Corn Moon",       "Crops ripen as autumn approaches."},
            {"Harvest Moon",    "Bounty of early autumn reaping the earth's gifts."},
            {"Ancestor’s Moon", "A time for remembrance and ancestral wisdom."},
            {"Frost Moon",      "A delicate chill as the year winds down."},
            {"Hecate’s Moon",   "The secret, transformative moon that closes the cycle."}
    };

    public static final String[] PLANET_WEEK_DAYS = {
            "Mercva",
            "Venuva",
            "Earava",
            "Marva",
            "Jupva",
            "Saturva",
            "Urava",
            "Neptuva"
    };
    //    public static final double WEEK_EPOCH = 2440587.5; // Reference JD for week cycle


    static final double JD_0 = 2460556.580372;
    static final double JD_START_OF_TIME = 347998.466192;

    public static final double WEEK_EPOCH = Math.floor(JD_START_OF_TIME); // Reference JD for week cycle
    static final double JD_FIRST_SOLSTICE = 348072.958333;
    static final double JD_Eclipse_0 = 2460571.614010;
    static final double  MEAN_SYNODIC_MONTH= 29.53059;
    static final double  SAROS_CYCLE= 6585.3213;
    static final double  TROPICAL_YEAR= 365.2422;
    static final double  LUNAR_YEAR_DAYS= 354.36708;
    // For the precise new moon calculation we use JDE_REF from Meeus:
    static final double JDE_REF = 2451550.09765;

    // Define the base year for the Selene calendar; the first Selene year is 1.
    private static final int BASE_YEAR = 1;

    static final double JD_JUNE_SOLSTICE = 2460481.612500;
    static final double JD_DECEMBER_SOLSTICE = 2460665.8888899;
    static final double JD_SEPTEMBER_EQUINOX = 2460576.263194;
    static final double JD_MARCH_SOLSTICE = 2460389.875694;
    static final double CONSTANTS_YEAR = 2024;
    static final double JD_OF_UNIX_0 = 2440587.5;

    // Add our own flag to mark that fields have been computed.
    private boolean myFieldsComputed = false;

    // --- Conversion: computeTime() and computeFields() ---
    @Override
    protected void computeTime() {
        // Convert the Selene calendar fields to internal time.
        int year = this.fields[YEAR];
        set(YEAR, year);
        int month = this.fields[MONTH];
        set(MONTH, month);
        int date = this.fields[DATE];
        set(DATE, date);
        // Global lunation number L.
        int L = (year - BASE_YEAR) * 13 + month;
        // Compute K₀ so that for L=0, new moon = JD_START_OF_TIME.
        double K0 = (JD_START_OF_TIME - JDE_REF) / MEAN_SYNODIC_MONTH;
        int kOffset = (int)Math.round(K0);
        // Compute JD for the new moon of the lunation (L + kOffset).
        double newMoonJD = preciseNewMoonForLunationFull(L + kOffset);
        // The current JD is newMoonJD plus (date - 1) days.
        double currentJD = newMoonJD + (date - 1);
        // Convert JD to millis.
        this.time = (long)((currentJD - JD_OF_UNIX_0) * 86400000.0);
        myFieldsComputed = true;
    }

    @Override
    protected void computeFields() {
        // Convert internal time (millis) to JD.
        double JD = (this.time / 86400000.0) + JD_OF_UNIX_0;
        // Estimate global lunation number from the mean formula.
        double kApprox = (JD - JDE_REF) / MEAN_SYNODIC_MONTH;
        double K0 = (JD_START_OF_TIME - JDE_REF) / MEAN_SYNODIC_MONTH;
        int kOffset = (int)Math.round(K0);
        int L = (int)Math.floor(kApprox - K0);
        // Compute the new moon for this lunation.
        double newMoonJD = preciseNewMoonForLunationFull(L + kOffset);
        int date = (int)Math.floor(JD - newMoonJD) + 1;
        // If date exceeds maximum, adjust.
        int maxDate = daysInLunation(L + kOffset);
        if (date > maxDate) {
            date -= maxDate;
            L++;
        }
        int computedYear = L / 13 + BASE_YEAR;
        int computedMonth = L % 13;
        set(YEAR, computedYear);
        set(MONTH, computedMonth);
        set(DATE, date);

        myFieldsComputed = true;
    }

    @Override
    protected void complete() {
        if (!myFieldsComputed) {
            computeFields();
        }
    }

    @Override
    public void set(int field, int value) {
        super.set(field, value);
        myFieldsComputed = false; // Mark fields as needing recomputation
    }

    /**
     * Adds the specified amount to the given field.
     */
    @Override
    public void add(int field, int amount) {
        switch (field) {
            case YEAR:
                set(YEAR, this.fields[YEAR] + amount);
                break;
            case MONTH:
                int newMonth = this.fields[MONTH] + amount;
                if (newMonth >= 13) {
                    int addYears = newMonth / 13;
                    newMonth = newMonth % 13;
                    set(YEAR, this.fields[YEAR] + addYears);
                } else if (newMonth < 0) {
                    int subYears = (-newMonth + 12) / 13;
                    newMonth = 13 - ((-newMonth) % 13);
                    set(YEAR, this.fields[YEAR] - subYears);
                }
                set(MONTH, newMonth);
                break;
            case DATE:
                set(DATE, this.fields[DATE] + amount);
                // Adjust DATE if it overflows the current lunation.
                while (this.fields[DATE] > getActualMaximum(DATE)) {
                    set(DATE, this.fields[DATE] - getActualMaximum(DATE));
                    add(MONTH, 1);  // this will update YEAR if necessary
                }
                while (this.fields[DATE] < getMinimum(DATE)) {
                    add(MONTH, -1);
                    set(DATE, this.fields[DATE] + getActualMaximum(DATE));
                }
                break;
            default:
                throw new IllegalArgumentException("Field not supported for add: " + field);
        }
        computeTime();  // Update internal time from fields
    }

    /**
     * Rolls the specified field up or down without changing larger fields.
     */
    @Override
    public void roll(int field, boolean up) {
        switch (field) {
            case YEAR:
                set(YEAR, this.fields[YEAR] + (up? 1 : -1 ));
                break;
            case MONTH:
                int month = this.fields[MONTH];
                month = up ? (month + 1) % 13 : (month - 1 + 13) % 13;
                set(MONTH, month);
                break;
            case DATE:
                int date = this.fields[DATE];
                int maxDate = getActualMaximum(DATE);
                if (up) {
                    date = date % maxDate + 1;
                } else {
                    date = (date - 1 + maxDate - 1 + maxDate) % maxDate + 1;
                }
                set(DATE, date);
                break;
            default:
                throw new IllegalArgumentException("Field not supported for roll: " + field);
        }

        computeTime();
    }

    @Override
    public int getMinimum(int field) {
        switch (field) {
            case YEAR: return Integer.MIN_VALUE;  // or a chosen lower bound
            case MONTH: return 0;
            case DATE: return 1;
            default: return 0;
        }
    }

    @Override
    public int getMaximum(int field) {
        switch (field) {
            case YEAR: return Integer.MAX_VALUE;  // or a chosen upper bound
            case MONTH: return 12;   // 0 to 12 gives 13 lunations
            case DATE: return 30;    // assume maximum 30 days per lunation
            default: return 0;
        }
    }

    @Override
    public int getGreatestMinimum(int field) {
        return getMinimum(field);
    }

    @Override
    public int getLeastMaximum(int field) {
        return getMaximum(field);
    }

    private double meanNewMoonForLunation(int L){
        return JD_0 + L * MEAN_SYNODIC_MONTH;
    }

    /**
     * Calculates the Julian Ephemeris Date (JDE) for the new moon of lunation k
     * using a version of Meeus's algorithm with the main periodic terms.
     *
     * @param k the lunation number relative to a reference new moon.
     * @return the computed Julian Ephemeris Date (JDE) for the new moon.
     */
    // --- Precise New Moon Calculation ---
    public double preciseNewMoonForLunationFull(int k) {
        double T = k / 1236.85;
        double T2 = T * T;
        double T3 = T2 * T;
        double T4 = T3 * T;

        double JDE_mean = JDE_REF
                + MEAN_SYNODIC_MONTH * k
                + 0.0001337 * T2
                - 0.000000150 * T3
                + 0.00000000073 * T4;

        double M = 2.5534 + 29.10535669 * k - 0.0000014 * T2 - 0.00000011 * T3;
        double Mprime = 201.5643 + 385.81693528 * k + 0.0107582 * T2
                + 0.00001238 * T3 - 0.000000058 * T4;
        double F = 160.7108 + 390.67050274 * k - 0.0016118 * T2
                - 0.00000227 * T3 + 0.000000011 * T4;

        M = M % 360.0; if (M < 0) M += 360.0;
        Mprime = Mprime % 360.0; if (Mprime < 0) Mprime += 360.0;
        F = F % 360.0; if (F < 0) F += 360.0;

        double Mrad = Math.toRadians(M);
        double MprimeRad = Math.toRadians(Mprime);
        double Frad = Math.toRadians(F);

        double deltaJDE =
                -0.40720 * Math.sin(MprimeRad)
                        + 0.17241 * Math.sin(Mrad)
                        + 0.01608 * Math.sin(2 * MprimeRad)
                        + 0.01039 * Math.sin(2 * Frad)
                        + 0.00739 * Math.sin(MprimeRad - Mrad)
                        - 0.00514 * Math.sin(MprimeRad + Mrad)
                        + 0.00208 * Math.sin(2 * Mrad)
                        - 0.00111 * Math.sin(MprimeRad - 2 * Frad)
                        - 0.00057 * Math.sin(MprimeRad + 2 * Frad)
                        + 0.00056 * Math.sin(2 * MprimeRad + Mrad)
                        - 0.00042 * Math.sin(3 * MprimeRad);

        return JDE_mean + deltaJDE;
    }

    /**
     * Determines the length of a lunation (in days) by computing the difference
     * between two consecutive new moons using the precise algorithm.
     *
     * @param k the lunation number.
     * @return the length (in days) of the lunation from k to k+1.
     */
    public double lunationLength(int k) {
        double newMoonK = preciseNewMoonForLunationFull(k);
        double newMoonKPlus1 = preciseNewMoonForLunationFull(k + 1);
        return newMoonKPlus1 - newMoonK;
    }

    /**
     * Determines the number of days in a given lunation (either 29 or 30) using
     * a threshold (29.5 days). If the lunation length is less than 29.5 days, it is
     * considered a 29-day lunation; otherwise, a 30-day lunation.
     *
     * @param k the lunation number.
     * @return 29 or 30, representing the days in that lunation.
     */
    public int daysInLunation(int k) {
        double length = lunationLength(k);
        return (length < MEAN_SYNODIC_MONTH) ? 29 : 30;
    }

    private double meanNewMoonForLunationAdjusted(int L){

        var timezone = getTimeZone();
        return meanNewMoonForLunation(L) + (timezone.getDSTSavings() + timezone.getRawOffset()) / 86400000.0 / 24;
    }

    private double eclipsePredictionKth(int k){

        return JD_Eclipse_0 + k * SAROS_CYCLE;
    }

    private double eclipsePredictionKthExeligmos(int k){
        return JD_Eclipse_0 + k * 3 * SAROS_CYCLE;
    }

    private double marchEquinoxPrediction(int year){
        return JD_MARCH_SOLSTICE + (year - CONSTANTS_YEAR) * TROPICAL_YEAR;
    }

    private double septemberEquinoxPrediction(int year){
        return JD_SEPTEMBER_EQUINOX + (year - CONSTANTS_YEAR) * TROPICAL_YEAR;
    }

    private double juneSolsticePrediction(int year){
        return JD_JUNE_SOLSTICE + (year - CONSTANTS_YEAR) * TROPICAL_YEAR;
    }

    public double decemberSolsticePrediction(int year){
        return JD_DECEMBER_SOLSTICE + (year - CONSTANTS_YEAR) * TROPICAL_YEAR;
    }

    private double fullMoonPrediction(int lunation){
        return meanNewMoonForLunationAdjusted(lunation) + MEAN_SYNODIC_MONTH/2;
    }

    private double firstQuarterMoonPrediction(int lunation){
        return meanNewMoonForLunationAdjusted(lunation) + MEAN_SYNODIC_MONTH/4;
    }

    private double thirdQuarterMoonPrediction(int lunation){
        return meanNewMoonForLunationAdjusted(lunation) + 3*MEAN_SYNODIC_MONTH/4;
    }

    private double deipnonPrediction(int lunation){
        return meanNewMoonForLunationAdjusted(lunation)-1;
    }

    public double GetJulianTime(long timeInMIllis){
        return getCurrentJD(timeInMIllis,false);
    }

    public double GetJulianTimeTimezoneAdjusted(long timeInMIllis){
        return getCurrentJD(timeInMIllis,true);
    }

    /**
     * Converts the system time to a Julian Date (JD) in UTC.
     */
    private double getCurrentJD() {
        return  getCurrentJD(System.currentTimeMillis(), true);
    }

    private double getCurrentJD(long timeInMillis, boolean correctTimezone) {
        // Unix epoch (1970-01-01 00:00:00 UTC) corresponds to JD 2440587.5
        double jdUtc = (timeInMillis / 86400000.0) + JD_OF_UNIX_0;

        if(correctTimezone) {
            // If you want local time, add the time zone offset (in ms) converted to days.
            int offset = getTimeZone().getOffset(timeInMillis);
            jdUtc += offset / 86400000.0;
        }
        return jdUtc;
    }

    // Function to calculate the current year
    public int calculateCurrentYearFromSolstice() {
        double currentJD = getCurrentJD();
        double yearsPassed = (currentJD - JD_FIRST_SOLSTICE) / TROPICAL_YEAR;
        // we add 1 year because our counting begins at 1 not at zero
        return (int) Math.floor(yearsPassed) + 1;
    }

    // Compute the lunation number for the first new moon after the winter solstice using Math.ceil,
    // avoiding an iterative loop.
    public int getFirstLunationAfterSolstice(int year) {
        double winterSolsticeJD = decemberSolsticePrediction(year);
        // Compute the offset from our reference new moon to the winter solstice.
        // Then, the first lunation after the solstice is:
        return (int) Math.ceil((winterSolsticeJD - JD_0) / MEAN_SYNODIC_MONTH);
    }

    // Retrieve the lunation name for a given year and lunation.
    // The lunation numbering resets each year such that the first new moon after the winter solstice
    // begins the new cycle.
    public String getLunationName(int year, int lunation) {
        int firstLunation = getFirstLunationAfterSolstice(year);
        int index = (lunation - firstLunation) % LUNATION_INFO.length;
        if (index < 0) index += LUNATION_INFO.length;
        return LUNATION_INFO[index][0];
    }

    // Retrieve the lunation description for a given year and lunation.
    public String getLunationDescription(int year, int lunation) {
        int firstLunation = getFirstLunationAfterSolstice(year);
        int index = (lunation - firstLunation) % LUNATION_INFO.length;
        if (index < 0) index += LUNATION_INFO.length;
        return LUNATION_INFO[index][1];
    }

    /**
     * Returns the name of the planet week day corresponding to the given Julian Date.
     *
     * @param timeInMillis time in milliseconds
     * @param correctForTZ true if you want to adjust for timezone
     * @return The name of the week day in the 8-day cycle.
     */
    public String getWeekDayFromJD(long timeInMillis, boolean correctForTZ) {
        return getWeekDayFromJD(getCurrentJD(timeInMillis, correctForTZ));
    }

    public String getWeekDayFromJD(double jd) {
        // Calculate the number of whole days elapsed since the week epoch.
        long daysSinceEpoch = (long) Math.floor(jd - WEEK_EPOCH);

        // Compute the index in the 8-day cycle. The double modulo ensures non-negative index.
        int index = (int) ((daysSinceEpoch % 8 + 8) % 8);

        return PLANET_WEEK_DAYS[index];
    }

    /**
     * Returns a formatted string for the given julian that shows:
     * - Selene Year
     * - Lunation (month) number and its name
     * - Day within the lunation
     * - 8-day week day name
     * - The computed Julian Date (JD)
     *
     * This method performs a read-only conversion based on the precise new moon calculation
     * and the conversion formulas in computeFields(), without updating any internal state.
     *
     * @param JD the julian time in milliseconds (since Unix epoch)
     * @return a formatted string representation of the corresponding Selene date
     */
    public String toString(long JD) {
        // Estimate global lunation number using the mean synodic month.
        // kApprox is based on our reference JDE_REF.
        double kApprox = (JD - JDE_REF) / MEAN_SYNODIC_MONTH;
        // Compute K₀ so that when global lunation L=0, we align with JD_START_OF_TIME.
        double K0 = (JD_START_OF_TIME - JDE_REF) / MEAN_SYNODIC_MONTH;
        int kOffset = (int) Math.round(K0);

        // Compute L: the number of lunations since BASE_YEAR.
        int L = (int) Math.floor(kApprox - K0);

        // Compute the JD for the new moon of lunation (L + kOffset)
        double newMoonJD = preciseNewMoonForLunationFull(L + kOffset);

        // Compute the day within the lunation: add 1 because DATE is 1-indexed.
        int computedDate = (int) Math.floor(JD - newMoonJD) + 1;

        // If computedDate exceeds the actual maximum (29 or 30), adjust by rolling over.
        int maxDate = daysInLunation(L + kOffset);
        if (computedDate > maxDate) {
            computedDate -= maxDate;
            L++;
        }

        // Determine the Selene year and lunation (month) from the global lunation number.
        int computedYear = L / 13 + BASE_YEAR;
        int computedMonth = L % 13;

        // Get the day of the 8-day week using your existing method.
        String weekDay = getWeekDayFromJD(JD);
        // Get the lunation name using your existing method.
        String lunationName = getLunationName(computedYear, computedMonth);

        // Return a formatted string with all components.
        return "Selene Date: Year " + computedYear +
                ", Lunation " + computedMonth + " " + lunationName +
                ", Day " + computedDate +
                ", Weekday " + weekDay;
    }
}
