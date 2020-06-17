package com.sfengine.components.contexts.renderjob;

import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextFactoryProvider;
import com.sfengine.core.context.renderjob.RenderJobContextFactory;
import com.sfengine.core.rendering.CommandBufferFactory;
import org.jetbrains.annotations.Nullable;

public class BasicRenderJobContextFactory {

    public static BasicRenderJobContext createContext(String name, CommandBufferFactory cmdFactory, ContextDictionary dict, @Nullable Runnable update) {
        BasicRenderJobContext context =
                new BasicRenderJobContext(name, dict, RenderJobContextFactory.CONTEXT_IDENTIFIER, cmdFactory, update);
        RenderJobContextFactory factory =
                ContextFactoryProvider.getFactory(
                        RenderJobContextFactory.CONTEXT_IDENTIFIER, RenderJobContextFactory.class);
        factory.putContext(context);
        return context;
    }

    public static BasicRenderJobContext createContext(String name, CommandBufferFactory cmdFactory, ContextDictionary dict) {
        return createContext(name, cmdFactory, dict, null);
    }

}
