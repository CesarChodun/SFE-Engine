package com.sfengine.core.synchronization.VkFence;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class VkFenceSupervisor {

    private final Set<VkFenceWrapper> fences = new HashSet<>();

    public void addFence(VkFenceWrapper fence) {
        synchronized (fences) {
            fences.add(fence);
        }
    }

    public void checkAll() {
        List<VkFenceWrapper> toRemove = new LinkedList<>();

        synchronized (fences) {
            for (VkFenceWrapper fence : fences) {
                if (fence.check())
                    toRemove.add(fence);
            }

            fences.removeAll(toRemove);
        }
    }
}
