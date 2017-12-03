package ca.thoughtflow.concurrency;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This counter uses primitive threads to count the number of primes. Each range is assigned to a worker thread and fired off. 
 * Threads are then joined by main thread and summed.
 * 
 * @author Nick Maiorano
 */
public class MultiThreadedPrimeCounter implements PrimeCounter {

	private Map<Thread, Worker> threads = new HashMap<>();
	private static final Consumer<Thread> uncheckedJoin = next -> {
		try {
			next.join();
		}
		catch (InterruptedException exception) {
			throw new CountingException("Could not finish joining", exception);
		}
	};

	@Override
	public void setup(List<LongRange> ranges) {

		// Create the workers.
		ranges.stream().forEach(nextRange -> {
		   Worker worker = new Worker(nextRange);
		   threads.put(new Thread(worker), worker);
		});
	}

	@Override
	public long countPrimes() {
		// Start, join and sum the threads.
		threads.keySet().stream().forEach(next -> next.start());
		threads.keySet().stream().forEach(uncheckedJoin);
		
		return threads.values().stream().mapToLong(next -> next.getCount()).sum();
	}
	
	private static class Worker implements Runnable {
		
		private final LongRange range;
		private long count = 0;
		
		public Worker(LongRange range) {
			this.range = range;
		}

		@Override
		public void run() {
			count = Util.countPrimesForOneRange(range).get();
		}
		
		public long getCount() {
			return count;
		}
	}
}