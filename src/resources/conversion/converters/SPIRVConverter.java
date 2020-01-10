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

public class SPIRVConverter implements Converter{

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
	

	private Logger logger = Logger.getLogger(SPIRVConverter.class.getName());	

	@Override
	public void convert(File from, File to) {
		logger.log(Level.INFO, "Converting " + from.getPath() + " to SPIRV shader and saveing it to " + to.getPath());
		File validator = new File(GLSLANG_VALIDATOR_LOCATION);
		assert(validator.exists());
		ProcessBuilder builder = new ProcessBuilder(validator.getAbsolutePath(), "-V100", "-o", to.getAbsolutePath(), from.getAbsolutePath());
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
