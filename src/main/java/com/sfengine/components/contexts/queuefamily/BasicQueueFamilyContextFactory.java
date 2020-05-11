package com.sfengine.components.contexts.queuefamily;

import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextFactoryProvider;
import com.sfengine.core.context.queuefamily.QueueFamilyContextFactory;

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
