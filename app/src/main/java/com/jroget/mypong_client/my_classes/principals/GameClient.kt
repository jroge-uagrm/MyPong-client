package com.jroget.mypong_client.my_classes.principals

import android.app.Application
import android.util.Log
import com.google.gson.Gson
import com.jroget.mypong_client.events.ClientMainThreadEvents
import com.jroget.mypong_client.my_classes.auxiliaries.*
import java.io.Serializable
import java.util.*

class GameClient : ClientMainThreadEvents, Serializable, Application() {

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
    private var myMatchId: String? = null

    companion object {
        private lateinit var gameClient: GameClient
        private lateinit var gameActions: GameActions
        private var invitations: LinkedList<User>? = LinkedList()

        fun getInstance(newGameActions: GameActions): GameClient {
            gameActions = newGameActions
            try {
                var x: GameClient = gameClient
            } catch (e: Exception) {
                gameClient = GameClient()
                invitations = LinkedList()
            } finally {
                return gameClient
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        gameClient = this
    }

    fun getUsername(): String? {
        return username
    }

    fun isConnected(): Boolean {
        return client!!.isConnected()
    }

    fun isInRoom(): Boolean {
        return myMatchId != ""
    }

    fun getUserList(): LinkedList<User>? {
        return playerList
    }

    fun getInvitations(): LinkedList<User>? {
        return invitations
    }

    fun getPartnerList(): LinkedList<User>? {
        return partnerList
    }

    fun getKey(): String? {
        return myKey
    }

    fun getRoomId(): String? {
        return myMatchId
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
        myMatchId = "waiting..."
        send(Protocol("createRoom", "Esto no cirbe"))
    }

    fun leaveMatch() {
        send(Protocol("leaveRoom", ""))
    }

    fun sendInvitation(playerIndex: Int) {
        send(Protocol("invite", playerList!![playerIndex].key.toString()))
    }

    fun acceptInvitation(userKey: String) {
        deleteInvitation(userKey)
        send(Protocol("acceptInvitation", userKey))
        gameActions.refresh()
    }

    fun rejectInvitation(userKey: String) {
        deleteInvitation(userKey)
        send(Protocol("rejectInvitation", userKey))
        gameActions.refresh()
    }

    fun deleteInvitation(userKey: String) {
        invitations!!.remove(getInvitationByKey(userKey))
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
                    myMatchId = ""
                    gameActions.showMessageDialog(protocol.content)
                }
                "deleteRoom" -> {
                    myMatchId = ""
                    gameActions.refresh()
                }
                "ballMoving" -> {
                    gameActions.catchBall(
                        gson!!.fromJson(
                            protocol.content,
                            BallConverted::class.java
                        )
                    )
                }
                "scoreboard" -> {
                    gameActions.refreshScoreboard(
                        gson!!.fromJson(
                            protocol.content,
                            Scoreboard::class.java
                        )
                    )
                }
                "winner" -> {
                    gameActions.showWinner(
                        gson!!.fromJson(
                            protocol.content,
                            User::class.java
                        )
                    )
                }
            }
        } else {
            gameActions.showMessageDialog(objec.body.toString())
        }
        gameActions.refresh()
    }

    override fun onClientDisconnected() {
        username = "Client";
        myMatchId = "";
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
            send(Protocol("login", gson!!.toJson(User(username!!, password!!))))
        } else {
            send(Protocol("register", gson!!.toJson(User(username!!, password!!))))
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
            myMatchId = newUserList[0].matchId
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
            getUserByKey(user.key)!!.matchId = user.matchId
            if (user.matchId == myMatchId) {
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
        invitations!!.add(userOwner)
        gameActions.showMessageDialog("You have a new invitation!!")
    }

    private fun changePlayerInRoom(stringUser: String) {
        val userLeftRoom =
            gson!!.fromJson(
                stringUser,
                User::class.java
            )
        if (userLeftRoom.key == myKey) {
            myMatchId = ""
            partnerList = LinkedList()
            gameActions.showMessageDialog("You have left the room")
        } else {
            if (partnerList!!.remove(getPartnerByKey(userLeftRoom.key))) {
                println(partnerList!!.size)
                gameActions.showMessageDialog(userLeftRoom.username + " has left the room")
            }
            getUserByKey(userLeftRoom.key)!!.matchId = ""
        }
    }

    fun sendBall(force: Int, angle: Int, y: Int) {
        send(
            Protocol(
                "ballMoving",
                gson!!.toJson(BallConverted(force, angle, y))
            )
        )
    }

    fun sendBallFailed() {
        send(
            Protocol(
                "ballFailed",
                ""
            )
        )
    }

    private fun send(protocol: Protocol) {
        //Send usado por el login
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

    private fun getInvitationByKey(key: String?): User? {
        for (user in invitations!!) {
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