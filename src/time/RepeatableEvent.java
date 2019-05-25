package time;

public interface RepeatableEvent extends SheduledEvent{

	//TODO: Make documentation.
	/**
	 * 
	 * @return next occurrence of the event in milliseconds.
	 */
	public boolean nextOccurance();
	
}
