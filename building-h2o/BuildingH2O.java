import java.util.concurrent.*;
import java.util.Random;

public class BuildingH2O
{
	public static int oCount = 0;
	public static int hCount = 0;
	
	public static Semaphore countLock = new Semaphore(1);
	public static CyclicBarrier bondBarrier = new CyclicBarrier(3);
	public static Semaphore oQueue = new Semaphore(0);
	public static Semaphore hQueue = new Semaphore(0);
	
	///Oxygen thread
	private static class Oxygen extends Thread
	{
		private Thread thread;
		private int id;
		
		Oxygen(int id)
		{
			this.id = id;
			thread = new Thread(this, "OxygenThread[" + id + "]");
		}
		
		public void run()
		{
			try {
				Random rand = new Random();
				Thread.sleep(rand.nextInt(500) + 100);
				
				System.out.println("O" + id + "-> start");
				countLock.acquire();
				oCount++;
				if (hCount >= 2)
				{
					hQueue.release();
					hQueue.release();
					hCount -= 2;
					oQueue.release();
					oCount--;
				}
				else
				{
					countLock.release();
				}
				
				oQueue.acquire();
				System.out.println("O" + id + "-> awaiting bond...");
				
				bondBarrier.await();
				countLock.release();
				
				System.out.println("O" + id + "-> bonded");
			} catch (Exception e) {
			}
		}
	}
	
	///Hydrogen thread
	private static class Hydrogen extends Thread 
	{
		private Thread thread;
		private int id;
		
		Hydrogen(int id)
		{
			this.id = id;
			
			thread = new Thread(this, "HydrogenThread[" + id + "]");
		}
		
		public void run() 
		{
			try {
				Random rand = new Random();
				Thread.sleep(rand.nextInt(2000) + 10);
				
				System.out.println("H" + id + "-> start");
				countLock.acquire();
				hCount++;
				if (hCount >= 2 && oCount >= 1)
				{
					hQueue.release();
					hQueue.release();
					hCount -= 2;
					oQueue.release();
					oCount--;
				}
				else
				{
					countLock.release();
				}
				
				hQueue.acquire();
				System.out.println("H" + id + "-> awaiting bond...");
				
				bondBarrier.await();
							
				System.out.println("H" + id + "-> bonded");
			} catch (Exception e) {
			}
		}
	}
	
	
	public static void main(String[] args) 
	{
		final int NUM_O = 6;
		final int NUM_H = NUM_O * 2;
		
		//start the timer (usecs)
		long startTime = System.nanoTime() / 1000000;
		
		//initializations
		Oxygen[] oxygens = new Oxygen[NUM_O];
		Hydrogen[] hydrogens = new Hydrogen[NUM_H];
		
		//start the oxygens
		for(int i = 0; i < NUM_O; i++)
		{
			oxygens[i] = new Oxygen(i+1); 
			oxygens[i].start();
		}
		
		//start the readers
		for(int i = 0; i < NUM_H; i++)
		{
			hydrogens[i] = new Hydrogen(i+1);
			hydrogens[i].start();
		}

		
		//wait for oxygens to finish
		for(int i = 0; i < NUM_O; i++)
		{
			try { oxygens[i].join(); }
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		//wait for hydrogens to finish
		for(int i = 0; i < NUM_H; i++)
		{
			try { hydrogens[i].join(); }
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		long elapsedTime = (System.nanoTime() / 1000000) - startTime;
		System.out.println("Simulation finished. Runtime: " + elapsedTime + "ms");
	}
}
