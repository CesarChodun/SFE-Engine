package com.sfengine.components.window.input;

import com.sfengine.components.window.CFrame;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.rendering.Window;
import com.sfengine.core.resources.Destroyable;
import com.sfengine.core.synchronization.Dependable;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.core.synchronization.DependencyFence;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import java.util.*;

public class CFrameInput implements Dependable {

    private volatile Engine engine = EngineFactory.getEngine();
    private volatile CFrame frame;

    private volatile Set<MouseButtonCallback> mouseButtonCallbacks = new HashSet<>();
    private volatile Set<MouseCursorCallback> mouseCursorCallbacks = new HashSet<>();
    private volatile Map<Integer, Set<KeyboardCallback>> keyboardCallbacks = new HashMap<>();

    private List<Destroyable> windowResources = Collections.synchronizedList(new ArrayList<>());

    private final DependencyFence created = new DependencyFence();

    public CFrameInput(CFrame frame) {
        this.frame = frame;

        engine.addConfig( () -> {
            engine.addTask(
                    () -> {
                        //
                        GLFWMouseButtonCallback mbCallaback = new GLFWMouseButtonCallback() {
                            @Override
                            public void invoke(long window, int button, int action, int mods) {
                                engine.addFast(() -> {
                                    synchronized (mouseButtonCallbacks) {
                                        for (MouseButtonCallback call : mouseButtonCallbacks) {
                                            call.invoke(window, button, action, mods);
                                        }
                                    }
                                });
                            }
                        };
                        windowResources.add(() -> {mbCallaback.free();});
                        GLFW.glfwSetMouseButtonCallback(frame.handle(), mbCallaback);

                        GLFWCursorPosCallback cpCallback = new GLFWCursorPosCallback() {
                            @Override
                            public void invoke(long window, double mouseX, double mouseY) {
                                engine.addFast(() -> {
                                    synchronized (mouseCursorCallbacks) {
                                        for (MouseCursorCallback call : mouseCursorCallbacks) {
                                            call.invoke(window, mouseX, mouseY);
                                        }
                                    }
                                });
                            }
                        };
                        windowResources.add(() -> {cpCallback.free();});
                        GLFW.glfwSetCursorPosCallback(frame.handle(), cpCallback);

                        GLFWKeyCallback kCallback = new GLFWKeyCallback() {
                            @Override
                            public void invoke(long window, int key, int scancode, int action, int mods) {
                                engine.addFast(() -> {
                                    synchronized (keyboardCallbacks) {
                                        if (keyboardCallbacks.get(key) != null) {
                                            for (KeyboardCallback call : keyboardCallbacks.get(key)) {
                                                call.invoke(window, scancode, action, mods);
                                            }
                                        }
                                    }
                                });
                            }
                        };
                        windowResources.add(() -> {kCallback.free();});
                        GLFW.glfwSetKeyCallback(frame.handle(), kCallback);

                        created.release();
                    });
        }, frame.getDependency());
    }


    public void addMouseButtonCallback(MouseButtonCallback call) {
        synchronized (mouseButtonCallbacks) {
            mouseButtonCallbacks.add(call);
        }
    }

    public void removeMouseButtonCallback(MouseButtonCallback call) {
        synchronized (mouseButtonCallbacks) {
            mouseButtonCallbacks.remove(call);
        }
    }

    public void addMouseCursorCallback(MouseCursorCallback call) {
        synchronized (mouseCursorCallbacks) {
            mouseCursorCallbacks.add(call);
        }
    }

    public void removeMouseCursorCallback(MouseCursorCallback call) {
        synchronized (mouseCursorCallbacks) {
            mouseCursorCallbacks.remove(call);
        }
    }

    public boolean addKeyboardCallback(KeyboardCallback call) {
        synchronized (keyboardCallbacks) {
            if (keyboardCallbacks.get(call.getKey()) == null) {
                keyboardCallbacks.put(call.getKey(), new HashSet<>());
            }

            return keyboardCallbacks.get(call.getKey()).add(call);
        }
    }

    public boolean removeKeyboardCallback(KeyboardCallback call) {
        synchronized (keyboardCallbacks) {
            if (keyboardCallbacks.get(call.getKey()) == null) {
                return false;
            }

            return keyboardCallbacks.get(call.getKey()).remove(call);
        }
    }

    @Override
    public Dependency getDependency() {
        return created;
    }
}
