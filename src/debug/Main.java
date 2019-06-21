package debug;

import time.*;

public class Main {

	public static void main(String[] args) {		
		EventDispatcher ed = new EventDispatcher(10, 10);
		
		ScheduledEvent se = new ScheduledEvent() {
			
			long dispatch = Time.getMilli() + 2000;

			@Override
			public void run() {
				System.out.println("sheduled");
			}

			@Override
			public long getMilliDispatchTime() {
				return dispatch;
			}

			@Override
			public boolean forceDispatch() {
				return true;
			}
			
		};
		ed.addScheduled(se);
		
		RepeatableEvent re = new RepeatableEvent() {

			long dispatch = Time.getMilli() + 1000;
			
			@Override
			public long getMilliDispatchTime() {
				return dispatch;
			}

			@Override
			public boolean forceDispatch() {
				return true;
			}

			@Override
			public void run() {
				System.out.println("repeat");
			}

			@Override
			public boolean nextOccurance() {
				dispatch = Time.getMilli() + 1000;
				return true;
			}
			
		};
		ed.addRepeatable(re);
		
		while(true) {
			System.out.println("tick");
			ed.update(Time.getMilli());
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
