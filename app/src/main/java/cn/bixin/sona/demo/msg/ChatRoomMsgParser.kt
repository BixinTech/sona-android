package cn.bixin.sona.demo.msg

import android.util.SparseArray
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject

object ChatRoomMsgParser {

    private const val TYPE_CHAT_ROOM_TEXT = 901
    private const val TYPE_GIFT_REWARD = 303
    private const val TYPE_GIFT_REWARD_TIPS = 304
    private const val TYPE_SEAT_OPERATE = 203

    private val classMap = SparseArray<Class<out BaseChatRoomMsg>>().apply {
        put(TYPE_CHAT_ROOM_TEXT, ChatRoomTextMsg::class.java)
        put(TYPE_GIFT_REWARD, ChatRoomGiftRewardMsg::class.java)
        put(TYPE_SEAT_OPERATE, ChatRoomSeatOperateMsg::class.java)
        put(TYPE_GIFT_REWARD_TIPS, ChatRoomGiftRewardTipsMsg::class.java)
    }

    fun parseMsg(msg: String): BaseChatRoomMsg? {
        kotlin.runCatching {
            val jsonObject = JSONObject.parseObject(msg)
            val type = jsonObject.getIntValue("msgType")
            val data = jsonObject.getString("data")
            return JSON.parseObject(data, classMap[type])
        }.onFailure {
            it.printStackTrace()
        }
        return null
    }

}