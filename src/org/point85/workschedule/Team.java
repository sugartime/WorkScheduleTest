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

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Class Team is a named group of individuals who rotate through a shift
 * schedule.
 * 
 * @author Kent Randall
 *
 */
public class Team extends Named implements Comparable<Team> {
	// owning work schedule
	private WorkSchedule workSchedule;

	// reference date for starting the rotations
	private LocalDate rotationStart;

	// shift rotation days
	private Rotation rotation;

	/**
	 * Default constructor
	 */
	public Team() {
		super();
	}

	Team(String name, String description, Rotation rotation, LocalDate rotationStart) throws Exception {
		super(name, description);
		this.rotation = rotation;
		this.setRotationStart(rotationStart);
	}

	/**
	 * Get rotation start
	 * 
	 * @return Rotation start date
	 */
	public LocalDate getRotationStart() {
		return rotationStart;
	}

	/**
	 * Get rotation start
	 * 
	 * @param rotationStart
	 *            Starting date of rotation
	 */
	public void setRotationStart(LocalDate rotationStart) {
		this.rotationStart = rotationStart;
	}

	private long getDayFrom() {
		return rotationStart.toEpochDay();
	}

	/**
	 * Get the shift rotation for this team
	 * 
	 * @return {@link Rotation}
	 */
	public Rotation getRotation() {
		return rotation;
	}

	/**
	 * Set the shift rotation for this team
	 * 
	 * @param rotation
	 *            {@link Rotation}
	 */
	public void setRotation(Rotation rotation) {
		this.rotation = rotation;
	}

	/**
	 * Get the duration of the shift rotation
	 * 
	 * @return Duration
	 * @throws Exception
	 *             exception
	 */
	public Duration getRotationDuration() throws Exception {
		return getRotation().getDuration();
	}

	/**
	 * Get the shift rotation's working time as a percentage of the rotation
	 * duration
	 * 
	 * @return Percentage
	 * @throws Exception
	 *             exception
	 */
	public float getPercentageWorked() throws Exception {
		return ((float) getRotation().getWorkingTime().getSeconds()) / ((float) getRotationDuration().getSeconds())
				* 100.0f;
	}

	/**
	 * Get the average number of hours worked each week by this team
	 * 
	 * @return Duration of hours worked per week
	 */
	public Duration getHoursWorkedPerWeek() {
		float days = (float) getRotation().getDuration().toDays();
		float secPerWeek = (float) getRotation().getWorkingTime().getSeconds() * (7.0f / days);
		return Duration.ofSeconds((long) secPerWeek);
	}

	/**
	 * Get the day number in the rotation for this local date
	 * 
	 * @param date
	 *            LocalDate
	 * @return day number in the rotation, starting at 1
	 * @throws Exception
	 *             exception
	 */
	public int getDayInRotation(LocalDate date) throws Exception {
		// 회전 시작부터 총 일수 계산
		long dayTo = date.toEpochDay();
		long dayFrom = getDayFrom(); //당직시작을 epchDay 로 변환
		long deltaDays = dayTo - dayFrom;

		if (deltaDays < 0) {
			String msg = MessageFormat.format(WorkSchedule.getMessage("end.earlier.than.start"), rotationStart, date);
			throw new Exception(msg);
		}

		return (int) (deltaDays % getRotation().getDuration().toDays()) + 1;
	}
	/**
	 * Get the {@link ShiftInstance} for the specified day
	 *
	 * @param day
	 *            Day with a shift instance
	 * @return {@link ShiftInstance}
	 * @throws Exception
	 *             exception
	 *
	 *   여기서 rotation.periods 에 값이 할당된다.
	 */
	public ShiftInstance getShiftInstanceForDay(LocalDate day) throws Exception {
		ShiftInstance instance = null;

		Rotation shiftRotation = getRotation();

		//여기서 rotation.periods 에 값이 할당된다. <=  rotation.getPeriods()
		// getDuration => periods 의 size
		if (shiftRotation.getDuration().equals(Duration.ZERO)) {
			// no instance for that day
			return instance;
		}
		
		int dayInRotation = -1;
		try {
			dayInRotation=getDayInRotation(day);
		}catch (Exception ex){
			 System.out.println("getShiftInstanceForDay():" +"end earier than start");
			return null;
		}
		// shift or off shift
		TimePeriod period = shiftRotation.getPeriods().get(dayInRotation - 1);

		if (period.isWorkingPeriod()) {
			LocalDateTime startDateTime = LocalDateTime.of(day, period.getStart());
			instance = new ShiftInstance((Shift) period, startDateTime, this);
		}else{
			//  youngil 추가
			LocalDateTime startDateTime = LocalDateTime.of(day, LocalTime.MIDNIGHT);
			if(period.enumTimePeriod.equals(EnumTimePeriod.DAYOFF)){
				instance = new ShiftInstance((DayOff)period,startDateTime,this);
			}

		}

		return instance;
	}

