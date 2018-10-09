using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading;

class BarberShop
{
	static readonly int MAX_SERVED = 3;
	static readonly int TOTAL_CUSTOMERS = 15;

	static Stopwatch stopwatch;
	static int currentCustomers = 0;

	//locks and signals
	static Semaphore chairLock = new Semaphore(1,1);
	static Semaphore customerReady = new Semaphore(0, MAX_SERVED);
	static Semaphore barberReady = new Semaphore(0, 1);
	static Semaphore customerDone = new Semaphore(0, 1);
	static Semaphore barberDone = new Semaphore(0, 1);

	static void Barber()
	{
		Random rand = new Random();
		Thread.Sleep(rand.Next(100) + 10);

		Console.WriteLine(Thread.CurrentThread.Name + "-> open shop");

		while(true)
		{
			//wait for a customer and tell them to sit when they've arrived
			customerReady.WaitOne();
			Console.WriteLine(Thread.CurrentThread.Name + "-> seat customer");
			barberReady.Release();

			//cut hair
			Thread.Sleep(rand.Next(200) + 10);
			Console.WriteLine(Thread.CurrentThread.Name + "-> cut done");

			//wait for the customer to be satisfied, and then tell them we are done
			customerDone.WaitOne();
			barberDone.Release();

			Console.WriteLine(Thread.CurrentThread.Name + "-> customer served");
		}
	}

	static void Customer()
	{
		Random rand = new Random();
		Thread.Sleep(rand.Next(2000) + 10);

		while (true) //loop for convenient exit if the shop is full
		{
			//Try and find a seat
			chairLock.WaitOne();
			
			//no chairs available -> leave
			if (currentCustomers == MAX_SERVED)
			{
				Console.WriteLine(Thread.CurrentThread.Name + "-> enter [WAIT: " + currentCustomers + "] [FULL; LEAVE]");
				chairLock.Release();
				break; 
			}

			Console.WriteLine(Thread.CurrentThread.Name + "-> enter [WAIT: " + currentCustomers + "] [JOIN QUEUE]");
			currentCustomers++;
			chairLock.Release();

			//tell barber we are here and wait
			customerReady.Release();
			barberReady.WaitOne();

			//get haircut
			Console.WriteLine(Thread.CurrentThread.Name + "-> seated");
			Thread.Sleep(rand.Next(200) + 10);

			//tell barber we are done and wait for the barber to confirm
			customerDone.Release();
			barberDone.WaitOne();

			//leave the shop
			chairLock.WaitOne();
			currentCustomers--;
			chairLock.Release();

			Console.WriteLine(Thread.CurrentThread.Name + "-> exit");

			break;
		}
	}


	static void Main(string[] args)
	{
		//start the timer (usecs)
		stopwatch = new Stopwatch();
		stopwatch.Start();

		//make and start barber and customer threads
		Thread barber = new Thread(Barber) { Name = "Barber" };
		barber.Start();

		Thread[] customers = new Thread[TOTAL_CUSTOMERS];
		for (int i = 0; i < TOTAL_CUSTOMERS; i++)
		{
			customers[i] = new Thread(Customer) { Name = "Customer" + (i + 1) };
			customers[i].Start();
		}


		//wait for customers to finish
		for (int i = 0; i < TOTAL_CUSTOMERS; i++)
		{
			customers[i].Join();
		}

		//we don't need to wait for the barber thread to finish, we are done whenever all the customers have come and gone
		///barber.Join();

		Console.WriteLine("Simulation finished. Runtime: " + stopwatch.ElapsedMilliseconds + "ms");
		Console.ReadLine();
	}
}