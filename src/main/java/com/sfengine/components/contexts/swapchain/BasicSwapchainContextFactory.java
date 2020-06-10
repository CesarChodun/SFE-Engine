package com.sfengine.components.contexts.swapchain;

import com.sfengine.components.window.CFrame;
import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.properties.PropertyDictionary;

import java.util.Arrays;

public class BasicSwapchainContextFactory {

    private static BasicSwapchainContext creaate(ContextDictionary dict, String name, CFrame frame, long oldSwapchain) {
        PropertyDictionary pdict = new PropertyDictionary();
        pdict.put("oldSwapchain", oldSwapchain);
//        pdict.put("desiredColorFormats", Arrays.asList({}));
//        pdict.put("desiredColorSpaces", colorSpace);


        return new BasicSwapchainContext(name, dict, pdict, frame);
    }

    public BasicSwapchainContext create(ContextDictionary dict, String name, CFrame frame) {
//        BasicSwapchainContext context = creaate(dict, name, frame, )
        return null;
    }
}
