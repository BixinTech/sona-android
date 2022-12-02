package cn.bixin.sona.demo.msg

import com.alibaba.fastjson.JSON
import java.io.Serializable


open class BaseChatRoomMsg : Serializable {
    var roomId: String? = null

    fun toJsonString(): String {
        return JSON.toJSONString(this)
    }

}