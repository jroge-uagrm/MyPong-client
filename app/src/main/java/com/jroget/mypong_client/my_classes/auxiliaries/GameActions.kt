package com.jroget.mypong_client.my_classes.auxiliaries

import java.io.Serializable

interface GameActions : Serializable {
    fun refresh()

    fun showMessageDialog(message: String)

    fun catchBall(ball: BallConverted)

    fun refreshScoreboard(scoreboard: Scoreboard)

    fun showWinner(winner: User)
}