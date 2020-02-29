package core.synchronization;

/**
 * 
 * @author Cezary Chodun
 * @since 26.02.2020
 */
public interface Dependency {

    /**
     * Adds a smart queue to the list, so that it can be updated
     * when the dependency will be released.
     * 
     * @param queue    the queue.
     */
    public void addSmartQueue(SmartWaitQueue queue);
    
    /**
     * Tells whether the dependency was satisfied.
     * 
     * @return    true if the dependency was satisfied
     *         and false otherwise.
     */
    public boolean isReleased();
}
