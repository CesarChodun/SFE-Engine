package debug;

import time.*;

public class Main {

	public static void main(String[] args) {		
		EventDispatcher ed = new EventDispatcher(10, 10);
		
		SheduledEvent se = new SheduledEvent() {
			
			long dispatch = Time.getMili() + 2000;

			@Override
			public void run() {
				System.out.println("sheduled");
			}

			@Override
			public long getMiliDispatchTime() {
				return dispatch;
			}

			@Override
			public boolean forceDispatch() {
				return true;
			}
			
		};
		ed.addSheduled(se);
		
		RepeatableEvent re = new RepeatableEvent() {

			long dispatch = Time.getMili() + 1000;
			
			@Override
			public long getMiliDispatchTime() {
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
				dispatch = Time.getMili() + 1000;
				return true;
			}
			
		};
		ed.addRepeatable(re);
		
		while(true) {
			System.out.println("tick");
			ed.tick();
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
