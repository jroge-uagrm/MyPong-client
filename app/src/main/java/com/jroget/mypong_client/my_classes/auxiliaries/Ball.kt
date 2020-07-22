package com.jroget.mypong_client.my_classes.auxiliaries

class Ball(
    var y: Float
) {
    var speedX: Float = 0f
    var speedY: Float = 0f
    var x: Float? = 1000f
    var radius: Float? = 65f
    var bounces: Int? = 0
    var goingToOpponent: Boolean? = false
    var falling: Boolean? = true
    var globalSpeed: Float? = 0.5f

    fun move() {
        if (goingToOpponent!!) {
            x = x?.plus(speedX!!)
        } else {
            x = x?.minus(speedX!!)
        }
        if (falling!!) {
            speedY = speedY?.plus(globalSpeed!! + 0.2f)
            y = y?.plus(speedY!!)
        } else {
            if (speedY!! > 0) {
                speedY = speedY?.minus(globalSpeed!! + 0.5f)
                y = y?.minus(speedY!!)
            } else {
                falling = true
            }
        }
    }

    fun getMinY(): Float {
        return y!! + radius!!
    }

    fun getMaxX(): Float {
        return x!! + radius!!
    }

    fun getMinX(): Float {
        return x!! - radius!!
    }

    fun getMaxY(): Float {
        return y!! - radius!!
    }

}