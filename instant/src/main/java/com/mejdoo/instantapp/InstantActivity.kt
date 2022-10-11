package com.mejdoo.instantapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.instantapps.InstantApps
import kotlinx.android.synthetic.main.activity_instant.*
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException
import kotlin.concurrent.thread

class InstantActivity : AppCompatActivity() {

    var isServer = false
    val port = 7883
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instant)
        setSupportActionBar(toolbar)


        runServer(isServer)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_instant, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_download -> {
                showInstallPrompt()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showInstallPrompt() {
        val postInstall = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_DEFAULT)
            .setPackage(BuildConfig.APPLICATION_ID)

        // The request code is passed to startActivityForResult().
        InstantApps.showInstallPrompt(this, postInstall, REQUEST_CODE, /* referrer= */ null)
    }

    companion object {
        const val REQUEST_CODE = 1
    }


    lateinit var udpClient: ClientSocket
    lateinit var udpServer: SocketServer

    private fun runServer(isServer: Boolean) {
        if (isServer) {
            udpServer = SocketServer(port)
            thread(start = true) {
                udpServer.run()
            }
        } else {
            udpClient = ClientSocket(port)
            thread(start = true) {
                udpClient.run()
            }
        }
    }


    class SocketServer(val port: Int) : Runnable {
        override fun run() {
            var run = true
            while (run) {
                try {
                    val udpSocket = DatagramSocket(port)
                    val message = ByteArray(8000)
                    val packet = DatagramPacket(message, message.size)
                    Log.i("UDP client: ", "about to wait to receive")
                    udpSocket.receive(packet)
                    val text = String(message, 0, packet.getLength())
                    Log.d("Received data", text)
                } catch (e: IOException) {
                    Log.e("UDP client", "error: ", e)
                    run = false
                }
            }
        }
    }

    class ClientSocket(val port: Int) : Runnable {
        override fun run() {
            try {
                val udpSocket = DatagramSocket(port)
                val serverAddr: InetAddress = InetAddress.getByName("192.168.1.5")
                val buf = "The String to Send".toByteArray()
                val packet = DatagramPacket(buf, buf.size, serverAddr, port)
                udpSocket.send(packet)
            } catch (e: SocketException) {
                Log.e("Udp:", "Socket Error:", e)
            } catch (e: IOException) {
                Log.e("Udp Send:", "IO Error:", e)
            }
        }
    }
}
