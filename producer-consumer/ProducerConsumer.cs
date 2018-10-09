using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading;

class ProducerConsumer
{
	static Stopwatch stopwatch;
	const int iter_count = 5;
	const int buffer_size = 3;

	static Queue<string> buffer;
	static object bufferLock = new object();

	static void Producer(object obj)
	{
		Random rand = new Random();
		Thread.Sleep(rand.Next(100) + 10);

		var id = (int)obj;

		for (int i = 0; i < iter_count; i++)
		{
			string tName = string.Format("P[{0}.{1}]", id, i);
			Console.WriteLine(tName + "-> start");

			lock(bufferLock)
			{
				//if the buffer is full, wait
				while (buffer.Count >= buffer_size)
					Monitor.Wait(bufferLock);

				var time = stopwatch.ElapsedMilliseconds;
				Console.WriteLine(tName + "-> producing @ t=" + time);

				//produce - a string that is the producer id and the time
				string newString = tName + " : " + time;
				buffer.Enqueue(newString);

				//notify the consumer that there is an element in the buffer
				Monitor.Pulse(bufferLock);

				Thread.Sleep(rand.Next(100) + 10);
			}

			Thread.Sleep(rand.Next(100) + 10);
		}
		
	}

	static void Consumer(object obj)
	{
		Random rand = new Random();
		Thread.Sleep(rand.Next(100) + 10);

		var id = (int)obj;

		for (int i = 0; i < iter_count; i++)
		{
			string tName = string.Format("C[{0}.{1}]", id, i);
			Console.WriteLine(tName + "-> start");

			lock (bufferLock)
			{
				//if the buffer is full, wait
				while (buffer.Count <= 0)
					Monitor.Wait(bufferLock);

				var time = stopwatch.ElapsedMilliseconds;

				//consume - print out the consumed string
				Console.WriteLine(tName + "-> consuming: " + buffer.Dequeue());

				//notify the producer that there is room in the buffer
				Monitor.Pulse(bufferLock);

				Thread.Sleep(rand.Next(100) + 10);
			}

			Thread.Sleep(rand.Next(100) + 10);
		}
		
	}

	static void Main(string[] args)
	{
		const int NUM_PRODUCERS = 3;
		const int NUM_CONSUMERS = 3;

		//start the timer (usecs)
		stopwatch = new Stopwatch();
		stopwatch.Start();

		buffer = new Queue<string>();
		Thread[] producers = new Thread[NUM_PRODUCERS];
		Thread[] consumers = new Thread[NUM_CONSUMERS];

		//start the producers
		for (int i = 0; i < NUM_PRODUCERS; i++)
		{
			producers[i] = new Thread(new ParameterizedThreadStart(Producer));
			producers[i].Start(i+1);
		}

		//start the consumers
		for (int i = 0; i < NUM_CONSUMERS; i++)
		{
			consumers[i] = new Thread(new ParameterizedThreadStart(Consumer));
			consumers[i].Start(i+1);
		}


		//wait for producers to finish
		for (int i = 0; i < NUM_PRODUCERS; i++)
		{
			producers[i].Join();
		}

		//wait for consumers to finish
		for (int i = 0; i < NUM_CONSUMERS; i++)
		{
			consumers[i].Join();
		}

		Console.WriteLine("Simulation finished. Runtime: " + stopwatch.ElapsedMilliseconds + "ms");
		Console.ReadLine();
	}
}
