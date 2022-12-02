package cn.bixin.sona.delegate.helper

import cn.bixin.sona.base.Sona
import cn.bixin.sona.component.connection.MessageItemEnum
import cn.bixin.sona.driver.RoomDriver
import cn.bixin.sona.plugin.PluginCallback
import cn.bixin.sona.plugin.internal.ConnectMessage
import cn.bixin.sona.util.MsgIdCreateHelper
import cn.bixin.sona.util.SonaConstant
import cn.bixin.sona.util.SonaLogger
import com.alibaba.fastjson.JSONObject

class ConnectPresenter(val roomDriver: RoomDriver, var sender: ConnectSender) {

    fun sendMessage(
        connectMessage: ConnectMessage,
        needToSave: Int,
        roomId: String,
        pluginCallback: PluginCallback?
    ) {
        when (connectMessage.sonaType) {
            ConnectMessage.CUSTOM -> {
                sendMessage(
                    connectMessage.msgType,
                    connectMessage.message,
                    roomId,
                    needToSave,
                    pluginCallback
                )
            }
            else -> {
                SonaLogger.log(
                    content = "can not sendMessage ${connectMessage.sonaType}"
                )
            }
        }
    }

    private fun sendMessage(
        msgType: Int,
        message: String?,
        roomId: String,
        needToSave: Int,
        pluginCallback: PluginCallback?
    ) {
        val data = JSONObject()
        data["content"] = message
        data["msgType"] = msgType
        data["msgFormat"] = MessageItemEnum.CUSTOM.value
        data["roomId"] = roomId
        data["uid"] = Sona.getUid()
        data["priority"] = 1
        data["messageId"] = MsgIdCreateHelper.createMessageId()
        if (needToSave != SonaConstant.UNNEEDED_TO_SAVE_MESSAGE) {
            data["needToSave"] = needToSave
        }
        sender.send(data.toJSONString()) { code, msg ->
            if (code == 0) {
                pluginCallback?.onSuccess()
            } else {
                pluginCallback?.onFailure(code, msg)
            }
        }
    }
}