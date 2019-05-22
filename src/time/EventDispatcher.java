package time;

import java.util.Collection;
import java.util.PriorityQueue;

public class EventDispatcher {

	private PriorityQueue<SheduledEvent> sheduled = 
			new PriorityQueue<SheduledEvent>(new SheduledEvent.EventComparator());
	private PriorityQueue<RepeatableEvent> repeatable = 
			new PriorityQueue<RepeatableEvent>(new SheduledEvent.EventComparator());
	
	private long maxLatency = 100;
	private long maxSpeeding = 5;
	
	public EventDispatcher() {}
	
	public EventDispatcher(long latency, long speeding) {
		this.maxLatency = latency;
		this.maxSpeeding = speeding;
	}
	
	//TODO: Make documentation.
	
	public void tick() {
		long time = Time.getMili();
		
		SheduledEvent e;
		while((e = sheduled.peek()) != null) {
			if(e.getMiliDispatchTime() > time + maxSpeeding)
				break;	//To early for dispatch
			
			if(e.getMiliDispatchTime() + maxLatency < time) {
				if(e.forceDispatch())
					e.run();
			}
			else
				e.run();
			
			sheduled.poll();
		}
		
		RepeatableEvent re;
		while((re = repeatable.peek()) != null) {
			if(re.getMiliDispatchTime() > time + maxSpeeding)
				break; //To early for dispatch
			
			if(re.getMiliDispatchTime() + maxLatency < time) {
				if(re.forceDispatch())
					re.run();
			}
			else
				re.run();
			
			repeatable.poll();
			
			if(re.nextOccurance())
				repeatable.add(re);
		}
	}
	
	void addSheduled(SheduledEvent event) {
		sheduled.add(event);
	}
	
	void addRepeatable(RepeatableEvent revent) {
		repeatable.add(revent);
	}
	
	void addAllSheduled(Collection<SheduledEvent> event) {
		sheduled.addAll(event);
	}
	
	void addAllRepeatable(Collection<RepeatableEvent> revent) {
		repeatable.addAll(revent);
	}
	
	void dumpRepeatable() {
		repeatable.clear();
	}
}
