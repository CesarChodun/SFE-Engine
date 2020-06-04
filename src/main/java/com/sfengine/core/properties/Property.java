package com.sfengine.core.properties;

public class Property<T> {

    private String name;
    private T val;

    public Property(String name, T val) {
        this.name = name;
        this.val = val;
    }

    public T get() {
        return val;
    }

    public String getName() {
        return name;
    }

}
