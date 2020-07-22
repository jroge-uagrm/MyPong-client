package com.jroget.mypong_client.uis

import ItemInvitations
import ItemUserList
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.jroget.mypong_client.GameActivity
import com.jroget.mypong_client.R
import com.jroget.mypong_client.my_classes.auxiliaries.BallConverted
import com.jroget.mypong_client.my_classes.auxiliaries.GameActions
import com.jroget.mypong_client.my_classes.auxiliaries.Scoreboard
import com.jroget.mypong_client.my_classes.auxiliaries.User
import com.jroget.mypong_client.my_classes.principals.GameClient
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity(), GameActions {

    private var gameClient: GameClient? = null
    private var username: String? = null
    private var password: String? = null
    private var connected: Boolean = false
    private var haveToShowInvitations: Boolean = false
    private var userList = mutableListOf<User>()
    private var invitations = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        getCredentials()
        gameClient = GameClient.getInstance(this)
        refresh()
        btn_close_session.setOnClickListener(closeSession)
        lsv_connected_users.setOnItemClickListener(selectUser)
        btn_change_list.setOnClickListener(changeList)
        btn_reconnect.setOnClickListener(reconnect)
        btn_play.setOnClickListener(play)

    }

    private val selectUser: AdapterView.OnItemClickListener =
        AdapterView.OnItemClickListener { parent, view, position, l ->
            val selectedItem = parent.getItemAtPosition(position)
            Toast.makeText(this, "Selected : $selectedItem", Toast.LENGTH_LONG).show()
        }

    private val closeSession: View.OnClickListener = View.OnClickListener {
        closeSession()
    }

    private val changeList: View.OnClickListener = View.OnClickListener {
        haveToShowInvitations = !haveToShowInvitations
        changeList()
    }

    private val reconnect: View.OnClickListener = View.OnClickListener {
        gameClient!!.disconnect()
        gameClient!!.connectByLogin(username, password)
        Toast.makeText(this, "Reconnecting", Toast.LENGTH_LONG).show()
    }

    private val play: View.OnClickListener = View.OnClickListener {
        Thread(Runnable {
            gameClient!!.createRoom()
            startActivity(Intent(this, GameActivity::class.java))
            finish()
        }).start()
    }

    private fun closeSession() {
        val myDB =
            openOrCreateDatabase("my.db", Context.MODE_PRIVATE, null)
        myDB.execSQL("DROP TABLE user")
        myDB.close()
        gameClient!!.disconnect()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun invite(pos: Int): View.OnClickListener {
        return View.OnClickListener {
            Thread(Runnable {
                gameClient!!.createRoom()
                gameClient!!.sendInvitation(pos)
                showMessageDialog("Invitation sent!")
            }).start()
            startActivity(Intent(this, GameActivity::class.java))
            finish()
        }
    }

    private fun acceptInvitation(userKey: String): View.OnClickListener {
        return View.OnClickListener {
            Thread(Runnable {
                gameClient!!.acceptInvitation(userKey)
                startActivity(Intent(this, GameActivity::class.java))
                finish()
            }).start()
        }
    }

    private fun rejectInvitation(userKey: String): View.OnClickListener {
        return View.OnClickListener {
            Thread(Runnable {
                gameClient!!.rejectInvitation(userKey)
            }).start()
        }
    }

    override fun refresh() {
        try {
            connected = gameClient!!.isConnected()
        } catch (e: Exception) {
//            connected = false
        }
        this.runOnUiThread(Runnable {
            if (connected) {
                txv_connected.text = "$username:Connected"
                btn_reconnect.isVisible = false
                refreshConnectedUsers()
                refreshInvitations()
            } else {
                txv_connected.text = "Disconnected"
                btn_reconnect.isVisible = true
            }
            changeList()
        })
    }

    override fun showMessageDialog(message: String) {
        this.runOnUiThread(Runnable {
            if (message.equals("Username does not exist")) {
                closeSession()
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun catchBall(ball: BallConverted) {
    }

    override fun refreshScoreboard(scoreboard: Scoreboard) {

    }

    override fun showWinner(winner: User) {
    }

    private fun getCredentials() {
        val myDB = openOrCreateDatabase("my.db", Context.MODE_PRIVATE, null)
        val myCursor: Cursor = myDB.rawQuery("select username,password from user", null)
        while (myCursor.moveToNext()) {
            username = myCursor.getString(0)
            password = myCursor.getString(1)
        }
        myCursor.close()
        myDB.close()
    }

    private fun refreshConnectedUsers() {
        userList = mutableListOf<User>()
        gameClient!!.getUserList()!!.forEach { user: User -> userList.add(user) }
    }

    private fun refreshInvitations() {
        invitations = mutableListOf<User>()
        gameClient!!.getInvitations()!!.forEach { user: User -> invitations.add(user) }
    }

    private fun changeList() {
        if (haveToShowInvitations) {
            val myCust: ItemInvitations = ItemInvitations(invitations, this)
            myCust.accept = { userKey ->
                acceptInvitation(userKey)
            }
            myCust.reject = { userKey ->
                rejectInvitation(userKey)
            }
            lsv_connected_users.adapter = myCust
            btn_change_list.text = "Change to online users"
            txv_title_list.text = "Invitations"
        } else {
            val myCust = ItemUserList(userList, this)
            myCust.invite = { pos ->
                invite(pos)
            }
            lsv_connected_users.adapter = myCust
            btn_change_list.text = "Change to invitations"
            txv_title_list.text = "Online users"
        }
    }
}