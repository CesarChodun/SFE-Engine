package core.synchronization;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * A fence for the dependencies. It updates automatically any smart queues waiting on it.
 *
 * @author Cezary Chodun
 * @since 26.02.2020
 */
public class DependencyFence extends Semaphore implements Dependency {

    /** */
    private static final long serialVersionUID = 1L;
    /** The SMQ set. */
    private Set<SmartWaitQueue> queues;

    private volatile boolean released = false;

    /**
     * Creates a DependencyFence with the given number of permits.
     *
     * <p><b>Note:</b> the first 'release' operation will unlock the fence for every SMQ waiting on
     * it. More precisely it is guaranteed that SMQ will not consume any of the semaphore permits.
     * But rather it will wait for the first 'release' operation.
     *
     * @param permits the initial number of permits available.
     */
    public DependencyFence(int permits) {
        super(permits);
        synchronized (this) {
            queues = new HashSet<>();
        }
    }

    @Override
    public void addSmartQueue(SmartWaitQueue queue) {
        synchronized (this) {
            if (released) {
                queue.popDependency(this);
            } else {
                queues.add(queue);
            }
        }
    }

    @Override
    public void release() {
        super.release();

        synchronized (this) {
            released = true;

            for (SmartWaitQueue q : queues) {
                q.popDependency(this);
            }
        }
    }

    @Override
    public boolean isReleased() {
        return released;
    }
}
