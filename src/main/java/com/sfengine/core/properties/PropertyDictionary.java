package com.sfengine.core.properties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PropertyDictionary {

    private final Map<String, Property> map = Collections.synchronizedMap(new HashMap<>());

    public PropertyDictionary() {}

    public PropertyDictionary(PropertyDictionary pdict) {
        putAll(pdict);
    }

    public PropertyDictionary(Property...properties) {
        put(properties);
    }

    public <T> T get(String name, Class<T> cls) {
        Object out = get(name);

        if (cls.isInstance(out))
            return cls.cast(out);
        return null;
    }

    public Object get(String name) {
        Property<?> prop = map.get(name);

        if (prop == null)
            return null;

        return prop.get();
    }

    public <T> void put(Property<T>... properties) {
        for (Property prop : properties)
            map.put(prop.getName(), prop);
    }

    public <T> void put(String name, T val) {
        put(new Property<T>(name, val));
    }

    public void putAll(PropertyDictionary pdict) {
        map.putAll(pdict.map);
    }
}
