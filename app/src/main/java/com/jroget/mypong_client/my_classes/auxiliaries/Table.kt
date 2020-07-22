package com.jroget.mypong_client.my_classes.auxiliaries

class Table(
    private val w: Int,
    private val h: Int
) {
    var x: Float? = 200f
    var y: Float? = h.toFloat() - h / 5
    var length: Float? = w.toFloat() - x!!.toFloat()
    var height: Float? = 20f
    var redX: Float? = w.toFloat() - 100
    var redY: Float? = h.toFloat() / 2
    var redHeight: Float? = y!! - redY!!
}