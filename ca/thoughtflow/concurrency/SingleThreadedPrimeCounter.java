package ca.thoughtflow.concurrency;

import java.util.List;

/**
 * This is the single-threaded prime counter used as a reference point for performance comparisons against all other concurrency models.
 *  
 * @author Nick Maiorano
 */
public class SingleThreadedPrimeCounter implements PrimeCounter {

	private List<LongRange> ranges;
	
	@Override
	public void setup(List<LongRange> ranges) {
		this.ranges = ranges;
	}

	@Override
	public long countPrimes() {
		return Util.countPrimesForRange(ranges).get();
	}
}
