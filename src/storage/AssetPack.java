package storage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.BiConsumer;

public class AssetPack implements Asset{
	
	private HashMap<String, Asset> assets = new HashMap<String, Asset>();

	@Override
	public File getAssetFile(String name) {
		if(!assets.containsKey(name))
			return null;
		return assets.get(name).getAssetFile(name);
	}

	@Override
	public Collection<File> allAssets() {
		ArrayList<File> out = new ArrayList<File>();
		assets.forEach(new BiConsumer<String, Asset>(){

			@Override
			public void accept(String t, Asset u) {
				out.addAll(u.allAssets());
			}
		});
		
		return out;
	}

	@Override
	public Collection<String> allAssetNames() {
		ArrayList<String> out = new ArrayList<String>();
		assets.forEach(new BiConsumer<String, Asset>(){

			@Override
			public void accept(String t, Asset u) {
				out.addAll(u.allAssetNames());
			}
		});
		
		return out;
	}


	//TODO: Docs
	public void addAsset(Asset a, String name) {
		assets.put(new String(name), a);
	}

	//TODO: Docs
	public void addAsset(FileAsset a) {
		assets.put(new String(a.getName()), a);
	}

	//TODO: Docs
	public Asset removeAsset(String name) {
		return assets.remove(name);
	}

	//TODO: Docs
	public Asset removeAsset(FileAsset a) {
		return assets.remove(a.getName());
	}

	//TODO: Docs
	public void clear() {
		assets.clear();
	}
}
