package com.sfengine.core;

import static com.sfengine.core.hardware.HardwareUtil.*;
import static com.sfengine.core.rendering.RenderAssetUtil.*;
import static com.sfengine.core.rendering.RenderUtil.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryUtil.*;

import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.result.GLFWError;
import com.sfengine.core.result.VulkanException;
import com.sfengine.core.hardware.Monitor;
import com.sfengine.core.hardware.PhysicalDeviceJudge;
import com.sfengine.core.resources.Asset;
import com.sfengine.core.resources.ConfigFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.sfengine.core.synchronization.DependencyFence;
import com.sfengine.core.synchronization.Dependency;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

/**
 * Class for hardware management. And Vulkan configuration.
 *
 * @author Cezary Chodun
 * @since 24.09.2019
 */
public class HardwareManager {

    /** Configuration files. */
    private static final String HARDWARE_ASSET_FILE = "hardware", HARDWARE_CFG = "hardware.cfg";

    /** Name of the queue family requirements field. */
    private static final String QUEUE_FAMILY_REQUIREMENTS_KEY = "queue_requirements";

    /** The enclosing class name. */
    private static final String CLASS_NAME = HardwareManager.class.getName();
    /** Default logger for this class. */
    protected static final Logger logger = Logger.getLogger(CLASS_NAME);

    /** A list of required extensions. */
    private static String[] requiredExtensions;

    /** Vulkan Instance. */
    private static VkInstance instance;
    /** A list of available vulkan devices. */
    private static VkPhysicalDevice[] physicalDevices;
    /** A list of available monitors(displays). */
    private static Monitor[] monitors;
    /** Hardware configuration file. */
    private static ConfigFile hardwareCFG;

    private static DependencyFence hardwareInitialized = new DependencyFence();

    /**
     * Initializes the most basic engine libraries.
     *
     * @throws VulkanException If there was a problem with Instance creation process, or when the
     *     device enumeration process failed.
     * @throws IOException If failed to create JSON file, or if an I/O error occurred.
     */
    public static void init() {
        Engine engine = EngineFactory.getEngine();

        engine.addConfig(() -> {
            Asset config = Application.getConfigAssets();

            initGLFW();
            try {
                instance = createInstanceFromAsset(Application.getApplicationInfo(), config, requiredExtensions);
            } catch (VulkanException | IOException e) {
                throw new Error("Failed to initialize hardware. Unable to proceed.", e);
            }
            try {
                physicalDevices = enumeratePhysicalDevices(instance);
            } catch (VulkanException e) {
                throw new Error("Failed to initialize hardware. Unable to proceed.", e);
            }

            try {
                hardwareCFG = new ConfigFile(config.getSubAsset(HARDWARE_ASSET_FILE), HARDWARE_CFG);
            } catch (IOException e) {
                throw new Error("Failed to initialize hardware. Unable to proceed.", e);
            }
            hardwareInitialized.release();
            System.out.println("HardwareManager data initialized!");
        }, Application.getDependency());
    }

    /** Initializes the GLFW library. This method <b>MUST</b> be invoked in order to use GLFW. */
    private static void initGLFW() {
        if (!glfwInit()) {
            throw new GLFWError("Failed to initialize GLFW!");
        }

        PointerBuffer pRequiredExtensions = glfwGetRequiredInstanceExtensions();
        int requiredInstanceExtensionsCount = pRequiredExtensions.capacity();
        requiredExtensions = new String[requiredInstanceExtensionsCount];
        for (int i = 0; i < requiredInstanceExtensionsCount; i++) {
            requiredExtensions[i] = memUTF8(pRequiredExtensions.get(i));
            System.err.println(requiredExtensions[i]);
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_FALSE);

        PointerBuffer pMonitors = glfwGetMonitors();
        int monitorCount = pMonitors.capacity();
        monitors = new Monitor[monitorCount];
        for (int i = 0; i < monitorCount; i++) {
            monitors[i] = new Monitor(pMonitors.get(i));
        }
    }

    /**
     * Searches for suitable queue family index(render queue family).
     *
     * @param physicalDevice <b>Must</b> be a valid pointer to VkPhysicalDevice.
     * @return queue family index, or <b>-1</b> if no suitable family was found.
     * @throws NoSuchFieldException if a field with the specified name is not found.
     * @throws SecurityException If a security manager, s, is present and the caller's class loader
     *     is not the same as or an ancestor of the class loader for the current class and
     *     invocation of s.checkPackageAccess() denies access to the package of this class.
     * @throws IllegalArgumentException if the specified object is not an instance of the class or
     *     interface declaring the underlying field (or a subclass or implementor thereof), or if
     *     the field value cannot be converted to the type int by a widening conversion.
     * @throws IllegalAccessException if this Field object is enforcing Java language access control
     *     and the underlying field is inaccessible.
     */
    public static int getMostSuitableQueueFamily(VkPhysicalDevice physicalDevice)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException,
                    IllegalAccessException {

        int requirements =
                hardwareCFG.getFlags(
                        VK10.class, QUEUE_FAMILY_REQUIREMENTS_KEY, new ArrayList<String>());

        VkQueueFamilyProperties.Buffer queueFamilyProperties =
                newQueueFamilyProperties(physicalDevice);
        int outIndex = getNextQueueFamilyIndex(0, requirements, queueFamilyProperties);
        queueFamilyProperties.free();

        return outIndex;
    }

    /**
     * Enumerates physical devices to find the most suitable one.
     *
     * @param judge Determines the device score(from 0 to MaxInt). If the score is lower or equal to
     *     0 the device is discarded.
     * @return The device handle or <b>null</b>.
     */
    public static VkPhysicalDevice getBestPhysicalDevice(PhysicalDeviceJudge judge) {

        int bestScore = 0;
        VkPhysicalDevice bestDevice = null;

        for (int i = 0; i < physicalDevices.length; i++) {
            VkPhysicalDevice device = physicalDevices[i];
            int score = judge.score(device);

            if (bestScore < score && score > 0) {
                bestScore = score;
                bestDevice = device;
            }
        }

        return bestDevice;
    }

    /** @return A list of available physical devices. */
    public static VkPhysicalDevice[] getPhysicalDevices() {
        return physicalDevices;
    }

    /** @return A handle to the primary monitor. */
    public static Monitor getPrimaryMonitor() {
        return monitors[0];
    }

    /** @return A list of available monitors. */
    public static Monitor[] getMonitors() {
        return monitors;
    }

    /** @return Application's Vulkan instance. */
    public static VkInstance getInstance() {
        return instance;
    }

    /** Destroys the hardware manager data(invoke after shutting down the engine). */
    public static void destroy() {
        hardwareCFG.close();
    }

    public static Dependency getDependency() {
        return hardwareInitialized;
    }
}
