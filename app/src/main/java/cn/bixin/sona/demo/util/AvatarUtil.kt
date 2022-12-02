package cn.bixin.sona.demo.util

import cn.bixin.sona.demo.R
import kotlin.math.roundToInt

object AvatarUtil {

    fun getSeatAvatar(index: Int): Int {
        return when (index) {
            0 -> R.mipmap.chatroom_img_avatar_1
            1 -> R.mipmap.chatroom_img_avatar_2
            2 -> R.mipmap.chatroom_img_avatar_3
            3 -> R.mipmap.chatroom_img_avatar_4
            4 -> R.mipmap.chatroom_img_avatar_5
            5 -> R.mipmap.chatroom_img_avatar_6
            else -> R.mipmap.chatroom_img_avatar_1
        }
    }

    fun getRandomAvatar(): Int {
        val random = (Math.random() * 10).roundToInt()
        return getSeatAvatar(random % 6)
    }
}