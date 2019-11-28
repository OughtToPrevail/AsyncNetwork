# AsyncNetwork
*AsyncNetwork* short for AsynchronousNetwork is a library which implements an easy-to-use, fast, TCP network

## Features

* Cross platform (Windows, Linux, Mac, Android etc...)
* Asynchronous
* Can handle lots of clients at once
* Easy-to-use
* Fast and efficient

## Maven
```xml
<dependency>
    <groupId>com.github.oughttoprevail</groupId>
    <artifactId>AsyncNetwork</artifactId>
    <version>1.3.0</version>
</dependency>
```
Here is <a href="https://search.maven.org/classic/#artifactdetails%7Ccom.github.oughttoprevail%7CAsyncNetwork%7C1.2.0%7Cjar">The Central Repository</a>.

## How to use
To create a client you would do:
```java
ClientSocket client = new ClientSocket();
client.onConnect(() -> WritablePacketBuilder.create().putByte(Byte.MAX_VALUE).build().writeAndClose(client));
client.connectLocalHost(/*Specify your port here (0-65535) example: 6000*/6000);
```
To create a server you would do:
```java
ReadablePacket packet = ReadablePacketBuilder.create().aByte().build();
ServerSocket server = new ServerSocket();
server.onConnection(client ->
{
	client.always(true);
	packet.read(client, readResult ->
	{
		byte value = readResult.poll();
		System.out.println("value=" + value);
	});
	client.onDisconnect(disconnectionType -> System.out.println("Disconnected" + disconnectionType));
});
server.bindLocalHost(6000);
while(true);
```

And you're finished! now you can use AsyncNetwork for your networking projects. Good luck!

## Server selector
### Windows
Windows uses <a href="https://docs.microsoft.com/en-us/windows/desktop/fileio/i-o-completion-ports">IO completion ports</a> for best asynchronous performance.
### Linux (Android is based on Linux)
Linux has <a href="http://man7.org/linux/man-pages/man7/epoll.7.html">sys/epoll</a> which is famous for it's O(1) `epoll_wait` performance.
### Mac
Mac has FreeBSD features including <a href="https://www.freebsd.org/cgi/man.cgi?query=kqueue&sektion=2">kqueue</a> which is also O(1).
### Other
Other operating systems are not supported by different native implementations,
so AsyncNetwork uses the already implemented <a href="https://docs.oracle.com/javase/7/docs/api/java/nio/channels/Selector.html">java.nio.channels.Selector</a>.

## Thanks
Special thanks to <a href="https://github.com/jhg023">Jacob</a> and <a href="https://github.com/despair86">despair</a> who helped me make this!