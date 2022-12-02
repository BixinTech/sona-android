package cn.bixin.sona.component.connection.mercury

import cn.bixin.sona.component.ComponentCallback
import cn.bixin.sona.component.connection.SessionTypeEnum
import cn.bixin.sona.driver.RoomDriver

class TeamMercuryConnection(roomDriver: RoomDriver) : MercuryConnection(roomDriver) {

    override fun assembling() {
        super.assembling()
    }

    override fun unAssembling() {
        super.unAssembling()
    }

    override fun sendMessage(message: String, callback: ComponentCallback?) {

    }

    override fun sendMessage(message: String, isAck: Boolean, callback: ComponentCallback?) {
        sendMessage(message, callback)
    }

    override fun getSessionType(): SessionTypeEnum {
        return SessionTypeEnum.TEAM
    }
}
