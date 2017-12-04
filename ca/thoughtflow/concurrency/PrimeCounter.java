package ca.thoughtflow.concurrency;

import java.util.List;

/**
 * Interface used to implement all prime counters. Each method will be called once to benchmark:
 *   - setup
 *   - countPrimes
 *   - tearDown
 * 
 * @author Nick Maiorano
 */
public interface PrimeCounter {

	/**
	 * Performs any setup required by the prime counter. 
	 * No counting should be done in this step - only setup the prime counter in order to fire off counting in the countPrimes() step.
	 *  
	 * @param ranges The list of ranges to to setup.
	 */
	public void setup(List<LongRange> ranges);
	
	/**
	 * Counts all of the prime numbers found in the list of ranges and returns the count.
	 * 
	 * @return The count of primes in the range.
	 * @throws CountingException Thrown if the primes could not be counted.
	 */
	public long countPrimes() throws CountingException;
	
	/**
	 * Optional method to tear down any state or running threads from the countPrimes() set.
	 */
	default public void tearDown() {};
}