@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.mqttapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.* // * means import all, but advisable to import only what is needed
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.items //
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mqttapp.ui.theme.MqttAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/*
 gradel 8 does not allow usage of android instead androidx is used
 Mqtt Libries like Eclipse Paho(did not support androidX libraries), hivwMQ did not support//(
(MQTT 3.1.1)

 Because Adafruit IO doesn't support the MQTT retain flag, we can use the
 get() function to ask IO to resend the last value for this feed to just
 this MQTT client after the io client is connected.
Use Dispatchers.IO for network requests, file I/O, or any blocking operations that should not be performed on the main thread.

Always ensure that UI updates (e.g., modifying a TextView) are done on the main thread (Dispatchers.Main).

//---------------------------What happens if the SUBSCRIBE and PUBLISH client IDs are the same start-----------------------
 What happens if the SUBSCRIBE and PUBLISH client IDs are the same, but used from different connections?
⚠️ It can cause one client to get disconnected automatically.
📜 According to the MQTT Spec:
If a second client connects using the same clientId as an existing connection, the broker will immediately disconnect the first one.

So in your case:

If connectAndSubscribe() connects with clientId = "myClient"

And later, connectAndPublish() connects with the same clientId = "myClient"

👉 The broker (like Adafruit IO or any MQTT broker) will drop the first (subscriber) connection as soon as the second one connects.

💥 Result:
Your subscription connection will get disconnected silently, possibly mid-read.

You might see a read = -1 or Socket Closed error.

KeepAlivePingRunning might silently stop working too.

✅ Solution: Use unique client IDs for each connection
kotlin
val subscribeClientId = "myApp_sub_001"
val publishClientId = "myApp_pub_001"
Then pass the appropriate one when building each CONNECT packet.
//---------------------------What happens if the SUBSCRIBE and PUBLISH client IDs are the same end-----------------------
*/

class MainActivity : ComponentActivity() {

    //mutableStateOf is necessary to update the ui immediately
    private var inputText =  mutableStateOf("Test")
    private val textState1 = mutableStateOf("") // External state variable
    private val textState2 = mutableStateOf("") // External state variable
    private var buttonConnectNpublish by mutableStateOf(true)
    private var buttonConnectSubscribe by mutableStateOf(true)
    private val logMessages = mutableStateListOf<String>()

    // AtomicBoolean for thread safety
    var KeepAlivePingRunning = AtomicBoolean(true)

    private var readBytes = 0


    private val mqttUtils = mqtt_Utils()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            val temp = greetUser("Pranjal")// this function called from another kotlin file
            Log.d("MQTT", temp)

            val temp1=mqttUtils.test()
            Log.d("MQTT", temp1)
            val temp2=mqttUtils.test1()
            Log.d("MQTT", temp2)

