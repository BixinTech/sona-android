package cn.bixin.sona.component.connection.mercury

import cn.bixin.sona.component.connection.IConnection
import cn.bixin.sona.component.connection.MessageBridge
import cn.bixin.sona.component.connection.MessageDispatcher
import cn.bixin.sona.data.entity.SonaRoomData
import cn.bixin.sona.driver.RoomDriver


abstract class MercuryConnection(roomDriver: RoomDriver) : IConnection(roomDriver),
    MessageBridge<MCMessage> {

    override fun createDispatcher(): MessageDispatcher? {
        acquire(SonaRoomData::class.java)?.roomId?.let {
            return MercuryMessageDispatcher(it, this, this, connectionHandlerThread)
        }
        return null
    }

    override fun filterMessage(message: MCMessage?): Boolean {
        if (message?.sessionId == getImRoomId()) {
            return true
        }
        return false
    }

    protected fun getImRoomId(): String {
        return acquire(SonaRoomData::class.java)?.roomId ?: ""
    }

}
