package ca.samuellewis.timedcounter.time;

import java.io.Serializable;

import org.joda.time.DateTimeConstants;

public class Period implements Serializable {

	private static final long serialVersionUID = -4365521520520918860L;

	private int hours;
	private int minutes;
	private int seconds;
	private int millis;

	public Period(final int hours, final int minutes, final int seconds) {
		if (hours < 0 || minutes < 0 || seconds < 0) {
			throw new IllegalArgumentException("Arguments cannot be zero");
		}
		this.hours = hours;
		this.minutes = minutes % DateTimeConstants.MINUTES_PER_HOUR;
		this.seconds = seconds % DateTimeConstants.SECONDS_PER_MINUTE;
	}

	public Period(final long millis) {
		this.millis = (int) (millis % DateTimeConstants.MILLIS_PER_SECOND);
		this.seconds = (int) (millis / DateTimeConstants.MILLIS_PER_SECOND % DateTimeConstants.SECONDS_PER_MINUTE);
		this.minutes = (int) (millis
				/ (DateTimeConstants.MILLIS_PER_SECOND * DateTimeConstants.SECONDS_PER_MINUTE) % DateTimeConstants.SECONDS_PER_MINUTE);
		this.hours = (int) (millis / DateTimeConstants.MILLIS_PER_HOUR);
	}

	public boolean isZero() {
		return hours == 0 && minutes == 0 && seconds == 0 && millis == 0;
	}

	public long getMillis() {
		return (hours * DateTimeConstants.SECONDS_PER_HOUR + minutes
				* DateTimeConstants.SECONDS_PER_MINUTE + seconds)
				* DateTimeConstants.MILLIS_PER_SECOND + millis;
	}

	public void minusHours(final int hours) {
		this.hours -= hours;
		if (this.hours < 0) {
			this.hours = 0;
		}
	}

	public void minusMinutes(final int minutes) {
		this.minutes -= minutes;
		if (this.minutes < 0) {
			if (this.hours == 0) {
				this.minutes = 0;
			} else {
				this.minutes = DateTimeConstants.MINUTES_PER_HOUR
						+ this.minutes;
				minusHours(1);
			}
		}
	}

	public void minusSeconds(final int seconds) {
		if (seconds >= DateTimeConstants.SECONDS_PER_MINUTE) {
			throw new IllegalArgumentException(
					"Only a mixinum of 59 seconds can be removed");
		}
		this.seconds -= seconds;
		if (this.seconds < 0) {
			if (this.hours == 0 && this.minutes == 0) {
				this.seconds = 0;
			} else {
				this.seconds = DateTimeConstants.SECONDS_PER_MINUTE
						+ this.seconds;
				minusMinutes(1);

			}
		}
	}

	public void minusMillis(final long milli) {
		if (milli >= DateTimeConstants.MILLIS_PER_SECOND) {
			throw new IllegalArgumentException(
					"Only a maximum of 999 milis can be removed");
		}

		this.millis -= milli;
		if (this.millis < 0) {
			if (this.hours == 0 && this.minutes == 0 && this.seconds == 0) {
				this.millis = 0;
			} else {
				this.millis = DateTimeConstants.MILLIS_PER_SECOND + this.millis;
				minusSeconds(1);

			}
		}
	}

	public int getHours() {
		return hours;
	}

	public int getMinutes() {
		return minutes;
	}

	public int getSeconds() {
		return seconds;
	}
}
