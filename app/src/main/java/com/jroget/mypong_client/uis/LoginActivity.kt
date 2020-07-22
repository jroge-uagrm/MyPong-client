package com.jroget.mypong_client.uis

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jroget.mypong_client.R
import com.jroget.mypong_client.my_classes.auxiliaries.BallConverted
import com.jroget.mypong_client.my_classes.auxiliaries.GameActions
import com.jroget.mypong_client.my_classes.auxiliaries.Scoreboard
import com.jroget.mypong_client.my_classes.auxiliaries.User
import com.jroget.mypong_client.my_classes.principals.GameClient
import kotlinx.android.synthetic.main.login_layout.*


class LoginActivity : AppCompatActivity(), GameActions {

    private lateinit var gameClient: GameClient
    private var username: String? = null
    private var password: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)
        gameClient = GameClient.getInstance(this)
        if (credentialsExist()) {
            gameClient.connectByLogin(username, password)
            goToHome()
        }
        btn_login.setOnClickListener(login)
        btn_register.setOnClickListener(register)
    }

    private val login: View.OnClickListener = View.OnClickListener {
        if (validate()) {
            username = edtUsername.text.toString()
            password = edtPassword.text.toString()
            gameClient!!.connectByLogin(username, password)
            enableComponents(false)
        } else {
            toast("Fields required.")
        }
    }

    private val register: View.OnClickListener = View.OnClickListener {
        if (validate()) {
            username = edtUsername.text.toString()
            password = edtPassword.text.toString()
            gameClient!!.connectByRegister(username, password)
            enableComponents(false)
        } else {
            toast("Fields required.")
        }
    }

    private fun validate(): Boolean {
        return edtUsername.text.isNotEmpty() && edtPassword.text.isNotEmpty()
    }

    private fun enableComponents(enable: Boolean = true) {
        this.runOnUiThread(Runnable {
            btn_login.isEnabled = enable
            btn_register.isEnabled = enable
            edtUsername.isEnabled = enable
            edtPassword.isEnabled = enable
        })
    }

    override fun refresh() {

    }

    override fun showMessageDialog(message: String) {
        toast(message)
        if (message == "OK") {
            saveCredentials()
            goToHome()
        }
        enableComponents()
    }

    override fun catchBall(ball: BallConverted) {
    }

    override fun refreshScoreboard(scoreboard: Scoreboard) {

    }

    override fun showWinner(winner: User) {
    }

    private fun toast(msg: String, isLong: Boolean = true) {
        this.runOnUiThread(Runnable {
            if (isLong)
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            else
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        })
    }

    private fun saveCredentials() {
        val myDB =
            openOrCreateDatabase("my.db", Context.MODE_PRIVATE, null)
//        myDB.execSQL("DROP TABLE user")
        val row1 = ContentValues()
        row1.put("id", 1)
        row1.put("username", username)
        row1.put("password", password)
        myDB.insert("user", null, row1)
        myDB.close()
    }

    private fun goToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun credentialsExist(): Boolean {
        val myDB =
            openOrCreateDatabase("my.db", Context.MODE_PRIVATE, null)
        myDB.execSQL(
            "CREATE TABLE IF NOT EXISTS user (" +
                    "id INT," +
                    "username VARCHAR(50)," +
                    "password VARCHAR(50)" +
                    ")"
        )
        val myCursor: Cursor = myDB.rawQuery("select username,password from user", null)
        var exist = false
        while (myCursor.moveToNext()) {
            username = myCursor.getString(0)
            password = myCursor.getString(1)
            exist = true
        }
        myCursor.close()
        myDB.close()
        return exist
    }
}