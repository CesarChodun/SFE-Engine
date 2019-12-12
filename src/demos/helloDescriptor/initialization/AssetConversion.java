package demos.helloDescriptor.initialization;

import java.io.File;

import converter.ConverterSet;

public class AssetConversion {

	private ConverterSet converter;
	
	public AssetConversion(File converters) {
		converter = new ConverterSet();
		converter.addConverters(converters);
	}

	public void convert(File source, File dest) {
		converter.convert(source, dest.getAbsolutePath());
	}
	
}
