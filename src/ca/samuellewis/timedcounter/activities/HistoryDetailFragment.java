package ca.samuellewis.timedcounter.activities;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ca.samuellewis.timedcounter.R;
import ca.samuellewis.timedcounter.db.DatabaseHelper;
import ca.samuellewis.timedcounter.results.HistogramFormatter;
import ca.samuellewis.timedcounter.results.Session;
import ca.samuellewis.timedcounter.results.Stats;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.BezierLineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

/**
 * A fragment representing a single History detail screen. This fragment is
 * either contained in a {@link HistoryListActivity} in two-pane mode (on
 * tablets) or a {@link HistoryDetailActivity} on handsets.
 */
public class HistoryDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private Session item;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public HistoryDetailFragment() {
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			final DatabaseHelper db = new DatabaseHelper(getActivity());
			item = db.getSession(getArguments().getInt(ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		if (item == null || item.getCount() <= 0) {
			final View rootView = inflater.inflate(
					R.layout.fragment_history_detail, container, false);

			((TextView) rootView.findViewById(R.id.history_detail))
					.setText(getText(R.string.nothing_found));
			return rootView;
		} else if (item.getCount() == 1) {
			final View rootView = inflater.inflate(R.layout.session_stats_min,
					container, false);

			((TextView) rootView.findViewById(R.id.tv_duration)).setText(Long
					.toString(item.getDuration()));

			((TextView) rootView.findViewById(R.id.tv_count)).setText(Integer
					.toString(item.getCount()));
			return rootView;
		} else {

			final View rootView = inflater.inflate(R.layout.session_stats,
					container, false);

			((TextView) rootView.findViewById(R.id.tv_duration)).setText(Long
					.toString(item.getDuration()));

			((TextView) rootView.findViewById(R.id.tv_count)).setText(Integer
					.toString(item.getCount()));

			final Long[] timeDifferences = new Long[item.getCount() - 1];
			final long[] counts = item.getValues();
			for (int i = 0; i < timeDifferences.length; ++i) {
				timeDifferences[i] = counts[i + 1] - counts[i];
			}

			Arrays.sort(timeDifferences);

			final long min = timeDifferences[0];
			final long max = timeDifferences[timeDifferences.length - 1];
			final long range = max - min;

			final double bucketSize = range / 20;
			final long[] buckets = new long[40];
			int bucket = 0;
			for (final long value : timeDifferences) {
				if (value > ((bucket) * bucketSize) + min) {
					++bucket;
				}
				++buckets[(bucket * 2) + 1];
			}

			for (int i = 0; i < buckets.length / 2; ++i) {
				buckets[i * 2] = (long) (min + (bucketSize * i));
			}

			final double mean = Stats.getMean(timeDifferences);

			final double stdDev = Stats.getStandardDeviation(timeDifferences);

			final XYPlot mySimpleXYPlot = (XYPlot) rootView
					.findViewById(R.id.xy_plot);

			// Create a couple arrays of y-values to plot:
			// Turn the above arrays into XYSeries':
			final XYSeries series1 = new SimpleXYSeries(
					Arrays.asList(ArrayUtils.toObject(buckets)),
					SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "");
			final HistogramFormatter sf = new HistogramFormatter(Color.argb(
					150, 0, 255, 0), Color.rgb(0, 255, 00));

			mySimpleXYPlot.addSeries(series1, sf);

			final double[] sd = new double[44];
			for (int i = 0; i < sd.length / 2; ++i) {
				final double z = (min + (i * bucketSize));
				sd[i * 2] = z;
				sd[(i * 2) + 1] = (z * mean) / stdDev;
			}

			final XYSeries series2 = new SimpleXYSeries(
					Arrays.asList(ArrayUtils.toObject(sd)),
					SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "");
			final BezierLineAndPointFormatter bf = new BezierLineAndPointFormatter(
					Color.rgb(0, 0, 255), Color.argb(0, 0, 0, 255), Color.argb(
							0, 0, 0, 255));

			mySimpleXYPlot.addSeries(series2, bf);

			((TextView) rootView.findViewById(R.id.tv_longest)).setText(Long
					.toString(max));

			((TextView) rootView.findViewById(R.id.tv_shortest)).setText(Long
					.toString(min));

			((TextView) rootView.findViewById(R.id.tv_average)).setText(String
					.format("%1$.2f", mean));

			((TextView) rootView.findViewById(R.id.tv_standard_deviation))
					.setText(String.format("%1$.2f", stdDev));

			return rootView;
		}
	}
}
