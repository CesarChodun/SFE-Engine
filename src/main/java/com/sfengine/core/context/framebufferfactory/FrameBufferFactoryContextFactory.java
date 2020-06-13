package com.sfengine.core.context.framebufferfactory;

import com.sfengine.core.context.ContextFactory;
import com.sfengine.core.resources.Asset;

public class FrameBufferFactoryContextFactory extends ContextFactory<FrameBufferFactoryContext> {

    public static final String CONTEXT_IDENTIFIER = FrameBufferFactoryContextFactory.class.getSimpleName();
    public static final String SUBASSET_NAME = "FrameBufferFactory";


    @Override
    public String getContextIdentifier() {
        return CONTEXT_IDENTIFIER;
    }

    @Override
    public Asset getSubasset(Asset asset) {
        return asset.getSubAsset(SUBASSET_NAME);
    }
}
