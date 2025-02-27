package com.ilisianstudios.calendar.seleneCalendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar

class SeleneCalendarTests {

    @Test
    fun testWinterSolsticeCalculation() {
        val calendar =
            SeleneCalendar()
        val expectedJD = 2460665.888889 // Winter Solstice 2024
        val actualJD = calendar.decemberSolsticePrediction(2024)
        assertEquals(expectedJD, actualJD, 0.01)
    }

    @Test
    fun testFirstLunationAfterSolstice() {
        val calendar =
            SeleneCalendar()
        val lunation = calendar.getFirstLunationAfterSolstice(2024)
        assertTrue(lunation > 0)

        for (i in lunation until lunation + 13) {
            val lunationJD: String = calendar.getLunationName(2024, i)
            println("Lunation #$i: JD = ${lunationJD}, Name: ${calendar.getLunationName(2024, i)}")

        }
    }

    @Test
    fun testSolsticeYearCalculation() {
        val calendar =
            SeleneCalendar()
        val seleneyear = calendar.calculateCurrentYearFromSolstice()

        // For example, if today's Gregorian year is 2023, one might expect:
        // expectedYear ≈ 2023 + 3761 = 5784.
        // (The exact number will depend on the precise value of JD_FIRST_WINTER_SOLSTICE.)
        val systemYear = GregorianCalendar().get(Calendar.YEAR)
        // we subtract one because that accounts for the Ad bc changeover
        val expectedYear = systemYear + 3761 - 1

        println("System (Gregorian) Year: $systemYear")
        println("Calculated Selene Year (from solstice): $seleneyear")
        println("Expected Selene Year (approx.): $expectedYear")

        // Allow a tolerance if necessary (for example, a difference of 1 year might occur).
        assertEquals(expectedYear, seleneyear)
    }

    @Test
    fun testPlanetWeekCycleWithTimeInMillis() {
        val calendar =
            SeleneCalendar()
        // The WEEK_EPOCH is defined as 2440587.5 (JD for 1970-01-01 00:00:00 UT)
        // If we pass timeInMillis corresponding to Unix epoch (0 ms), we expect the day to be the first in our list.
        var timeInMillis = ((SeleneCalendar.WEEK_EPOCH - 2440587.5) * 86400000L).toLong()
        val expectedWeekDays = SeleneCalendar.PLANET_WEEK_DAYS

        // Test a full cycle plus extra (e.g., 16 days)
        for (i in 0 until 16) {

            val weekDay = calendar.getWeekDayFromJD(timeInMillis, true)
            val expected = expectedWeekDays[i % 8]
            println("TimeInMillis: $timeInMillis -> Weekday: $weekDay (expected: $expected)")
            assertEquals("For offset of $i days, expected $expected", expected, weekDay)
            timeInMillis += 86400000L  // each day = 86,400,000 ms
        }
    }

    @Test
    fun testPlanetWeekCycleNegativeTime() {
        val calendar =
            SeleneCalendar()
        // Calculate the JD we want: one day before our WEEK_EPOCH.
        val jdDesired = SeleneCalendar.WEEK_EPOCH - 1.0
        // Convert desired JD to millis relative to Unix epoch.
        val timeInMillis = ((jdDesired - 2440587.5) * 86400000L).toLong()

        val weekDay = calendar.getWeekDayFromJD(timeInMillis, true)
        val expected = SeleneCalendar.PLANET_WEEK_DAYS[7] // "Neptuva"

        println("JD Desired: $jdDesired, timeInMillis: $timeInMillis")
        println("Weekday: $weekDay (expected: $expected)")
        assertEquals("Negative offset should yield 'Neptuva'", expected, weekDay)
    }

    @Test
    fun testPlanetWeekCycleAtEpoch() {
        val calendar =
            SeleneCalendar()
        // For time exactly equal to WEEK_EPOCH:
        val timeInMillis = ((SeleneCalendar.WEEK_EPOCH - 2440587.5) * 86400000L).toLong()
        val weekDay = calendar.getWeekDayFromJD(timeInMillis, true)
        val expected = SeleneCalendar.PLANET_WEEK_DAYS[0] // "Merva"
        println("TimeInMillis: $timeInMillis -> Weekday: $weekDay (expected: $expected)")
        assertEquals("At epoch, expected 'Merva'", expected, weekDay)
    }

