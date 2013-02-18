package ca.samuellewis.timedcounter.time;

public class Period {

	private int hours;
	private int minutes;
	private int seconds;
	private int millis;

	public Period(final int hours, final int minutes, final int seconds) {
		if (hours < 0 || minutes < 0 || seconds < 0) {
			throw new IllegalArgumentException("Arguments cannot be zero");
		}
		this.hours = hours;
		this.minutes = minutes % 60;
		this.seconds = seconds % 60;
	}

	public Period(final long millis) {
		this.millis = (int) (millis % 1000);
		this.seconds = (int) (millis / 1000 % 60);
		this.minutes = (int) (millis / 60000 % 60);
		this.hours = (int) (millis / 3600000);
	}

	public boolean isZero() {
		return hours == 0 && minutes == 0 && seconds == 0 && millis == 0;
	}

	public long getMillis() {
		return (hours * 3600 + minutes * 60 + seconds) * 1000 + millis;
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
				this.minutes = 60 + this.minutes;
				minusHours(1);
			}
		}
	}

	public void minusSeconds(final int seconds) {
		if (seconds >= 60) {
			throw new IllegalArgumentException(
					"Only a mixinum of 59 seconds can be removed");
		}
		this.seconds -= seconds;
		if (this.seconds < 0) {
			if (this.hours == 0 && this.minutes == 0) {
				this.seconds = 0;
			} else {
				this.seconds = 60 + this.seconds;
				minusMinutes(1);

			}
		}
	}

	public void minusMillis(final long milli) {
		if (seconds >= 1000) {
			throw new IllegalArgumentException(
					"Only a maximum of 999 milis can be removed");
		}

		this.millis -= milli;
		if (this.millis < 0) {
			if (this.hours == 0 && this.minutes == 0 && this.seconds == 0) {
				this.millis = 0;
			} else {
				this.millis = 1000 + this.millis;
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
