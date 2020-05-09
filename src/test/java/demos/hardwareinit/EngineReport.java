package demos.hardwareinit;

import com.sfengine.core.engine.EngineTask;
import com.sfengine.core.rendering.RenderUtil;
import com.sfengine.core.result.VulkanException;
import java.util.concurrent.Semaphore;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkLayerProperties;

public class EngineReport implements EngineTask {

    private Semaphore workDone;

    public EngineReport(Semaphore workDone) {
        this.workDone = workDone;
    }

    @Override
    public void run() throws AssertionError {
        try {
            System.err.println(getAvailableValidationLayers());

            System.err.println(getAvailableExtensions());
        } catch (VulkanException e) {
            System.out.println("Failed to obtain hardware information.");
            e.printStackTrace();
        }

        workDone.release();
    }

    /**
     * Creates a report of available vulkan validation layers.
     *
     * @return A string with the report.
     * @throws VulkanException
     */
    private String getAvailableValidationLayers() throws VulkanException {
        StringBuilder sb = new StringBuilder();

        sb.append("Available vulkan validation layers:\n");

        VkLayerProperties[] layers = RenderUtil.listAvailableValidationLayers();

        for (int i = 0; i < layers.length; i++) {
            sb.append("\t" + layers[i].layerNameString() + "\n");
        }

        return sb.toString();
    }

    /**
     * Creates a report of available vulkan extensions.
     *
     * @return A string containing the report.
     * @throws VulkanException
     */
    private String getAvailableExtensions() throws VulkanException {
        StringBuilder sb = new StringBuilder();

        sb.append("Available vulkan instance extensions:\n");

        VkExtensionProperties[] layers = RenderUtil.listAvailableExtensions();

        for (int i = 0; i < layers.length; i++) {
            sb.append("\t" + layers[i].extensionNameString() + "\n");
        }

        return sb.toString();
    }
}
