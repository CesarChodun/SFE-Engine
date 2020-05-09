package com.sfengine.components.util;

import com.sfengine.core.hardware.Monitor;
import com.sfengine.core.result.GLFWError;
import org.lwjgl.PointerBuffer;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryUtil.memUTF8;

public class GLFWUtil {

    /** Initializes the GLFW library. This method <b>MUST</b> be invoked in order to use GLFW. */
    private static String[] initGLFW() {
        if (!glfwInit()) {
            throw new GLFWError("Failed to initialize GLFW!");
        }

        PointerBuffer pRequiredExtensions = glfwGetRequiredInstanceExtensions();
        int requiredInstanceExtensionsCount = pRequiredExtensions.capacity();
        String[] requiredExtensions = new String[requiredInstanceExtensionsCount];
        for (int i = 0; i < requiredInstanceExtensionsCount; i++) {
            requiredExtensions[i] = memUTF8(pRequiredExtensions.get(i));
            System.err.println(requiredExtensions[i]);
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_FALSE);

        return requiredExtensions;
    }

    private static Monitor[] getMonitors() {
        PointerBuffer pMonitors = glfwGetMonitors();
        int monitorCount = pMonitors.capacity();
        Monitor[] monitors = new Monitor[monitorCount];
        for (int i = 0; i < monitorCount; i++) {
            monitors[i] = new Monitor(pMonitors.get(i));
        }
        return monitors;
    }

}
