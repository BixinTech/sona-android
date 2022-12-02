package cn.bixin.sona.data.entity

import java.io.Serializable

class SonaConfigInfo(
    var roomId: String?,
    var streamConfig: RoomInfo.StreamConfig?
) : Serializable