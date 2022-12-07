package com.yupaopao.mercury.library.tunnel

import android.os.Build
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.yupaopao.mercury.library.Common
import com.yupaopao.mercury.library.common.CommandEnum
import com.yupaopao.mercury.library.common.Header
import com.yupaopao.mercury.library.core.Config
import com.yupaopao.mercury.library.tunnel.MercuryException
import com.yupaopao.mercury.library.tunnel.MercuryResponse
import com.yupaopao.mercury.library.tunnel.Tunnel
import com.yupaopao.mercury.library.tunnel.model.MercuryMessage

class TunnelMessageApi(val tunnel: Tunnel) {

    suspend fun connect(): MercuryResponse {
        return tunnel.localRequest(
            CommandEnum.LOCAL_CONNECT.command,
            null,
            timeout = Config.connectionTimeout,
            twoWay = true
        )
    }

    suspend fun appState(): MercuryResponse {
        val body = JSON.toJSONString(JSONObject().apply {
            val data = JSONObject()
            data["foreground"] = Common.isBackground

            put("type", "appstate")
            put("data", data)
        })
        return tunnel.request(CommandEnum.CLIENT_PUSH.command, body, timeout = 10000, twoWay = false)
    }

    suspend fun login( uid: String): MercuryResponse {
        val body = JSONObject().apply {
            put("p", "2")
            put("sv", Build.VERSION.SDK_INT.toString())
            put("m", Build.MANUFACTURER + " " + Build.MODEL)
            put("d", Common.deviceId ?: "abcjcskksckslclssc,sc")
            put("u", uid)
            put(
                "b", if (Common.isBackground) 1 else {
                    0
                }
            )
            put("t", tunnel.type)
        }
        return tunnel.request(CommandEnum.LOGIN_AUTH.command, body.toJSONString(), timeout = 10000)
    }
}