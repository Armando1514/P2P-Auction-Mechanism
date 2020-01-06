# P2P AUCTION SYSTEM

<p align="center">
  <img src="http://ferrara.link/img/p2pAuctionMechanism2020/logo.jpg">
</p>

An auctions system based on P2P Network in which each peer can sell and buy goods using a Second-Price Auction mechanism (like E-Bay).

## FEATURES

### Functional Features

- Registration: to create a new user.
- Login in: possibility to log in from different peers and different sessions without losing persistence.
- User status: monitor your user status, including the auctions won, auctions joined or the own auctions.
- Create auction:  fill out a form to open a new auction in the network where all the peers can compete.
- Skip the line: for each auction is possible to add a fast price, where one peer can buy the goods without compete.
- Place a bid: the peers can bid to race in the auction.
- Check the auction:  view the auction status.
- Check All: list all the auction in the system.
- Notification: receive a notification message every time a user places a bid in the auction you are playing in or when it has ended.
- Unread notification: If you are offline, the notifications will be saved and you will be able to read them the next time you log in.

### Technical features

- Timezone support:  the moment when the auction takes place is common all over the world. For doing that there is a conversion from the '[UTC](https://en.wikipedia.org/wiki/Coordinated_Universal_Time)' timezone to the user time zone.
- Multi-address support:  when a user logs in, all the addresses in the auctions in which he participates are changed with his new address. Thus, the user can still receive notifications even if he logs in from a different peer.
- Resource contention support: a distributed algorithm has been implemented to allow peers accessing the same resource at the same time without overwriting the other's changes (explained below).

## DEMO VIDEO

<p align="center">
    <a href="http://ferrara.link/img/p2pAuctionMechanism2020/video-p2p-auction.mp4"><img src="http://ferrara.link/img/p2pAuctionMechanism2020/video.png"></a></p>



## DESIGN CHOICES

## Architecture

The project structure is relying on the architectural pattern [model-view-controller (MVC)](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller)

<p align="center">
  <img src="http://ferrara.link/img/p2pAuctionMechanism2020/mvc.png">
</p>

### Model

The 'model' directly manages the data, in our case communicate with the [overlay network](https://en.wikipedia.org/wiki/Overlay_network) through the [DHT](https://en.wikipedia.org/wiki/Distributed_hash_table). To design this component it has been adopted the structural pattern  "[**Data Access Object** (DAO)](https://www.oracle.com/technetwork/java/dataaccessobject-138824.html)" and the design patterns "[**factory**](https://www.tutorialspoint.com/design_pattern/factory_pattern.htm)" and "[**singleton**](https://www.tutorialspoint.com/design_pattern/singleton_pattern.htm)".    



<p align="center">
  <img src="http://ferrara.link/img/p2pAuctionMechanism2020/DAO.jpg">
</p>



​            

### Controller 

The controller responds to the user input and performs interactions on the data model objects.  Beyond that has been implemented the messaging pattern "[**publish-subscribe**](https://en.wikipedia.org/wiki/Publish%E2%80%93subscribe_pattern)", where the topic is the auction and each peer that competes is the subscriber. When a peer places a bid or discovers that the auction is ended, it becomes a publisher and sends to all a notification.

 ![controller](http://ferrara.link/img/p2pAuctionMechanism2020/control.jpg)                       

### View

The view is the GUI of our system and is composed of two classes: 'AuthenticationGUI' for the login/ registration system and 'AuctionGUI' once the user is logged for managing the auction.

## Distributed algorithm for the consistency

There is a clear problem when different peers try to update the same resource at the same time. One of them may overwrite the update of the other. So the system needs a '[consensus algorithm](https://whatis.techtarget.com/definition/consensus-algorithm)' to solve this problem. The solution that has been proposed for each update related to a critical resource: 

1. Get the latest version and check if all replica peers have the latest version, if not wait and try again.
2. Put prepared the critical data updated and set a short TTL (after this operation nobody will update that data), if the status is OK on all replica peers (this because during the propagation of the message someone else may have done the same thing.), go ahead, otherwise, remove the data and go to step 1.
3. Put confirmed, now is possible to remove the prepared flag.

The solution has been tested with 20 peers executing the same operation in parallel at the same time, and it still works (You can read about the test cases below).

## Test cases

The attention of the test cases has been placed on the folder on which the whole system is based, that is "DAO" since it communicates with the "DHT".
As shown in the following [report](http://ferrara.link/img/p2pAuctionMechanism2020/tests.jpg), is achieved an 80% line coverage for the "DAO" package. Currently, the test cases instantiate 5 peers, however, you can change the variable 'NUMBER_OF_PEERS' for more, paying attention to not instantiate too many connections triggering the DDOS protection. For operations on critical resources, several peers were started in parallel ( until a maximum of 20 peers) for testing the previous consensus algorithm. 

A little attention has also been given to the "control" package, carrying out tests on the basic operations.

## HOW TO RUN

1. Install '[Docker](https://docs.docker.com/install/)'.

2. Clone the project on your desktop 

   ```
   git clone https://github.com/Armando1514/P2P-Auction-Mechanism.git
   ```

3. Open the terminal and go to the folder where you have cloned the project with it 

   ```
   cd P2P-Auction-Mechanism
   ```

4. Build your Docker container typing: 

   ```
   docker build --no-cache -t p2p-pp-client .
   ```

    

**Start the master peer**

You can start the master peer:

```
docker run -i -t --name MASTER-PEER -e MASTERIP="127.0.0.1" -e ID=0 p2p-pp-client
```

The 'MASTERIP' environment variable is the master peer IP address and the ID environment variable is the unique id of your peer. The master peer needs to start with id=0. 

***Note***: The timezone is set by default on "Europe/Rome", you can change it or editing the 'DockerFile' or running:

```
docker run -i -t --name MASTER-PEER -e MASTERIP="127.0.0.1" -e ID=0 -e TIMEZONE="YOUR TIMEZONE" p2p-pp-client
```

For the timezone list, you can refer [here](https://garygregory.wordpress.com/2013/06/18/what-are-the-java-timezone-ids/)

**Start a generic peer**

When the master is started you have to check the IP address of your container:

1. Check the id of the docker container typing: 

   ```
   docker ps
   ```

2. Check the IP address of the master peer with: 

   ```
   docker inspect <Master container ID> 
   ```

Now you can start your peers changing the unique peer id and the peer name:

```
docker run -i -t --name PEERNAME -e MASTERIP="Master container ID" -e ID=1 p2p-pp-client
```

The same speech as before for the timezone.

# Credits

- [TomP2P](https://github.com/tomp2p/TomP2P)
- [Apache maven](https://github.com/apache/maven)
- [Text-io](https://github.com/beryx/text-io)
- [args4j](https://github.com/kohsuke/args4j)
- [Docker](https://www.docker.com/)