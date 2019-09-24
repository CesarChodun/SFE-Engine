package core.resources;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * TODO documentation
 * @author Cezary Chodun
 *
 */
public class ConfigFile implements Closeable{
	
	private static final Logger cfgLogging = Logger.getLogger(ConfigFile.class.getName());
	
	private static final Level NULL_PARAMETER_RETURNED = Level.FINER;

	boolean created = false;
	private JSONObject file;
	private Asset asset;
	private String path;
	
	public ConfigFile(Asset asset, String path) throws IOException, AssertionError {
		this.asset = asset;
		this.path = path;
		
		try {
			this.file = asset.getJSON(path);
		} catch (IOException e) {
			cfgLogging.log(Level.INFO, "Failed to locate CFG file: " + asset.getAssetLocation().getPath() + "/" + path, e);
		}
		
		if (this.file == null) {
			
			if (!asset.newFile(path))
				throw new AssertionError("Failed to create json file!");
			created = true;
			
			this.file = new JSONObject();//asset.getJSON(path);
		}
	}
	
	public void close() {
		if (created) {
			FileWriter writer;
			try {
				writer = new FileWriter(asset.get(path));
				file.write(writer, ResourceUtil.INDENT_FACTOR, ResourceUtil.INDENT);
				writer.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> getArray(String key, List<T> defaultValue) {		
		if (file.isNull(key)) {
			file.put(key, defaultValue);
			return defaultValue;
		}

		JSONArray arr = file.getJSONArray(key);
		List<T> out = new ArrayList<T>();
		for (int i = 0; i < arr.length(); i++)
			out.add((T) arr.get(i));
		
		return out;
	}
	
	public String getString(String key, String defaultValue) {
		
		if (!file.isNull(key)) 
			return file.getString(key);
		
		file.put(key, defaultValue);
		return defaultValue;
	}
	
	public Integer getInteger(String key, Integer defaultValue) {
		
		if (!file.isNull(key))
			return file.getInt(key);
		
		file.put(key, (int) defaultValue);
		return defaultValue;
	}

	public Float getFloat(String key, Float defaultValue) {
		
		if (!file.isNull(key))
			return file.getFloat(key);
		
		file.put(key, (float) defaultValue);
		return defaultValue;
	}
	
	public Double getDouble(String key, Double defaultValue) {
		
		if (!file.isNull(key))
			return file.getDouble(key);
		
		file.put(key, (double) defaultValue);
		return defaultValue;
	}
	
	public Boolean getBoolean(String key, Boolean defaultValue) {
		
		if (!file.isNull(key))
			return file.getBoolean(key);
		
		file.put(key, (boolean) defaultValue);
		return defaultValue;
	}
}
