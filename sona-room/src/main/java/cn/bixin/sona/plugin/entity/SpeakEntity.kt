package cn.bixin.sona.plugin.entity

class SpeakEntity(uid: String?, roomId: String?, var streamId: String) : PluginEntity(uid, roomId) {
    var userName: String? = ""

    constructor(uid: String?, userName: String?, roomId: String?, streamId: String) : this(
        uid,
        roomId,
        streamId
    ) {
        this.userName = userName
    }
}