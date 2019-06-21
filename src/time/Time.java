package time;

public class Time {
	
	protected static long THOUSAND = 1000;
	protected static long MILLION = THOUSAND * THOUSAND;

	//TODO: Make documentation.
	public static long getNano() {
		return System.nanoTime();
	}

	//TODO: Make documentation.
	public static long getMilli() {
		return System.nanoTime() / MILLION;
	}
}
