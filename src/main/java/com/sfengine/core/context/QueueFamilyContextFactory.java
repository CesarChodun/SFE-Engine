package com.sfengine.core.context;

import com.sfengine.core.resources.Asset;

import java.util.HashMap;
import java.util.Map;

public class QueueFamilyContextFactory extends ContextFactory<QueueFamilyContext>{
    public static final String CONTEXT_IDENTIFIER = QueueFamilyContextFactory.class.getSimpleName();
    public static final String SUBASSET_NAME = "QueueFamilyContexts";

    @Override
    public String getContextIdentifier() {
        return CONTEXT_IDENTIFIER;
    }

    @Override
    public Asset getSubasset(Asset asset) {
        return asset.getSubAsset(SUBASSET_NAME);
    }
}
