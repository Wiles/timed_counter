package ca.samuellewis;

import android.app.Activity;
import android.view.Menu;
import android.widget.NumberPicker;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {
	@ViewById
	NumberPicker np_hours;
	@ViewById
	NumberPicker np_minutes;
	@ViewById
	NumberPicker np_seconds;
	
	@AfterViews
    public void init() {
        
        np_hours.setMinValue(0);
        np_hours.setMaxValue(99);

        np_minutes.setMinValue(0);
        np_minutes.setMaxValue(59);

        np_seconds.setMinValue(0);
        np_seconds.setMaxValue(59);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
	