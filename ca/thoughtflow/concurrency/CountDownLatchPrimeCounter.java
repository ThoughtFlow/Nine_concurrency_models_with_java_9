package ca.thoughtflow.concurrency;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

/**
 * Builds upon the Multi-threaded prime counter and uses a countdown latch instead of a join. 
 * 
 * @author Nick Maiorano
 */
public class CountDownLatchPrimeCounter implements PrimeCounter {

	private final Map<Thread, Worker> threads = new HashMap<>();
	private CountDownLatch latch;

	@Override
	public void setup(List<LongRange> ranges) {

		latch = new CountDownLatch(ranges.size());
		ranges.stream().forEach(nextRange -> {
		   Worker worker = new Worker(nextRange, latch);
		   threads.put(new Thread(worker), worker);
		});
	}

	@Override
	public long countPrimes() throws CountingException {
		threads.keySet().stream().forEach(nextThread -> nextThread.start());

		try {
			latch.await();
		}
		catch (InterruptedException exception) {
			throw new CountingException("Could not finish waiting", exception);
		}

		return threads.values().stream().mapToLong(w -> w.getCount()).sum();
	}
	
	private static class Worker implements Runnable {
		
		private final Supplier<Long> primeFinderFunction;
		private final CountDownLatch latch;
		private long count = 0;
		
		public Worker(LongRange range, CountDownLatch latch) {
			primeFinderFunction = Util.countPrimesForOneRange(range);
			this.latch = latch;
		}

		@Override
		public void run() {
			count = primeFinderFunction.get();
			latch.countDown();
		}
		
		public long getCount() {
			return count;
		}
	}
}