package ca.thoughtflow.concurrency;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * This model uses a fork/join flavor of the executor service. Each range is split (forked) into smaller ranges until the range is at the preferred range.
 * The each forked range is then summed and rejoined to the parent range.
 * 
 * @author Nick Maiorano
 */
public class ForkJoinPrimeCounter implements PrimeCounter {

	private final ForkJoinPool executor = (ForkJoinPool) Executors.newWorkStealingPool();
	private List<Worker> workers;

	@Override
	public void setup(List<LongRange> ranges) {
		workers = ranges.stream().map(nextRange -> new Worker(nextRange.getStart(), nextRange.getEnd())).collect(Collectors.toList());
	}

	@Override
	public long countPrimes() throws CountingException {

		workers.stream().forEach(nextWorker -> executor.execute(nextWorker));

		return workers.stream().mapToLong(nextWorker -> Util.uncheckedGet(nextWorker)).sum();
	}

	@Override
	public void tearDown() {
		executor.shutdownNow();
	}
	
	@SuppressWarnings("serial")
	private static class Worker extends RecursiveTask<Long> {

		private static final int MINIMUM_RANGE = 1000;
		
		private final long start;
		private final long end;
		
		public Worker(long start, long end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public Long compute() {
			long count = 0;
			if (end - start > MINIMUM_RANGE) {
				long halfWay = (end - start) / 2 + start;
				ForkJoinTask<Long> firstHalf = doSplit(start, halfWay);
				ForkJoinTask<Long> secondHalf = doSplit(halfWay + 1, end);
				
				count = firstHalf.join() + secondHalf.join();
			}
			else {
				count = doCompute(start, end);
			}
			
			return count;
		}
		
		private ForkJoinTask<Long> doSplit(long startRange, long endRange) {
			Worker newWorker = new Worker(startRange, endRange);
			return newWorker.fork();
		}
		
		private Long doCompute(long startRange, long endRange) {
			return LongStream.rangeClosed(startRange, endRange).filter(Util::isPrime).count();
		}
	}
}