package cn.bixin.sona.demo.util

import android.text.TextUtils
import cn.bixin.sona.demo.R
import kotlin.math.roundToInt

object AvatarUtil {

    private val avatarMap = hashMapOf<String, Int>()

    private fun getAvatar(index: Int): Int {
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

    private fun getRandomAvatar(): Int {
        val random = (Math.random() * 10).roundToInt()
        return getAvatar(random % 6)
    }

    fun getUserAvatar(uid: String): Int {
        if (avatarMap[uid] == null || avatarMap[uid] == 0) {
            avatarMap[uid] = getRandomAvatar()
        }
        return avatarMap[uid] ?: 0
    }
}