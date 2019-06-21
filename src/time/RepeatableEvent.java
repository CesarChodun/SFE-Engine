package time;

/**
 * Interface for time event management.
 * @author Cezary Chodun
 *
 */
public interface RepeatableEvent extends ScheduledEvent{

	/**
	 * Determines whether the event should be repeated.
	 * @return next occurrence of the event in milliseconds.
	 */
	public boolean nextOccurance();
	
}
