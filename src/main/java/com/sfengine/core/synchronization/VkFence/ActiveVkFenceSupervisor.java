package com.sfengine.core.synchronization.VkFence;

import java.util.Timer;
import java.util.TimerTask;

public class ActiveVkFenceSupervisor implements Runnable {

    private class SuperviosrTimerTask extends TimerTask {

        private  VkFenceSupervisor supervisor;

        public SuperviosrTimerTask(VkFenceSupervisor superviosr) {
            this.supervisor = superviosr;
        }

        @Override
        public void run() {
            supervisor.checkAll();
        }
    }

    private Timer timer;
    VkFenceSupervisor supervisor = new VkFenceSupervisor();

    @Override
    public void run() {

        timer = new Timer("Time timer");
        timer.schedule(new SuperviosrTimerTask(supervisor), 30, 10);

        supervisor.checkAll();
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public VkFenceSupervisor getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(VkFenceSupervisor supervisor) {
        this.supervisor = supervisor;
    }
}
