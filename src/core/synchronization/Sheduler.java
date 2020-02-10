package core.synchronization;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sheduler implements Runnable {
	
	private Logger logger = Logger.getLogger(Sheduler.class.getName());

	private ArrayBlockingQueue<Order> pending;
	private ArrayBlockingQueue<Product> ready;
	private ConcurrentHashMap<Product, List<Order>> waiting; 
	private volatile boolean running = true, listening = true;
	
	private Semaphore waitNew;
	
	public Sheduler() {
		pending = new ArrayBlockingQueue<>(1000);
		ready = new ArrayBlockingQueue<>(1000);
		
		waiting = new ConcurrentHashMap<Product, List<Order>>();
		
		waitNew = new Semaphore(0);
	}

	@Override
	public void run() {
		Order pend = null;
		Product red = null;
		
		while(running) {
			if (pend == null && red == null) {
				try {
					if (!listening)
						break;
					waitNew.acquire();
				} catch (InterruptedException e) {
					logger.log(Level.FINEST, "Sheduler was interrupted.", e);
				}
			}
			
			pend = pending.poll();
			if (pend != null) {
				if (waiting.containsKey(pend.getProduct()))
					waiting.get(pend.getProduct()).add(pend);
				else
					pend.run();
			}
			
			red = ready.poll();
			if (red != null) {
				List<Order> ls = waiting.get(red);
				waiting.remove(red);
				
				for (Order o : ls)
					o.run();
			}
		}
	}
	
	public void addOrder(Order... orders) {
		if (!listening)
			return;
		
		for (Order o : orders)
			pending.add(o);
		waitNew.release();
	}
	
	public void addProduct(Product... products) {
		if (!listening)
			return;
		
		for (Product p : products)
			ready.add(p);
		waitNew.release();
	}
	
	public void forceShutDown() {
		softShutDown();
		running = false;
	}
	
	public void softShutDown() {
		listening = false;
	}
}
