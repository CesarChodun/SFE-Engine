package resources.conversion;

import java.io.File;
import java.util.List;

/**
 * Converts files.
 * 
 * @author Cezary Chodun
 * @since 11.01.2020
 */
public interface Converter {

	/**
	 * Extension list for the input files.
	 * 
	 * @return the file extension list.
	 */
	public List<String> fileExtensionFrom();
	
	/**
	 * Converts a file('from') to a file('to').
	 * It might override the file extension.
	 * 
	 * @param from	File to be converted.
	 * @param to	File that will contain conversion result.
	 */
	public void convert(File from, File to);
	
}
