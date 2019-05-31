package util;

public class Pair <R, T>{

	private R first;
	private T second;
	
	public Pair(R first, T second) {
		this.first = first;
		this.second = second;
	}
	
	public Pair(Pair<R, T> pair) {
		this.first = pair.first;
		this.second = pair.second;
	}
	
	public Pair<T, R> flip() {
		return new Pair<T, R>(this.second, this.first);
	}

	public R getFirst() {
		return first;
	}

	public void setFirst(R first) {
		this.first = first;
	}

	public T getSecond() {
		return second;
	}

	public void setSecond(T second) {
		this.second = second;
	}
}
