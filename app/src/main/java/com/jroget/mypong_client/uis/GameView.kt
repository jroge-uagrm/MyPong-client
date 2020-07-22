package com.jroget.mypong_client.uis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.jroget.mypong_client.my_classes.auxiliaries.Ball
import com.jroget.mypong_client.my_classes.auxiliaries.BallActions
import com.jroget.mypong_client.my_classes.auxiliaries.Table
import kotlin.math.cos
import kotlin.math.sin

open class GameView(
    context: Context,
    private val W: Int,
    private val H: Int,
    private val ballActions: BallActions
) : SurfaceView(context), Runnable {

    private var thread: Thread? = null
    private lateinit var canvas: Canvas
    private var surfaceHolder: SurfaceHolder = holder
    private var paint: Paint = Paint()
    private var ball: Ball
    private var table: Table
    private var inGame: Boolean? = false

    init {
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.strokeWidth = 10F
        table = Table(W, H)
        ball = Ball(20f)
        Log.d("W-H", "$width-$height")
    }

    override fun run() {
        while (ballIsInGame()) {
            if (!surfaceHolder.surface.isValid) {
                continue
            }
            try {
                canvas = surfaceHolder.lockCanvas()
            } catch (e: Exception) {
                break
            }
            canvas.drawColor(Color.BLACK)
            drawTable()
            if (inGame!!) {
                drawBall()
                moveBall()
            }
            surfaceHolder.unlockCanvasAndPost(canvas)
        }
        if (!ball.goingToOpponent!! || ball.bounces!! == 2) {
//            Log.d("FAIL", "FAIL")
            ballActions.ballFailed()
        } else {
            ballActions.sendBallToOpponent(ball)
        }
    }

    fun setBallInGame(newBall: Ball) {
        thread = Thread(this)
        thread!!.start()
        ball = newBall
        ball.bounces = 0
        ball.radius = W.toFloat() / 27
        ball.x = table.redX!! - ball.radius!!
        ball.globalSpeed = ((W / H) * 0.5f) / (1780 / 1080)
        inGame = true
    }


    fun setBallToStart() {
        var newBall = Ball(100f)
        thread = Thread(this)
        thread!!.start()
        ball = newBall
        ball.bounces = 0
        ball.radius = W.toFloat() / 27
        ball.x = table.x!! + 1
        ball.globalSpeed = ((W / H) * 0.5f) / (1780 / 1080)
        inGame = true
    }

    fun setActionInGame(angle: Int, force: Int, goingToOpponent: Boolean) {
        val angleInRadians: Double = ((angle) * Math.PI) / 180
        var forceConverted = W * force / 1780
        ball.speedX = cos(angleInRadians).toFloat() * forceConverted
        ball.speedY = sin(angleInRadians).toFloat() * forceConverted
        if (angle < 90) {
            ball.falling = true
        }
        ball.goingToOpponent = goingToOpponent
    }

    private fun ballIsInGame(): Boolean {
        if (ball.getMinY() >= table.y!! && ball.x!! >= table.x!!) {
            ball.y = table.y!! - ball.radius!! - 1
        }
        return ball.bounces!! < 2 &&
                ball.x!! > 10 &&
                ball.getMinY() < table.y!! &&
                ball.getMinX()!! < table.redX!!
    }

    private fun drawTable() {
        paint.color = Color.GREEN
        canvas.drawRect(
            table.x!!,
            table.y!!,
            table.length!! + table.x!!,
            table.y!! + table.height!!,
            paint
        )
        paint.color = Color.WHITE
        canvas.drawLine(
            table.redX!!,
            table.redY!!,
            table.redX!!,
            table.redY!! + table.redHeight!!,
            paint
        )
    }

    private fun moveBall() {
        ball.move()
        if (ball.getMinY() >= table.y!! && ball.x!! >= table.x!!) {
            Log.d("BOUNCE", "YES")
            ball.falling = false
            ball.bounces = ball.bounces?.plus(1)
            if (ball.goingToOpponent!!) {
                Log.d("BOUNCE", "GOING OPO")
                ball.bounces = ball.bounces?.plus(1)
//                ballActions.ballFailed()
            } else if (ball.bounces == 2) {
//                ballActions.ballFailed()
            }
        } else if (ball.goingToOpponent!! &&
            ball.getMaxX()!! >= table.redX!! &&
            ball.getMinY() > table.redY!!
        ) {
            ball.bounces = 2
        }
    }

    private fun drawBall() {
        canvas.drawCircle(
            ball.x!!,
            ball.y!!,
            ball.radius!!,
            paint
        )
    }

    fun pause() {
        while (true) {
            try {
//                thread!!.join()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            break
        }
        thread = null
    }

    fun resume() {
        thread = Thread(this);
        thread!!.start();
    }

}