            setContent {
                MqttAppTheme {
                    Box(
                        modifier = Modifier.fillMaxSize(), // Fills the entire screen
                        contentAlignment = Alignment.Center // Centers content in the middle of the screen
                    ) {
                        Column(modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp) // Adds 16dp padding to all sides
                        )
                        {

                            // Row 0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ){LogConsoleApp(logMessages)}


                            // Row 1
                            // Spacer to add space between rows
                            Spacer(modifier = Modifier.height(25.dp)) // Adds vertical space between rows

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Input Text Box
                                OutlinedTextField(
                                    value = inputText.value,
                                    onValueChange = { inputText.value = it },
                                    label = { Text("Enter text") },
                                    modifier = Modifier.width(180.dp) // 👈 fixed width
                                )
                                Button(enabled = buttonConnectNpublish, onClick = { connectAndPublish(inputText.value)}) {
                                    Text("connectAndPublish")
                                }
                            }

                            // Row 2
                            // Spacer to add space between rows
                            Spacer(modifier = Modifier.height(25.dp)) // Adds vertical space between rows

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(textState2.value)
                                Button(enabled = buttonConnectSubscribe, onClick = { connectAndSubscribe()  }) {
                                    Text("Connect&Subscribe")
                                }
                            }

                            // Row 3
                            // Spacer to add space between rows
                            Spacer(modifier = Modifier.height(25.dp)) // Adds vertical space between rows

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(textState1.value)
                                Button(enabled = false, onClick = {/*Action*/}) {
                                    Text("Not Used")
                                }
                            }
                        }
                    }

                }
            }
    }
    //------------------------------refer chatgpt or google for detailed explaination-----------------------------------------------------

    private val username = "YourUsername"
    private val aioKey = "YourAdafruitKey"
    private val topic = "$username/feeds/test"

    private val clientID_subscriber = "myClient_subscriber"
    private val clientId_publish = "clientId_publish"

    private fun encodeString(str: String): ByteArray {
        val bytes = str.toByteArray(StandardCharsets.UTF_8)
        return byteArrayOf((bytes.size shr 8).toByte(), (bytes.size and 0xFF).toByte()) + bytes
    }

    private fun encodeLength(length: Int): ByteArray {
        var value = length
        val encoded = mutableListOf<Byte>()
        do {
            var byte = (value % 128).toByte()
            value /= 128
            if (value > 0) byte = (byte.toInt() or 0x80).toByte()
            encoded.add(byte)
        } while (value > 0)
        return encoded.toByteArray()
    }

    private fun buildConnectPacket(clientId: String, username: String, password: String): ByteArray {
        val protocolName = encodeString("MQTT")
        val protocolLevel = byteArrayOf(0x04) // MQTT 3.1.1
        val connectFlags = byteArrayOf(0b1100_0000.toByte()) // username & password
        val keepAlive = byteArrayOf(0x00, 0x3C) // 60 seconds

        val payload = encodeString(clientId) + encodeString(username) + encodeString(password)
        val variableHeader = protocolName + protocolLevel + connectFlags + keepAlive
        val remainingLength = encodeLength(variableHeader.size + payload.size)

        return byteArrayOf(0x10) + remainingLength + variableHeader + payload
    }

    private fun buildPublishPacket(topic: String, message: String): ByteArray {
        val topicBytes = encodeString(topic)
        val messageBytes = message.toByteArray(StandardCharsets.UTF_8)
        val remainingLength = encodeLength(topicBytes.size + messageBytes.size)
        //first byte 0x30 means qos =0;0x32 means qos =1; 0x34 means qos =2; google for mor details
        return byteArrayOf(0x30) + remainingLength + topicBytes + messageBytes
    }

    private fun buildSubscribePacket(packetId: Int, topic: String): ByteArray {
        // Step 1: Encode the topic as a UTF-8 string with a length prefix
        val topicBytes = encodeString(topic)


        // Step 2: Fixed Header
        // 0x80 for SUBSCRIBE packet type, with reserved flag bits
        val fixedHeader = byteArrayOf(0x82.toByte())

        // Step 3: Remaining Length
        // Remaining length is calculated as the sum of the length of the variable header and payload
        val remainingLength =
            encodeLength(2 + topicBytes.size + 1) // 2 bytes for packetId, topic length, and QoS byte

        // Step 4: Packet Identifier (2-byte, big-endian)
        val packetIdBytes = byteArrayOf(
            (packetId shr 8).toByte(),  // High byte of packet ID
            (packetId and 0xFF).toByte() // Low byte of packet ID
        )

        // Step 5: Payload (Topic + QoS level)
        // Topic is encoded in the previous step, and QoS is provided by the argument
//        val qosByte = byteArrayOf(0x01)  // QoS 1
        val payload = topicBytes + byteArrayOf(0x01) // byteArrayOf(0x01) qos
        // Step 6: Combine everything
        return fixedHeader + remainingLength + packetIdBytes + payload
    }

    private fun connectAndSubscribe() {
        // Launch a coroutine in the IO dispatcher (background thread)
        CoroutineScope(Dispatchers.IO).launch {
//            withContext(Dispatchers.Main) { //switch to the main thread to update the UI immediately
                // on value change of the Ui properties example buttonConnectSubscribe = false, logMessages.add("📢 trying to connect... ") etc
                try {
                    // Now, switch to the main thread to update the UI immediately
                    buttonConnectSubscribe = false;
                    textState2.value = "trying to connect..."
                    logMessages.add("📢 trying to connect... ${LocalTime.now().format(
                        DateTimeFormatter.ofPattern("HH:mm:ss"))} ")

                    val socket = SocketChannel.open()
                    /*The SocketChannel.open() method itself does not set a default keep-alive timeout. It just creates a SocketChannel, which internally uses a Socket object.
                    Any keep-alive behavior comes from the underlying TCP socket settings. SocketChannel.open() has no built-in keep-alive timeout. it takes the default value set on the Hardware level.*/
                    socket.socket().keepAlive = true;

                    socket.connect(InetSocketAddress("io.adafruit.com", 1883)) //blocking type

                    if (socket.isConnected) {
                        Log.d("MQTT", "🔌 Connected to Adafruit IO")
                            logMessages.add("✅ Connected to Adafruit IO. ${LocalTime.now().format(
                                DateTimeFormatter.ofPattern("HH:mm:ss"))} ") // you can use emojis(the tick Emoji) from https://emojipedia.org/

                    } else {
                        Log.e("MQTT", "Connection to Adafruit IO Failed !")

                            logMessages.add("❌ Connection to Adafruit IO Failed ! ${LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))} ")
                            buttonConnectSubscribe = true

//                        return@withContext
                    }


                    val connectPacket = buildConnectPacket(clientID_subscriber, username, aioKey)
                    //socket.write(ByteBuffer.wrap(connectPacket)), Possibly blocking call in non-blocking context could lead to thread starvation
                    // may freeze the User interface and jetpack compose warns it
                    var status =
                        socket.write(ByteBuffer.wrap(connectPacket))//blocking type send the data over tcp
                    if (status < 0) {
                        Log.e("MQTT", "🔌Failed to establish communication!")

                            logMessages.add("❌ Failed to establish communication! ${LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))} ")
                            buttonConnectSubscribe = false;

//                        return@withContext
                    }

                    Log.d("MQTT", "📤 CONNECT sent")
                    logMessages.add("✅ connect sent. ${LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))}")

                    val buffer = ByteBuffer.allocate(1024)
                    status =
                        socket.read(buffer) //blocking type read the data if available in buffer
                    if (status < 0) {
                        Log.e("MQTT", "🔌Failed to establish communication!")

                            logMessages.add("❌ Failed to establish communication! ${LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))} ")
                            buttonConnectSubscribe = false
//                        return@withContext
                    }
                    buffer.flip()
                    val connack = ByteArray(buffer.remaining())
                    buffer.get(connack)
                    Log.d("MQTT", "📥 CONNACK: ${connack.joinToString(" ") { "%02x".format(it) }}")
                    logMessages.add("✅ Connect Ack. ${LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))}")

                    // Send SUBSCRIBE
                    val subscribePacket = buildSubscribePacket(1, topic)
                    socket.write(ByteBuffer.wrap(subscribePacket))
                    Log.d("MQTT", "📤 SUBSCRIBE sent")
                    logMessages.add("✅ Subscribe Sent. ${LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))}")


                    // Read SUBACK
                    buffer.clear()
                    socket.read(buffer)
                    buffer.flip()
                    val suback = ByteArray(buffer.remaining())
                    buffer.get(suback)
                    Log.d("MQTT", "📥 SUBACK: ${suback.joinToString(" ") { "%02x".format(it) }}")
                    logMessages.add("✅ Subscribe Ack. ${LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))}")
                    buffer.clear()

                    //run in main thread to update the ui immediately
                    withContext(Dispatchers.Main) {
                        textState2.value = "Listening..."
                    }

                    //keep Alive ping request broker server replies 2 byte data, byte[0] is PINGRES(0xD0) also contains the 5 bit flags, byte[1] is Remaining Length (0x00)
                    KeepAlivePingRunning = AtomicBoolean(true)
                    startPingLoop(socket, KeepAlivePingRunning)

                    while (true) {
                        buffer.clear()
                        readBytes = socket.read(buffer)
                        /* // ping response from broker
                if (data.size == 2 && data[0] == 0xD0.toByte() && data[1] == 0x00.toByte()) {
                    Log.d("MQTT", "📥 Received PINGRESP from broker")
                }
                */
                        if (readBytes > 0) { // readBytes == 0  means data has not yet finished receiving, often when the socket is waiting for an incoming data or receiving is in progress
                            buffer.flip()
                            val data = ByteArray(buffer.remaining())
                            buffer.get(data)
                            Log.d(
                                "MQTT",
                                "📥 MESSAGE: ${data.joinToString(" ") { "%02x".format(it) }}"
                            )
                            logMessages.add("📥 MESSAGE: ${data.joinToString(" ") { "%02x".format(it) }}  ${LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))}")

                            //run in main thread to update the ui immediately
                            withContext(Dispatchers.Main) {
                                textState1.value = parsePublish(data)
                            }

                        } else if (readBytes < 0) {
                            Log.e("MQTT", "📥 Connection Closed By the Broker server.")
                            logMessages.add("❌ Connection Closed By the Broker server. ${LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))}")

                            //run in main thread to update the ui immediately
                            withContext(Dispatchers.Main) {
                                textState2.value = "Socket Closed !"
                            }
                            break
                        }
//                Thread.sleep(1000)
                    }
                    socket.close()
                    buttonConnectSubscribe = true;

                } catch (e: Exception) {
                    Log.e("MQTT", "❌ Error: ${e.message}", e)
                    logMessages.add("❌ Failed to Open Socket ! ${LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))}")
                    buttonConnectSubscribe = true;
                }
