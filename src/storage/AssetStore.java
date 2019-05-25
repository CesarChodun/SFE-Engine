package storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;

public class AssetStore {

	private File source = null;
	
	//TODO: 
	/**
	 * 
	 * @param assetsFolder
	 */
	public AssetStore(File assetsFolder) {
		this.source = assetsFolder;
	}
	
	//TODO: 
	/**
	 * 
	 * @param path
	 * @param name
	 * @return
	 * @throws FileNotFoundException
	 */
	public FileAsset getAsset(String path, String name) throws FileNotFoundException {
		File assetFile = new File(source.getAbsolutePath() + "/" + path);
		
		if(!assetFile.exists())
			throw new FileNotFoundException("File: " + assetFile.getPath() + " was not found.");
		
		return new FileAsset(assetFile, name);
	}
	
	//TODO: 
	/**
	 * 
	 * @param asset - a string with path to the asset
	 * 					and a name of the asset('=' separated)
	 * @return
	 * @throws FileNotFoundException 
	 */
	public FileAsset getAsset(String asset) throws FileNotFoundException {
		String[] tab = asset.split("=");
		String name = tab[tab.length-1];
		String path = asset.substring(0, asset.length() - name.length() -1);
		
		return getAsset(path, name);
	}
	
	//TODO:
	/**
	 * 
	 * @param assets - each string contains the path to a file
	 * 					and asset name('=' separated)
	 * @return
	 * @throws FileNotFoundException 
	 */
	public AssetPack getAsset(Collection<String> assets) throws FileNotFoundException {
		AssetPack pack = new AssetPack();
		
		for(String s : assets) 
			pack.addAsset(getAsset(s));
		
		return pack;
	}
}
