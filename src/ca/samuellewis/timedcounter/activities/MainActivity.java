package ca.samuellewis.timedcounter.activities;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import ca.samuellewis.timedcounter.R;
import ca.samuellewis.timedcounter.config.Preferences_;
import ca.samuellewis.timedcounter.db.DatabaseHelper;
import ca.samuellewis.timedcounter.results.Session;
import ca.samuellewis.timedcounter.time.Period;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.LongClick;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

@OptionsMenu(R.menu.activity_main)
@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {

	@Pref
	Preferences_ preferences;

	DatabaseHelper dbHelper;

	private boolean running = false;

	private Timer timer;

	private Period period;

	private Session session;

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

	@SystemService
	Vibrator vibrator;

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

		dbHelper = new DatabaseHelper(this);

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

		updateDisplay(new Period(preferences.period().get()));
	}

	@Click
	void btnPlus() {
		if (!running) {
			start();
		}
		if (running) {
			if (session.size() < 100000) {
				session.addValue(new Date().getTime());
				updateCount(session.size());
			}
			vibrator.vibrate(10);
		}
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
			updateDisplay(new Period(session.getDuration()));
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

			period = getPeriod();
			session = new Session(new DateTime(), period.getMillis());
			for (long i = 0; i < 5000; ++i) {
				session.addValue(i);
			}
			preferences.period().put(session.getDuration());
			np_hours.setEnabled(false);
			np_minutes.setEnabled(false);
			np_seconds.setEnabled(false);

			timer = new Timer(true);
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					period.minusSeconds(1);
					updateDisplay(period);
					if (period.isZero()) {
						stop();
						showResults();
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

	@UiThread
	void showResults() {

		final Dialog p = ProgressDialog.show(this, null, "Saving", true, false);
		final AsyncTask<Session, Integer, Integer> t = new AsyncTask<Session, Integer, Integer>() {

			@Override
			protected Integer doInBackground(final Session... params) {

				dbHelper.saveSession(session);
				p.dismiss();
				menuHistory();
				return 0;
			}
		};

		t.execute(session);

	}
}
