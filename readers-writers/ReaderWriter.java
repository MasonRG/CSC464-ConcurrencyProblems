import java.util.concurrent.*;
import java.util.Random;

public class ReaderWriter
{
	//reader count and resource to be shared
	public static int numReaders;
	public static String resource;
	
	//locks
	public static Semaphore readLock = new Semaphore(1);
	public static Semaphore writeLock = new Semaphore(1);
	
	///Writer thread
	private static class Writer extends Thread
	{
		private Thread thread;
		private int id;
		
		Writer(int id)
		{
			this.id = id;
			
			thread = new Thread(this, "WriterThread[" + id + "]");
		}
		
		public void run()
		{
			try {
				writeLock.acquire();
				System.out.println("w" + id + "-> start");
				resource = resource + id;
				System.out.println("w" + id + "-> done");
				writeLock.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	///Reader thread
	private static class Reader extends Thread
	{
		private Thread thread;
		private int id;
		
		Reader(int id)
		{
			this.id = id;
			
			thread = new Thread(this, "ReaderThread[" + id + "]");
		}
		
		public void run()
		{
			try {
				//grab the reader count lock and increment the count
				readLock.acquire();
				numReaders++;
				System.out.println("r" + id + "-> start | r_count: " + numReaders);
				if (numReaders == 1)
					writeLock.acquire();
				readLock.release();
				
				//perform the read
				System.out.println("r" + id + "-> read: " + resource);

				//grab the reader count lock and decrement the count
				readLock.acquire();
				numReaders--;
				System.out.println("r" + id + "-> done");
				if (numReaders == 0)
					writeLock.release();
				readLock.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main(String[] args) 
	{
		final int NUM_WRITERS = 4;
		final int NUM_READERS = 9;
		
		//start the timer (msecs)
		long startTime = System.nanoTime() / 1000;
		
		//initializations
		numReaders = 0;
		resource = "";
		Writer[] writers = new Writer[NUM_WRITERS];
		Reader[] readers = new Reader[NUM_READERS];
		
		//start the writers
		for(int i = 0; i < NUM_WRITERS; i++)
		{
			writers[i] = new Writer(i+1); 
			writers[i].start();
		}
		
		//start the readers
		for(int i = 0; i < NUM_READERS; i++)
		{
			readers[i] = new Reader(i+1);
			readers[i].start();
		}

		
		//wait for writers to finish
		for(int i = 0; i < NUM_WRITERS; i++)
		{
			try { writers[i].join(); }
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		//wait for readers to finish
		for(int i = 0; i < NUM_READERS; i++)
		{
			try { readers[i].join(); }
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		long elapsedTime = (System.nanoTime() / 1000) - startTime;
		System.out.println("Simulation finished. Runtime: " + elapsedTime + "usec");
	}
}
