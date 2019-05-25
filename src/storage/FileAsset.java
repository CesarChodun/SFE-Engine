package storage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileAsset implements Asset{
	
	protected File file;
	protected String name;

	//TODO: Docs
	public FileAsset(File f, String name) {
		this.file = f;
		this.name = new String(name);
	}

	@Override
	public File getAssetFile(String name) {
		if(this.name.contentEquals(name))
			return file;
		return null;
	}

	@Override
	public Collection<File> allAssets() {
		List<File> out = new ArrayList<File>();
		out.add(file);
		return out;
	}

	@Override
	public Collection<String> allAssetNames() {
		List<String> out = new ArrayList<String>();
		out.add(name);
		return out;
	}
	
	//TODO: Docs
	public File getFile() {
		return file;
	}

	//TODO: Docs
	public String getName() {
		return name;
	}

	
}
