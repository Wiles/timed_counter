package ca.samuellewis.timedcounter.activities;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;

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
import ca.samuellewis.timedcounter.time.Period;

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
	BackgroundTask task;

	@Pref
	Preferences_ preferences;

	@InstanceState
	boolean running = false;

	private Timer timer;

	@InstanceState
	protected Period period;

	@InstanceState
	protected Session session;

	@InstanceState
	protected long count;

	@ViewById
	NumberPicker np_hours;

	@ViewById
	NumberPicker np_minutes;

	@ViewById
	NumberPicker np_seconds;

	@ViewById
	TextView txt_count;

	@ViewById
	TextView txt_count_background;

	@ViewById
	Button btnPlus;

	private UncaughtExceptionHandler originalUEH;

	Typeface face;

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
		np_hours.setMaxValue(99);

		np_minutes.setMinValue(0);
		np_minutes.setMaxValue(59);

		np_seconds.setMinValue(0);
		np_seconds.setMaxValue(59);

		face = Typeface.createFromAsset(getAssets(),
				"fonts/digital_segment_thin.ttf");

		txt_count.setTypeface(face);
		txt_count_background.setTypeface(face);

		if (period == null) {
			task.updateDisplay(new Period(preferences.period().get()));

		} else {
			task.updateDisplay(period);
		}
	}

	@Click
	void btnPlus() {
		btnPlus.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		if (!running) {
			start();
		}
		if (running) {
			if (count < 100000) {
				++count;
				updateCount(count);
				addEntry();
			}
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
		if (session != null) {
			task.updateDisplay(new Period(session.getDuration()));
		}
		updateCount(0);
		np_hours.setEnabled(true);
		np_minutes.setEnabled(true);
		np_seconds.setEnabled(true);
	}

	private Period getPeriod() {
		return new Period(np_hours.getValue(), np_minutes.getValue(),
				np_seconds.getValue());
	}

	private void start() {

		if (!running) {
			if (getPeriod().getMillis() == 0) {
				Toast.makeText(this, R.string.no_time_entered,
						Toast.LENGTH_SHORT).show();
				return;
			}
			running = true;
			count = 0;

			period = getPeriod();
			session = new Session(new DateTime(), period.getMillis());
			session = new DatabaseHelper(this)
					.createSession(period.getMillis());
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
					period.minusSeconds(1);
					task.updateDisplay(period);
					if (period.isZero()) {
						stop();
						menuHistory();
					}
				}
			}, 0, 1000);
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
	void updateDisplay(final Period period) {
		np_hours.setValue(period.getHours());
		np_minutes.setValue(period.getMinutes());
		np_seconds.setValue(period.getSeconds());
	}

}
