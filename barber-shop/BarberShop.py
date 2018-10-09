
import threading
from threading import Semaphore
import time
import random

MAX_SERVED = 3
TOTAL_CUSTOMERS = 15;

currentCustomers = 0;

#locks and signals
chairLock = Semaphore(1)
customerReady = Semaphore(0)
barberReady = Semaphore(0)
customerDone = Semaphore(0)
barberDone = Semaphore(0)


#Function to clean up getting random time to sleep
def getRandTimeMilli(r, b):
    return (random.randint(0, r) + b) / 1000


#Barber thread
def Barber (threadName):
    global currentCustomers

    time.sleep(getRandTimeMilli(100, 10))
    print(threadName + "-> open shop")
    while(True):
        #wait for a customer and tell them to sit when they've arrived
        customerReady.acquire()
        print(threadName + "-> seat customer")
        barberReady.release()
        
        #cut hair
        time.sleep(getRandTimeMilli(200, 10))
        print(threadName + "-> cut done")
        
        #wait for customer to be satisfied, and then tell them we are done
        customerDone.acquire()
        barberDone.release()
        print(threadName + "-> customer served")



#Customer thread
def Customer (threadName):
    global currentCustomers

    time.sleep(getRandTimeMilli(2000, 10))
    while(True):
        #try and find a spot in the queue
        chairLock.acquire()
        #if no chairs available; leave
        if currentCustomers == MAX_SERVED:
            print(threadName + "-> enter [WAIT: " + str(currentCustomers) + "] [FULL; LEAVE]")
            chairLock.release()
            break
        print(threadName + "-> enter [WAIT: " + str(currentCustomers) + "] [JOIN QUEUE]")
        currentCustomers += 1
        chairLock.release()

        #tell barber we are here and wait
        customerReady.release()
        barberReady.acquire()

        #get haircut
        print(threadName + "-> seated")
        time.sleep(getRandTimeMilli(200, 10))

        #tell barber we are done and wait for the barber to confirm
        customerDone.release()
        barberDone.acquire()

        #leave the shop
        chairLock.acquire()
        currentCustomers -= 1
        chairLock.release()

        print(threadName + "-> exit")
        break



#Run simulation
startTime = time.perf_counter_ns() / 1000000

#make and start barber and customer threads
barber = threading.Thread(target=Barber, args=("Barber",))
barber.start()

customers = []
for i in range(0, TOTAL_CUSTOMERS):
    customers.append(threading.Thread(target=Customer, args = ("Customer" + str((i+1)),)))
    customers[i].start()

#wait for customers to finish
for i in range(0, TOTAL_CUSTOMERS):
    customers[i].join()

#we don't need to wait for the barber to finish - just until all customers have come and gone
#barber.join()

print("Simulation finished. Runtime: " + str(int((time.perf_counter_ns() / 1000000) - startTime)) + "ms")