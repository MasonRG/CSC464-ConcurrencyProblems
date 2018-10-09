import java.util.concurrent.*;
import java.util.Random;
import java.util.LinkedList;
import java.util.Queue;

public class ProducerConsumer
{
	final static int time_divisor = 1000000; //nano to milli
	final static int iter_count = 5;
	final static int buffer_size = 3;
	static long start_time; //read-only, main thread sets
	
	//shared buffer and locks
	public static Queue<String> buffer;
	public static Object bufferLock = new Object();
	
	///Producer thread
	private static class Producer extends Thread
	{
		private Thread thread;
		private int id;
		
		Producer(int id)
		{
			this.id = id;
			
			thread = new Thread(this, "ProducerThread[" + id + "]");
		}
		
		public void run()
		{
			try {
				Random rand = new Random();
				Thread.sleep(rand.nextInt(100) + 10);
			
				for(int i = 0; i < iter_count; i++)
				{
					String name = "P[" + id + "." + i + "]";
					System.out.println(name + "-> start");
					
					//acquire the buffer lock
					synchronized(bufferLock)
					{
						//if the buffer is full, wait.
						while(buffer.size() >= buffer_size)
							bufferLock.wait();
						
						long t = (System.nanoTime() / time_divisor) - start_time;
						System.out.println(name + "-> producing @ t=" + t);
						
						//produce - a string that is the producer id and the time
						String newString = name + " : " + t;
						buffer.add(newString);
						
						//notify the consumer that there is an element in the buffer
						bufferLock.notify();
						
						Thread.sleep(rand.nextInt(100) + 10);
					}
					
					Thread.sleep(rand.nextInt(100) + 10);
				}
				
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	///Consumer thread
	private static class Consumer extends Thread
	{
		private Thread thread;
		private int id;
		
		Consumer(int id)
		{
			this.id = id;
			
			thread = new Thread(this, "ConsumerThread[" + id + "]");
		}
		
		public void run()
		{
			try {
				Random rand = new Random();
				Thread.sleep(rand.nextInt(100) + 10);
				
				for(int i = 0; i < iter_count; i++)
				{
					String name = "C[" + id + "." + i + "]";
					
					System.out.println(name + "-> start");
					
					//acquire the buffer lock
					synchronized(bufferLock)
					{
						//if the buffer is empty, wait
						while(buffer.size() <= 0)
							bufferLock.wait();
									
						long t = (System.nanoTime() / time_divisor) - start_time;
						
						//consume - print out the consumed string
						System.out.println(name + "-> consuming: " + buffer.remove());
						
						//notify producer that there is room in the buffer
						bufferLock.notify();
						
						Thread.sleep(rand.nextInt(100) + 10);
					}
					
					Thread.sleep(rand.nextInt(100) + 10);
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main(String[] args) 
	{
		final int NUM_PRODUCERS = 3;
		final int NUM_CONSUMERS = 3;
		
		//start the timer (usecs)
		start_time = System.nanoTime() / time_divisor;
		
		//initializations
		buffer = new LinkedList<>();
		Producer[] producers = new Producer[NUM_PRODUCERS];
		Consumer[] consumers = new Consumer[NUM_CONSUMERS];
		
		//start the producers
		for(int i = 0; i < NUM_PRODUCERS; i++)
		{
			producers[i] = new Producer(i+1); 
			producers[i].start();
		}
		
		//start the consumers
		for(int i = 0; i < NUM_CONSUMERS; i++)
		{
			consumers[i] = new Consumer(i+1);
			consumers[i].start();
		}

		
		//wait for producers to finish
		for(int i = 0; i < NUM_PRODUCERS; i++)
		{
			try { producers[i].join(); }
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		//wait for consumers to finish
		for(int i = 0; i < NUM_CONSUMERS; i++)
		{
			try { consumers[i].join(); }
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		long elapsedTime = (System.nanoTime() / time_divisor) - start_time;
		System.out.println("Simulation finished. Runtime: " + elapsedTime + "ms");
	}
}
