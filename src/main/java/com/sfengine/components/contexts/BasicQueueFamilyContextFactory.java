package com.sfengine.components.contexts;

import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextFactoryProvider;
import com.sfengine.core.context.QueueFamilyContext;
import com.sfengine.core.context.QueueFamilyContextFactory;

public class BasicQueueFamilyContextFactory {

    public static BasicQueueFamilyContext createQueueFamilyContext(String name, ContextDictionary dict) {
        QueueFamilyContextFactory factory =
                ContextFactoryProvider.getFactory(
                        QueueFamilyContextFactory.CONTEXT_IDENTIFIER, QueueFamilyContextFactory.class);
        BasicQueueFamilyContext context =
                new BasicQueueFamilyContext(name, QueueFamilyContextFactory.CONTEXT_IDENTIFIER, dict);
        factory.putContext(context);
        return context;
    }

}
