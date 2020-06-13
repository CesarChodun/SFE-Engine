package com.sfengine.core.synchronization.VkFence;


import com.sfengine.core.engine.EngineTask;

public class VkFenceSupervisorTask implements EngineTask {

    VkFenceSupervisor supervisor;

    public VkFenceSupervisorTask(VkFenceSupervisor supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    public void run() throws AssertionError {
        supervisor.checkAll();
    }
}
