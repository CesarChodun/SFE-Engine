package demos.helloResources;

import java.io.File;
import java.util.concurrent.Semaphore;

import resources.conversion.ConversionEngine;
import resources.conversion.converters.SPIRVConverter;

public class Main {
	
	private static final File 
		SOURCE_FILE = new File("res"),
		STORAGE_FILE = new File("storage");

	public static void main(String[] args) {
		
		ConversionEngine cEngine = new ConversionEngine(2, 4);
		cEngine.addConverter(new SPIRVConverter());
		
		Semaphore conversionComplete = new Semaphore(0);
		int remaining = cEngine.convertFile(SOURCE_FILE, STORAGE_FILE, conversionComplete);
		
		System.out.println("Waiting");
		try {
			conversionComplete.acquire(remaining);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Done " + remaining);
		
		
		cEngine.shutDown();
	}

}
