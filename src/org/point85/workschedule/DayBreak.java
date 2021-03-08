package org.point85.workschedule;

import java.time.Duration;
import java.time.LocalTime;

//비번객체
public class DayBreak  extends TimePeriod{


    // Construct a period of time when not working
    DayBreak(String name, String description, EnumTimePeriod enumTimePeriod, LocalTime start, Duration duration) throws Exception {
        super(name, description, enumTimePeriod, start, duration);
    }


    @Override
    boolean isWorkingPeriod() {
        return false;
    }

    @Override
    boolean isWorkingOffPeriod() {
        return true;
    }

    @Override
    EnumTimePeriod getEnumTimePeriod() {
        return super.enumTimePeriod;
    }

    @Override
    void setEnumTimePeriod(EnumTimePeriod enumTimePeriod) {
        super.enumTimePeriod=enumTimePeriod;
    }
}
