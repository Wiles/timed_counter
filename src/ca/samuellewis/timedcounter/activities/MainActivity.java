package ca.samuellewis.timedcounter.activities;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.HapticFeedbackConstants;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import ca.samuellewis.timedcounter.R;
import ca.samuellewis.timedcounter.config.Preferences_;
import ca.samuellewis.timedcounter.db.DatabaseHelper;
import ca.samuellewis.timedcounter.results.Session;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.LongClick;
import com.googlecode.androidannotations.annotations.NonConfigurationInstance;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

@OptionsMenu(R.menu.activity_main)
@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {

	@NonConfigurationInstance
	@Bean
	protected BackgroundTask task;

	@Pref
	protected Preferences_ preferences;

	@InstanceState
	protected boolean running = false;

	private Timer timer;

	@InstanceState
	protected long period;

	@InstanceState
	protected Session session;

	@InstanceState
	protected long count;

	@ViewById
	protected NumberPicker np_hours;

	@ViewById
	protected NumberPicker np_minutes;

	@ViewById
	protected NumberPicker np_seconds;

	@ViewById
	protected TextView txt_count;

	@ViewById
	protected TextView txt_count_background;

	@ViewById
	protected Button btnPlus;

	private UncaughtExceptionHandler originalUEH;

	private static final int MAX_COUNT = 99999;

	private static final int MAX_HOURS = 99;

	@AfterViews
	void initMainActivity() {
		originalUEH = Thread.getDefaultUncaughtExceptionHandler();

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(final Thread thread,
					final Throwable ex) {
				originalUEH.uncaughtException(thread, ex);
			}
		});

		Thread.currentThread().setUncaughtExceptionHandler(
				Thread.getDefaultUncaughtExceptionHandler());

		np_hours.setMinValue(0);
		np_hours.setMaxValue(MAX_HOURS);

		np_minutes.setMinValue(0);
		np_minutes.setMaxValue(DateTimeConstants.MINUTES_PER_HOUR - 1);

		np_seconds.setMinValue(0);
		np_seconds.setMaxValue(DateTimeConstants.SECONDS_PER_MINUTE - 1);

		final Typeface face = Typeface.createFromAsset(getAssets(),
				"fonts/digital_segment_thin.ttf");

		txt_count.setTypeface(face);
		txt_count_background.setTypeface(face);

		start(DateTimeConstants.MILLIS_PER_MINUTE);
		btnReset();

		period = preferences.period().get();
		task.updateDisplay(period);
	}

	@Click
	void btnPlus() {
		btnPlus.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		if (!running) {
			start(getPeriod());
		}
		if (running && count <= MAX_COUNT) {
			++count;
			updateCount(count);
			addEntry();
		}
	}

	@Background
	void addEntry() {
		new DatabaseHelper(this).addEntry(session, new Date().getTime());
	}

	@OptionsItem
	boolean menuHistory() {
		final Intent intent = new Intent(this, HistoryListActivity.class);
		startActivity(intent);
		return true;
	}

	@LongClick
	void btnReset() {
		stop();
		count = 0;
		updateCount(0);
		if (session != null) {
			task.updateDisplay(session.getDuration());
		}
		np_hours.setEnabled(true);
		np_minutes.setEnabled(true);
		np_seconds.setEnabled(true);
	}

	private long getPeriod() {
		return (np_hours.getValue() * DateTimeConstants.MILLIS_PER_HOUR)
				+ (np_minutes.getValue() * DateTimeConstants.MILLIS_PER_MINUTE)
				+ (np_seconds.getValue() * DateTimeConstants.MILLIS_PER_SECOND);
	}

	private void start(final long period) {

		if (!running) {
			if (period == 0) {
				Toast.makeText(this, R.string.no_time_entered,
						Toast.LENGTH_SHORT).show();
				return;
			}
			running = true;
			count = 0;
			updateCount(0);
			this.period = period;
			session = new Session(new DateTime(), period);
			session = new DatabaseHelper(this).createSession(period);
			preferences.period().put(session.getDuration());
			np_hours.setEnabled(false);
			np_minutes.setEnabled(false);
			np_seconds.setEnabled(false);

			timer = task.getTimer();
			timer.cancel();
			timer = new Timer(true);
			task.setTimer(timer);
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					MainActivity.this.period -= DateTimeConstants.MILLIS_PER_SECOND;
					task.updateDisplay(MainActivity.this.period);
					if (MainActivity.this.period == 0L) {
						stop();
						final DatabaseHelper db = new DatabaseHelper(
								MainActivity.this);
						db.finishSession(session.getId());
						db.removeUnfinished();
						menuHistory();
					}
				}
			}, 0, DateTimeConstants.MILLIS_PER_SECOND);
		}
	}

	@UiThread
	void stop() {
		running = false;
		timer.cancel();
	}

	@UiThread
	void updateCount(final long count) {
		txt_count.setText(String.format("% 5d", count));
	}

	@UiThread
	void updateDisplay(final long period) {
		final Duration dur = new Duration(period);
		np_hours.setValue((int) dur.getStandardHours());
		np_minutes
				.setValue((int) (dur.getStandardMinutes() % DateTimeConstants.MINUTES_PER_HOUR));
		np_seconds
				.setValue((int) (dur.getStandardSeconds() % DateTimeConstants.SECONDS_PER_MINUTE));
	}

}
