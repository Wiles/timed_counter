package ca.samuellewis.timedcounter.results;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.androidplot.exception.PlotRenderException;
import com.androidplot.series.XYSeries;
import com.androidplot.util.ValPixConverter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeriesRenderer;

public class HistogramRenderer extends XYSeriesRenderer<HistogramFormatter> {

	public HistogramRenderer(final XYPlot plot) {
		super(plot);
	}

	/**
	 * Synchronizes the current thread across multiple objects before executing
	 * a given task.
	 */
	/*
	 * private void multiSynch(Canvas canvas, RectF plotArea, List<XYSeries> sl,
	 * int depth) { if (sl != null) { synchronized (sl.get(depth)) { if (depth <
	 * sl.size()-1) { multiSynch(canvas, plotArea, sl, ++depth); } else { int
	 * longest = getLongestSeries(sl); if(longest == 0) { return; // no data,
	 * nothing to do. } TreeMap<Number, XYSeries> seriesMap = new
	 * TreeMap<Number, XYSeries>(); for(int i = 0; i < longest; i++) {
	 * seriesMap.clear(); List<XYSeries> seriesList =
	 * getPlot().getSeriesListForRenderer(this.getClass()); for(XYSeries series
	 * : seriesList) { if(i < series.size()) { seriesMap.put(series.getY(i),
	 * series); } } drawBars(canvas, plotArea, seriesMap, i); } } } } }
	 */

	@Override
	public void onRender(final Canvas canvas, final RectF plotArea)
			throws PlotRenderException {

		final List<XYSeries> sl = getPlot().getSeriesListForRenderer(
				this.getClass());
		// need to synch on each series in sl before proceeding with render
		// multiSynch(canvas, plotArea, sl, 0);

		final int longest = getLongestSeries(sl);
		if (longest == 0) {
			return; // no data, nothing to do.
		}
		final TreeMap<Number, XYSeries> seriesMap = new TreeMap<Number, XYSeries>();
		for (int i = 0; i < longest; i++) {
			seriesMap.clear();
			final List<XYSeries> seriesList = getPlot()
					.getSeriesListForRenderer(this.getClass());
			for (final XYSeries series : seriesList) {
				if (i < series.size()) {
					seriesMap.put(series.getY(i), series);
				}
			}
			drawBars(canvas, plotArea, seriesMap, i);
		}
	}

	@Override
	protected void doDrawLegendIcon(final Canvas canvas, final RectF rect,
			final HistogramFormatter formatter) {
		canvas.drawRect(rect, formatter.getFillPaint());
		canvas.drawRect(rect, formatter.getBorderPaint());
	}

	private int getLongestSeries(final List<XYSeries> seriesList) {
		int longest = 0;

		for (final XYSeries series : seriesList) {
			final int seriesSize = series.size();
			if (seriesSize > longest) {
				longest = seriesSize;
			}
		}
		return longest;
	}

	private int getLongestSeries() {
		return getLongestSeries(getPlot().getSeriesListForRenderer(
				this.getClass()));
	}

	@SuppressWarnings("unchecked")
	private void drawBars(final Canvas canvas, final RectF plotArea,
			final TreeMap<Number, XYSeries> seriesMap, final int x) {
		final Paint p = new Paint();
		p.setColor(Color.RED);
		final Object[] oa = seriesMap.entrySet().toArray();
		Map.Entry<Number, XYSeries> entry;
		for (int i = oa.length - 1; i >= 0; i--) {
			entry = (Map.Entry<Number, XYSeries>) oa[i];
			final HistogramFormatter formatter = getFormatter(entry.getValue()); // TODO:
			// make
			// this
			// more
			// efficient
			Number yVal = null;
			Number xVal = null;
			if (entry.getValue() != null) {
				yVal = entry.getValue().getY(x);
				xVal = entry.getValue().getX(x);
			}

			if (yVal != null && xVal != null) { // make sure there's a real
												// value to draw
				final float halfWidth = (plotArea.width() / getLongestSeries()) / 2;
				final float pixX = ValPixConverter.valToPix(xVal.doubleValue(),
						getPlot().getCalculatedMinX().doubleValue(), getPlot()
								.getCalculatedMaxX().doubleValue(), plotArea
								.width(), false)
						+ (plotArea.left);
				final float pixY = ValPixConverter.valToPix(yVal.doubleValue(),
						getPlot().getCalculatedMinY().doubleValue(), getPlot()
								.getCalculatedMaxY().doubleValue(), plotArea
								.height(), true)
						+ plotArea.top;
				canvas.drawRect(pixX - halfWidth, pixY, pixX + halfWidth,
						plotArea.bottom, formatter.getFillPaint());
				canvas.drawRect(pixX - halfWidth, pixY, pixX + halfWidth,
						plotArea.bottom, formatter.getBorderPaint());
			}
		}
	}
}
