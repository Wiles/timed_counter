package ca.samuellewis.timedcounter.results;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class Session {

	private Integer count;
	private int id;
	private DateTime date;
	private long duration;
	private List<Long> values;
	private ValuesSource source;

	final DateTimeFormatter dtf = ISODateTimeFormat.basicDateTime();

	public Session(final DateTime date, final long duration) {
		this(date, duration, new ArrayList<Long>());
	}

	public Session(final DateTime date, final long duration,
			final List<Long> values) {
		this.date = date;
		this.duration = duration;
		this.values = values;
	}

	public Session(final DateTime date, final long duration,
			final ValuesSource source) {
		this.date = date;
		this.duration = duration;
		this.values = null;
		this.source = source;
	}

	public DateTime getDate() {
		return date;
	}

	public long getDuration() {
		return duration;
	}

	public long[] getValues() {
		if (values == null && source != null) {
			values = Arrays.asList(ArrayUtils.toObject(source.getValues()));
		}
		return ArrayUtils.toPrimitive(values.toArray(new Long[values.size()]));
	}

	public int size() {
		return values.size();
	}

	public void addValue(final long value) {
		values.add(value);
	}

	@Override
	public String toString() {
		return dtf.print(date);
	}

	public void setId(final int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public int getCount() {
		if (values != null) {
			return values.size();
		} else if (count != null) {
			return count;
		} else if (source != null) {
			count = source.getCount();
			return count;
		}
		return 0;
	}
}
