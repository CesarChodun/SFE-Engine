package resources.conversion;

import java.io.File;

public interface Converter {

	public String fileExtensionFrom();
	
	public String fileExtensionTo();
	
	public void convert(File from, File to);
	
}
