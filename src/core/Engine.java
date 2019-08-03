package core;

import java.util.HashSet;
import java.util.Set;

public class Engine implements Runnable{

	private Set<EngineTask> tasks = new HashSet<EngineTask>();
	private boolean running = false;

	@Override
	public void run() {
		running = true;
		
		try {
			while (running)
				for (EngineTask task : tasks)
					task.run();
		}
		catch (AssertionError e) {
			running = false;
		}
	}
	
	public void stop() {
		running = false;
	}
	
	public boolean isRunning() {
		return running;
	}
}
