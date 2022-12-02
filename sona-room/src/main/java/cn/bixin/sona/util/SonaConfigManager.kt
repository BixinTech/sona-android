package cn.bixin.sona.util

import android.text.TextUtils
import cn.bixin.sona.api.ApiRegister
import cn.bixin.sona.api.ApiSubscriber
import cn.bixin.sona.api.SonaApi
import cn.bixin.sona.component.ComponentMessage
import cn.bixin.sona.component.connection.ConnectionMessage
import cn.bixin.sona.component.connection.MessageGroupEnum
import cn.bixin.sona.component.connection.MessageItemEnum
import cn.bixin.sona.data.entity.SonaRoomData
import cn.bixin.sona.driver.RoomDriver
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class SonaConfigManager : ApiRegister() {

    companion object {

        const val LOOP_INTERVAL = 5

        @Volatile
        private var instance: SonaConfigManager? = null

        @JvmStatic
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: SonaConfigManager().also { instance = it }
        }
    }

    var disposable: Disposable? = null

    private var roomDriver: RoomDriver? = null

    fun init(roomDriver: RoomDriver) {
        this.roomDriver = roomDriver
    }

    fun startTask() {
        disposable?.dispose()
        disposable = Flowable.interval(LOOP_INTERVAL * 1000L, TimeUnit.MILLISECONDS)
            .subscribe({
                getConfig()
            }, {
                it.printStackTrace()
            })
        disposable?.let {
            register(it)
        }
    }

    fun stopTask() {
        disposable?.dispose()
    }

    fun getConfig() {
        val roomId = roomDriver?.acquire(SonaRoomData::class.java)?.roomId ?: ""
        if (roomId.isEmpty()) {
            SonaLogger.print("SonaConfigManager getConfig roomId is empty")
            return
        }
        register(
            SonaApi.syncConfig(roomId)
                .subscribeWith(object : ApiSubscriber<String>() {
                    override fun onSuccess(streamConfig: String?) {
                        super.onSuccess(streamConfig)
                        dispose()
                        if (!TextUtils.isEmpty(streamConfig)) {
                            val message = ConnectionMessage(
                                group = MessageGroupEnum.ADMIN,
                                message = streamConfig,
                                item = MessageItemEnum.AUDIO_HOT_SWITCH
                            )
                            roomDriver?.dispatchMessage(
                                ComponentMessage.CONNECT_REV_MESSAGE,
                                message
                            )
                        }
                    }

                    override fun onFailure(e: Throwable?) {
                        super.onFailure(e)
                        dispose()
                    }
                })
        )
    }

    fun release() {
        stopTask()
        clear()
        instance = null
    }

}