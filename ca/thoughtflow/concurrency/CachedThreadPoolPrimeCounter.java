package ca.thoughtflow.concurrency;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * This model uses a cached thread pool flavor of the executor service. Each range is given to a thread of the executor service and the pool expands as needed.
 *  
 * @author Nick Maiorano
 */
public class CachedThreadPoolPrimeCounter implements PrimeCounter {

	private final ExecutorService executor = Executors.newCachedThreadPool();
	private List<Callable<Long>> callables = new LinkedList<>();

	@Override
	public void setup(List<LongRange> ranges) {
		callables = ranges.stream().map(nextRange -> (Callable<Long>) () -> Util.countPrimesForOneRange(nextRange).get()).collect(Collectors.toList());
	}

	@Override
	public long countPrimes() throws CountingException {
		long count = 0;

		List<Future<Long>> futures;
		try {
			futures = executor.invokeAll(callables);
			count = futures.stream().mapToLong(next -> Util.uncheckedGet(next)).sum();
		} catch (InterruptedException e) {
			throw new CountingException("Could not count primes", e);
		}
		
		return count;
	}

	@Override
	public void tearDown() {
		executor.shutdownNow();
	}
}