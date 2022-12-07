package com.yupaopao.mercury.library.tunnel.model

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.yupaopao.mercury.library.common.AccessMessage


data class MercuryMessage(
    val origin: AccessMessage
) {
    val body: JSONObject? by lazy {
        if (origin.body?.isNotEmpty() == true) {
            return@lazy JSON.parse(String(origin.body)) as JSONObject
        }
        null
    }

    val id = origin.id
    val code = body?.getInteger("c") ?: -1
    val responseData = body?.getString("d")
    val requestData: String? by lazy {
        if (origin.body?.isNotEmpty() == true) {
            return@lazy String(origin.body)
        }
        null
    }
    val headers = origin.headers
    val cmd = origin.cmd
    fun isSuccess(): Boolean {
        return code == 0
    }
    fun needResponse():Boolean{
        return origin.isTwoWay;
    }

    fun isRequest(): Boolean {
        return origin.isReq
    }

    fun isResponse(): Boolean {
        return !origin.isReq
    }

    fun isHearBeat(): Boolean {
        return origin.isHeartbeat
    }
}