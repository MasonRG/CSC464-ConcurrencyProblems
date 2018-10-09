
import threading
from threading import Barrier
from threading import Semaphore
import time
import random

NUM_O = 6
NUM_H = NUM_O * 2

oCount = 0
hCount = 0

#locks, barrier
countLock = Semaphore(1)
bondBarrier = Barrier(3)
oQueue = Semaphore(0)
hQueue = Semaphore(0)

#Function for getting random time to sleep
def getRandTimeMilli(r, b):
    return (random.randint(0, r) + b) / 1000


#Oxygen thread
def Oxygen (threadName):
    global oCount
    global hCount
    time.sleep(getRandTimeMilli(500, 100))

    print(threadName + "-> start")
    countLock.acquire()
    oCount += 1
    if hCount >= 2:
        hQueue.release()
        hQueue.release()
        hCount -= 2
        oQueue.release()
        oCount -= 1
    else:
        countLock.release()

    oQueue.acquire()
    print(threadName + "-> awaiting bond...")

    bondBarrier.wait()
    countLock.release()
    print(threadName + "-> bonded")
       


#Hydrogen thread
def Hydrogen (threadName):
    global oCount
    global hCount
    time.sleep(getRandTimeMilli(2000, 10))
    
    print(threadName + "-> start")
    countLock.acquire()
    hCount += 1
    if hCount >= 2 and oCount >= 1:
        hQueue.release()
        hQueue.release()
        hCount -= 2
        oQueue.release()
        oCount -= 1
    else:
        countLock.release()

    hQueue.acquire()
    print(threadName + "-> awaiting bond...")

    bondBarrier.wait()
    print(threadName + "-> bonded")



#Run simulation
startTime = time.perf_counter_ns() / 1000000

#make and start oxygen threads
oxygens = []
for i in range(0, NUM_O):
    oxygens.append(threading.Thread(target=Oxygen, args = ("O" + str((i+1)),)))
    oxygens[i].start()

#make and start hydrogen threads
hydrogens = []
for i in range(0, NUM_H):
    hydrogens.append(threading.Thread(target=Hydrogen, args = ("H" + str((i+1)),)))
    hydrogens[i].start()

#wait for oxygens to finish
for i in range(0, NUM_O):
    oxygens[i].join()

#wait for oxygens to finish
for i in range(0, NUM_H):
    hydrogens[i].join()

print("Simulation finished. Runtime: " + str(int((time.perf_counter_ns() / 1000000) - startTime)) + "ms")