package cn.bixin.sona.demo

import android.util.Log
import cn.bixin.sona.*
import cn.bixin.sona.component.audio.AudioMixBuffer
import cn.bixin.sona.demo.msg.BaseChatRoomMsg
import cn.bixin.sona.demo.msg.ChatRoomMsgParser
import cn.bixin.sona.demo.util.ToastUtil
import cn.bixin.sona.plugin.AdminPlugin
import cn.bixin.sona.plugin.AudioPlugin
import cn.bixin.sona.plugin.ConnectPlugin
import cn.bixin.sona.plugin.config.AudioConfig
import cn.bixin.sona.plugin.entity.*
import cn.bixin.sona.plugin.observer.AdminPluginObserver
import cn.bixin.sona.plugin.observer.AudioPluginObserver
import cn.bixin.sona.plugin.observer.ConnectPluginObserver
import org.greenrobot.eventbus.EventBus


object ChatRoomManager {
    private val TAG = "ChatRoomManager"

    private var sonaRoom: SonaRoom? = null

    var roomId = ""
    var myUid = ""

    var onReceiveMsg: ((msg: BaseChatRoomMsg) -> Unit)? = null
    var onSoundLevelInfo: ((soundLevelInfoList: MutableList<SoundLevelInfoEntity>?) -> Unit)? = null

    fun initSonaRoom() {
        sonaRoom = SonaRoom()

        sonaRoom?.addPlugin(AdminPlugin::class.java)?.observe(object : AdminPluginObserver {
            override fun onAdministratorChange(admin: Int, entity: PluginEntity?) {
                Log.i(TAG, "onAdministratorChange: $admin  ${entity?.uid}")
            }

            override fun onUserBlockChange(block: Int, entity: PluginEntity?) {
                Log.i(TAG, "onUserBlockChange: ${entity?.uid}")
            }

            override fun onUserMuteChange(mute: Int, entity: MuteEntity?) {
                Log.i(TAG, "onUserMuteChange: ${entity?.uid}")
            }

            override fun onUserKick(entity: PluginEntity?) {
                Log.i(TAG, "onUserKick: ${entity?.uid}")
            }

        })
        sonaRoom?.addPlugin(ConnectPlugin::class.java)?.observe(object : ConnectPluginObserver {
            override fun onReceiveMessage(messageEntity: MessageEntity?) {
                messageEntity?.msg?.let {
                    ChatRoomMsgParser.parseMsg(it)?.let { baseChatRoomMsg ->
                        onReceiveMsg?.invoke(baseChatRoomMsg)
                    }
                }
            }

            override fun onDisconnect() {
                Log.i(TAG, "ConnectPlugin onDisconnect")
            }

            override fun onReconnect() {
                Log.i(TAG, "ConnectPlugin onReconnect")
            }

            override fun onConnectError(code: Int) {
                ToastUtil.showToast(
                    MainApplication.getContext(),
                    "ConnectPlugin onConnectError code = $code"
                )
            }

        })

        val config = AudioConfig(true, 500)
        sonaRoom?.addPlugin(AudioPlugin::class.java)?.config(config)
            ?.observe(object : AudioPluginObserver {
                override fun onSpeakerSilent(silent: Int, entity: SpeakEntity?) {

                }

                override fun onSpeakerSpeaking(speak: Int, entity: SpeakEntity?) {

                }

                override fun onAudioFrameDetected(audioMixBuffer: AudioMixBuffer?) {

                }

                override fun onDisconnect() {
                    ToastUtil.showToast(MainApplication.getContext(), "AudioPlugin onDisconnect")
                }

                override fun onReconnect() {
                    ToastUtil.showToast(MainApplication.getContext(), "AudioPlugin onReconnect")
                }

                override fun onAudioError(code: Int) {
                    ToastUtil.showToast(
                        MainApplication.getContext(),
                        "AudioPlugin onAudioError code = $code"
                    )
                }

                override fun onSoundLevelInfo(soundLevelInfoList: MutableList<SoundLevelInfoEntity>?) {
                    onSoundLevelInfo?.invoke(soundLevelInfoList)
                }

            })

        sonaRoom?.observe { roomEvent, roomEntity ->
            when (roomEvent) {
                RoomEvent.USER_ENTER -> {
                    Log.i(TAG, "initSonaRoom: USER_ENTER = ${roomEntity.uid}")
                }
                RoomEvent.USER_LEAVE -> {
                    Log.i(TAG, "initSonaRoom: USER_LEAVE = ${roomEntity.uid}")
                }
                RoomEvent.ROOM_CLOSE -> {
                    Log.i(TAG, "initSonaRoom: ROOM_CLOSE")
                }
            }
        }

        sonaRoom?.observeError(object : SonaRoomErrorObserver {
            override fun onError(error: Int) {
                Log.i(TAG, "observeError onError: $error")
            }
        })
    }

    fun unInitSonaRoom() {
        roomId = ""
        sonaRoom?.let {
            it.getPlugin(ConnectPlugin::class.java)?.observe(null)
            it.getPlugin(AdminPlugin::class.java)?.observe(null)
            it.getPlugin(AudioPlugin::class.java)?.observe(null)
            it.observe(null)
            it.observeError(null)
            it.leaveRoom(null)
            onReceiveMsg = null
            onSoundLevelInfo = null
            sonaRoom = null
        }
    }

    fun createRoom(roomTitle: String, success: () -> Unit, fail: (reason: String) -> Unit) {
        sonaRoom?.createRoom(roomTitle, SonaRoomProduct.CHATROOM, "", null, object : SonaRoomCallback {
            override fun onSuccess(roomId: String?) {
                this@ChatRoomManager.roomId = roomId ?: ""
                success.invoke()
            }

            override fun onFailed(code: Int, reason: String?) {
                fail.invoke(reason ?: "")
            }
        })
    }

    fun enterRoom(roomId: String, success: () -> Unit, fail: (reason: String) -> Unit) {
        sonaRoom?.enterRoom(roomId, SonaRoomProduct.CHATROOM, "", null, object : SonaRoomCallback {
            override fun onSuccess(roomId: String?) {
                success.invoke()
            }

            override fun onFailed(code: Int, reason: String?) {
                fail.invoke(reason ?: "")
            }
        })
    }

    fun connectPlugin(): ConnectPlugin? {
        return sonaRoom?.getPlugin(ConnectPlugin::class.java)
    }

    fun audioPlugin(): AudioPlugin? {
        return sonaRoom?.getPlugin(AudioPlugin::class.java)
    }

    fun adminPlugin(): AdminPlugin? {
        return sonaRoom?.getPlugin(AdminPlugin::class.java)
    }

}