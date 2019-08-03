package core.dataStructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class FixedSizeQueue <T> {
	
	//TODO: test
	
	private int next;
	private int first;
	private int size;
	private List<T> arr;

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
	 * Get the x-th element in the queue.
	 * 
	 * @param x
	 * @return
	 */
	public T get(int x) {
		return arr.get((first + x) % arr.size());
	}
	
	private T set(int x, T t) {
		return arr.set((first + x) % arr.size(), t);
	}
	
	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public boolean contains(Object o) {
		
		for (int i = 0; i < size; i++)
			if (o.equals(get(i)))
				return true;
				
		return false;
	}

	public Iterator<T> iterator() {
		return arr.iterator();
	}

	public void addAll(Collection<? extends T> c) {

		for (T t  : c)
			add(t);
	}

	public void clear() {
		size = 0;
		first = 0;
		next = 0;
		for (int i = 0; i < size; i++)
			arr.set(i, null);
	}

	public void add(T e) {
		size++;
		set(size, e);
		size = Math.min(size, arr.size());
		next++;
		
		if (next >= arr.size())
			next -= arr.size();
	}

	public T pop() {
		if(size == 0)
			return null;
		
		size--;
		first++;
		return set(-1, null);
	}

	public T top() {
		return get(0);
	}
	
}
