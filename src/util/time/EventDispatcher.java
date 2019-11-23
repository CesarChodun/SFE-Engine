package util.time;

import java.util.ArrayList;
import java.util.Collection;
import java.util.PriorityQueue;

/**
 * 
 * Class for time events management.
 * 
 * @author Cezary Chodun
 *
 */
public class EventDispatcher {
	
	/**
	 * The default maximum amount of milliseconds that an event 
	 * can be late before it will be discarded.
	 */
	public static final long DEFAULT_MAX_LATENCY = 100;
	/**
	 * The default maximum amount of milliseconds that an event
	 * can be invoked ahead of the schedule.
	 */
	public static final long DEFAULT_MAX_SPEEDING = 5;

	/**
	 * The list of scheduled events.
	 * Events are stored based on the dispatch time
	 * in ascending order.
	 */
	private PriorityQueue<ScheduledEvent> scheduled = 
			new PriorityQueue<ScheduledEvent>(new ScheduledEvent.EventComparator());
	/**
	 * The list of repeatable events.
	 * Events are stored based on the dispatch time
	 * in ascending order.
	 */
	private PriorityQueue<RepeatableEvent> repeatable = 
			new PriorityQueue<RepeatableEvent>(new ScheduledEvent.EventComparator());
	
	/**
	 * The maximum amount of milliseconds that an event 
	 * can be late before it will be discarded.
	 */
	private long maxLatency = DEFAULT_MAX_LATENCY;
	/**
	 * The maximum amount of milliseconds that an event
	 * can be invoked ahead of the schedule.
	 */
	private long maxSpeeding = DEFAULT_MAX_SPEEDING;

	/**
	 * Creates a new {@link time.EventDispatcher} with default 
	 * latency({@link time.EventDispatcher#DEFAULT_MAX_LATENCY}),
	 * and default speeding({@link time.EventDispatcher#DEFAULT_MAX_SPEEDING}).
	 */
	public EventDispatcher() {}

	/**
	 * Creates a new {@link time.EventDispatcher} with specified 
	 * latency({@link time.EventDispatcher#maxLatency}),
	 * and speeding({@link time.EventDispatcher#maxSpeeding}). 
	 * 
	 * @param latency		the maximum amount of milliseconds that an event 
	 * 						can be late before it will be discarded
	 * @param speeding		the maximum amount of milliseconds that an event
	 * 						can be invoked ahead of the schedule.
	 */
	public EventDispatcher(long latency, long speeding) {
		this.maxLatency = latency;
		this.maxSpeeding = speeding;
	}
	
	/**
	 * Dispatches events based on the given millisecond 
	 * time(<b><code>milliTime</code></b>).
	 * 
	 * @param milliTime		current time in milliseconds
	 */
	public void update(long milliTime) {	
		ScheduledEvent e;
		while((e = scheduled.peek()) != null) {
			if(e.getMilliDispatchTime() > milliTime + maxSpeeding)
				break;	//To early for dispatch
			
			if(e.getMilliDispatchTime() + maxLatency < milliTime) {
				if(e.forceDispatch())
					e.run();
			}
			else
				e.run();
			
			scheduled.poll();
		}
		
		ArrayList<RepeatableEvent> nextCycle = new ArrayList<RepeatableEvent>();
		
		RepeatableEvent re;
		while((re = repeatable.peek()) != null) {			
			if(re.getMilliDispatchTime() > milliTime + maxSpeeding)
				break; //To early for dispatch
			
			if(re.getMilliDispatchTime() + maxLatency < milliTime) {
				if(re.forceDispatch())
					re.run();
			}
			else
				re.run();
			
			repeatable.poll();
			
			if(re.nextOccurance())
				nextCycle.add(re);
		}
		
		for(RepeatableEvent ne : nextCycle)
			repeatable.add(ne);
	}

	/**
	 * Adds a new {@link time.ScheduledEvent} to the 
	 * {@link time.EventDispatcher#scheduled} list.
	 * @param event		The event to be added to the scheduled list.
	 */
	public void addScheduled(ScheduledEvent event) {
		scheduled.add(event);
	}

	/**
	 * Adds a new {@link time.RepeatableEvent} to the 
	 * {@link time.EventDispatcher#repeatable} list.
	 * @param revent	The event to be added to the repeatable list.
	 */
	public void addRepeatable(RepeatableEvent revent) {
		repeatable.add(revent);
	}

	/**
	 * Adds the list of {@link time.ScheduledEvent} to the scchedule.
	 * @param event		The list of events to be added to the scchedule.
	 */
	public void addAllScheduled(Collection<ScheduledEvent> event) {
		scheduled.addAll(event);
	}

	/**
	 * Adds the list of {@link time.RepeatableEvent} to the scchedule.
	 * @param revent	The list of events to be added to the scchedule.
	 */
	public void addAllRepeatable(Collection<RepeatableEvent> revent) {
		repeatable.addAll(revent);
	}

	/**
	 * Clears the list of repeatable events({@link time.RepeatableEvent}).
	 */
	public void clearRepeatable() {
		repeatable.clear();
	}
}
