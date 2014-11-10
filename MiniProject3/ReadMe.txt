How to use the system in a cmd window

___________________________________________
Node:

Type 'java Node 1025' to create a Node at port 1025 that knows nobody.
Type 'java Node 1026 1025 to create a Node at port 1026 that knows the Node at port 1025.
When creating Node, or whenever a routing table is updated, an array is printed consisting
of the updated routing table. It has the format: [left, leftsLeft, right].


___________________________________________
GetClient:

Type 'java PutClient' to initiate the PutClient. 
Then type 'PUT 1025 1 A' to send key/value pair: (1, A) to the Node at port 1025.


___________________________________________
PutClient:

Type 'java GetClient' to initiate the GetClient.
Then type 'GET 1025 1' to request the value for key 1 from the Node at port 1025.


___________________________________________
At Node Crash:

When test-crashing a Node, please wait 1-2 seconds, before test-crashing another Node.
Within 2 seconds the system will reroute and restore its duplicated data.
when it it done re-routing, the new routing table will be printed.