package time;

import java.util.Comparator;

/**
 * Interface for scheduled events.
 * @author Cezary Chodun
 *
 */
public interface ScheduledEvent extends Event{

	/**
	 * Events comparator(based on dispatch time).
	 * <p>
	 * <b>See</b> {@link java.util.Comparator}.
	 * </p>
	 */
	public class EventComparator implements Comparator<ScheduledEvent>{

		@Override
		public int compare(ScheduledEvent o1, ScheduledEvent o2) {
			return Long.compare(o1.getMilliDispatchTime(), o2.getMilliDispatchTime());
		}
		
	}

	/**
	 * Returns the time in milliseconds when
	 * the event should be dispatched.
	 * @return dispatch time in milliseconds
	 */
	public long getMilliDispatchTime();
	
	
	/**
	 * Tells whether the event should be dispatched
	 * long after it's anticipated dispatch time.
	 * 
	 * @return 	true if the event should be dispatched anyway, 
	 * 			and false otherwise
	 */
	public boolean forceDispatch();
}
