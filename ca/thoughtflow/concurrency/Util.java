package ca.thoughtflow.concurrency;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.LongStream;

/**
 * This utility class provides the common prime functions so that they can be shared by all prime counters. This reduces the code they need and can focus solely on the
 * concurrency models.
 * 
 * @author Nick Maiorano
 */
public interface Util {

	/**
	 * Default function to determine of a given number is prime or not.
	 * 
	 * @param primeCandidate The number to test.
	 * @return True if number is prime - false otherwise.
	 */
	static boolean isPrime(long primeCandidate) {
		boolean isPrime = primeCandidate == 2;

		if (primeCandidate > 2) {
			isPrime = true;
			for (int testValue = 2; testValue <= Math.sqrt(primeCandidate); ++testValue) {
				if (primeCandidate % testValue == 0) {
					isPrime = false;
					break;
				}
			}
		}

		return isPrime;
	}
	
	/**
	 * Returns the default level of parallelism based on the number of cores.
	 * 
	 * @return The default level of parallelism.
	 */
	static int getDefaultParallelism() {
		return Runtime.getRuntime().availableProcessors();
	}
	
	/**
	 * Counts the number of primes for the given list of ranges.
	 * 
	 * @param ranges The list of ranges for which to count primes.
	 * @return The count of primes wrapped in a Supplier function.
	 */
	static Supplier<Long> countPrimesForRange(List<LongRange> ranges) {
		return () -> ranges.stream().map(Util::countPrimesForOneRange).mapToLong(s -> (long) s.get()).sum();
	}
	
	/**
	 * Counts the number of primes for the given range.
	 * 
	 * @param range The range for which to count the primes.
	 * @return The count of primes wrapped in a Supplier function.
	 */
	static Supplier<Long> countPrimesForOneRange(LongRange range) {
		return () -> LongStream.range(range.getStart(), range.getEnd()).filter(Util::isPrime).count();
	}

	/**
	 * Returns the value of a future wrapped inside an unchecked exception to make prime counters less verbose.
	 * 
	 * @param future The future from which to get the value.
	 * @return The value of the future
	 * @throws CountingException Thrown if the future throws a counting exception.
	 */
	static Long uncheckedGet(Future<Long> future) throws CountingException {
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new CountingException("Could not get future value", e);
		}
	}
}