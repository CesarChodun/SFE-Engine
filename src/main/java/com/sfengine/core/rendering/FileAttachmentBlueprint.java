package com.sfengine.core.rendering;

import com.sfengine.core.Application;
import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.resources.ConfigFile;
import com.sfengine.core.resources.ResourceUtil;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.core.synchronization.DependencyFence;
import org.lwjgl.vulkan.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.VK10.*;

public class FileAttachmentBlueprint implements AttachmentBlueprint {

    /** Flags for the configuration file. */
    public static final String FLAGS_KEY = "flags",
            FORMAT_KEY = "format",
            SAMPLES_KEY = "samples",
            LOAD_OP_KEY = "loadOp",
            STORE_OP_KEY = "stroeOp",
            STENCIL_LOAD_OP_KEY = "stencilLoadOp",
            STENCIL_STORE_OP_KEY = "stencilStoreOp",
            INITIAL_LAYOUT_KEY = "initialLayout",
            FINAL_LAYOUT_KEY = "finalLayout",
            CLEAR_VALUE_COLOR_KEY = "clearValueColor",
            CLEAR_VALUE_DEPTH_STENCIL_KEY = "clearValueDepthStencil",
            COLOR_ATTACHMENT_KEY = "colorAttachment",
            IMAGE_TYPE_KEY = "imageType",
            MIP_LEVELS_KEY = "mipLevels",
            ARRAY_LAYERS_KEY = "arrayLayers",
            TILING_KEY = "tiling",
            USAGE_KEY = "usage",
            SHARING_MODE_KEY = "sharingMode",
            VIEW_TYPE_KEY = "viewType",
            COMPONENTS_KEY = "components",
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

    /** Default values for the configuration file. */
    public static final String DEFAULT_FORMAT = "VK_FORMAT_B8G8R8_UNORM",
            DEFAULT_SAMPLES = "VK_SAMPLE_COUNT_1_BIT",
            DEFAULT_LOAD_OP = "VK_ATTACHMENT_LOAD_OP_CLEAR",
            DEFAULT_STORE_OP = "VK_ATTACHMENT_STORE_OP_STORE",
            DEFAULT_STENCIL_LOAD_OP = "VK_ATTACHMENT_LOAD_OP_DONT_CARE",
            DEFAULT_STENCIL_STORE_OP = "VK_ATTACHMENT_STORE_OP_DONT_CARE",
            DEFAULT_INITIAL_LAYOUT = "VK_IMAGE_LAYOUT_UNDEFINED",
            DEFAULT_FINAL_LAYOUT = "VK_IMAGE_LAYOUT_UNDEFINED",
            DEFAULT_IMAGE_TYPE = "VK_IMAGE_TYPE_2D",
            DEFAULT_TILING = "VK_IMAGE_TILING_OPTIMAL",
            DEFAULT_USAGE = "VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT",
            DEFAULT_SHARING_MODE = "VK_SHARING_MODE_EXCLUSIVE",
            DEFAULT_VIEW_TYPE = "VK_IMAGE_VIEW_TYPE_2D",
            DEFAULT_COMPONENT_R = "VK_COMPONENT_SWIZZLE_R",
            DEFAULT_COMPONENT_G = "VK_COMPONENT_SWIZZLE_G",
            DEFAULT_COMPONENT_B = "VK_COMPONENT_SWIZZLE_B",
            DEFAULT_COMPONENT_A = "VK_COMPONENT_SWIZZLE_A",
            DEFAULT_ASPECT_MASK = "VK_IMAGE_ASPECT_COLOR_BIT",
            DEFAULT_LEVEL_COUNT = "VK_REMAINING_MIP_LEVELS";

    /** Default values for the configuration file. */
    public static final Integer
            DEFAULT_FLAGS = 0,
            DEFAULT_MIP_LEVELS = 1,
            DEFAULT_ARRAY_LAYERS = 1,
            DEFAULT_BASE_MIP_LEVEL = 0,
            DEFAULT_LAYER_COUNT = 1,
            DEFAULT_BASE_ARRAY_LAYER = 0;

