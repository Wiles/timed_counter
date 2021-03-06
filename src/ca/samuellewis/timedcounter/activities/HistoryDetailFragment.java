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
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.eaio.util.text.HumanTime;

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

	private static final int BUCKET_COUNT = 20;

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
			item = db.getSession(getArguments().getLong(ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		// TODO clean this up
		if (item == null || item.getCount() <= 0) {
			final View rootView = inflater.inflate(
					R.layout.fragment_history_detail, container, false);

			((TextView) rootView.findViewById(R.id.history_detail))
					.setText(getText(R.string.nothing_found));
			return rootView;
		} else if (item.getCount() == 1) {
			final View rootView = inflater.inflate(R.layout.session_stats_min,
					container, false);

			((TextView) rootView.findViewById(R.id.tv_duration))
					.setText(HumanTime.exactly(item.getDuration()));

			((TextView) rootView.findViewById(R.id.tv_count)).setText(Long
					.toString(item.getCount()));
			return rootView;
		} else {
			View rootView = null;
			rootView = inflater.inflate(R.layout.session_stats, container,
					false);
			((TextView) rootView.findViewById(R.id.tv_duration))
					.setText(HumanTime.exactly(item.getDuration()));

			((TextView) rootView.findViewById(R.id.tv_count)).setText(Long
					.toString(item.getCount()));

			final Long[] timeDifferences = new Long[(int) (item.getCount() - 1)];
			final long[] counts = item.getValues();
			for (int i = 0; i < timeDifferences.length; ++i) {
				timeDifferences[i] = counts[i + 1] - counts[i];
			}

			Arrays.sort(timeDifferences);

			final double min = timeDifferences[0];
			final double max = timeDifferences[timeDifferences.length - 1];
			double range;
			if (min == max) {
				range = 1;
			} else {
				range = max - min;
			}
			final double bucketSize = range / BUCKET_COUNT;
			final long[] buckets = new long[BUCKET_COUNT * 2 + 2];
			int bucket = 0;
			for (final long value : timeDifferences) {
				while (value > ((bucket + 1) * bucketSize) + min) {
					++bucket;
				}
				++buckets[(bucket * 2) + 1];
			}

			for (int i = 0; i < buckets.length / 2; ++i) {
				buckets[i * 2] = (long) (min + (bucketSize * i));
			}

			buckets[buckets.length - 1] -= 1;

			final double mean = Stats.getMean(timeDifferences);

			final double stdDev = Stats.getStandardDeviation(timeDifferences);

			final XYPlot plot = (XYPlot) rootView.findViewById(R.id.xy_plot);

			final XYSeries series1 = new SimpleXYSeries(
					Arrays.asList(ArrayUtils.toObject(buckets)),
					SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "");
			final HistogramFormatter sf = new HistogramFormatter(Color.argb(
					0x80, 0, 0x99, 0xcc), Color.rgb(0, 0x99, 0xcc));

			plot.setTitle(getString(R.string.distribution));
			plot.setDomainLabel(getString(R.string.time));
			plot.setRangeLabel(getString(R.string.count_label));
			plot.getLegendWidget().setVisible(false);

			plot.addSeries(series1, sf);
			plot.setRangeLowerBoundary(0, BoundaryMode.FIXED);

			((TextView) rootView.findViewById(R.id.tv_longest))
					.setText(HumanTime.exactly((long) max));

			((TextView) rootView.findViewById(R.id.tv_shortest))
					.setText(HumanTime.exactly((long) min));

			((TextView) rootView.findViewById(R.id.tv_average))
					.setText(HumanTime.exactly((long) mean));

			((TextView) rootView.findViewById(R.id.tv_standard_deviation))
					.setText(String.format("%1$.2f", stdDev));

			return rootView;
		}
	}
}
