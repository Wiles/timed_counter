package ca.samuellewis.timedcounter.results;

public class Stats {
	public static double getMean(final long[] numbers) {

		double average = 0;
		for (final long diff : numbers) {
			average += diff;
		}
		average /= numbers.length;
		return average;
	}

	public static double getStandardDeviation(final long[] numbers) {
		return Math.sqrt(getVariance(numbers));
	}

	public static double getVariance(final long[] numbers) {
		final double mean = getMean(numbers);
		double temp = 0;

		for (final long a : numbers) {
			temp += Math.pow(mean - a, 2);
		}
		return temp / numbers.length;
	}
}
