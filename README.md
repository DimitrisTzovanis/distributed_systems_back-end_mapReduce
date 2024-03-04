# Distributed Systems Back-End

## Members: Dimitris Tzovanis, Elpida Stasinou

Users via the mobile app upload this GPX file to the system and the system performs some processing on this file. A GPX file contains only one activity/route.
Usually waypoints are created every x meters and this depends on the GPS accuracy of the device. Therefore the size of the file grows according to the total distance covered during the exercise. The file can be processed in parallel by multiple machines in MapReduce format to speed up the processing of large files.
The MapReduce framework is a programming model that allows parallel processing of large volumes of data.
MapReduce relies on the use of two functions: 
- map(key,value) -> [(key2, value2)]
- reduce(key2,[value2]) -> [final_value]

  
- "Map" function: processes a key/value pair and produces an intermediate key/value pair. The input to the map function can be lines of a file, etc., and are in the form (key, value). The map function converts each such pair into another pair (key2, value2). The map function can be executed in parallel, on different data input and on different nodes. The degree of parallelism depends on the application and can be defined by the user.
- "Reduce" function: merges all intermediate values associated with the same key and produces the final results. For each individual key, a list of values corresponding to that key is generated. This function calculates a final value for the key by processing the list of values corresponding to that key. The reduce function is processed after all map functions have finished processing.
The mobile application first sends the GPX to a Master Node. Then the Master Node creates chunks of n waypoints and sends them to the Worker Nodes in Round Robin order. Each Worker calculates for the received chunk the total distance, average speed, total climb and total time. It then turns these intermediate results back to the Master to be reduced to the final result. Therefore in this version of the system the Master is also the Reducer.
When the Master receives all the intermediate results and completes the reduce, it outputs the final results for the activity. Finally, it asynchronously forwards the results back to the mobile application for the user to view.
At the same time the Master must keep statistics from all users. In particular, he keeps the Average Exercise Time, Average Distance and Average Climb for each user individually as well as for all users.

### Bonus

In many applications, such as Strava, users can define a sequence of Waypoints as a Segment, e.g. a small part of the route of the Athens Classic Marathon. Then each time the system detects a sub-sequence
 
of Waypoints that is identified with a Segment, it can keep the previous user statistics for that segment. At the same time it maintains a leaderboard with the performance of all users in descending order. You are encouraged to integrate this additional functionality into the system by modifying MapReduce appropriately.
Finally the android application should also output in a graphical way, e.g. table, the leaderboard for the selected segment.
