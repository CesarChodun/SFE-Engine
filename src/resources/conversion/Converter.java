package resources.conversion;

import java.io.File;
import java.util.List;

public interface Converter {

	public List<String> fileExtensionFrom();
	
	public void convert(File from, File to);
	
}
