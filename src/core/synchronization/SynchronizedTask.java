package core.synchronization;

import java.util.List;

/**
 * Represents a task that has to be
 * invoked after all of its dependencies
 * are completed.
 * 
 * @author Cezary Chodun
 * @since 26.20.2020
 */
public interface SynchronizedTask extends Runnable {

	/**
	 * The dependencies list.
	 * 
	 * @return
	 */
	public List<Dependency> dependencies();
	
}
