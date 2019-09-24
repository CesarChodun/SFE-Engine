package core.dataStructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * Quick access queue data structure.
 * 
 * @author Cezary Chodun
 *
 * @param <T>	The data type.
 */
public class FixedSizeQueue <T> {
	
	/** Position for the next element. */
	private int next;
	/** Position of the first element. */
	private int first;
	/** Size of the queue. */
	private int size;
	/** Storage data structure. */
	private List<T> arr;

	/**
	 * Creates the queue.
	 * 
	 * @param capacity		Queue capacity.
	 */
	public FixedSizeQueue(int capacity) {
		first = 0;
		next = 0;
		size = 0;
		arr = new ArrayList<T>(capacity);
		
		arr.clear();
		for (int i = 0; i < arr.size(); i++)
			arr.add(null);
	}
	
	/**
	 * Obtains the x-th element in the queue.
	 * 
	 * @param x				Index of the required element.
	 * @return		The x-th element.
	 */
	public T get(int x) {
		return arr.get((first + x) % arr.size());
	}
	
	/**
	 * 
	 * Sets the x-th element in the queue.
	 * 
	 * @param x				Element index.
	 * @param t				The value.
	 * @return		The inserted element.
	 */
	private T set(int x, T t) {
		return arr.set((first + x) % arr.size(), t);
	}
	
	/** Returns the queue size(not capacity). */
	public int size() {
		return size;
	}
	
	/** Returns the queue capacity. */
	public int capacity() {
		return arr.size();
	}

	/** Checks whether the queue is empty. */
	public boolean isEmpty() {
		return size == 0;
	}

	/** Checks whether an element is present in the queue(O(n) complexity). */
	public boolean contains(T o) {
		
		for (int i = 0; i < size; i++)
			if (o.equals(get(i)))
				return true;
				
		return false;
	}

	/** Adds a collection of elements to the queue. */
	public void addAll(Collection<? extends T> c) {

		for (T t  : c)
			add(t);
	}

	/** Clears the queue data. */
	public void clear() {
		size = 0;
		first = 0;
		next = 0;
		for (int i = 0; i < size; i++)
			arr.set(i, null);
	}

	/** Adds an element to the queue. */
	public void add(T e) {
		size++;
		set(size, e);
		size = Math.min(size, arr.size());
		next++;
		
		if (next >= arr.size())
			next -= arr.size();
	}

	/** Returns and removes the first element in the queue. */
	public T pop() {
		if(size == 0)
			return null;
		
		size--;
		first++;
		return set(-1, null);
	}

	/** Returns the first element in the queue. */
	public T top() {
		return get(0);
	}
	
}
