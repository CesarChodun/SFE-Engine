package time;

public interface RepeatableEvent extends SheduledEvent{

	/**
	 * 
	 * @return next occurrence of the event in milliseconds.
	 */
	public boolean nextOccurance();
	
}
