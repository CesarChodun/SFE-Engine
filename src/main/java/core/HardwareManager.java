package main.java.core;

import static main.java.core.hardware.HardwareUtil.*;
import static main.java.core.rendering.RenderAssetUtil.*;
import static main.java.core.rendering.RenderUtil.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryUtil.*;

import main.java.core.hardware.Monitor;
import main.java.core.hardware.PhysicalDeviceJudge;
import main.java.core.resources.Asset;
import main.java.core.resources.ConfigFile;
import main.java.core.result.GLFWError;
import main.java.core.result.VulkanException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkApplicationInfo;
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

    /**
     * Initializes the most basic engine libraries.
     *
     * @throws VulkanException If there was a problem with Instance creation process, or when the
     *     device enumeration process failed.
     * @throws IOException If failed to create JSON file, or if an I/O error occurred.
     */
    public static void init(VkApplicationInfo appInfo, Asset config)
            throws VulkanException, IOException {

        initGLFW();
        instance = createInstanceFromAsset(appInfo, config, requiredExtensions);
        physicalDevices = enumeratePhysicalDevices(instance);

        hardwareCFG = new ConfigFile(config.getSubAsset(HARDWARE_ASSET_FILE), HARDWARE_CFG);
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
}
