package cn.bixin.sona.data.entity

import java.io.Serializable

class RoomInfo : Serializable {
    var roomId: String? = ""
    var guestUid: String? = ""
    var addr: String? = ""
    var nickname: String? = ""
    var productConfig: ProductConfig? = null
    var extra: Map<String, Any?>? = null

    class ProductConfig : Serializable {
        var productCode: String? = null
        var productCodeAlias: String? = null
        var imConfig: IMConfig? = null
        var streamConfig: StreamConfig? = null
    }

    class IMConfig : Serializable {
        var type: String? = ""
        var module: String = ""
        var clientQueueSize: Int? = 0 // 客户端消息队列大小
        var messageExpireTime: Long? = 0 // 消息过期时间 （毫秒）
        var arrivalMessageSwitch: Boolean? = false // 消息必达开发
        var imSendType: Int? = null //2 长连，,1 短连
    }

    class StreamConfig : Serializable {
        var supplier: String = ""
        var type: String = ""
        var pushMode: String = ""
        var pullMode: String = ""
        var streamList: List<String>? = null
        var streamUrl: String? = null
        var streamId: String? = null
        var audioToken: String? = null
        var streamRoomId: HashMap<String, String>? = null
        var switchSpeaker: String? = "0"
        var appInfo: AppInfo? = null
        var playerType: Int = 1 // 1： 三方  2：自建
        var bitrate: Int = 64000
        var fromSyncConfig = false
    }
}