package com.sfengine.core.context.renderjob;

import com.sfengine.core.context.Context;
import com.sfengine.core.rendering.recording.RenderJob;

public interface RenderJobContext extends Context {

    void recreateJobs(long... frameBuffers);

    RenderJob getJob(long frameBuffer);

}
