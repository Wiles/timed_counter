package ca.samuellewis.timedcounter.config;

import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultLong;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref
public interface Preferences {
	@DefaultLong(300000L)
	long period();
}
