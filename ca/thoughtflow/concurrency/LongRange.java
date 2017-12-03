package ca.thoughtflow.concurrency;

/**
 * This class holds ranges of longs. This class is assigned to worker threads.
 * 
 * @author Nick Maiorano
 */
public class LongRange {
	
	private final long start;
	private final long end;
	
	public static LongRange createDummy() {
		return new LongRange(0, 0);
	}
	
	public static LongRange createNext(LongRange previous, long range) {
		return new LongRange(previous.getEnd() + 1, previous.getEnd() + range);
	}

	public static LongRange createLast(LongRange previous, long range) {
		return new LongRange(previous.getEnd() + 1, range);
	}
	
	private LongRange(long start, long end) {
		this.start = start;
		this.end = end;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}
	
	@Override
	public String toString() {
		return "Start: " + start + " end: " + end;
	}
}
