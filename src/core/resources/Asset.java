package core.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONObject;
import org.json.JSONTokener;

public class Asset {

	protected File location;
	
	public Asset(File location) {
		this.location = location;
	}
	
	public Asset(String path) {
		this(new File(path));
	}
	
	public File get(String path) throws FileNotFoundException {
		File out = new File(location.getAbsolutePath() + "/" + path);
		
		if(!out.exists())
			throw new FileNotFoundException("Failed to locate file: " + location.getAbsolutePath() + "/" + path);
		return out;
	}
	
	public JSONObject getJSON(String path) throws IOException {
		File file = get(path);
		
		FileReader fileReader = new FileReader(file);
		JSONObject out = new JSONObject(new JSONTokener(fileReader));
		fileReader.close();
		
		return out;
	}
	
	public Asset getSubAsset(String path) {
		return new Asset(location.getAbsoluteFile() + "/" + path);
	}
	
	public boolean exists(String path) {
		File out = new File(location.getAbsolutePath() + "/" + path);
		
		return out.exists();
	}
	
	public File getAssetLocation() {
		return location;
	}
	
	@Deprecated
	public ConfigFile getConfigFile(String path) throws IOException, AssertionError {
		return new ConfigFile(this, path);
	}
	
	/**
	 * 
	 * @see {@link java.io.File.createNewFile()}
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public boolean newFile(String fileName) throws IOException {
		File file = new File(location.getAbsolutePath() + "/" + fileName);
		return file.createNewFile();
	}
}
