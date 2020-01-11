package resources.conversion.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import resources.conversion.Converter;

/**
 * Compiles SPIRV shader code to binaries.
 * 
 * @author Cezary Chodun
 * @since 11.01.2020
 */
public class SPIRVConverter implements Converter{

	/** Path to the GLSL validator executable. */
	private static final String GLSLANG_VALIDATOR_LOCATION = "glslangValidator.exe";
	private static final String[] EXT = {
			"conf", 
			"vert", 
			"tesc", 
			"tese", 
			"geom", 
			"frag", 
			"comp", 
			"mesh", 
			"task", 
			"rgen", 
			"rint", 
			"rahit", 
			"rchit", 
			"rmiss", 
			"rcall", 
			"glsl", 
			"hlsl"
			};
	
	/** Class specific logger. */
	private Logger logger = Logger.getLogger(SPIRVConverter.class.getName());	

	@Override
	public void convert(File from, File to) {
		File newOut = new File(to.getPath() + ".spv");
		logger.log(Level.INFO, "Converting " + from.getPath() + " to SPIRV shader and saveing it to " + newOut.getPath());
		File validator = new File(GLSLANG_VALIDATOR_LOCATION);
		assert(validator.exists());
		ProcessBuilder builder = new ProcessBuilder(validator.getAbsolutePath(), "-V100", "-o", newOut.getAbsolutePath(), from.getAbsolutePath());
		builder.directory(new File(System.getProperty("user.dir")));
        builder.redirectErrorStream(true);
        
        try {
			Process p = builder.start();
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuilder sb = new StringBuilder();
			
	        String line;
	        while ((line = r.readLine()) != null) 
	            sb.append(line + "\n");
	        
	        logger.log(Level.FINER, sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
        
	}
	
	@Override
	public List<String> fileExtensionFrom() {
		return Arrays.asList(EXT);
	}

}
