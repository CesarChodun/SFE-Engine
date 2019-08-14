package core;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class Engine implements Runnable{

	private Set<EngineTask> tickTasks = new HashSet<EngineTask>();
	private Queue<EngineTask> tasks = new LinkedList<EngineTask>();
	private boolean running = false;
	
	private static final int MINIMUM_TASKS_PER_TICK = 5;
	private static final float TASKS_PER_TICK_SCALING = 0.5f;
	

	@Override
	public void run() {
		running = true;
		
		try {			
			while (running) {
				// Performs simple tasks(one time tasks)
				int taskToComplete = (int) (tasks.size() * TASKS_PER_TICK_SCALING) + MINIMUM_TASKS_PER_TICK;
				for (int  i = 0; i < taskToComplete && tasks.size() > 0; i++) 
					tasks.poll().run();

				// Performs per-tick tasks.
				for (EngineTask tickTask : tickTasks)
					tickTask.run();
			}
		}
		catch (AssertionError e) {
			running = false;
			e.printStackTrace();
		}
	}
	
	public synchronized void addTask(EngineTask task) {
		tasks.add(task);
	}
	
	public void addTickTask(EngineTask tickTask) {
		tickTasks.add(tickTask);
	}
	
	public void removeTickTask(EngineTask tickTask) {
		tickTasks.remove(tickTask);
	}
	
	public void stop() {
		running = false;
	}
	
	public boolean isRunning() {
		return running;
	}
}
