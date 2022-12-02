package cn.bixin.sona.plugin.entity

open class PluginEntity(var uid: String?, var roomId: String?, var reasonCode: Int? = null) {
    constructor(uid: String?, roomId: String?) : this(uid, roomId, null)
}