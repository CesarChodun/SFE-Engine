package com.sfengine.core.rendering;

import com.sfengine.core.resources.Destroyable;

public interface AttachmentSet extends Destroyable {

    long[] getViews(int frame);

    int framesCount();

}
