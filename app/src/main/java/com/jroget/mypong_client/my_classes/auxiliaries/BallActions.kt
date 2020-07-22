package com.jroget.mypong_client.my_classes.auxiliaries

interface BallActions {
    fun ballFailed()
    fun sendBallToOpponent(ball: Ball)
}