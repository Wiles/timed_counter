package ca.samuellewis.timedcounter.activities;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import ca.samuellewis.timedcounter.R;
import ca.samuellewis.timedcounter.time.Period;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.LongClick;
import com.googlecode.androidannotations.annotations.NoTitle;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;

@NoTitle
@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {

	// TODO ensure ArrayList orders by insertion
	private List<Long> counts = new ArrayList<Long>();

	private boolean running = false;

	private Timer timer;

	private Period period;

	private Period fullPeriod;

	@ViewById
	NumberPicker np_hours;

	@ViewById
	NumberPicker np_minutes;

	@ViewById
	NumberPicker np_seconds;

	@ViewById
	TextView txt_count;

	private UncaughtExceptionHandler originalUEH;

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
		np_minutes.setValue(5);

		np_seconds.setMinValue(0);
		np_seconds.setMaxValue(59);
	}

	@Click
	void btnPlus() {
		if (!running) {
			start();
		}
		counts.add(new Date().getTime());
		updateCount(counts.size());
	}

	@LongClick
	void btnReset() {
		stop();
		counts.clear();
		updateDisplay(fullPeriod);
		updateCount(counts.size());
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

			fullPeriod = getPeriod();
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
		txt_count.setText(Long.toString(count));
	}

	@UiThread
	void updateDisplay(final Period period) {
		np_hours.setValue(period.getHours());
		np_minutes.setValue(period.getMinutes());
		np_seconds.setValue(period.getSeconds());
	}

	private void showResults() {
		/*
		 * final Intent intent = new Intent(getApplicationContext(),
		 * ResultsActivity_.class);
		 * 
		 * intent.putExtra("results", ArrayUtils.toPrimitive(counts.toArray(new
		 * Long[counts.size()]))); intent.putExtra("period",
		 * fullPeriod.getMillis()); startActivity(intent);
		 */
	}
}
