package core.rendering;

import static core.rendering.RenderUtil.*;

import core.resources.Asset;
import core.resources.ConfigFile;
import core.result.VulkanException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;

/**
 * Class with static methods for render object creation from a configuration file.
 *
 * @author Cezary Chodun
 * @since 25.09.2019
 */
public class RenderAssetUtil {

    /** Configuration file path(within the asset). */
    protected static final String INSTANCE_CONFIG_FILE = "instance.cfg";
    /** Configuration file keys. */
    protected static final String INSTANCE_VALIDATION_LAYERS_KEY = "VALIDATION_LAYERS",
            INSTANCE_EXTENSIONS_KEY = "EXTENSIONS";

    /**
     * Creates an instance from a asset file.
     *
     * @param appInfo The application info.
     * @param config A folder containing the configuration file.
     * @param requiredExtensions An array of required Vulkan extensions(can be empty).
     * @return The created Vulkan instance.
     * @throws VulkanException If there was a problem with Instance creation process.
     * @throws IOException If failed to create JSON file.
     */
    public static VkInstance createInstanceFromAsset(
            VkApplicationInfo appInfo, Asset config, String[] requiredExtensions)
            throws VulkanException, IOException {

        ConfigFile cfgFile = config.getConfigFile(INSTANCE_CONFIG_FILE);

        List<String> layerNames =
                cfgFile.getStringArray(INSTANCE_VALIDATION_LAYERS_KEY, new ArrayList<String>());
        List<String> extensions =
                cfgFile.getStringArray(INSTANCE_EXTENSIONS_KEY, new ArrayList<String>());
        for (String required : requiredExtensions) extensions.add(required);

        cfgFile.close();

        return createInstance(appInfo, layerNames, extensions);
    }
}
