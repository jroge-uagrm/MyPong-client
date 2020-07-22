package com.jroget.mypong_client

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.database.Cursor
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jroget.mypong_client.my_classes.auxiliaries.*
import com.jroget.mypong_client.my_classes.principals.GameClient
import com.jroget.mypong_client.uis.GameView
import com.jroget.mypong_client.uis.HomeActivity
import java.util.*
import kotlin.math.sinh
import kotlin.math.sqrt


class GameActivity : AppCompatActivity(), GameActions, BallActions {

    private lateinit var gameClient: GameClient
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var gameView: GameView
    private lateinit var gameWidgets: LinearLayout
    private lateinit var txvMessage: TextView
    private lateinit var txvScoreboard: TextView
    private var H: Int = 1
    private var W: Int = 1
    private var inGame: Boolean? = true

    //Sensores
    private var accelerometerZAxis: Float = 0f
    private var gyroscopeZAxis: kotlin.Float = 0f
    private var accelerometerValueList: LinkedList<Int>? = LinkedList()
    private var gyroscopeValueList: LinkedList<Int?>? = LinkedList()
    private var ForceValue: TextView? = null
    private var AngleValue: TextView? = null
    private var x: TextView? = null
    private var y: TextView? = null
    private var z: TextView? = null
    private var isPressing = false

    private val accelerometerType: Int = android.hardware.Sensor.TYPE_ACCELEROMETER
    private val angleType: Int = android.hardware.Sensor.TYPE_ROTATION_VECTOR

    //fastest:0.01 game:0.02 normal:0.20 ui:~0.65
    private val accelerometerSensorSpeed = SensorManager.SENSOR_DELAY_GAME
    private val gyroscopeSensorSpeed = SensorManager.SENSOR_DELAY_GAME


    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        getCredentials()
        gameClient = GameClient.getInstance(this)
        var game = FrameLayout(this)

        val d = resources.displayMetrics
        W = d.widthPixels
        H = d.heightPixels
        Log.d("W-H", "$W-$H")
        gameView = GameView(this, W, H, this)

        gameWidgets = LinearLayout(this)
        val btnLeaveMatch = Button(this)
        btnLeaveMatch.setOnClickListener(leaveMatch)
        btnLeaveMatch.setBackgroundColor(Color.RED)
        btnLeaveMatch.setTextColor(Color.BLACK)
        btnLeaveMatch.text = "Leave match"
        txvMessage = TextView(this)
        txvMessage.x = (W / 3).toFloat()
        txvMessage.text = "Game started!"
        txvMessage.setTextColor(Color.YELLOW)

        txvScoreboard = TextView(this)
        txvScoreboard.x = txvMessage.x + 300
        txvScoreboard.text = "0|0\n0|0"
        txvScoreboard.setTextColor(Color.YELLOW)

        gameWidgets.addView(btnLeaveMatch)
        gameWidgets.addView(txvMessage)
        gameWidgets.addView(txvScoreboard)
        game.addView(gameView)
        game.addView(gameWidgets)
        setContentView(game)

