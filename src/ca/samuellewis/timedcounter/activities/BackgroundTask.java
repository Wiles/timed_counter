package ca.samuellewis.timedcounter.activities;

import java.util.Timer;

import ca.samuellewis.timedcounter.time.Period;

import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;

@EBean
public class BackgroundTask {

	@RootContext
	protected MainActivity activity;

	private boolean saving;

	private Timer timer = new Timer(true);

	@Background
	void updateDisplay(final Period period) {
		activity.updateDisplay(period);
	}

	public boolean isSaving() {
		return saving;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(final Timer timer) {
		this.timer = timer;
	}
}
