package util;

/**
 * Data structure for storing pairs of objects.
 * @author Cezary Chodun
 *
 * @param <F>	The type of the first pair member.
 * @param <S>	The type of the second pair member.
 */
public class Pair <F, S>{

	/**
	 * First object in the pair.
	 */
	private F first;
	
	/**
	 * Second object in the pair.
	 */
	private S second;
	
	/**
	 * Creates a pair of two objects.
	 * @param first		- first pair object
	 * @param second	- second pair object
	 */
	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}
	
	/**
	 * Duplicates objects contained in the 'pair'.
	 * @param pair		- the pair to copy objects from 
	 */
	public Pair(Pair<F, S> pair) {
		this.first = pair.first;
		this.second = pair.second;
	}
	
	/**
	 * Creates a pair with switched pair members.
	 * @return	a new Pair with switched members
	 */
	public Pair<S, F> flip() {
		return new Pair<S, F>(this.second, this.first);
	}

	/**
	 * 
	 * @return the first object from the pair
	 */
	public F getFirst() {
		return first;
	}

	/**
	 * Sets the first object in the pair.
	 * @param object which will become the first member in the pair
	 */
	public void setFirst(F first) {
		this.first = first;
	}

	/**
	 * 
	 * @return the second object from the pair
	 */
	public S getSecond() {
		return second;
	}
	
	/**
	 * Sets the second object in the pair.
	 * @param object which will become the second member in the pair
	 */
	public void setSecond(S second) {
		this.second = second;
	}
}