	/**
	 * Check to see if this day is a day off
	 * 
	 * @param day
	 *            Date to check
	 * @return True if a day off
	 * @throws Exception
	 *             Exception
	 */
	public boolean isDayOff(LocalDate day) throws Exception {

		boolean dayOff = false;

		Rotation shiftRotation = getRotation();
		int dayInRotation = getDayInRotation(day);

		// shift or off shift
		TimePeriod period = shiftRotation.getPeriods().get(dayInRotation - 1);

		if (!period.isWorkingPeriod()) {
			dayOff = true;
		}

		return dayOff;

	}

	/**
	 * 지정된 날짜와 시간 사이의 일정 작업 시간을 계산합니다.
	 * 
	 * @param from
	 *            시작 날짜 및 시간
	 * @param to
	 *            종료 날짜 및 시간
	 * @return 근무 시간
	 * @throws Exception
	 *             exception
	 */
	public Duration calculateWorkingTime(LocalDateTime from, LocalDateTime to) throws Exception {
		if (from.isAfter(to)) {
			String msg = MessageFormat.format(WorkSchedule.getMessage("end.earlier.than.start"), to, from);
			throw new Exception(msg);
		}

		Duration sum = Duration.ZERO;

		LocalDate thisDate = from.toLocalDate();
		LocalTime thisTime = from.toLocalTime();
		LocalDate toDate = to.toLocalDate();
		LocalTime toTime = to.toLocalTime();
		int dayCount = getRotation().getDayCount(); //로테이션 돌 갯수를 의미 (주,야,비,휴 이면 =>4)

		// get the working shift from yesterday
		Shift lastShift = null;

		LocalDate yesterday = thisDate.plusDays(-1);
		ShiftInstance yesterdayInstance = getShiftInstanceForDay(yesterday);

		if (yesterdayInstance != null) {
			lastShift = yesterdayInstance.getShift();
		}

		// step through each day until done
		while (thisDate.compareTo(toDate) < 1) {
			if (lastShift != null && lastShift.spansMidnight()) {
				// check for days in the middle of the time period
				boolean lastDay = thisDate.compareTo(toDate) == 0;
				
				if (!lastDay || (lastDay && !toTime.equals(LocalTime.MIDNIGHT))) {
					// add time after midnight in this day
					int afterMidnightSecond = lastShift.getEnd().toSecondOfDay();
					int fromSecond = thisTime.toSecondOfDay();

					if (afterMidnightSecond > fromSecond) {
						sum = sum.plusSeconds((long)afterMidnightSecond - (long)fromSecond);
					}
				}
			}

			// today's shift
			ShiftInstance instance = getShiftInstanceForDay(thisDate);

			Duration duration = null;

			if (instance != null) {
				lastShift = instance.getShift();
				// check for last date
				if (thisDate.compareTo(toDate) == 0) {
					duration = lastShift.calculateWorkingTime(thisTime, toTime, true);
				} else {
					duration = lastShift.calculateWorkingTime(thisTime, LocalTime.MAX, true);
				}
				sum = sum.plus(duration);
			} else {
				lastShift = null;
			}

			int n = 1;
			if (getDayInRotation(thisDate) == dayCount) {
				// move ahead by the rotation count if possible
				LocalDate rotationEndDate = thisDate.plusDays(dayCount);

				if (rotationEndDate.compareTo(toDate) < 0) {
					n = dayCount;
					sum = sum.plus(getRotation().getWorkingTime());
				}
			}

			// move ahead n days starting at midnight
			thisDate = thisDate.plusDays(n);
			thisTime = LocalTime.MIDNIGHT;
		} // end day loop

		return sum;
	}

	/**
	 * Get the work schedule that owns this team
	 * 
	 * @return {@link WorkSchedule}
	 */
	public WorkSchedule getWorkSchedule() {
		return workSchedule;
	}

	void setWorkSchedule(WorkSchedule workSchedule) {
		this.workSchedule = workSchedule;
	}

	/**
	 * Compare one team to another
	 */
	@Override
	public int compareTo(Team other) {
		return this.getName().compareTo(other.getName());
	}
	
	/**
	 * Compare this Team to another Team
	 * 
	 * @return true if equal
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Team)) {
			return false;
		}
		Team otherTeam = (Team) other;

		// same work schedule
		if (getWorkSchedule() != null && otherTeam.getWorkSchedule() != null) {
			if (!getWorkSchedule().equals(otherTeam.getWorkSchedule())) {
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
	 * Build a string value for this team
	 */
	@Override
	public String toString() {
		String rpct = WorkSchedule.getMessage("rotation.percentage");
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);

		String rs = WorkSchedule.getMessage("rotation.start");
		String avg = WorkSchedule.getMessage("team.hours");

		String text = "";
		try {
			text = super.toString() + ", " + rs + ": " + getRotationStart() + ", " + getRotation() + ", " + rpct + ": "
					+ df.format(getPercentageWorked()) + "%" + ", " + avg + ": " + getHoursWorkedPerWeek();

		} catch (Exception e) {
			// ignore
		}

		return text;
	}
}
