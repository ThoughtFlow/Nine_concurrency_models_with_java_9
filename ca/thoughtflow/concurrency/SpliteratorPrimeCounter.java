package ca.thoughtflow.concurrency;

import static ca.thoughtflow.concurrency.Util.isPrime;

import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * This model builds upon the cached thread pool and uses spliterators to distribute the workload.
 * 
 * @author Nick Maiorano
 */
public class SpliteratorPrimeCounter implements PrimeCounter {

	private final ExecutorService executor = Executors.newCachedThreadPool();
	private List<Callable<Long>> callables;

	@Override
	public void setup(List<LongRange> ranges) {

		// Currying function that a spliterator and returns a callable function.
		Function<Spliterator<Long>, Callable<Long>> function = 
			spliterator -> () ->
				{
					AtomicLong count = new AtomicLong();
					Consumer<Long> consumer = l -> count.addAndGet(isPrime(l) ? 1 : 0);
					spliterator.forEachRemaining(consumer);

					return count.get();
				};
		
		// Converts each range into a spliterator. Note that this overhead is not added to the overall execution duration.
				
		// Outer stream		
		callables = ranges.stream().map(
				// Inner stream
				nextRange -> LongStream.range(nextRange.getStart(), nextRange.getEnd()).spliterator()).
			// Outer stream
			map(nextSpliterator -> function.apply(nextSpliterator)).collect(Collectors.toList());
	}

	@Override
	public long countPrimes() throws CountingException {
		List<Future<Long>> futures;
		
		try {
			futures = executor.invokeAll(callables);
		} catch (Exception e) {
			throw new CountingException("Could not find primes", e);
		}
		
		return futures.stream().mapToLong(nextFuture -> Util.uncheckedGet(nextFuture)).sum();
	}

	@Override
	public void tearDown() {
		executor.shutdownNow();
	}
}