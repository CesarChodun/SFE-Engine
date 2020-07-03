package com.sfengine.core.synchronization;

public interface Dependable {

    /**
     * Returns a <code>Dependency</> object that will
     * be released when initialization of <b><code>this</></> object will be complete.
     *
     * @return <code>Dependency</> semaphore.
     */
    Dependency getDependency();
}
