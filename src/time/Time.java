package time;

public class Time {
	
	protected static long THOUSAND = 1000;

	public static long getNano() {
		return System.nanoTime();
	}
	
	public static long getMili() {
		return System.nanoTime() / THOUSAND;
	}
}