    @Test
    fun testAddYear() {
        val calendar =
            SeleneCalendar()
        // Set initial Selene year explicitly (e.g., 5780)
        calendar.set(Calendar.YEAR, 5780)
        val initialYear = calendar.get(Calendar.YEAR)
        // Add 5 years
        calendar.add(Calendar.YEAR, 5)
        val newYear = calendar.get(Calendar.YEAR)
        println("Initial YEAR: $initialYear, after add(5): $newYear")
        assertEquals(initialYear + 5, newYear)
    }

    @Test
    fun testAddMonth() {
        val calendar =
            SeleneCalendar()
        // Set initial YEAR and MONTH
        calendar.set(Calendar.YEAR, 5780)
        calendar.set(Calendar.MONTH, 0)
        // Add 1 month, expecting MONTH to become 1 and YEAR unchanged
        calendar.add(Calendar.MONTH, 1)
        assertEquals(1, calendar.get(Calendar.MONTH))
        assertEquals(5780, calendar.get(Calendar.YEAR))

        // Now, reset to MONTH 0 and add 13 months.
        calendar.set(Calendar.MONTH, 0)
        calendar.set(Calendar.YEAR, 5780)
        calendar.add(Calendar.MONTH, 13)
        // With 13 lunations, we expect MONTH to wrap to 0 and YEAR to increment by 1.
        assertEquals(0, calendar.get(Calendar.MONTH))
        assertEquals(5781, calendar.get(Calendar.YEAR))
    }

    @Test
    fun testAddDateOverflow() {
        val calendar =
            SeleneCalendar()
        // Set a known lunation date.
        calendar.set(Calendar.YEAR, 5780)
        calendar.set(Calendar.MONTH, 0)
        val maxDate = calendar.getActualMaximum(Calendar.DATE)
        // Set DATE to one less than the maximum.
        calendar.set(Calendar.DATE, maxDate - 1)
        calendar.computeTime()
        // Add 2 days; should overflow into the next lunation.
        calendar.add(Calendar.DATE, 2)
        println("After add(2) from DATE=${maxDate - 1}: YEAR=${calendar.get(Calendar.YEAR)}, MONTH=${calendar.get(Calendar.MONTH)}, DATE=${calendar.get(Calendar.DATE)}")
        // Expect DATE resets to 1 and MONTH increments by 1.
        assertEquals("After overflow, DATE should be 1", 1, calendar.get(Calendar.DATE))
        assertEquals("After overflow, MONTH should be 1", 1, calendar.get(Calendar.MONTH))
    }

    @Test
    fun testRollMonth() {
        val calendar =
            SeleneCalendar()
        // Set initial YEAR and MONTH
        calendar.set(Calendar.YEAR, 5780)
        calendar.set(Calendar.MONTH, 0)
        // Roll month up once (should change MONTH from 0 to 1 without affecting YEAR)
        calendar.roll(Calendar.MONTH, true)
        assertEquals(1, calendar.get(Calendar.MONTH))
        assertEquals(5780, calendar.get(Calendar.YEAR))
        // Roll month down once: from 1 back to 0.
        calendar.roll(Calendar.MONTH, false)
        assertEquals(0, calendar.get(Calendar.MONTH))
        // Roll month down once more from 0. With 13 lunations, this should wrap to 12.
        calendar.roll(Calendar.MONTH, false)
        assertEquals(12, calendar.get(Calendar.MONTH))
        // YEAR should remain unchanged when rolling.
        assertEquals(5780, calendar.get(Calendar.YEAR))
    }

    @Test
    fun testRollDate() {
        val calendar =
            SeleneCalendar()
        // Set known lunation date: YEAR=5780, MONTH=0, DATE=1.
        calendar.set(Calendar.YEAR, 5780)
        calendar.set(Calendar.MONTH, 0)
        calendar.set(Calendar.DATE, 1)
        // Get the precise maximum days in this lunation.
        val maxDate = calendar.getActualMaximum(Calendar.DATE)
        println("Initial max date for lunation: $maxDate")

        // Roll DATE up: from 1 to 2.
        calendar.roll(Calendar.DATE, true)
        println("After rolling DATE up: ${calendar.get(Calendar.DATE)} (expected 2)")
        assertEquals("Rolling DATE up from 1 should yield 2", 2, calendar.get(Calendar.DATE))

        // Roll DATE down: should return to 1.
        calendar.roll(Calendar.DATE, false)
        println("After rolling DATE down: ${calendar.get(Calendar.DATE)} (expected 1)")
        assertEquals("Rolling DATE down from 2 should yield 1", 1, calendar.get(Calendar.DATE))

        // Roll DATE down from 1: should wrap to maxDate.
        calendar.roll(Calendar.DATE, false)
        println("After rolling DATE down from 1: ${calendar.get(Calendar.DATE)} (expected $maxDate)")
        assertEquals("Rolling DATE down from 1 should wrap to maxDate", maxDate, calendar.get(Calendar.DATE))
    }

