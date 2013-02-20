package ca.samuellewis.timedcounter.results;

import java.util.ArrayList;
import java.util.Arrays;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.ocpsoft.prettytime.PrettyTime;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import ca.samuellewis.timedcounter.R;

public class HistoryListAdapter extends ArrayAdapter<Session> {

	final DateTimeFormatter dtf = ISODateTimeFormat.basicDateTime();
	final DateTimeFormatter d = DateTimeFormat.shortDateTime();
	final PrettyTime prettyTime = new PrettyTime();
	private Typeface face;

	public HistoryListAdapter(final Context context, final Session[] values) {
		super(context, R.layout.session_row, new ArrayList<Session>(
				Arrays.asList(values)));

		face = Typeface.createFromAsset(context.getAssets(),
				"fonts/digital_segment_thin.ttf");
	}

	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {

		final Session entry = getItem(position);

		final LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.session_row, parent,
				false);
		final TextView date = (TextView) rowView.findViewById(R.id.tv_date);
		final TextView age = (TextView) rowView.findViewById(R.id.tv_age);
		final TextView count = (TextView) rowView.findViewById(R.id.tv_count);
		final TextView countBackground = (TextView) rowView
				.findViewById(R.id.tv_count_back);
		date.setText(d.print(entry.getDate()));

		count.setTypeface(face);
		countBackground.setTypeface(face);

		age.setText(prettyTime.format(entry.getDate().toDate()));

		count.setText(String.format("% 5d", entry.getCount()));

		return rowView;
	}

}
