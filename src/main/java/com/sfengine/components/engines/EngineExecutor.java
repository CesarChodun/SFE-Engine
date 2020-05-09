package com.sfengine.components.engines;

import com.sfengine.core.engine.EngineTask;
import com.sfengine.core.synchronization.Dependency;

import java.util.concurrent.Executor;

public interface EngineExecutor extends Executor, Runnable {

    boolean isRunning();

    void shutdown();

    void addTickTask(EngineTask task);

    void removeTickTask(EngineTask tickTask);
}
