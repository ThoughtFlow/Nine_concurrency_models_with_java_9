package ca.thoughtflow.concurrency;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Main class used to benchmark the different concurrency models. Using average of multiple runs.
 *  
 * @author Nick Maiorano
 */
public class Benchmark {

	private static final String USAGE = "Timer numberOfRanges range primeCounterClassNames...";
	
	private static List<String> getClassesNotFound(List<String> classes) {
		
		return classes.stream().map(next -> {
			String primeFinder;
			try {
				Class.forName(next);
				primeFinder = null;
			} catch (ClassNotFoundException e) {
				primeFinder = next;;
			}
			return primeFinder;
		}).filter(next -> next != null).collect(Collectors.toList());	
	}
	
	@SuppressWarnings("unchecked")
	private static List<PrimeCounter> getPrimeCounters(List<String> classes) {
		
		List<PrimeCounter> primeCounters = classes.stream().map(next -> {
			Class<PrimeCounter> primeFinder;
			try {
				primeFinder = (Class<PrimeCounter>) Class.forName(next);
			} catch (ClassNotFoundException e) {
				primeFinder = null;
			}
			return primeFinder;
		}).map(clazz -> {
			PrimeCounter primeCounter;
			try {
				primeCounter = clazz.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				primeCounter = null;
			}
			return primeCounter;
		}).collect(Collectors.toList());
		
		return primeCounters;
	}
	
	private static List<LongRange> getRanges(int numberOfRanges, long range) {
		
		List<Float> sizes = Arrays.asList(.5f, 1f, 1.5f);
		List<LongRange> ranges = new LinkedList<>();
		long averageRange = range / numberOfRanges;
		LongRange previousRange = LongRange.createDummy();
		
		for (int index = 0; index < numberOfRanges - 1; ++index) {
			long nextRange = (long) (sizes.get(index % sizes.size()) * averageRange);
			previousRange = LongRange.createNext(previousRange, nextRange);
			ranges.add(previousRange);
		}
		
		ranges.add(LongRange.createLast(previousRange, range));
		
		return ranges;
	}
	
	private static Result timeExecution(PrimeCounter counter, List<LongRange> ranges) {
		
		Result result;
		
		counter.setup(ranges);
		long startTime = System.currentTimeMillis();

		try {
			long count = counter.countPrimes();
		
			long endTime = System.currentTimeMillis() - startTime;
		
			result = new Result(counter.getClass().getName(), endTime, count);
		}
		catch (CountingException exception) {
			result = new Result(counter.getClass().getName(), exception);
		}
		finally {
			counter.tearDown();
		}
		
		return result;
	}

	private static void executeTest(int rounds, List<String> primeCounterClassNames, List<LongRange> ranges) {
		
		final Map<String, Double> averages = new HashMap<>();
		
		IntStream.range(0, rounds).forEach(iter -> {
		   List<PrimeCounter> counters = getPrimeCounters(primeCounterClassNames);
		   List<Result> results = counters.stream().map(next -> timeExecution(next, ranges)).peek(System.out::println).collect(Collectors.toList());
		   boolean allIdentical = results.stream().reduce(results.get(0), (l, r) -> l != null && l.getCount() == r.getCount() ? l : null) != null;

		   if (!allIdentical) {
			   System.err.println("Error: Not all prime counters generated the same value");
		   }
		   
		   List<Result> countingErrorResults = results.stream().filter(r -> r.getException() != null).collect(Collectors.toList());
		   if (countingErrorResults.size() > 0) {
			   System.err.println("Obtained these counting errors:");
			   countingErrorResults.stream().forEach(r -> {System.err.println(r.getClass()); r.getException().printStackTrace();});
		   }
		   
		   results.stream().filter(r -> r.getException() == null).forEach(next -> averages.merge(next.getCounterClass(), (double) next.getDuration(), (v1, v2) -> v1 + v2));
		});
		
		averages.replaceAll((k, v) -> Math.round(v / rounds * 10) / 10d);
		
		System.out.println("=========");
		System.out.println("Averages:");
		averages.forEach((k, v) -> System.out.println(k + " " + v));
	}
	
	public static void main(String[] args) {

		if (args.length > 2) {
			try {
				int numberOfRanges = Integer.parseInt(args[0]);
				long range = Long.parseLong(args[1]);
				int rounds = Integer.parseInt(args[2]);


				List<String> primeCounterClassNames = new LinkedList<>();
				for (int index = 3; index < args.length; ++index) {
					primeCounterClassNames.add(args[index]);
				}

				List<String> classesNotFound = getClassesNotFound(primeCounterClassNames);
				if (classesNotFound.size() == 0) {
					List<LongRange> ranges = getRanges(numberOfRanges, range);
					System.out.println("Counting primes for range 1 to " + range);
					System.out.println("Number of ranges: " + numberOfRanges);
					System.out.println("Average range size: " + (int) range / numberOfRanges);
					System.out.println("Rounds: " + rounds);

					executeTest(rounds, primeCounterClassNames, ranges);
				} 
				else {
					System.err.println("These classes were not found");
					classesNotFound.stream().forEach(System.out::println);
				}
			}
			catch (NumberFormatException e) {
				System.err.println("Invalid arguments");
				System.err.println(USAGE);
			}
		}
		else {
			System.err.println(USAGE);
		}
	}
	
	private static class Result implements Comparable<Result> {

		private final String counterClass;
		private final long duration;
		private final long count;
		private final CountingException exception;
		
		public Result(String counterClass, long duration, long count) {
			this.counterClass = counterClass;
			this.duration = duration;
			this.count = count;
			this.exception = null;
		}
		
		public Result(String counterClass, CountingException exception) {
			this.counterClass = counterClass;
			this.exception = exception;
			
			this.duration = 0;
			this.count = 0;
		}
		
		public String getCounterClass() {
			return counterClass;
		}
		
		public long getDuration() {
			return duration;
		}
		
		public long getCount() {
			return count;
		}
		
		public CountingException getException() {
			return exception;
		}

		@Override
		public String toString() {
			String toString;
			
			if (exception == null) {
			  toString = "Counter class: " + getCounterClass() + ". Duration: " + getDuration() + ". Count: " + getCount();
			}
			else {
				  toString = "Counter class: " + getCounterClass() + ". Counting exception: " + exception.getMessage(); 
			}
			
			return toString;
		}

		@Override
		public int compareTo(Result objectToCompare) {

			return duration < objectToCompare.getDuration() ? -1 : duration == objectToCompare.getDuration() ? 0 : 1;
		}
	}
}