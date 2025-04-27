import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
// code to read byte by byte from a file and print the byte read to the console
// open a hex editor and check the results
fun main() {
    val fileName = "2.pdf" // Replace with your file name or path 
    val file = File(fileName)
    if (file.exists()) {
        
        println("File size: ${file.length()} bytes")
        println("-------------------------------------")
        val inputStream = FileInputStream(file)                // Step 1: Open stream
        var byteRead: Int
        byteRead = inputStream.read() // read the first byte of the file and increment the index of read
        println("Byte read: $byteRead")
        println("-------------------------------------")
        byteRead = inputStream.read()// this will read the second byte of the file and increment the index of read
        println("Byte read: $byteRead")
        println("-------------------------------------")
    inputStream.close() // close the read
    } else {
        println("File not found: $fileName")
    }
}