package com.sfengine.core.rendering.recording;

import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.synchronization.Dependable;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.core.synchronization.DependencyFence;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.*;

public class CMDProvider implements Dependable {

    private static final int MAX_CONCURRENT_RECORDINGS = 5;

    private final Engine engine = EngineFactory.getEngine();

    private volatile RecorderFactory recorderFactory;
    private volatile long[] frameBuffers;

    private final Queue<Recorder> ready = new LinkedList<>();
    private final Map<Long, Recorder> recordersInUse = new HashMap<>();
    private final Map<Recorder, Integer> cmdsInUse = new HashMap<>();

    private volatile int concurrentRecordings = 0;
    private volatile boolean needsUpdate = false;

    private final DependencyFence cmdsReady = new DependencyFence();

    public CMDProvider(RecorderFactory recorderFactory, long... frameBuffers) {
        this.recorderFactory = recorderFactory;
        this.frameBuffers = frameBuffers;
    }

    private void quietUpdate() {
        synchronized (this) {
            if (!needsUpdate)
                return;

            if (concurrentRecordings < MAX_CONCURRENT_RECORDINGS) {
                Recorder rec = recorderFactory.createRecorder();
                rec.record(frameBuffers);
                concurrentRecordings++;

                engine.addFast(() -> {
                    synchronized (this) {
                        boolean release = (ready.size() == 0);
                        ready.add(rec);
                        concurrentRecordings--;
                        if (release)
                            cmdsReady.release();
                    }
                    quietUpdate();
                }, rec.getDependency());
            }
        }
    }

    public void update() {
        synchronized (this) {
            needsUpdate = true;
        }
        quietUpdate();
    }

    public VkCommandBuffer getCMD(long frameBuffer) {
        synchronized (this) {
            while(ready.size() > 1)
                ready.poll();

            Recorder old = recordersInUse.get(frameBuffer);

            Recorder newRec = ready.peek();
//            cmdsInUse.get(newRec).;
            recordersInUse.put(frameBuffer, newRec);
            return recordersInUse.get(frameBuffer).getCMDs().get(frameBuffer);
        }
    }

    @Override
    public Dependency getDependency() {
        return cmdsReady;
    }
}
