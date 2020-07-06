package com.jroget.mypong_client.uis

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jroget.mypong_client.R
import com.jroget.mypong_client.my_classes.auxiliaries.GameActions
import com.jroget.mypong_client.my_classes.principals.GameClient
import kotlinx.android.synthetic.main.login_layout.*


class Login : AppCompatActivity(), GameActions {

    private var gameClient: GameClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        gameClient = GameClient(this)

        btn_login.setOnClickListener(login)
        btn_register.setOnClickListener(register)
    }

    private val login: View.OnClickListener = View.OnClickListener {
        if (validate()) {
            gameClient!!.connectByLogin(edtUsername.text.toString(), edtPassword.text.toString())
            enableComponents(false)
        } else {
            toast("Fields required.")
        }
    }

    private val register: View.OnClickListener = View.OnClickListener {
        if (validate()) {
            gameClient!!.connectByRegister(edtUsername.text.toString(), edtPassword.text.toString())
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
            /*edtUsername.isEnabled = enable
            edtPassword.isEnabled = enable*/
        })
    }

    override fun refresh() {
    }

    override fun showMessageDialog(message: String) {
        toast(message)
        if (message.equals("OK")) {
            startActivity(Intent(this, Home::class.java))
        }
        enableComponents()
    }

    override fun showConfirmDialog(message: String): Int {
        return 1
    }

    private fun toast(msg: String, isLong: Boolean = true) {
        this.runOnUiThread(Runnable {
            if (isLong)
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            else
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        })
    }
}