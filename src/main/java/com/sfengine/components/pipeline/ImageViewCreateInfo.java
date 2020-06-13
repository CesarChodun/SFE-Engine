package com.sfengine.components.pipeline;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;

import com.sfengine.core.resources.Asset;
import com.sfengine.core.resources.ConfigFile;
import com.sfengine.core.resources.Destroyable;
import java.io.IOException;
import java.util.ArrayList;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

/**
 * Class that loads and stores the VKImageViewCreateInfo object.
 *
 * @author Cezary Chodun
 * @since 23.10.2019
 */
public class ImageViewCreateInfo implements Destroyable {

    /** Keys for the configuration file. */
    public static final String FLAGS_KEY = "flags",
            VIEW_TYPE_KEY = "viewType",
            COLOR_FORMAT_KEY = "colorFormat",
            COMPONENTS_KEY = "com/sfengine/components",
            COMPONENTS_R_KEY = COMPONENTS_KEY + ".R",
            COMPONENTS_G_KEY = COMPONENTS_KEY + ".G",
            COMPONENTS_B_KEY = COMPONENTS_KEY + ".B",
            COMPONENTS_A_KEY = COMPONENTS_KEY + ".A",
            SUBRESOURCE_RANGE_KEY = "subresourceRange",
            SUB_ASPECT_MASK_KEY = SUBRESOURCE_RANGE_KEY + ".aspectMask",
            SUB_BASE_MIP_LEVEL_KEY = SUBRESOURCE_RANGE_KEY + ".baseMipLevel",
            SUB_LAYER_COUNT_KEY = SUBRESOURCE_RANGE_KEY + ".layerCount",
            SUB_BASE_ARRAY_LAYER_KEY = SUBRESOURCE_RANGE_KEY + ".baseArrayLayer",
            SUB_LEVEL_COUNT_KEY = SUBRESOURCE_RANGE_KEY + ".levelCount";

    /** Default string values. */
    public static final String DEFAULT_FLAGS = "",
            DEFAULT_VIEW_TYPE = "VK_IMAGE_VIEW_TYPE_2D",
            DEFAULT_COLOR_FORMAT = "VK_FORMAT_B8G8R8_UNORM",
            DEFAULT_COMPONENT_R = "VK_COMPONENT_SWIZZLE_R",
            DEFAULT_COMPONENT_G = "VK_COMPONENT_SWIZZLE_G",
            DEFAULT_COMPONENT_B = "VK_COMPONENT_SWIZZLE_B",
            DEFAULT_COMPONENT_A = "VK_COMPONENT_SWIZZLE_A",
            DEFAULT_ASPECT_MASK = "VK_IMAGE_ASPECT_COLOR_BIT",
            DEFAULT_LEVEL_COUNT = "VK_REMAINING_MIP_LEVELS";

    /** Default integer values. */
    public static final Integer DEFAULT_BASE_MIP_LEVEL = 0,
            DEFAULT_LAYER_COUNT = 1,
            DEFAULT_BASE_ARRAY_LAYER = 0;

    private ConfigFile cfg;

    /** The image view create info(with loaded data). */
    protected VkImageViewCreateInfo info;



    /**
     * Loads an VkImageViewCreateInfo object from the configuration file.
     *
     * @throws AssertionError When failed to load the data.
     * @throws IOException If an I/O error occurred.
     */
    public ImageViewCreateInfo(ConfigFile cfg) {
        this.cfg = cfg;
        info = VkImageViewCreateInfo.calloc();
        loadDataFromAsset();
    }

    /**
     * Loads VKImageViewCreateInfo data from the file.
     *
     * @throws AssertionError When failed to load the data.
     * @throws IOException If an I/O error occurred.
     */
    private void loadDataFromAsset() {

        try {
            info.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                    .pNext(NULL)
                    .flags(cfg.getFlags(VK10.class, FLAGS_KEY, new ArrayList<String>()))
                    .viewType(
                            cfg.getStaticIntFromClass(VK10.class, VIEW_TYPE_KEY, DEFAULT_VIEW_TYPE))
                    .format(
                            cfg.getStaticIntFromClass(
                                    VK10.class, COLOR_FORMAT_KEY, DEFAULT_COLOR_FORMAT));

            info.components()
                    .r(cfg.getStaticIntFromClass(VK10.class, COMPONENTS_R_KEY, DEFAULT_COMPONENT_R))
                    .g(cfg.getStaticIntFromClass(VK10.class, COMPONENTS_G_KEY, DEFAULT_COMPONENT_G))
                    .b(cfg.getStaticIntFromClass(VK10.class, COMPONENTS_B_KEY, DEFAULT_COMPONENT_B))
                    .a(
                            cfg.getStaticIntFromClass(
                                    VK10.class, COMPONENTS_A_KEY, DEFAULT_COMPONENT_A));

            info.subresourceRange()
                    .aspectMask(
                            cfg.getStaticIntFromClass(
                                    VK10.class, SUB_ASPECT_MASK_KEY, DEFAULT_ASPECT_MASK))
                    .baseMipLevel(cfg.getInteger(SUB_BASE_MIP_LEVEL_KEY, DEFAULT_BASE_MIP_LEVEL))
                    .layerCount(cfg.getInteger(SUB_LAYER_COUNT_KEY, DEFAULT_LAYER_COUNT))
                    .baseArrayLayer(
                            cfg.getInteger(SUB_BASE_ARRAY_LAYER_KEY, DEFAULT_BASE_ARRAY_LAYER))
                    .levelCount(
                            cfg.getStaticIntFromClass(
                                    VK10.class, SUB_LEVEL_COUNT_KEY, DEFAULT_LEVEL_COUNT));
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Failed to load Image View data.");
        } finally {
            cfg.close();
        }
    }

    /**
     * Returns the allocated create info object. <b>Note:</b> Implementation can make changes to the
     * create info file if and only if it wasn't destroyed jet.
     *
     * @return the image view create info.
     */
    public VkImageViewCreateInfo getInfo() {
        return info;
    }

    @Override
    public void destroy() {
        info.free();
    }
}
