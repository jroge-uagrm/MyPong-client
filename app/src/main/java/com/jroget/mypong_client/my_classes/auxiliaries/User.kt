package com.jroget.mypong_client.my_classes.auxiliaries

import java.io.Serializable

class User(
    val username: String,
    val password: String
) : Serializable {
    var key: String? = null
    var matchId: String? = null
}