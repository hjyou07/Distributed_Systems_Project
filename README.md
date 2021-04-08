# Distributed_Systems_Project
This repository includes a system design project done in CS6650, Building Scalable Distributed Systems.  
We created a mock system for an "old decrepit supermarket chain Tigle" that is embarking on a rapid expansion strategy with a huge investment. It explores the fundamental elements of concurrent, distributed systems, and uses many widely used techonologies such as but not limited to:  
+ Apache Tomcat
+ RabbitMQ
+ HikariCP
+ Apache Kafka
+ MySQL
+ Amazon RDS
+ Amazon EC2
+ Amazon ELB
## Introduction
An old decrepit supermarket chain, Tigle, is embarking on a rapid acquisition strategy to buy up new stores nationwide. exercise. They’re in need of a solution to integrate these new stores into their existing business system and hired us to help them build the new system. 
The system was developed in separate milestones. The contract builds upon the previous milestones and completes the design of the system.  
  
For the first part of the contract, we built a client that generates and sends synthetic item purchases to a server in the cloud. A multithreaded Java client that we can configure uploads a day of item purchases from multiple stores and exert various loads on the server.  
  
For the second part of the contract, we had the mock client exert a heavy POST requests load to the server and the server concurrently writing the record to the relational database. 
  
For the third part of the contract, we made modifications to the server and database design in the previous contract to make the application more responsive to client requests and introduced new microservices to delegate tasks. The server writes all the new POST request payloads to a queue - we chose RabbitMQ - to deliver the request to the microservice. The microservice "eventually" persists the record to the database. An additional microservice is introduced that also receives every purchase record and create an in-memory data structure that records the quantity of each item purchased at each store.
This microservice is designed to answer two queries:  
1. What are the top 10 most purchased items at Store N
1. Which are the top 5 stores for sales for item N 
  This data structure is implemented as a 2d array synchronized for each row. When the client requests the above information, it runs an algorithm to return the result to the client via remote procedure call.
