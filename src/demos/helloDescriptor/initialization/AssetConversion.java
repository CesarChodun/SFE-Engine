package demos.helloDescriptor.initialization;

import java.io.File;

import converter.ConverterSet;
import demos.helloDescriptor.Main;

public class AssetConversion {

	private static final String STORAGE_FILE = "storage/" + Main.DEMO_NAME;
	private static final String RESOURCES_FILE = "resources/" + Main.DEMO_NAME;
	private static final String PLUGINS_FILE = "plugins/StorageManager";

	private ConverterSet converter;
	
	public static void convertData() {
		ConverterSet converter = new ConverterSet();
		converter.addConverters(new File(PLUGINS_FILE));
		converter.convert(new File(RESOURCES_FILE), STORAGE_FILE);
	}
	
	public AssetConversion(File converters) {
		converter = new ConverterSet();
		converter.addConverters(converters);
	}

	public void convert(File source, File dest) {
		converter.convert(source, dest.getAbsolutePath());
	}
	
}
