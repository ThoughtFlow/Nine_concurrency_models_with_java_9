package ca.thoughtflow.concurrency;

import java.util.List;
import java.util.stream.LongStream;

/**
 * This model uses the parallel stream to count the number of primes. The parallel stream handles all of the distribution of work.
 * It is the most abstract model of all (e.g. least amount of code of all).
 * 
 * @author Nick Maiorano
 */
public class ParallelStreamPrimeCounter implements PrimeCounter {
	
	private List<LongRange> ranges;
	
	@Override
	public void setup(List<LongRange> ranges) {
		this.ranges = ranges;
	}
	
	@Override
	public long countPrimes() {
		// Inner and outer stream both use parallel streams.
		
		// Outer stream
		return ranges.stream().parallel().map(
				// Inner stream
				nextRange -> LongStream.range(nextRange.getStart(), nextRange.getEnd()).parallel().filter(Util::isPrime).count()).
			// Outer stream	
			mapToLong(l -> l).sum();
	}
}