//            }
        }
    }

    private fun connectAndPublish(str: String) {
//        KeepAlivePingRunning = AtomicBoolean(false)

//        readBytes = -1 //close the socket in blocking read mode (listning to subscription in connectAndSubscribe())
        CoroutineScope(Dispatchers.IO).launch { // run in a thread as it has
            try {
                // Now, switch to the main thread to update the UI
                withContext(Dispatchers.Main) { buttonConnectNpublish = false }
                val buffer = ByteBuffer.allocate(1024)
                val socket = SocketChannel.open()

                Log.d("MQTT", "📤 sending connect")

                socket.connect(InetSocketAddress("io.adafruit.com", 1883))
                Log.d("MQTT", "🔌 Connected to Adafruit IO")

                val connectPacket = buildConnectPacket(clientId_publish, username, aioKey)
                socket.write(ByteBuffer.wrap(connectPacket))
                Log.d("MQTT", "📤 CONNECT sent")

                socket.read(buffer)
                buffer.flip()
                val connack = ByteArray(buffer.remaining())
                buffer.get(connack)
                Log.d("MQTT", "📥 CONNACK: ${connack.joinToString(" ") { "%02x".format(it) }}")

                val publishPacket = buildPublishPacket(topic, str)
                socket.write(ByteBuffer.wrap(publishPacket))
                Log.d("MQTT", "📤 Published: $str")

                // Now, switch to the main thread to update the UI
                withContext(Dispatchers.Main) {inputText.value = ""}

                socket.read(buffer)
                buffer.flip()
                buffer.get(connack)
                Log.d("MQTT", "📥 CONNACK: ${connack.joinToString(" ") { "%02x".format(it) }}")
                socket.close()
                buttonConnectNpublish = true
            } catch (e: Exception) {
                Log.e("MQTT", "❌ Error: ${e.message}", e)
            }
        }
    }


    private fun parsePublish(data: ByteArray): String {
        var payload = "Not Valid !"
        if (data.isEmpty()) return "Empty !"
        val header = data[0].toInt() and 0xF0
        if (header == 0x30) { // PUBLISH
            val topicLength = (data[2].toInt() shl 8) or data[3].toInt()
            val topic = String(data, 4, topicLength, StandardCharsets.UTF_8)
            val payloadStart = 4 + topicLength
            payload = String(data, payloadStart, data.size - payloadStart, StandardCharsets.UTF_8)
            Log.d("MQTT", "🟢 Topic: $topic\n📦 Message: $payload")
            logMessages.add("✅ playload: $payload ${LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))}")
        }
        return payload
    }

    private fun buildPingReqPacket(): ByteArray {
        return byteArrayOf(0xC0.toByte(), 0x00)
    }

    //keep Alive ping request // atomic boolen gets delivered to all
    private fun startPingLoop(socket: SocketChannel, isRunning: AtomicBoolean) {
        thread(start = true) {
            try {
                while (isRunning.get()) {
                    Thread.sleep(80_000) // Wait 80 seconds, 1min20sec, adafruit io expects a keep alive ping request before (1 min30 secs approx)
                    val pingPacket = buildPingReqPacket()
                    socket.write(ByteBuffer.wrap(pingPacket))
                    Log.d("MQTT", "📤 Sent PINGREQ")
                    logMessages.add("✅ PING alive sent. ${LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))}")
                }
            } catch (e: Exception) {
                Log.e("MQTT", "❌ PING loop stopped: ${e.message}")
                logMessages.add("❌ PING loop stopped: ${e.message} ${LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))}")
            }
        }
    }
}

//-------Main activity code end-----------

@Composable
fun LogConsoleApp(logMessages: SnapshotStateList<String>) {
    val scope = rememberCoroutineScope()
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "📜 Log Console",
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp, top = 10.dp)
        )

        LogView(messages = logMessages)

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = {
                scope.launch {
                    withContext(Dispatchers.Main) {
                        logMessages.add("✅ Log entry at ${System.currentTimeMillis()}")
                    }
                }
            }) {
                Text("Add Log")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                logMessages.clear()
            }) {
                Text("Clear")
            }
        }
    }
}

@Composable
fun LogView(messages: List<String>) {
    val scrollState = rememberLazyListState()

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(Color.Black)
            .padding(8.dp)
    ) {
        items(messages) { msg -> // this items() function is of "import androidx.compose.foundation.lazy.items"
            Text(
                text = msg,
                color = Color.Green,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
    // Auto-scroll when new messages are added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.size - 1)
        }
    }
}