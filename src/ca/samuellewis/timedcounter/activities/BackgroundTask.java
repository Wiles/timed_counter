package ca.samuellewis.timedcounter.activities;

import java.util.Timer;

import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;

@EBean
public class BackgroundTask {
	@RootContext
	protected MainActivity activity;
	private Timer timer = new Timer(true);

	@Background
	void updateDisplay(final long period) {
		activity.updateDisplay(period);
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(final Timer timer) {
		this.timer = timer;
	}
}
