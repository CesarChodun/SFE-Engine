package core.resources;

import java.io.File;
import java.io.FileNotFoundException;

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
	
	public Asset getSubAsset(String path) {
		return new Asset(location.getAbsoluteFile() + "/" + path);
	}
}
