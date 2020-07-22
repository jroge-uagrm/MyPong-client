package com.jroget.mypong_client.my_classes.principals

import com.jroget.mypong_client.events.ClientMainThreadEvents
import com.jroget.mypong_client.my_classes.auxiliaries.ContainerObject
import com.jroget.mypong_client.threads.ClientMainThread
import java.io.Serializable


class Client(
    host: String,
    port: Int,
    events: ClientMainThreadEvents
) : Serializable {
    private val clientMainThread = ClientMainThread(host, port, events);

    fun connect() {
        Thread(clientMainThread).start()
    }

    fun send(objec: ContainerObject) {
        //Usado por el login
        clientMainThread.sendMessage(objec)
    }

    fun disconnect() {
        clientMainThread.disconnect()
    }

    fun isConnected(): Boolean {
        return clientMainThread != null && clientMainThread.getConnectedStatus()
    }
}