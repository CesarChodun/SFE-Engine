package demos.util;

import java.io.File;
import java.util.concurrent.Semaphore;
import resources.conversion.ConversionEngine;
import resources.conversion.converters.SPIRVConverter;

/**
 * Converts shader programs from the resources package. Runs concurrently so it is required to wait
 * for the result.
 *
 * @author Cezary Chodun
 * @since 11.01.2020
 */
public class DefaultResourceConverter {

    private static final File SOURCE_FILE = new File("res"), STORAGE_FILE = new File("storage");
    private static final Integer THREADS = 4, MAX_THREADS = 8;

    private Semaphore sem;
    private int remaining;

    public DefaultResourceConverter() {}

    private static void deleteContents(File file) {
        for (File f : file.listFiles()) {
            if (!f.isFile()) {
                deleteContents(f);
            }
            f.delete();
        }
    }

    /**
     * Runs conversion on the resources package saving results to the storage folder.
     *
     * <p>Must not be run multiple times within the application lifetime.
     */
    public void runConversion() {

        if (sem != null) {
            throw new AssertionError("This conversion can only be run once!");
        }

        STORAGE_FILE.mkdir();
        deleteContents(STORAGE_FILE);

        ConversionEngine cEngine = new ConversionEngine(THREADS, MAX_THREADS);
        cEngine.addConverter(new SPIRVConverter());

        sem = new Semaphore(0);
        remaining = cEngine.convertFile(SOURCE_FILE, STORAGE_FILE, sem);

        cEngine.shutDown();
    }

    /** Waits for the files to be converted and saved to the storage directory. */
    public void await() {
        try {
            sem.acquire(remaining);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
