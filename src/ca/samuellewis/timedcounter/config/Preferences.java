package ca.samuellewis.timedcounter.config;

import org.joda.time.DateTimeConstants;

import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultLong;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref
public interface Preferences {
	@DefaultLong(DateTimeConstants.MILLIS_PER_MINUTE)
	long period();
}
