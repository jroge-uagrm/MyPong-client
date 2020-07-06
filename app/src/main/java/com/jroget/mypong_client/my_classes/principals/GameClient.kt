package com.jroget.mypong_client.my_classes.principals

import android.util.Log
import com.google.gson.Gson
import com.jroget.mypong_client.events.ClientMainThreadEvents
import com.jroget.mypong_client.my_classes.auxiliaries.ContainerObject
import com.jroget.mypong_client.my_classes.auxiliaries.GameActions
import com.jroget.mypong_client.my_classes.auxiliaries.Protocol
import com.jroget.mypong_client.my_classes.auxiliaries.User
import java.util.*


class GameClient(
    private val gameActions: GameActions
) : ClientMainThreadEvents {

    private val host = "192.168.1.117"
    private val port = 32000
    private val client: Client? = Client(host, port, this)
    private var playerList: LinkedList<User>? = LinkedList()
    private var partnerList: LinkedList<User>? = LinkedList()
    private val gson: Gson? = Gson()
    private var myKey: String? = null
    private var username: String? = null
    private var password: String? = null
    private var action: String? = null
    private var myRoomId: String? = null

    fun getUsername(): String? {
        return username
    }

    fun isConnected(): Boolean {
        return client!!.isConnected()
    }

    fun isInRoom(): Boolean {
        return myRoomId != ""
    }

    fun getUserList(): LinkedList<User>? {
        return playerList
    }

    fun getPartnerList(): LinkedList<User>? {
        return partnerList
    }

    fun getKey(): String? {
        return myKey
    }

    fun getRoomId(): String? {
        return myRoomId
    }

    fun sending(): Boolean {
        return action != ""
    }

    fun connectByLogin(username: String?, password: String?) {
        this.username = username
        this.password = password
        action = "login"
        gameActions.refresh()
        client!!.connect()
    }

    fun connectByRegister(username: String?, password: String?) {
        this.username = username
        this.password = password
        action = "register"
        gameActions.refresh()
        client!!.connect()
    }

    fun disconnect() {
        client!!.disconnect()
    }

    fun createRoom() {
        myRoomId = "waiting..."
        send(Protocol("createRoom", ""))
    }

    fun leaveRoom() {
        send(Protocol("leaveRoom", ""))
    }

    fun sendInvitation(playerIndex: Int) {
        send(Protocol("invite", playerList!![playerIndex].key.toString()))
    }

    override fun onClientConnectionLost() {
        if (action.equals("")) {
            client!!.connect();
        }
        gameActions.refresh();
    }

    override fun onClientNewResponse(serverResponse: String) {
        val objec: ContainerObject = gson!!.fromJson(serverResponse, ContainerObject::class.java)
        if (objec.origin == "server") {
            Log.d("MYPONG", "Handling:$serverResponse")
            val x = gson.toJson(objec.body)
            val protocol = gson.fromJson(x, Protocol::class.java)
            when (protocol.action) {
                "newKey" -> {
                    myKey = protocol.content
                    sendCredentials()
                }
                "userList" -> setUserList(protocol.content)
                "serverStopped" -> gameActions.showMessageDialog("Server stopped")
                "loginResponse" -> manageLoginResponse(protocol.content)
                "registerResponse" -> manageRegisterResponse(protocol.content)
                "connectedUser" -> addUser(protocol.content)
                "disconnectedUser" -> removeUser(protocol.content)
                "newInvitation" -> catchInvitation(protocol.content)
                "rejectedInvitation" -> gameActions.showMessageDialog(getUserByKey(protocol.content)!!.username + " has rejected the invitation")
                "joinedRoom" -> verifyPartnerList(protocol.content)
                "leftRoom" -> changePlayerInRoom(protocol.content)
                "errorRoom" -> gameActions.showMessageDialog(protocol.content)
                "emptyRoom" -> {
                    myRoomId = ""
                    gameActions.showMessageDialog(protocol.content)
                }
                "deleteRoom" -> {
                    myRoomId = ""
                    gameActions.refresh()
                }
            }
        } else {
            gameActions.showMessageDialog(objec.body.toString())
        }
        gameActions.refresh()
    }

    override fun onClientDisconnected() {
        username = "Client";
        myRoomId = "";
        action = "";
        gameActions.refresh();
    }

    override fun onClientConnected() {
        gameActions.refresh();
    }

    override fun mainThreadLog(message: String) {
        Log.d("MYPONG", message)
    }

    private fun sendCredentials() {
        if (action.equals("login")) {
            send(
                Protocol(
                    "login",
                    gson!!.toJson(User(username!!, password!!))
                )
            )
        } else {
            send(
                Protocol(
                    "register",
                    gson!!.toJson(User(username!!, password!!))
                )
            )
        }
    }

    private fun setUserList(listString: String) {
        playerList = LinkedList()
        val newUserList =
            gson!!.fromJson(
                listString,
                Array<User>::class.java
            )
        for (i in newUserList.indices) {
            if (newUserList[i].key != myKey) {
                playerList!!.add(newUserList[i])
            }
        }
    }

    private fun verifyPartnerList(info: String) {
        try {
            val newUserList =
                gson!!.fromJson(
                    info,
                    Array<User>::class.java
                )
            myRoomId = newUserList[0].roomId
            gameActions.showMessageDialog("Welcome to room")
            partnerList = LinkedList()
            for (i in newUserList.indices) {
                partnerList!!.add(newUserList[i])
            }
        } catch (e: Exception) {
            val user =
                gson!!.fromJson(
                    info,
                    User::class.java
                )
            getUserByKey(user.key)!!.roomId = user.roomId
            if (user.roomId == myRoomId) {
                partnerList!!.add(user)
                gameActions.showMessageDialog(user.username + " has joined to room")
            }
        }
    }

    private fun manageLoginResponse(response: String) {
        if (response == "OK") {
            gameActions.refresh()
        }
        gameActions.showMessageDialog(response)
        action = ""
    }

    private fun manageRegisterResponse(response: String) {
        if (response == "OK") {
            gameActions.refresh()
        }
        gameActions.showMessageDialog(response)
        action = ""
    }

    private fun addUser(userString: String) {
        val newUser =
            gson!!.fromJson(
                userString,
                User::class.java
            )
        if (newUser.key != myKey) {
            playerList!!.add(newUser)
        }
        gameActions.refresh()
    }

    private fun removeUser(userKey: String) {
        playerList!!.remove(getUserByKey(userKey))
        val userRemoved =
            getPartnerByKey(userKey)
        if (partnerList!!.remove(userRemoved)) {
            gameActions.showMessageDialog(userRemoved!!.username + " has left the room")
        }
    }

    private fun catchInvitation(stringUser: String) {
        val userOwner =
            gson!!.fromJson(
                stringUser,
                User::class.java
            )
        val opt =
            gameActions.showConfirmDialog(userOwner.username + " has invited you to join a room")
        if (opt == 0) {
            myRoomId = userOwner.roomId
            send(Protocol("acceptInvitation", userOwner.key.toString()))
        } else {
            send(Protocol("rejectInvitation", userOwner.key.toString()))
        }
    }

    private fun changePlayerInRoom(stringUser: String) {
        val userLeftRoom =
            gson!!.fromJson(
                stringUser,
                User::class.java
            )
        if (userLeftRoom.key == myKey) {
            myRoomId = ""
            partnerList = LinkedList()
            gameActions.showMessageDialog("You have left the room")
        } else {
            if (partnerList!!.remove(getPartnerByKey(userLeftRoom.key))) {
                println(partnerList!!.size)
                gameActions.showMessageDialog(userLeftRoom.username + " has left the room")
            }
            getUserByKey(userLeftRoom.key)!!.roomId = ""
        }
    }

    private fun send(protocol: Protocol) {
        client!!.send(
            ContainerObject(
                myKey!!,
                protocol,
                arrayOf("server")
            )
        )
    }

    private fun getUserByKey(key: String?): User? {
        for (user in playerList!!) {
            if (user.key == key) {
                return user
            }
        }
        return null
    }

    private fun getPartnerByKey(key: String?): User? {
        for (user in partnerList!!) {
            if (user.key == key) {
                return user
            }
        }
        return null
    }
}