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

/**
 * Class Break is a defined working period of time during a shift, for example lunch.
 * @author Kent Randall
 *
 */
public class Break extends TimePeriod {

	/**
	 * Construct a period of time for a break
	 * 
	 * @param name
	 *            Name of break
	 * @param description
	 *            Description of break
	 * @param start
	 *            Starting time of day
	 * @param duration
	 *            Duration of break
	 * @throws Exception exception
	 */
	public Break(String name, String description,EnumTimePeriod enumTimePeriod, LocalTime start, Duration duration) throws Exception {
		super(name, description,enumTimePeriod, start, duration);
	}

	@Override
	boolean isWorkingPeriod() {
		return true;
	}

	@Override
	boolean isWorkingOffPeriod() {return true;	}

	@Override
	EnumTimePeriod getEnumTimePeriod() {
		return this.enumTimePeriod;
	}

	@Override
	void setEnumTimePeriod(EnumTimePeriod enumTimePeriod) {
		this.enumTimePeriod=enumTimePeriod;
	}
}
