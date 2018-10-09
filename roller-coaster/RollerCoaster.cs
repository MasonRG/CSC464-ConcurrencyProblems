using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading;

class RollerCoaster
{
	static readonly int MAX_PASSENGERS = 4;
	static readonly int NUM_PASSENGERS = 24;

	//counters for boarding/unboarding
	static int boarders;
	static int unboarders;

	static readonly object boardLock = new object();
	static readonly object unboardLock = new object();

	static Semaphore boardQueue = new Semaphore(0, MAX_PASSENGERS);
	static Semaphore unboardQueue = new Semaphore(0, MAX_PASSENGERS);
	static Semaphore load = new Semaphore(0, 1);
	static Semaphore unload = new Semaphore(0, 1);
	
	//Car thread
	static void Car()
	{
		Random rand = new Random();
		int served = 0;
		Console.WriteLine(Thread.CurrentThread.Name + "-> start");

		while (served < NUM_PASSENGERS)
		{
			//Load up
			Console.WriteLine(Thread.CurrentThread.Name + "-> load");
			boardQueue.Release(MAX_PASSENGERS);
			load.WaitOne();

			//Run around the track
			Console.WriteLine(Thread.CurrentThread.Name + "-> departing");
			Thread.Sleep(50 + rand.Next(500));
			Console.WriteLine(Thread.CurrentThread.Name + "-> arriving");

			//Unload
			unboardQueue.Release(MAX_PASSENGERS);
			unload.WaitOne();
			Console.WriteLine(Thread.CurrentThread.Name + "-> unloaded");

			served += MAX_PASSENGERS;
		}

		Console.WriteLine(Thread.CurrentThread.Name + "-> exit");
	}

	//Passenger thread
	static void Passenger()
	{
		//wait for boarding
		boardQueue.WaitOne();

		//board
		lock(boardLock)
		{
			boarders++;
			if (boarders == MAX_PASSENGERS)
			{
				Console.WriteLine(Thread.CurrentThread.Name + "-> last one on");
				load.Release();
				boarders = 0;
			}
			else
			{
				Console.WriteLine(Thread.CurrentThread.Name + "-> getting on...");
			}
		}

		//ride running
		unboardQueue.WaitOne();

		//unboard
		lock(unboardLock)
		{
			unboarders++;
			if (unboarders == MAX_PASSENGERS)
			{
				Console.WriteLine(Thread.CurrentThread.Name + "-> last one off");
				unload.Release();
				unboarders = 0;
			}
			else
			{
				Console.WriteLine(Thread.CurrentThread.Name + "-> getting off...");
			}
		}
	}

	static void Main(string[] args)
	{
		Stopwatch stopwatch = new Stopwatch();
		stopwatch.Start();

		//start car thread
		Thread car = new Thread(Car) { Name = "Car1" };
		car.Start();

		//start passenger threads
		Thread[] passengers = new Thread[NUM_PASSENGERS];
		for(int i = 0; i < NUM_PASSENGERS; i++)
		{
			passengers[i] = new Thread(Passenger) { Name = "Pas" + (i + 1) };
			passengers[i].Start();
		}

		//wait for car to finish
		car.Join();

		//wait for passengers to finish
		for (int i = 0; i < NUM_PASSENGERS; i++)
		{
			passengers[i].Join();
		}

		Console.WriteLine("Simulation finished. Runtime: " + stopwatch.ElapsedMilliseconds + "ms");
		Console.ReadLine();
	}
}