package com.yupaopao.mercury.library.core

import com.alibaba.fastjson.JSONObject

object Config {
    private const val protectFactor = 0.2

    private const val defaultPingTimeout = 25000L
    var pingTimeout = defaultPingTimeout
        set(value) {
            if (value >= defaultPingTimeout * protectFactor) {
                field = value
            }
        }


    private const val defaultConnectionTimeout = 8000L
    var connectionTimeout = defaultConnectionTimeout
        set(value) {
            if (value >= defaultConnectionTimeout * protectFactor) {
                field = value
            }
        }

    private const val defaultPingInterval = 45000L
    var pingInterval = defaultPingInterval
        set(value) {
            if (value >= defaultPingInterval * protectFactor) {
                field = value
            }
        }

    private const val defaultBackgroundPingInterval = 120000L
    var backgroundPingInterval = defaultBackgroundPingInterval
        set(value) {
            if (value >= defaultBackgroundPingInterval * protectFactor) {
                field = value
            }
        }

    var closeBackgroundConnect = false
    var closeMercury = true

    private const val defaultErrorReportInterval = 180000L


    var ip = ""


    fun setIp(ip: String, port: Int) {
        this.ip = ip
        this.port = port
    }

    var port = 0

    var appId: Int = 0
    var handShakeBody: JSONObject = JSONObject().apply {
    }
        set(value) {
            appId = value.getIntValue("a")
            field = value
        }
}