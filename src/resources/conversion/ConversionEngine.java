package resources.conversion;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConversionEngine {
	
	private class ConversionTask implements Runnable {
		
		private Converter conv;
		private File from, to;
		
		public ConversionTask (Converter conv, File from, File to) {
			this.conv = conv;
			this.from = from;
			this.to = to;
		}
		

		@Override
		public void run() {
			conv.convert(from, to);
		}
	};

	private Map<String, Converter> converters;
	private ThreadPoolExecutor pool;
	
	public ConversionEngine(int threads) {
		converters = Collections.synchronizedMap(new HashMap<String, Converter>());
		pool = new ThreadPoolExecutor(threads, threads, 10, 
				TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1024));
	}
	
	public ConversionEngine(int threads, int maxThreads) {
		converters = Collections.synchronizedMap(new HashMap<String, Converter>());
		pool = new ThreadPoolExecutor(threads, maxThreads, 10, 
				TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1024));
	}
	
	public void addConverter(Converter conv) {
		converters.put(conv.fileExtensionFrom(), conv);
	}
	
	private String getExt(File f) {
		String[] tab = f.getName().split("\\.");
		
		return tab[tab.length - 1];
	}
	
	private File subFile(File from, File to) {
		return new File(to.getPath() + "/" + from.getName());
	}
	
	private void convert(File from, File to) {
		Converter conv = converters.get(getExt(from));
		if (conv != null) {
			ConversionTask task = new ConversionTask(conv, from, to);
			
			synchronized (this) {
				pool.execute(task);
			}
		}
	}
	
	public void convertFile(File from, File to) {
		
		if (!to.isDirectory()) {
			if (from.isDirectory())
				throw new AssertionError("Cannot convert directiories to froms!");
			
			convert(from, to);
		}
		else if (!from.isDirectory()) 
			convert(from, subFile(from, to));
		else
			for (File f : from.listFiles())
				convertFile(f, subFile(f, to));
	}
	
}
