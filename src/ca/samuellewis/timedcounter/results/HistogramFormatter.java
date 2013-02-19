package ca.samuellewis.timedcounter.results;

import android.content.Context;
import android.graphics.Paint;

import com.androidplot.ui.DataRenderer;
import com.androidplot.util.Configurator;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeriesFormatter;

@SuppressWarnings("rawtypes")
public class HistogramFormatter extends XYSeriesFormatter {
	public Paint getFillPaint() {
		return fillPaint;
	}

	public void setFillPaint(final Paint fillPaint) {
		this.fillPaint = fillPaint;
	}

	public Paint getBorderPaint() {
		return borderPaint;
	}

	public void setBorderPaint(final Paint borderPaint) {
		this.borderPaint = borderPaint;
	}

	private Paint fillPaint;
	private Paint borderPaint;

	{
		fillPaint = new Paint();
		// fillPaint.setColor(Color.RED);
		fillPaint.setStyle(Paint.Style.FILL);
		fillPaint.setAlpha(100);
		borderPaint = new Paint();
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setAlpha(100);
	}

	/**
	 * Provided as a convenience to users; allows instantiation and xml
	 * configuration to take place in a single line
	 * 
	 * @param ctx
	 * @param xmlCfgId
	 *            Id of the xml config file within /res/xml
	 */
	public HistogramFormatter(final Context ctx, final int xmlCfgId) {
		// prevent configuration of classes derived from this one:
		if (getClass().equals(HistogramFormatter.class)) {
			Configurator.configure(ctx, this, xmlCfgId);
		}
	}

	public HistogramFormatter(final int fillColor, final int borderColor) {
		fillPaint.setColor(fillColor);
		borderPaint.setColor(borderColor);
	}

	@Override
	public Class<? extends DataRenderer> getRendererClass() {
		return HistogramRenderer.class;
	}

	@Override
	public DataRenderer getRendererInstance(final XYPlot plot) {
		return new HistogramRenderer(plot);
	}
}
