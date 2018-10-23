## Analysis

### C semaphores

#### Correctness
Multiple executions produced expected results, for what it is worth.\
Using mutex for guarding the board and unboard counters provides simple locking, and ensures that we will always have
the correct number of passengers in the car at all times.\
Using the semaphore.h library allows us to rely on a reliable semaphore implementation, and its ease of use reduces the
chance we will make a mistake.\
Semaphores for signalling load and unload ensure that we do not load or unload the car until all passengers are ready.

#### Comprehensibility
C thread creation is cryptic and can be hard to parse. The calls to manipulate mutexes and semaphores can be unusual
compared to an object oriented design.\
Passing of pointers and dereferencing makes it hard to follow what object is doing what, and requires experience with
C (and pointers in general) to really make much sense.\
Loops are verbose and clunky, but not necessarily hard to understand.\
Solution is ~150 lines of code. More verbose, due to the need for more memory management and clunkier loops.

#### Performance
Solution performance is hard to assess, given the calls to sleep threads.\
More performant as expected, but not by much. However, considering that the C implementation was run on a significantly
weaker machine, it is likely that the C solution would be much faster than the C# solution if it were run on equivalent hardware.\
Runtime of ~1800ms.


### C# lock with semaphores

#### Correctness:
Multiple executions produced expected results, for what it is worth.\
Using the lock keyword to mark the critical sections of the code involving the board and unboard counters ensures we 
always have the correct amount of passengers boarding or unboarding the car.\
C# semaphore library is straightforward to use and this reduces likelihood for error. Easily enables signalling for 
waiting to coordinate the passenger load and unload phases and ensure that the correct number of passengers are on the
car before it leaves, and that the car doesn't leave or begin boarding again until the car is fully emptied.

#### Comprehensibility:
Thread creation is a similar process to the C implementation, however the omission of pointers and cryptic library calls
makes the C# solution much more understandable, especially to one unfamiliar with C programming but with object oriented
programming experience.\
The lock code block very explicitly shows the critical sections, and this makes it very obvious where critical code is running.
Visually, this makes the code much more comprehensible compared to the C solution.\
Solution is ~125 lines of code. Much of it is whitespace though and could be substantially condensed.

#### Performance:
Solution performance is hard to assess, given the calls to sleep threads.\
Presumably similar to other garbage collected, oop focused languages.\
Runtime of ~2300ms. Slower than C, especially considering hardware differences. On equivalent hardware, this difference might be
quite substantial. Unfortunately, testing on the same machine was not possible for this experiment.
