package resources.conversion;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.Nullable;

public class ConversionEngine {

    private class ConversionTask implements Runnable {

        private Converter conv;
        private File from, to;
        private Semaphore sem;

        public ConversionTask(Converter conv, File from, File to, @Nullable Semaphore workDone) {
            this.conv = conv;
            this.from = from;
            this.to = to;
            this.sem = workDone;
        }

        @Override
        public void run() {
            conv.convert(from, to);
            if (sem != null) {
                sem.release();
            }

            System.out.println("Converted!");
        }
    };

    private Map<String, Converter> converters;
    private ThreadPoolExecutor pool;

    public ConversionEngine(int threads) {
        converters = Collections.synchronizedMap(new HashMap<String, Converter>());
        pool =
                new ThreadPoolExecutor(
                        threads,
                        threads,
                        10,
                        TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue<Runnable>(1024));
    }

    public ConversionEngine(int threads, int maxThreads) {
        converters = Collections.synchronizedMap(new HashMap<String, Converter>());
        pool =
                new ThreadPoolExecutor(
                        threads,
                        maxThreads,
                        10,
                        TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue<Runnable>(1024));
    }

    public void addConverter(Converter conv) {
        for (String ext : conv.fileExtensionFrom()) {
            converters.put(ext, conv);
        }
    }

    private String getExt(File f) {
        String[] tab = f.getName().split("\\.");

        return tab[tab.length - 1];
    }

    private File subFile(File from, File to) {
        return new File(to.getPath() + "\\" + from.getName());
    }

    private int convert(File from, File to, Semaphore sem) {
        Converter conv = converters.get(getExt(from));
        if (conv != null) {
            ConversionTask task = new ConversionTask(conv, from, to, sem);

            synchronized (this) {
                pool.execute(task);
            }

            return 1;
        }

        return 0;
    }

    public int convertFile(File from, File to, Semaphore workDone) {

        int converted = 0;

        if (!to.isFile()) {
            to.mkdirs();
        }

        if (to.isFile()) {
            if (!from.isFile()) {
                throw new AssertionError("Cannot convert a directiory to a file!");
            }

            converted += convert(from, to, workDone);
        } else if (from.isFile()) {
            converted += convert(from, subFile(from, to), workDone);
        } else {
            for (File f : from.listFiles()) {
                if (!f.isFile()) {
                    converted += convertFile(f, subFile(f, to), workDone);
                } else {
                    converted += convert(f, subFile(f, to), workDone);
                }
            }
        }
        return converted;
    }

    public synchronized void shutDown() {
        pool.shutdown();
    }
}
