package com.jroget.mypong_client.events

import java.io.Serializable

interface ClientMainThreadEvents : Serializable {

    fun onClientConnectionLost()

    fun onClientNewResponse(serverResponse: String)

    fun onClientDisconnected()

    fun onClientConnected()

    fun mainThreadLog(string: String)
}