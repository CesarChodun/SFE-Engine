package storage;

import java.io.File;
import java.util.Collection;

public interface Asset {

	//TODO: Docs
	public File getAssetFile(String name);

	//TODO: Docs
	public Collection<File> allAssets();

	//TODO: Docs
	public Collection<String> allAssetNames();
	
}
