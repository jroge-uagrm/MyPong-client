package com.jroget.mypong_client.my_classes.auxiliaries

import java.io.Serializable
import java.util.*

class ContainerObject(
    val origin: String,
    val body: Object,
    val destinations: Array<String>
) : Serializable {
}