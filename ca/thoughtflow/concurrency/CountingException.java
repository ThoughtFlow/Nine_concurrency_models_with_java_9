package ca.thoughtflow.concurrency;

/**
 * Exception used to short-circuit the counting.
 * 
 * @author Nick Maiorano
 */
public class CountingException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	public CountingException(String message, Exception originalException) {
		super(message, originalException);
	}
	
	public CountingException(Exception originalException) {
		super(originalException);
	}
	
	public CountingException(String message) {
		super(message);
	}
}
