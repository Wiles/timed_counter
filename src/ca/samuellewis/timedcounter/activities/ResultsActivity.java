package ca.samuellewis.timedcounter.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import ca.samuellewis.timedcounter.R;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.ViewById;

@EActivity(R.layout.grid_main)
public class ResultsActivity extends Activity {

	@Extra
	long[] results;

	@Extra
	long period;

	@ViewById
	ListView listview;

	@AfterViews
	void initResultsActivity() {
		// https://github.com/excilys/androidannotations/wiki/Adapters-and-lists
		// tb_duration.setText(Long.toString(period));
		// tb_total_ticks.setText(Long.toString(results.length));

		// create the grid item mapping
		final String[] from = new String[] { "rowid", "col_1", "col_2", "col_3" };
		final int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3,
				R.id.item4 };

		// prepare the list of all records
		final List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < 10; i++) {
			final HashMap<String, String> map = new HashMap<String, String>();
			map.put("rowid", "" + i);
			map.put("col_1", "col_1_item_" + i);
			map.put("col_2", "col_2_item_" + i);
			map.put("col_3", "col_3_item_" + i);
			fillMaps.add(map);
		}

		// fill in the grid_item layout
		final SimpleAdapter adapter = new SimpleAdapter(this, fillMaps,
				R.layout.grid_item, from, to);
		listview.setAdapter(adapter);
	}
}
