package game.rendering;

public class FPSCounter {
	
	public static long NANO = 1 * 1000 * 1000 * 1000;

	private int count = 0;
	private long lastTime;
	private int[] remembered;
	private int sum = 0;
	private int nid = 0;
	private int samples;
	
	public double avrFPS;
	
	public FPSCounter(int samples) {
		this.samples = samples;
		remembered = new int[samples];
		for(int i = 0; i < samples; i++)
			remembered[i] = 0;
		
		avrFPS = 1;
		lastTime = System.nanoTime();
	}
	
	private void newReading(long atReading) {		
		sum -= remembered[nid];
		sum += count;
		remembered[nid++] = count;
		
		nid = nid%samples;
		avrFPS = (double)sum/samples;
		
		lastTime = atReading;
		count = 0;
	}
	
	public void newFrame() {
		count++;
		
		long time = System.nanoTime();
		if(time - lastTime >= NANO)
			newReading(time);
	}
}
