# AsyncNetwork
*AsyncNetwork* short for AsynchronousNetwork is a library which implements an easy-to-use, fast, TCP network

## Features

* Cross platform
* Asynchronous
* Can handle lots of clients at once
* Easy-to-use
* Fast and efficient

## Maven
```xml
<dependency>
    <groupId>com.github.oughttoprevail</groupId>
    <artifactId>AsyncNetwork</artifactId>
    <version>1.0.0</version>
</dependency>
```
Here is <a href="https://search.maven.org/classic/#artifactdetails%7Ccom.github.oughttoprevail%7CAsyncNetwork%7C1.0.0%7Cjar">The Central Repository</a>.

## How to use
To create a client you would do:
```java
		Client.newClient()
				.onConnect(client -> client.readByte(aByte -> System.out.println("BYTE " + aByte), true))
				.connectLocalHost(/*Specify your port here (0-65535) example: 6000*/6000);
```
To create a server you would do:
```java
		Server.newServer()
				.onBind(server -> System.out.println("Bind successful"))
				.onClose(disconnectionType -> System.out.println("Closed " + disconnectionType))
				.onConnection(serverClient -> serverClient.onDisconnect(disconnectionType -> System.out.println(
						"Client disconnected " + disconnectionType))
						.putByte(Byte.MAX_VALUE)
						.write())
				.bindLocalHost(/*Specify your port here (0-65535) example: 6000*/6000);
```

And you're finished! now you can use AsyncNetwork for your networking projects. Good luck!

## Performance
These tests will be done using iperf and will test AsyncNetwork against a popular
network library named "Netty". (Tests were done on Windows)
### Code
AsyncNetwork: https://sourceb.in/d948ac3d2a.java
<br/>
Netty: https://sourceb.in/e5f5a3442b.java
<br/>
Test commands:
<br/>
Test 1: `iperf -c ip -p port -f M` - Will run a single client and 
return the bandwidth in MB/s.
<br/>
Test 2: `iperf -c ip -p port -f M -P 100` - Will run 100 parallel clients 
and return the SUM (all bandwidths from all clients together) in MB/s.
### Results
#### AsyncNetwork
Test 1: 184 MB/s
<br/>
Test 2: 162 MB/s

#### Netty
Test 1: 187 MB/s
<br/>
Test 2: 117 MB/s


## Server selector
### Windows
Windows uses <a href="https://docs.microsoft.com/en-us/windows/desktop/fileio/i-o-completion-ports">IO completion ports</a> for best asynchronous performance.
### Linux
Linux has <a href="http://man7.org/linux/man-pages/man7/epoll.7.html">sys/epoll</a> which is famous for it's O(1) `epoll_wait` performance.
### Mac
Mac has FreeBSD features including <a href="https://www.freebsd.org/cgi/man.cgi?query=kqueue&sektion=2">kqueue</a> which is also O(1).
### Other
Other operating systems are not supported by different native implementations,
so AsyncNetwork uses the already implemented <a href="https://docs.oracle.com/javase/7/docs/api/java/nio/channels/Selector.html">java.nio.channels.Selector</a>.

## Thanks
Special thanks to <a href="https://github.com/jhg023">Jacob</a> and <a href="https://github.com/despair86">despair</a> who helped me make this!