    /** Default values for the configuration file. */
    public static final Float DEFAULT_CLEAR_VALUE_DEPTH_STENCIL = 1.0f;

    /** Default values for the configuration file. */
    public static final Float[] DEFAULT_CLEAR_VALUE_COLOR = {0.0f, 0.0f, 0.7f, 1.0f};

    /** Default values for the configuration file. */
    public static Boolean DEFAULT_COLOR_ATTACHMENT = true;

    private final Engine engine = EngineFactory.getEngine();

    private final VkAttachmentDescription description = VkAttachmentDescription.calloc();
    private final VkClearValue clearValue = VkClearValue.calloc();
    private final VkImageCreateInfo imageInfo = VkImageCreateInfo.calloc();
    private final VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.calloc();

    private boolean primary = false;

    private final DependencyFence created = new DependencyFence();

    public FileAttachmentBlueprint(ContextDictionary dict, ConfigFile cfg) {
        //TODO: lambda version
//        engine.addConfig(() -> {
//            loadAll(cfg);
//            created.release();
//        }, Application.getDependency());
        loadAll(cfg);
    }

    private void loadAll(ConfigFile cfg) {
        primary = cfg.getBoolean("primary", primary);

        loadDescription(cfg);
        loadClear(cfg);
        loadImage(cfg);
        loadView(cfg);

        cfg.close();
    }

    private int getImageLayout(String name) {
        try {
            if (name.contains("KHR")) {
                return ResourceUtil.getStaticIntValueFromClass(KHRSwapchain.class, name);
            }

            return ResourceUtil.getStaticIntValueFromClass(VK10.class, name);
        } catch (NoSuchFieldException
                | SecurityException
                | IllegalArgumentException
                | IllegalAccessException e) {
            e.printStackTrace();
        }
        return VK10.VK_IMAGE_LAYOUT_UNDEFINED;
    }

    /**
     * Loads an attachment from a file.
     */
    private void loadDescription(ConfigFile cfg) {
        try {
            description
                    .flags(cfg.getFlags(VK10.class, FLAGS_KEY, new ArrayList<String>()))
                    .format(cfg.getStaticIntFromClass(VK10.class, FORMAT_KEY, DEFAULT_FORMAT))
                    .samples(cfg.getStaticIntFromClass(VK10.class, SAMPLES_KEY, DEFAULT_SAMPLES))
                    .loadOp(cfg.getStaticIntFromClass(VK10.class, LOAD_OP_KEY, DEFAULT_LOAD_OP))
                    .storeOp(cfg.getStaticIntFromClass(VK10.class, STORE_OP_KEY, DEFAULT_STORE_OP))
                    .stencilLoadOp(
                            cfg.getStaticIntFromClass(
                                    VK10.class, STENCIL_LOAD_OP_KEY, DEFAULT_STENCIL_LOAD_OP))
                    .stencilStoreOp(
                            cfg.getStaticIntFromClass(
                                    VK10.class, STENCIL_STORE_OP_KEY, DEFAULT_STENCIL_STORE_OP))
                    .initialLayout(
                            getImageLayout(
                                    cfg.getString(INITIAL_LAYOUT_KEY, DEFAULT_INITIAL_LAYOUT)))
                    .finalLayout(
                            getImageLayout(cfg.getString(FINAL_LAYOUT_KEY, DEFAULT_FINAL_LAYOUT)));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError("Failed to load attachment description.", e);
        }
    }

    private void loadClear(ConfigFile cfg) {
        if (cfg.getBoolean(COLOR_ATTACHMENT_KEY, DEFAULT_COLOR_ATTACHMENT)) {
            List<Float> colorArray =
                    cfg.getFloatArray(
                            CLEAR_VALUE_COLOR_KEY, Arrays.asList(DEFAULT_CLEAR_VALUE_COLOR));

            clearValue
                    .color()
                    .float32(0, colorArray.get(0).floatValue())
                    .float32(1, colorArray.get(1).floatValue())
                    .float32(2, colorArray.get(2).floatValue())
                    .float32(3, colorArray.get(3).floatValue());
        } else {
            clearValue
                    .depthStencil()
                    .depth(
                            cfg.getFloat(
                                    CLEAR_VALUE_DEPTH_STENCIL_KEY,
                                    DEFAULT_CLEAR_VALUE_DEPTH_STENCIL));
        }
    }

