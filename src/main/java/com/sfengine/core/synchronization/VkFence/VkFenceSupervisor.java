package com.sfengine.core.synchronization.VkFence;

import java.util.*;

public class VkFenceSupervisor {

    private final Queue<VkFenceWrapper> fences = new LinkedList<>();

    public void addFence(VkFenceWrapper fence) {
        synchronized (fences) {
            fences.add(fence);
        }
    }

    public void checkAll() {
        synchronized (fences) {

            VkFenceWrapper fence = fences.peek();
            if (fence == null)
                return;

            if (fence.check())
                fences.poll();
        }
//        List<VkFenceWrapper> toRemove = new LinkedList<>();
//
//        synchronized (fences) {
//            for (VkFenceWrapper fence : fences) {
//                if (fence.check())
//                    toRemove.add(fence);
//            }
//
//            fences.removeAll(toRemove);
//        }
    }
}
