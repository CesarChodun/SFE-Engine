package time;

import java.util.Comparator;

public interface SheduledEvent extends Event{

	//TODO: Make documentation.
	public class EventComparator implements Comparator<SheduledEvent>{

		@Override
		public int compare(SheduledEvent o1, SheduledEvent o2) {
			return Long.compare(o1.getMiliDispatchTime(), o2.getMiliDispatchTime());
		}
		
	}

	//TODO: Make documentation.
	/**
	 * 
	 * @return dispatch time in milliseconds.
	 */
	public long getMiliDispatchTime();
	
	
	/**
	 * Tells whether the event should be dispatched
	 * long after it's anticipated dispatch time.
	 * 
	 * @return 	True if the event should be dispatched anyway, 
	 * 			and false otherwise.
	 */
	public boolean forceDispatch();
}
