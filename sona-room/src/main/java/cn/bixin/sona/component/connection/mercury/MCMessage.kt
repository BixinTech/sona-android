package cn.bixin.sona.component.connection.mercury


data class MCMessage(
    val sessionId: String,
    val message: String,
    val isAck: Boolean
)