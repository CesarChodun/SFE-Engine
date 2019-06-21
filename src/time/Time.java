package time;

/**
 * Class containing informations 
 * about the current time
 * (both physical and application time).
 * @author Cezary Chodun
 *
 */
public class Time {
	
	protected static long THOUSAND = 1000;
	protected static long MILLION = THOUSAND * THOUSAND;

	/**
	 * Obtains system time in nanoseconds.
	 * @return 		- system time in nanoseconds
	 */
	public static long getNano() {
		return System.nanoTime();
	}

	/**
	 * Obtains system time in milliseconds.
	 * @return 		- system time in milliseconds
	 */
	public static long getMilli() {
		return System.nanoTime() / MILLION;
	}
}
