package time;

import java.util.Comparator;

public interface ScheduledEvent extends Event{

	//TODO: Make documentation.
	public class EventComparator implements Comparator<ScheduledEvent>{

		@Override
		public int compare(ScheduledEvent o1, ScheduledEvent o2) {
			return Long.compare(o1.getMilliDispatchTime(), o2.getMilliDispatchTime());
		}
		
	}

	//TODO: Make documentation.
	/**
	 * 
	 * @return dispatch time in milliseconds.
	 */
	public long getMilliDispatchTime();
	
	
	/**
	 * Tells whether the event should be dispatched
	 * long after it's anticipated dispatch time.
	 * 
	 * @return 	True if the event should be dispatched anyway, 
	 * 			and false otherwise.
	 */
	public boolean forceDispatch();
}