    @Test
    fun testGetActualMaximumForDate() {
        val calendar =
            SeleneCalendar()
        // Set a known lunation.
        calendar.set(Calendar.YEAR, 5780)
        calendar.set(Calendar.MONTH, 0)
        // The actual maximum for DATE should equal either 29 or 30, as computed by daysInLunation.
        val actualMax = calendar.getActualMaximum(Calendar.DATE)
        println("For YEAR: ${calendar.get(Calendar.YEAR)} and MONTH: ${calendar.get(Calendar.MONTH)}, actual max DATE = $actualMax")
        assertTrue("Actual max DATE should be 29 or 30", actualMax == 29 || actualMax == 30)
    }

    @Test
    fun testComputeTimeAndFieldsRoundTrip() {
        val calendar =
            SeleneCalendar()
        // Set a known Selene date.
        calendar.set(Calendar.YEAR, 5780)
        calendar.set(Calendar.MONTH, 0)   // first lunation
        calendar.set(Calendar.DATE, 1)    // first day
        // Convert fields to internal time.
        calendar.computeTime()
        val timeMillis = calendar.timeInMillis

        // Save expected field values.
        val expectedYear = calendar.get(Calendar.YEAR)
        val expectedMonth = calendar.get(Calendar.MONTH)
        val expectedDate = calendar.get(Calendar.DATE)

        // Clear fields, set time, and recompute fields.
        calendar.clear()
        calendar.timeInMillis = timeMillis
        calendar.computeFields()

        println("Round-trip: Expected $expectedYear-${expectedMonth}-${expectedDate}, got " +
                "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DATE)}")
        assertEquals(expectedYear, calendar.get(Calendar.YEAR))
        assertEquals(expectedMonth, calendar.get(Calendar.MONTH))
        assertEquals(expectedDate, calendar.get(Calendar.DATE))
    }

    @Test
    fun testAddDateAcrossLunation() {
        val calendar =
            SeleneCalendar()
        // Set a known lunation date.
        calendar.set(Calendar.YEAR, 5780)
        calendar.set(Calendar.MONTH, 0)
        // Obtain the actual maximum day for the current lunation using our precise calculation.
        val maxDate = calendar.getActualMaximum(Calendar.DATE)
        // Set DATE to one day before the maximum.
        calendar.set(Calendar.DATE, maxDate - 1)
        calendar.computeTime() // Update internal time from fields.

        // Now add 2 days, which should cause DATE to overflow and roll into the next lunation.
        calendar.add(Calendar.DATE, 2)
        // Expect the DATE to become 1 and the MONTH to increment by 1.
        println("After adding 2 days: YEAR = ${calendar.get(Calendar.YEAR)}, " +
                "MONTH = ${calendar.get(Calendar.MONTH)}, DATE = ${calendar.get(Calendar.DATE)}")
        assertEquals(1, calendar.get(Calendar.DATE))
        assertEquals(1, calendar.get(Calendar.MONTH))
    }

    @Test
    fun testComputeFieldsFromTime() {
        // Choose an arbitrary Julian Date, for instance, JD = 2450000.0.
        val JD = 2450000.0
        // Convert JD to milliseconds since Unix epoch using the standard relation.
        val timeMillis = ((JD - SeleneCalendar.JD_OF_UNIX_0) * 86400000.0).toLong()

        val calendar =
            SeleneCalendar()
        calendar.setTimeInMillis(timeMillis)
        calendar.computeFields()

        println("From JD $JD: computed YEAR = ${calendar.get(Calendar.YEAR)}, " +
                "MONTH = ${calendar.get(Calendar.MONTH)}, DATE = ${calendar.get(Calendar.DATE)}")
        // Without an external reference for the correct fields, we manually inspect the output.
    }
}