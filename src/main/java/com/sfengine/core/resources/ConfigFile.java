package com.sfengine.core.resources;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * Class for obtaining configuration data from a file.
 *
 * @author Cezary Chodun
 * @since 23.10.2019
 */
public class ConfigFile extends ConfigAsset implements Closeable {

    /** Default logger for the class. */
    private static final Logger cfgLogging = Logger.getLogger(ConfigFile.class.getName());

    public static final String EXT = "cfg";

    /** Tells whether the JSON object was created. */
    boolean created = false;
    /** Parental asset folder. */
    private Asset asset;
    /** The path to the JSON file. */
    private String path;

    /**
     * Obtains configuration data from a file within the asset.
     *
     * @param asset Asset containing the configuration file.
     * @param path Path to the configuration file within the asset.
     * @throws IOException If an I/O error occurred.
     */
    public ConfigFile(Asset asset, String path) throws IOException {
        super(null);
        this.asset = asset;
        this.path = path;

        try {
            data = asset.getJSON(path);
        } catch (IOException e) {
            cfgLogging.log(
                    Level.INFO,
                    "Failed to locate CFG data: " + asset.getAssetLocation().getPath() + "/" + path,
                    e);
        }

        if (this.data == null) {
            created = true;

            data = new JSONObject(); // asset.getJSON(path);
        }
    }

    /** Saves JSON data to the file. */
    @Override
    public void close() {
        if (created) {
            FileWriter writer;
            try {
                asset.newFile(path);
                writer = new FileWriter(asset.get(path));
                data.write(writer, ResourceUtil.INDENT_FACTOR, ResourceUtil.INDENT);
                writer.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
