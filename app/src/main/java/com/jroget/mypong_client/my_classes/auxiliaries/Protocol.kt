package com.jroget.mypong_client.my_classes.auxiliaries

import java.io.Serializable

class Protocol(
    val action: String,
    val content: String
) : Object(), Serializable {
}