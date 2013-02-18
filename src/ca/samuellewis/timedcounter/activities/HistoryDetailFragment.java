package ca.samuellewis.timedcounter.activities;

import java.util.Arrays;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ca.samuellewis.timedcounter.R;
import ca.samuellewis.timedcounter.db.DatabaseHelper;
import ca.samuellewis.timedcounter.results.Session;
import ca.samuellewis.timedcounter.results.Stats;

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

			final long[] timeDifferences = new long[item.getCount() - 1];
			final long[] counts = item.getValues();
			for (int i = 0; i < timeDifferences.length; ++i) {
				timeDifferences[i] = counts[i + 1] - counts[i];
			}

			Arrays.sort(timeDifferences);

			final double average = Stats.getMean(timeDifferences);

			final double stdDev = Stats.getStandardDeviation(timeDifferences);

			((TextView) rootView.findViewById(R.id.tv_longest)).setText(Long
					.toString(timeDifferences[timeDifferences.length - 1]));

			((TextView) rootView.findViewById(R.id.tv_shortest)).setText(Long
					.toString(timeDifferences[0]));

			((TextView) rootView.findViewById(R.id.tv_average)).setText(String
					.format("%1$.2f", average));

			((TextView) rootView.findViewById(R.id.tv_standard_deviation))
					.setText(String.format("%1$.2f", stdDev));

			return rootView;
		}
	}
}
