/*
MIT License

Copyright (c) 2016 Kent Randall

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package org.point85.workschedule;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Class Rotation maintains a sequenced list of shift and off-shift time
 * periods.
 * 
 * @author Kent Randall
 *
 */
public class Rotation extends Named implements Comparable<Rotation> {

	// working periods in the rotation
	private List<RotationSegment> rotationSegments = new ArrayList<>();

	// list of working and non-working days
	private List<TimePeriod> periods;

	// 비번 이름
	public static final String DAY_OFF_NAME = "DAY_OFF";

	// 비번 객체
	private static final DayOff DAY_OFF = initializeDayOff();


	// 휴무 이름
	public static final String DAY_BREAK_NAME = "DAY_BREAK";
	
	// 휴무 객체 
	private static final DayBreak DAY_BREAK = initializeDayBreak();


	// owning work schedule
	private WorkSchedule workSchedule;

	/**
	 * Default constructor
	 */
	public Rotation() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param name        Rotation name
	 * @param description Description
	 * @throws Exception Exception
	 */
	Rotation(String name, String description) throws Exception {
		super(name, description);
	}

	// 비번 객체 09:00 부터 비번시작
	private static DayOff initializeDayOff() {
		DayOff dayOff = null;
		try {

			dayOff = new DayOff(DAY_OFF_NAME, "09 After off period",EnumTimePeriod.DAY_OFF, LocalTime.of(9, 00, 00), Duration.ofHours(24));
		} catch (Exception e) {
			// ignore
		}
		return dayOff;
	}

	// 휴무객체 24시간 휴무
	private static DayBreak initializeDayBreak() {
		DayBreak dayBreak = null;
		try {
			dayBreak = new DayBreak(DAY_BREAK_NAME, "24 hour off period",EnumTimePeriod.DAY_BREAK, LocalTime.MIDNIGHT, Duration.ofHours(24));
		} catch (Exception e) {
			// ignore
		}
		return dayBreak;
	}

	/**
	 * Get the shifts and off-shifts in the rotation
	 * 
	 * @return List of periods
	 */
	public List<TimePeriod> getPeriods() {
		if (periods == null) {
			periods = new ArrayList<>();

			// sort by sequence number
			Collections.sort(rotationSegments);

			for (RotationSegment segment : rotationSegments) {
				// DayShift (주간근무) 또는 NightShift  (야간근무)
				if (segment.getStartingShift() != null) {

					//daysOn 의 숫자 만큼 DayShift 또는 NightShift  를 추가한다.
					for (int i = 0; i < segment.getDaysOn(); i++) {

						periods.add(segment.getStartingShift());
					}
				}

				// 비번 갯수 만큼 추가
				for (int i = 0; i < segment.getDaysOff(); i++) {
					periods.add(Rotation.DAY_OFF);
				}

				// 휴무 갯수 만큼 추가
				for (int i = 0; i < segment.getDaysBreak(); i++) {
					periods.add(Rotation.DAY_BREAK);
				}
			}
		}

		return periods;
	}

	//  periods 설정
	//  같은날, 같은주기로 당직이 돌아가는 팀이 있으면, 팀의 주기를 변경할 경우 사용
	public void setPeriods(List<TimePeriod> periods) {
		this.periods = periods;
	}

	/**
	 * Get the number of days in the rotation
	 * 
	 * @return Day count
	 */

	public int getDayCount() {
		return getPeriods().size();
	}

	/**
	 * 로테이션 주기
	 * 
	 * @return Duration
	 */
	public Duration getDuration() {
		int nPeriodSize = getPeriods().size();
		return Duration.ofDays(nPeriodSize);
	}

	/**
	 * Get the shift rotation's total working time
	 * 
	 * @return Duration of working time
	 */
	public Duration getWorkingTime() {
		Duration sum = Duration.ZERO;

		for (TimePeriod period : getPeriods()) {
			if (period.isWorkingPeriod()) {
				sum = sum.plus(period.getDuration());
			}
		}
		return sum;
	}

	/**
	 * Get the rotation's working periods
	 * 
	 * @return List of {@link RotationSegment}
	 */
	public List<RotationSegment> getRotationSegments() {
		return rotationSegments;
	}

	/**
	 *  이 순환에 작업 기간을 추가합니다. 근무 기간은 교대 근무로 시작하여 근무일과 휴일을 지정합니다.
	 *
	 * @param startingShift {@link Shift} that starts the period
	 * @param daysOn        근무 갯수 (Shift 객체의 수)  - 주간 또는 야간 근무
	 * @param daysOff       근무 off 갯수  (DayOff 객체의 수) - 비번
	 * @param daysBreak    휴무 갯수  (DayOff 객체의 수) - 휴무 (youngil 추가)
	 * @return {@link RotationSegment}
	 * @throws Exception Exception
	 */
	public RotationSegment addSegment(Shift startingShift, int daysOn, int daysOff, int daysBreak) throws Exception {
		if (startingShift == null) {
			throw new Exception("The starting shift must be specified.");
		}
		RotationSegment segment = new RotationSegment(startingShift, daysOn, daysOff, daysBreak, this);
		rotationSegments.add(segment);
		int nSize = rotationSegments.size();
		segment.setSequence(nSize);
		return segment;
	}

	/**
	 * Get the work schedule that owns this rotation
	 * 
	 * @return {@link WorkSchedule}
	 */
	public WorkSchedule getWorkSchedule() {
		return workSchedule;
	}

	void setWorkSchedule(WorkSchedule workSchedule) {
		this.workSchedule = workSchedule;
	}

	@Override
	public int compareTo(Rotation other) {
		return getName().compareTo(other.getName());
	}
	
	/**
	 * Compare this Rotation to another Rotation
	 * 
	 * @return true if equal
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Rotation)) {
			return false;
		}
		Rotation otherRotation = (Rotation) other;

		// same work schedule
		if (getWorkSchedule() != null && otherRotation.getWorkSchedule() != null) {
			if (!getWorkSchedule().equals(otherRotation.getWorkSchedule())) {
				return false;
			}
		}

		return super.equals(other);
	}
	
	/**
	 * Get the hash code
	 * 
	 * @return hash code
	 */
	@Override
	public int hashCode() {
		return Objects.hash(getName(), getWorkSchedule());
	}

	/**
	 * Build a string representation of this rotation
	 */
	@Override
	public String toString() {
		String named = super.toString();
		String rd = WorkSchedule.getMessage("rotation.duration");
		String rda = WorkSchedule.getMessage("rotation.days");
		String rw = WorkSchedule.getMessage("rotation.working");
		String rper = WorkSchedule.getMessage("rotation.periods");
		String on = WorkSchedule.getMessage("rotation.on");
		String off = WorkSchedule.getMessage("rotation.off");

		String periodsString = "";

		for (TimePeriod period : getPeriods()) {
			if (periodsString.length() > 0) {
				periodsString += ", ";
			}

			String onOff = period.isWorkingPeriod() ? on : off;
			periodsString += period.getName() + " (" + onOff + ")";
		}

		return named + "\n" + rper + ": [" + periodsString + "], " + rd + ": " + getDuration() + ", " + rda + ": "
				+ getDuration().toDays() + ", " + rw + ": " + getWorkingTime();
	}
}
