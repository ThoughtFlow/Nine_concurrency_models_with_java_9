package ca.thoughtflow.concurrency;

import static ca.thoughtflow.concurrency.Util.getDefaultParallelism;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Uses the reactive streams library to count prime numbers.
 * Granted that reactive streams are meant to allow subscribers and publishers to communicate to each other to control the flow and this model doesn't employ any of 
 * these types of communications. Used for demonstrative purposes only.
 * 
 * Creates a subscriber per publisher the reactive streams support multiple subscribers in which case each message is multicasted to all.
 * Round-robin each range to one of four publishers.
 * 
 * @author Nick Maiorano
 */
public class ReactiveStreamPrimeFinder implements PrimeCounter {

	private List<LongRange> ranges;
	private List<ReactiveWorker> workers;
	List<SubmissionPublisher<LongRange>> publishers = new LinkedList<>();
	
	@Override
	public void setup(List<LongRange> ranges) {
		this.ranges = ranges;
		workers = IntStream.range(0, getDefaultParallelism()).mapToObj(i -> new ReactiveWorker()).collect(Collectors.toList());
		publishers = IntStream.range(0, getDefaultParallelism()).mapToObj(i -> new SubmissionPublisher<LongRange>()).collect(Collectors.toList());
		IntStream.range(0, getDefaultParallelism()).forEach(i -> publishers.get(i).subscribe(workers.get(i))); 
	}

	@Override
	public long countPrimes() throws CountingException {
		// Round-robin each range to one publisher queue.
		IntStream.range(0, ranges.size()).forEach(i -> publishers.get(i % getDefaultParallelism()).submit(ranges.get(i)));
				
		// Close each publisher queue
		IntStream.range(0, getDefaultParallelism()).forEach(i -> publishers.get(i).close());
		
		// Sum the results of each queue.
		return workers.stream().mapToLong(o -> o.getCount()).sum();
	}
	
	private static class ReactiveWorker implements Flow.Subscriber<LongRange> {

		private final CompletableFuture<Long> finalCount = new CompletableFuture<>();
		private long count = 0;
		private Subscription subscription;
		
		@Override
		public void onSubscribe(Subscription subscription) {
			this.subscription = subscription;
			
			// Subscriber must communicate that it's ready to receive requests.
			subscription.request(1);
		}
		
		@Override
		public void onComplete() {
			finalCount.complete(count);
		}

		@Override
		public void onError(Throwable exception) {
			System.err.println("Error: " + exception.getMessage());
		}

		@Override
		public void onNext(LongRange nextRange) {
			count += Util.countPrimesForOneRange(nextRange).get();
			
			// Subscriber must communicate that it's ready to receive more requests.			
			subscription.request(1);
		}
			
		public long getCount() throws CountingException {
			return Util.uncheckedGet(finalCount);
		}
	}
}
