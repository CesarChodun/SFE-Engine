package com.sfengine.core;

public class EngineFactory {

    public enum BuiltInEngines {
        DEFAULT(new DefaultEngine());

        private Engine engine;

        private BuiltInEngines(Engine engine) {
            this.engine = engine;
        }

        private Engine getEngine() {
            return engine;
        }
    }

    private static Engine selectedEngine = BuiltInEngines.DEFAULT.getEngine();

    public static void runEngine() {
        selectedEngine.run();
    }

    public static void destroyEngine() {
        selectedEngine.destroy();
    }

    public static Engine getEngine() {
        return selectedEngine;
    }

    public static void setEngine(BuiltInEngines builtin) {
        selectedEngine = builtin.getEngine();
    }

    public static void setEngine(Engine engine) {
        selectedEngine = engine;
    }

}
