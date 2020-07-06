package com.jroget.mypong_client.events

interface ClientMainThreadEvents {

    fun onClientConnectionLost()

    fun onClientNewResponse(serverResponse: String)

    fun onClientDisconnected()

    fun onClientConnected()

    fun mainThreadLog(string: String)
}