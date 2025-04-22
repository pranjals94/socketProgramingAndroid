after connecting to adafruit.io server it breaks the connection in 1.5 minutes(1min30/35sec) approx. 
it does not care even if you are subscribed to a feed(s) and the server is publishing the changing data. 
also it does not refresh the keep alive even if u publish some data to the feed.
this is because both publish and subscribed connections have different client ids to determine the connection.

network connection routins should be ran on seperare thread rather than in the main thread to avoid irresponsiveness of the UI

it will return -1 in socket.read(buffer).

Using jetpack compose for UI
Jetpack Compose is Android's modern toolkit for building native UI. It simplifies and accelerates UI development on Android by using Kotlin and a declarative programming model.

🧱 Traditional Android UI vs. Jetpack Compose

Traditional (XML + Java/Kotlin)	                      Jetpack Compose

Uses XML layout files	                              Uses Kotlin code for UI
UI and logic are separate	                      UI and logic are together
More boilerplate code	                              Less code, more concise
Harder to manage state	                              Built-in state handling

💡 Key Concepts in Jetpack Compose
@Composable functions: Functions that define UI.

State Management: Automatically updates the UI when state changes.

Modifiers: Used to style and position components.

Recomposition: When state changes, only the affected UI parts recompose (re-render).

Layouts: Like Column, Row, Box replace XML layouts like LinearLayout, ConstraintLayout.


🧠 How Multiple Connections Work with One Ethernet Port
The Ethernet port connects your device to a network (like a switch, router, or modem). From there, it can communicate with many other devices or servers. The magic happens in software and protocols, not the physical port.

🧵 Key Concepts That Enable This
1. IP Address & Ports
Your device gets one IP address (like a house address).

But each program or service uses a port number (like an apartment number/room number/floor number).

Example:

Browser uses port 443 for HTTPS

SSH might use port 22

MQTT client may use port 1883

2. Sockets
Each connection is managed using a socket.

A socket = IP address + port number.

The OS can open many sockets at once, allowing multiple simultaneous connections.


🧵 It Works Like This:
Example: One App Talking to 3 Servers
Your IP: 192.168.1.10
Socket 1 → 192.168.1.10:50001 ↔ 8.8.8.8:53  (DNS)
Socket 2 → 192.168.1.10:50002 ↔ 142.250.72.14:443 (Google)
Socket 3 → 192.168.1.10:50003 ↔ 52.23.23.23:1883 (MQTT)
Each connection has a unique socket, even though they share:
Same Ethernet port
Same local IP address

📦 Internally: The Kernel Is Multitasking
Interrupts & buffers let the NIC (Network Interface Card) receive/send packets quickly.

The OS uses a network stack (TCP/IP stack) to:

Read packet headers

Find matching socket

Deliver data to the correct app

🧰 Bonus: Multi-Threading
Each socket can be:

Blocking (waits for data)

Non-blocking (checks if data is ready)

With Java NIO, Kotlin coroutines, or epoll/select in Linux, the OS efficiently manages thousands of open sockets at once — even in a single-threaded app.

🚀 Summary
You can have thousands of simultaneous connections on:

One Ethernet port

One IP address
Because: ✅ Each socket is uniquely identified
✅ The OS tracks them independently
✅ The NIC and OS handle packet routing efficiently


Uses twisted pairs of wires:

TX+ / TX− → Transmit data

RX+ / RX− → Receive data

Means they can send and recieve at the same time but if different ports try to send the data at same time transmission will be multithreaded same will be for receive.