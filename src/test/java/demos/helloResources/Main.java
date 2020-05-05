package test.java.demos.helloResources;

import main.java.components.resources.ConversionEngine;
import main.java.components.resources.converters.SPIRVConverter;
import java.io.File;
import java.util.concurrent.Semaphore;

public class Main {

    private static final File SOURCE_FILE = new File("res"), STORAGE_FILE = new File("storage");

    private static void deleteContents(File file) {
        for (File f : file.listFiles()) {
            if (!f.isFile()) {
                deleteContents(f);
            }
            f.delete();
        }
    }

    public static void main(String[] args) {

        final long startTime = System.nanoTime();

        STORAGE_FILE.mkdir();
        deleteContents(STORAGE_FILE);

        ConversionEngine cEngine = new ConversionEngine(2, 4);
        cEngine.addConverter(new SPIRVConverter());

        Semaphore conversionComplete = new Semaphore(0);
        int remaining = cEngine.convertFile(SOURCE_FILE, STORAGE_FILE, conversionComplete);
        cEngine.shutDown();

        System.out.println("Waiting");
        try {
            conversionComplete.acquire(remaining);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Done " + remaining);

        long endTime = System.nanoTime();

        System.out.println("Conversion took: " + (double) (endTime - startTime) / 1000000 + "ms");
    }
}