        //Sensors
        val senSensorManager =
            getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometerSensor: Sensor? = senSensorManager.getDefaultSensor(accelerometerType)
        val gyroscopeSensor: Sensor? = senSensorManager.getDefaultSensor(angleType)
        if (accelerometerSensor != null) {
            senSensorManager.registerListener(
                accelerometerSensorListener,
                accelerometerSensor,
                accelerometerSensorSpeed
            )
            Log.d("OK", "Accelerometer listening")
        } else {
            Log.d("ERROR", "Accelerometer not listening")
        }
        if (gyroscopeSensor != null) {
            senSensorManager.registerListener(
                gyroscopeSensorListener,
                gyroscopeSensor,
                gyroscopeSensorSpeed
            )
            Log.d("OK", "Gyroscope listening")
        } else {
            Log.d("ERROR", "Gyroscope not listening")
        }
        ForceValue = TextView(this)
        AngleValue = TextView(this)
        x = TextView(this)
        y = TextView(this)
        z = TextView(this)
        accelerometerValueList = LinkedList()
        gyroscopeValueList = LinkedList()
        isPressing = false
    }

    private val accelerometerSensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == accelerometerType) {
                accelerometerZAxis = event.values[2]
                if (isPressing) {
                    accelerometerValueList!!.add(accelerometerZAxis.toInt())
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private val gyroscopeSensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == angleType) {
                gyroscopeZAxis = event.values[2]
                x!!.text = "x:" + String.format("%.2f", event.values[0])
                y!!.text = "y:" + String.format("%.2f", event.values[1])
                z!!.text = "z:" + String.format("%.2f", event.values[2])
                if (isPressing) {
                    gyroscopeValueList!!.add((gyroscopeZAxis * 100).toInt())
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
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

    private val leaveMatch: View.OnClickListener = View.OnClickListener {
        Thread(Runnable {
//            gameView.pause()
            gameClient!!.leaveMatch()
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }).start()
    }

    override fun refresh() {}

    override fun showMessageDialog(message: String) {
        this.runOnUiThread(Runnable {
            txvMessage.text = message
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//            isPressing = true
        }
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            if (inGame!!) {
//                isPressing = false;
//                showValues();
                showMessageDialog("You can do it!!")
                gameView.setBallToStart()
//                gameView.setBallInGame(Ball(100f))
//                gameView.setActionInGame(-10, 10, true)
            }
        }
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            if (inGame!!) {
                gameView.setActionInGame(-40, 40, true)
            }
        }
        return false
    }

    private fun showValues() {
        ForceValue!!.text = Integer.toString(getAccelerometerValuesAverage())
        AngleValue!!.text = Integer.toString(getDifferenceOfTheLastAndFirstGyroscopeValues()) + "°"
        accelerometerValueList = LinkedList()
        gyroscopeValueList = LinkedList()
    }

    private fun getAccelerometerValuesAverage(): Int {
        var s = 0
        for (i in accelerometerValueList!!.indices) {
            s += accelerometerValueList!![i]
            Log.d("AccelerometerValue[$i]", "" + accelerometerValueList!![i])
        }
        return if (accelerometerValueList!!.size > 0) s / accelerometerValueList!!.size else 0
    }

    private fun getGyroscopeValuesAverage(): Int {
        var s = 0
        for (i in gyroscopeValueList!!.indices) {
            s += gyroscopeValueList!![i]!!
            Log.d("GyroscopeValue[$i]", "" + gyroscopeValueList!![i])
        }
        return if (gyroscopeValueList!!.size > 0) s / gyroscopeValueList!!.size else 0
    }

    private fun getDifferenceOfTheLastAndFirstGyroscopeValues(): Int {
        return gyroscopeValueList!![0]!! - gyroscopeValueList!![gyroscopeValueList!!.size - 1]!!
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
    }

    override fun ballFailed() {
        showMessageDialog("You failed!")
        gameClient.sendBallFailed()
    }

    override fun catchBall(ball: BallConverted) {
        showMessageDialog("You can do it!!")
        gameView.setBallInGame(Ball((ball.height * H / 100).toFloat()))
        gameView.setActionInGame(ball.angle, ball.force, false)
    }

    override fun refreshScoreboard(scoreboard: Scoreboard) {
        this.runOnUiThread(Runnable {
            txvScoreboard.text =
                "${scoreboard.setsWonPlayerA}|${scoreboard.pointsWonPlayerA}\n" +
                        "${scoreboard.setsWonPlayerB}|${scoreboard.pointsWonPlayerB}"
            txvMessage.text = "Point, press to start"
        })
    }

    override fun showWinner(winner: User) {
        showMessageDialog("${winner.username} has won!")
        inGame = false
    }

    override fun sendBallToOpponent(ball: Ball) {
        showMessageDialog("Yeah!")
        val force = sqrt(
            ((ball.speedX!! * ball.speedX!!) + (ball.speedY * ball.speedY)).toDouble()
        )
        //Donde pongo el angulo
        val angle = sinh(ball.speedY / force)
        val angleInGradians = angle * 180 / Math.PI
        val height: Float = ball.y * 100 / H
        gameClient.sendBall(force.toInt(), angleInGradians.toInt(), height.toInt())
    }

}