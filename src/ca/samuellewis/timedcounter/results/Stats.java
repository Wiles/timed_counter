package ca.samuellewis.timedcounter.results;

public final class Stats {

	private Stats() {
	}

	public static <T extends Number> double getMean(final T[] numbers) {

		double average = 0.0;
		for (final T diff : numbers) {
			average += diff.floatValue();
		}
		average /= numbers.length;
		return average;
	}

	public static <T extends Number> double getStandardDeviation(
			final T[] numbers) {
		return Math.sqrt(getVariance(numbers));
	}

	public static <T extends Number> double getVariance(final T[] numbers) {
		final double mean = getMean(numbers);
		double temp = 0;

		for (final T a : numbers) {
			temp += Math.pow(mean - a.doubleValue(), 2);
		}
		return temp / numbers.length;
	}
}
