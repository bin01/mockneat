package com.mockneat.mock.unit.time;

/*
 * Copyright 2017, Andrei N. Ciobanu

 Permission is hereby granted, free of charge, to any user obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import com.mockneat.mock.MockNeat;
import com.mockneat.mock.interfaces.MockUnitLocalDate;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

import static com.mockneat.mock.utils.ValidationUtils.INPUT_PARAMETER_NOT_NULL;
import static com.mockneat.mock.utils.ValidationUtils.LOWER_DATE_SMALLER_THAN_UPPER_DATE;
import static java.time.LocalDate.*;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

public class LocalDates implements MockUnitLocalDate {

    private static final long DEFAULT_DAYS_BEFORE = 10;
    private static final long DEFAULT_DAYS_AFTER = 10;
    public static final LocalDate EPOCH_START = ofEpochDay(0);

    private MockNeat mock;

    public LocalDates(MockNeat mock) {
        this.mock = mock;
    }

    @Override
    public Supplier<LocalDate> supplier() {
        return between(EPOCH_START, LocalDate.now())::val;
    }

    public MockUnitLocalDate thisYear() {
        Supplier<LocalDate> supp = () -> {
            int year = now().getYear();
            int maxDays = now().lengthOfYear() + 1;
            int randDay = mock.ints().range(1, maxDays).val();
            return LocalDate.ofYearDay(year, randDay);
        };
        return () -> supp;
    }

    public MockUnitLocalDate thisMonth() {
        Supplier<LocalDate> supp = () -> {
            int year = now().getYear();
            Month month = now().getMonth();
            int lM = now().lengthOfMonth() + 1;
            int randDay = mock.ints().range(1, lM).val();
            return LocalDate.of(year, month, randDay);
        };
        return () -> supp;
    }

    public MockUnitLocalDate between(LocalDate lowerDate, LocalDate upperDate) {
        notNull(lowerDate, INPUT_PARAMETER_NOT_NULL, "lowerDate");
        notNull(upperDate, INPUT_PARAMETER_NOT_NULL, "upperDate");
        isTrue(lowerDate.compareTo(upperDate)<0,
                LOWER_DATE_SMALLER_THAN_UPPER_DATE,
                lowerDate,
                upperDate);
        Supplier<LocalDate> supp = () -> {
            long lowerEpoch = lowerDate.toEpochDay();
            long upperEpoch = upperDate.toEpochDay();
            long diff = upperEpoch - lowerEpoch;
            long randEpoch = mock.longs().range(0, diff).val();
            return ofEpochDay(lowerEpoch + randEpoch);
        };
        return ()-> supp;
    }

    public MockUnitLocalDate future(LocalDate max) {
        return between(now(), max);
    }

    public MockUnitLocalDate past(LocalDate min) {
        return between(min, now());
    }

    public MockUnitLocalDate around(LocalDate date, long days) {
        //TODO
        notNull(date, INPUT_PARAMETER_NOT_NULL, "date");
        return null;
    }

    public MockUnitLocalDate around(LocalDate date, ChronoUnit unit, long unitsBefore, long unitsAfter) {
        notNull(date, INPUT_PARAMETER_NOT_NULL, "date");
        isTrue(unit.getDuration().compareTo(DAYS.getDuration())>=0);
        isTrue(date.minus(unitsBefore, unit).compareTo(MIN)>=0);
        isTrue(date.plus(unitsAfter + 1, unit).compareTo(MAX)<=0);

        LocalDate lower = date.minus(unitsBefore, unit);
        LocalDate upper = date.minus(unitsAfter + 1, unit);

        return between(lower, upper);
    }

    public MockUnitLocalDate around(LocalDate date, long daysBefore, long daysAfter) {
        return around(date, DAYS, daysBefore, daysAfter);
    }

    public MockUnitLocalDate around(LocalDate date) {
        return around(date, DAYS, DEFAULT_DAYS_BEFORE, DEFAULT_DAYS_AFTER);
    }

    public MockUnitLocalDate aroundToday() {
        return around(now());
    }
}