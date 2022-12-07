package com.yupaopao.mercury.library

import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.yupaopao.mercury.library.chatroom.ChatRoomInterface

object Common {
    const val TUNNEL_CHAT_ROOM = 2

    var isBackground = false
        set(value) {
            field = value
          ChatRoomInterface.instance.forEach {
              it.value.appState()
          }
        }
    var deviceId: String? = null

    var logCallback: ((type: Int, log: String) -> Unit)? = null

    var exceptionCallback: ((Int, ArrayList<Triple<Long, String, Throwable>>) -> Unit)? = null

}