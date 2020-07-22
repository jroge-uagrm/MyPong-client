package com.jroget.mypong_client.threads

import android.os.NetworkOnMainThreadException
import android.util.Log
import com.google.gson.Gson
import com.jroget.mypong_client.events.ClientMainThreadEvents
import com.jroget.mypong_client.my_classes.auxiliaries.ContainerObject
import java.io.*
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.log

class ClientMainThread(
    private val host: String,
    private val port: Int,
    private val events: ClientMainThreadEvents
) : Runnable, Serializable {

    private var clientSocket: Socket? = null
    private var printerWriterOUT: PrintWriter? = null
    private var bufferedReaderIN: BufferedReader? = null
    private var connected: Boolean = false
    private var connectionAttempts = 0
    private var tryingConnect = false
    private val MAX_RECONNECTION_ATTEMPS = 5
    private val TIME_BETWEEN_RECONNECTIONS = 2
    private val gson: Gson = Gson()

    override fun run() {
        tryingConnect = true
        tryToConnectToSocket()
        var message: String
        try {
            while (connected) {
                try {
                    message = bufferedReaderIN!!.readLine();
                } catch (e: Exception) {
                    message = ""
                }
                if (message.isEmpty()) {
                    connected = false
                    Log.d("MESSAGE", "EMPTY")
                } else {
                    internalLog("Received:$message")
                    events.onClientNewResponse(message)
                }
            }
            internalLog("Connection lost.")
        } catch (ex: IOException) {
            internalLog("Disconnected")
        } finally {
            disconnect();
            events.onClientDisconnected()
        }
    }

    fun sendMessage(objec: ContainerObject) {
        //Usado por el login
        if (connectionAttempts == 0) {
            val jsonObject: String = gson.toJson(objec)
            internalLog("Trying:$jsonObject")
            printerWriterOUT!!.println(jsonObject)
            internalLog("Sent:$jsonObject")
        }
    }

    fun disconnect() {
        connected = false;
        tryingConnect = false
        connectionAttempts = 0
        closeAll();
    }

    private fun closeAll() {
        if (clientSocket != null) {
            try {
                printerWriterOUT?.close()
                bufferedReaderIN?.close()
                clientSocket!!.close();
                events.onClientDisconnected()
            } catch (e: Exception) {
                internalLog("ERROR on disconnect" + e.message)
            }
        }
    }

    private fun startAll() {
        connected = true
        connectionAttempts = 0
        try {
            printerWriterOUT = PrintWriter(clientSocket!!.getOutputStream(), true)
            bufferedReaderIN = BufferedReader(InputStreamReader(clientSocket!!.getInputStream()))
        } catch (ex: IOException) {
            internalLog("ERROR(999):" + ex.message)
        }
    }

    private fun tryToConnectToSocket() {
        if (tryingConnect) {
            connectionAttempts++
            try {
                clientSocket = Socket(host, port)
                startAll()
                internalLog("Connected.")
                events.onClientConnected()
            } catch (ex: Exception) {
                connected = false
                internalLog("Unable to connect.Error:" + ex.message)
                if (connectionAttempts <= MAX_RECONNECTION_ATTEMPS) {
                    internalLog("Reconnecting... " + connectionAttempts + "/" + MAX_RECONNECTION_ATTEMPS + " attempt(s)");
                    try {
                        Thread.sleep((TIME_BETWEEN_RECONNECTIONS * 1000).toLong())
                    } catch (e: Exception) {
                        internalLog("ERROR on connectToSocket:" + e.message)
                    }
                    tryToConnectToSocket()
                } else {
                    disconnect()
                }
            }
        }
    }

    fun getConnectedStatus(): Boolean {
        return connected || tryingConnect
    }

    private fun internalLog(msg: String) {
        events.mainThreadLog(msg)
    }
}