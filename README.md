# observepoint

##How does your code work?

requestHandled receives a new IP and will check if its in a correct format. If not, just wont do anything. If that validation its correct, the method will check if the map contains that IP, if yes, will increase the amount of IP register, and if the IP doesnt exists, will just add to the map.

After adding/merging a new IP on the map, also this IP will be added in a new map that is going to be used in order to keep 100 top IP registered. When happend an insert, if the top100 map isnt full, we just add the new element, if the top100 map is full, will update the map in order to add the element in the correct place.


##What is the runtime complexity of each function?

requestHandled(...)  -> O(1) 
top100() -> O(n) -> n = 100

##What other approaches did you decide not to pursue?
Need to use concurrentHashMap in order to avoid concurrency problems, so i decided not to use a Map that wont have lock control.
Sort all ip map on demand. Sounds better just to keep a separate array for just 100 positions and sort asynchronous in order not to block insertion.

##How would you test this?
Unit test, concurrency/volume test and stress test  to validate if data would get lost, and also with a timer to check response time.

