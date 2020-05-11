package com.sfengine.core.context.queue;

import com.sfengine.core.context.ContextFactory;
import com.sfengine.core.resources.Asset;

public class QueueContextFactory extends ContextFactory<QueueContext> {
    public static final String CONTEXT_IDENTIFIER = QueueContextFactory.class.getSimpleName();
    public static final String SUBASSET_NAME = "QueueContexts";

    @Override
    public String getContextIdentifier() {
        return CONTEXT_IDENTIFIER;
    }

    @Override
    public Asset getSubasset(Asset asset) {
        return asset.getSubAsset(SUBASSET_NAME);
    }
}
