package ca.thoughtflow.concurrency;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This model uses promises to count the number of primes in the range.
 * Each promise counts the primes for one range and chained together with other primes to sum.
 * This is a faster algorithm because even chaining is done asynchronously.
 * 
 * @author Nick Maiorano
 */
public class PromisePrimeCounter implements PrimeCounter {

	private CompletableFuture<Long> firstPromise;
	private CompletableFuture<Long> lastPromise;

	@Override
	public void setup(List<LongRange> ranges) {
		
		firstPromise = new CompletableFuture<Long>();
		lastPromise = firstPromise;

		for (LongRange nextRange : ranges) {
		
			CompletableFuture<Long> nextPromise = CompletableFuture.supplyAsync(
					() -> Util.countPrimesForOneRange(nextRange).get());
			
			// Create one big chain of promises to sum the results.
			lastPromise = lastPromise.thenCombine(nextPromise, (first, second) -> first + second);
		}
	}

	@Override
	public long countPrimes() throws CountingException {
		// First first prime counter and the rest will follow.
		firstPromise.complete(0L);
		
		return Util.uncheckedGet(lastPromise);
	}
}