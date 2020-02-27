package core.synchronization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Synchronized queue that stores tasks and their
 * dependencies. It invokes the tasks as soon as
 * all of the dependencies are completed.
 * 
 * @author Cezary Chodun
 * @since 26.02.2020
 */
public class SmartWaitQueue {

	private Map<Dependency, List<SynchronizedTask>> links;
	private Map<SynchronizedTask, Integer> degree;
	
	/**
	 * Creates an empty queue.
	 */
	public SmartWaitQueue() {
		links = new HashMap<>();
		degree = new HashMap<>();
	}
	
	/**
	 * Adds a task to the queue.
	 * 
	 * @param task	the task.
	 */
	public void addTask(SynchronizedTask task) {
		List<Dependency> deps = task.dependencies();
		if (deps == null || deps.size() == 0) {
			invoke(task);
			return;
		}
			
		synchronized(this) {
			int added = 0;
			
			for (Dependency d : deps) {
				if (d != null && !d.isReleased()) {
					d.addSmartQueue(this);
					
					if (!links.containsKey(d))
						links.put(d, new ArrayList<>());
					
					links.get(d).add(task);
					added++;
				}
			}

			if (added == 0) {
				invoke(task);
				return;
			}
			degree.put(task, added);
		}
	}
	
	/**
	 * Updates the state of a dependency.
	 * 
	 * @param d	the dependency.
	 */
	public void popDependency(Dependency d) {
		
		List<SynchronizedTask> toInvoke = new ArrayList<>();
		
		synchronized(this) {
			List<SynchronizedTask> tasks = links.remove(d);
			
			for (SynchronizedTask t : tasks) {
				int x = degree.get(t) - 1;
				
				if (x <= 0) {
					degree.remove(t);
					toInvoke.add(t);
				}
				else
					degree.put(t, x);
			}
		}
		
		for (SynchronizedTask t : toInvoke)
			invoke(t);
	}
	
	/**
	 * Invokes a task.
	 * 
	 * @param task	the task.
	 */
	private void invoke(SynchronizedTask task) {
		task.run();
	}
}
