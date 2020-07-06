package com.jroget.mypong_client.my_classes.auxiliaries

interface GameActions {
    fun refresh()

    fun showMessageDialog(message: String)

    fun showConfirmDialog(message: String): Int
}