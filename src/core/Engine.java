package core;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Class for scheduling the game engine tasks.
 * It <b>SHOULD</b> be running from the first thread.
 * 
 * @author Cezary Chodun
 * @since 26.09.2019
 */
public class Engine implements Runnable{
	
	/** A minimal amount of tasks to be performed per tick. */
	private static final int MINIMUM_TASKS_PER_TICK = 5;
	/** A percentage of tasks that will be executed per tick(from queue). */
	private static final float TASKS_PER_TICK_SCALING = 0.5f;

	/** A list of tasks that should be executed once per engine tick. */
	private Set<EngineTask> tickTasks = new HashSet<EngineTask>();
	/** A list of task for engine to complete. */
	private Queue<EngineTask> tasks = new LinkedList<EngineTask>();
	/** Tells whether the engine should be running or if it should shut itself down.*/
	private boolean running = false;
	
	

	@Override
	/**
	 * 	Starts the engine.
	 */
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
	
	/**
	 * Adds a task to the task queue.
	 * If the task is given to the engine it is ensured that it will
	 * be performed within a few engine ticks.
	 * 
	 * @param task		Task to be added.
	 */
	public synchronized void addTask(EngineTask task) {
		tasks.add(task);
	}
	
	/**
	 * Adds a task to the per tick task list.
	 * The added task will execute once per tick.
	 * 
	 * @param tickTask	Per tick task that will be added to the list.
	 */
	public void addTickTask(EngineTask tickTask) {
		tickTasks.add(tickTask);
	}
	
	/**
	 * Removes a per tick task from the list.
	 * 
	 * @param tickTask	Per tick task to be removed.
	 */
	public void removeTickTask(EngineTask tickTask) {
		tickTasks.remove(tickTask);
	}
	
	/**
	 * Stops the engine(or rather informs the engine thread to stop).
	 */
	public void stop() {
		running = false;
	}
	
	/**
	 * Tells whether the engine is currently running.
	 * 
	 * @return		True if the engine is running and false if it is not.
	 */
	public boolean isRunning() {
		return running;
	}
}