    private void loadImage(ConfigFile cfg) {
        imageInfo
                .sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
                .imageType(cfg.getStaticIntFromClass(VK10.class, IMAGE_TYPE_KEY, DEFAULT_IMAGE_TYPE))
                .mipLevels(cfg.getInteger(MIP_LEVELS_KEY, DEFAULT_MIP_LEVELS))
                .arrayLayers(cfg.getInteger(ARRAY_LAYERS_KEY, DEFAULT_ARRAY_LAYERS))
                .format(cfg.getStaticIntFromClass(VK10.class, FORMAT_KEY, DEFAULT_FORMAT))
                .tiling(cfg.getStaticIntFromClass(VK10.class, TILING_KEY, DEFAULT_TILING))
                .initialLayout(cfg.getStaticIntFromClass(VK10.class, INITIAL_LAYOUT_KEY, DEFAULT_INITIAL_LAYOUT))
                .usage(cfg.getStaticIntFromClass(VK10.class, USAGE_KEY, DEFAULT_USAGE))
                .samples(cfg.getStaticIntFromClass(VK10.class, SAMPLES_KEY, DEFAULT_SAMPLES))
                .sharingMode(cfg.getStaticIntFromClass(VK10.class, SHARING_MODE_KEY, DEFAULT_SHARING_MODE));

        if (imageInfo.usage() == VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT)
            imageInfo.extent().depth(1);
    }

    private void loadView(ConfigFile cfg) {
        try {
            viewInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                    .pNext(NULL)
                    .flags(cfg.getFlags(VK10.class, FLAGS_KEY, new ArrayList<String>()))
                    .viewType(
                            cfg.getStaticIntFromClass(VK10.class, VIEW_TYPE_KEY, DEFAULT_VIEW_TYPE))
                    .format(
                            cfg.getStaticIntFromClass(
                                    VK10.class, FORMAT_KEY, DEFAULT_FORMAT));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError("Failed to load image view create info.", e);
        }

        if (cfg.getBoolean(COLOR_ATTACHMENT_KEY, DEFAULT_COLOR_ATTACHMENT)) {
            viewInfo.components()
                    .r(cfg.getStaticIntFromClass(VK10.class, COMPONENTS_R_KEY, DEFAULT_COMPONENT_R))
                    .g(cfg.getStaticIntFromClass(VK10.class, COMPONENTS_G_KEY, DEFAULT_COMPONENT_G))
                    .b(cfg.getStaticIntFromClass(VK10.class, COMPONENTS_B_KEY, DEFAULT_COMPONENT_B))
                    .a(
                            cfg.getStaticIntFromClass(
                                    VK10.class, COMPONENTS_A_KEY, DEFAULT_COMPONENT_A));
        }
        else {
        }

        viewInfo.subresourceRange()
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
        if (imageInfo.usage() == VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT) {
            viewInfo.subresourceRange()
                    .aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT | VK_IMAGE_ASPECT_STENCIL_BIT);
        }
        else {
            int dd = viewInfo.subresourceRange().aspectMask();
            System.out.println(dd);
        }
    }

    @Override
    public VkAttachmentDescription getDescription() {
        return description;
    }

    @Override
    public VkClearValue getClearValue() {
        return clearValue;
    }

    @Override
    public VkImageCreateInfo getImageInfo() {
        return imageInfo;
    }

    @Override
    public VkImageViewCreateInfo getViewInfo() {
        return viewInfo;
    }

    @Override
    public boolean primary() {
        return primary;
    }

    @Override
    public void destroy() {
        description.free();
        clearValue.free();
        imageInfo.free();
        viewInfo.free();
    }

    @Override
    public Dependency getDependency() {
        return created;
    }
}
