package cn.bixin.sona.driver

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import cn.bixin.sona.SonaRoomProduct
import cn.bixin.sona.base.Sona
import cn.bixin.sona.base.pattern.IProvider
import cn.bixin.sona.base.pattern.Observable
import cn.bixin.sona.base.pattern.Provider
import cn.bixin.sona.component.ComponentMessage
import cn.bixin.sona.component.audio.AudioComponent
import cn.bixin.sona.component.connection.IConnection
import cn.bixin.sona.data.StreamTypeEnum
import cn.bixin.sona.data.entity.SonaRoomData
import cn.bixin.sona.data.entity.UserData
import cn.bixin.sona.delegate.SonaRoomDelegate
import cn.bixin.sona.delegate.observer.RoomEnterObserver
import cn.bixin.sona.plugin.entity.PluginEnum
import cn.bixin.sona.report.ReportCode
import cn.bixin.sona.report.SonaReport
import cn.bixin.sona.util.SonaConfigManager
import cn.bixin.sona.util.SonaLogger
import java.util.concurrent.atomic.AtomicInteger

class RoomDriver(private val roomDelegate: SonaRoomDelegate) : IProvider, MessageDispatcher {
    private val uiHandler = Handler(Looper.getMainLooper())
    private val componentLoadSuccessMonitor = AtomicInteger(0)
    private val componentLoadMonitor = AtomicInteger(0)
    private val componentLoadResultMonitor = AtomicInteger(0)
    var connection: IConnection? = null
    var audio: AudioComponent? = null

    var serverCertified: Boolean = false

    val provider by lazy { Provider() }

    override fun provide(any: Any) {
        provider.provide(any)
    }

    override fun <T : Any?> acquire(clz: Class<T>?): T? {
        return provider.acquire(clz)
    }

    override fun <T : Any?> remove(clz: Class<T>?) {
        provider.remove(clz)
    }

    override fun <T : Any?> observe(clz: Class<T>?): Observable<T> {
        return provider.observe(clz)
    }

    override fun clear() {
        provider.clear()
    }

    override fun dispatchMessage(msgType: ComponentMessage, message: Any?) {
        // 组件初始化失败
        if (msgType == ComponentMessage.CONNECT_INIT_FAIL ||
            msgType == ComponentMessage.AUDIO_INIT_FAIL
        ) {
            if (componentLoadResultMonitor.get() == 1) {
                // 已经走过一趟加载流程，说明是在重连或者热切
                setComponentCertified(msgType)
                observe(SonaRoomData::class.java).update { t ->
                    t?.let { it.roomCertified = false }
                    t
                }
                roomDelegate.dispatchMessage(msgType, message)
                return
            }

            setComponentCertified(msgType)

            observe(SonaRoomData::class.java).update { t ->
                t?.let { it.roomCertified = false }
                t
            }

            val count = componentLoadMonitor.incrementAndGet()
            if (count == loadComponentCount()) {
                // 组件初始化失败
                reportLoadComponentFailed()
                componentLoadResultMonitor.set(1)
                roomDelegate.dispatchMessage(
                    ComponentMessage.COMPONENT_INIT_FAIL,
                    message
                )
            }
            return
        }

        // 组件初始化成功
        if (msgType == ComponentMessage.CONNECT_INIT_SUCCESS ||
            msgType == ComponentMessage.AUDIO_INIT_SUCCESS
        ) {
            if (componentLoadResultMonitor.get() == 1) {
                // 已经走过一趟加载流程，说明是在重连或者热切
                setComponentCertified(msgType)
                // 需要判断总开关的情况
                if (allComponentCertified()) {
                    observe(SonaRoomData::class.java).update { t ->
                        t?.let { it.roomCertified = true }
                        t
                    }
                }
                roomDelegate.dispatchMessage(msgType, message)
                return
            }

            setComponentCertified(msgType)

            componentLoadMonitor.incrementAndGet()

            val count = componentLoadSuccessMonitor.incrementAndGet()
            if (count == loadComponentCount()) {
                observe(SonaRoomData::class.java).update { t ->
                    t?.let { it.roomCertified = true }
                    t
                }
                componentLoadResultMonitor.set(1)
                roomDelegate.dispatchMessage(
                    ComponentMessage.COMPONENT_INIT_SUCCESS,
                    message
                )
            } else if (loadComponentCount() == componentLoadMonitor.get()) {
                // 加载的组件 与 需要加载的组件数量相等，说明已经加载失败
                observe(SonaRoomData::class.java).update { t ->
                    t?.let { it.roomCertified = false }
                    t
                }
                // 组件初始化失败
                reportLoadComponentFailed()
                componentLoadResultMonitor.set(1)
                roomDelegate.dispatchMessage(
                    ComponentMessage.COMPONENT_INIT_FAIL,
                    message
                )
            }

            return
        }

        roomDelegate.dispatchMessage(msgType, message)
    }

    fun runUiThread(runnable: Runnable) {
        uiHandler.post(runnable)
    }

    /**
     * 组装功能组件
     */
    fun assembling() {
        SonaConfigManager.getInstance().init(this)
        startReport()
        loadUserInfo()
        loadComponents()
        connection?.assembling()
        audio?.assembling()
    }

    /**
     * 卸载所有组件
     */
    fun unAssembling() {
        connection?.unAssembling()
        audio?.unAssembling()
        connection = null
        audio = null
        componentLoadMonitor.set(0)
        componentLoadSuccessMonitor.set(0)
        componentLoadResultMonitor.set(0)
        uiHandler.removeCallbacksAndMessages(null)
        remove(UserData::class.java)

        stopReport()
        SonaConfigManager.getInstance().release()

        observe(SonaRoomData::class.java).update { t ->
            t?.let { it.roomCertified = false }
            t
        }
    }

    /**
     * 卸载指定组件
     */
    fun unAssembling(component: ComponentType) {
        when (component) {
            ComponentType.IM -> {
                connection?.unAssembling()
                connection = null
            }
            ComponentType.AUDIO -> {
                audio?.unAssembling()
                audio = null
            }
        }
    }

    /**
     * 装载指定组件
     */
    fun assembling(component: ComponentType) {
        when (component) {
            ComponentType.IM -> {
                provider.acquire(SonaRoomData::class.java)?.imInfo?.let {
                    roomDelegate.pluginLoaded(PluginEnum.CONNECT).let { loaded ->
                        if (loaded) {
                            val factory = ComponentProducer.getFactory(ComponentType.IM)
                            connection = factory?.createComponent(this) as? IConnection
                        }
                    }
                }
                connection?.assembling()
            }
            ComponentType.AUDIO -> {
                provider.acquire(SonaRoomData::class.java)?.streamInfo?.let {
                    if (it.type == StreamTypeEnum.AUDIO.streamName) {
                        roomDelegate.pluginLoaded(PluginEnum.AUDIO).let { loaded ->
                            if (loaded) {
                                val factory = ComponentProducer.getFactory(ComponentType.AUDIO)
                                audio = factory?.createComponent(this) as? AudioComponent
                            }
                        }
                    }
                }
                audio?.assembling()
            }
        }
    }

    /**
     * 房间是否合法
     */
    fun roomCertified(): Boolean {
        return provider.acquire(SonaRoomData::class.java)?.roomCertified == true
    }

    /**
     * 组件是否合法
     */
    fun componentCertified(component: ComponentType): Boolean {
        return when (component) {
            ComponentType.IM -> {
                connection?.isCertified() ?: false
            }
            ComponentType.AUDIO -> {
                audio?.isCertified() ?: false
            }
        }
    }

    /**
     * 组件是否支持
     */
    fun componentSupport(component: ComponentType): Boolean {
        return when (component) {
            ComponentType.IM -> {
                connection != null
            }
            ComponentType.AUDIO -> {
                audio != null
            }
        }
    }

    /**
     * 设置component加载状态
     */
    private fun setComponentCertified(msgType: ComponentMessage) {
        when (msgType) {
            ComponentMessage.CONNECT_INIT_SUCCESS -> connection?.setCertified(true)
            ComponentMessage.CONNECT_INIT_FAIL -> connection?.setCertified(false)
            ComponentMessage.AUDIO_INIT_SUCCESS -> audio?.setCertified(true)
            ComponentMessage.AUDIO_INIT_FAIL -> audio?.setCertified(false)
        }
    }

    /**
     * 组件是否完全加载
     */
    private fun allComponentCertified(): Boolean {
        audio?.run {
            if (!this.isCertified()) {
                return false
            }
        }
        connection?.run {
            if (!this.isCertified()) {
                return false
            }
        }
        return true
    }

    /**
     * 加载组件的数量
     */
    private fun loadComponentCount(): Int {
        var count = 0
        connection?.let { count++ }
        audio?.let { count++ }
        return count
    }

    /**
     * 按需加载组件
     */
    private fun loadComponents() {

        provider.acquire(SonaRoomData::class.java)?.imInfo?.let {
            roomDelegate.pluginLoaded(PluginEnum.CONNECT).let { loaded ->
                if (loaded) {
                    val factory = ComponentProducer.getFactory(ComponentType.IM)
                    connection = factory?.createComponent(this) as? IConnection
                }
            }
        }
        provider.acquire(SonaRoomData::class.java)?.streamInfo?.let {
            when (it.type) {
                StreamTypeEnum.AUDIO.streamName -> {
                    roomDelegate.pluginLoaded(PluginEnum.AUDIO).let { loaded ->
                        if (loaded) {
                            val factory = ComponentProducer.getFactory(ComponentType.AUDIO)
                            audio = factory?.createComponent(this) as? AudioComponent
                        }
                    }
                }
            }
        }

        if (connection == null && audio == null) {
            provider.observe(SonaRoomData::class.java).update { t ->
                t?.let { it.roomCertified = true }
                t
            }
            componentLoadResultMonitor.set(1)
            roomDelegate.dispatchMessage(ComponentMessage.COMPONENT_INIT_SUCCESS, null)
        }
    }

    private fun loadUserInfo() {
        val userData = UserData(Sona.getUid())
        if (TextUtils.isEmpty(userData.uid)) {
            backup()
        } else {
            provide(userData)
            fillReport("uid", userData.uid ?: "")
        }
    }

    private fun backup() {
        fillReport("uid", "")
    }

    /**
     * 开启上报
     */
    private fun startReport() {
        SonaReport.start()
        // 这里做下区分
        provider.acquire(SonaRoomData::class.java)?.productCode?.let {
            if (it == SonaRoomProduct.CHATROOM.name) {
                fillReport("productCode", "ChatRoom")
            } else {
                fillReport("productCode", it)
            }
            fillReport("snProductCode", it)
        }

        fillReport(
            "supplier",
            provider.acquire(SonaRoomData::class.java)?.streamInfo?.supplier ?: ""
        )

        fillReport("roomId", provider.acquire(SonaRoomData::class.java)?.roomId ?: "")
    }

    /**
     * 停止上报
     */
    private fun stopReport() {
        SonaReport.stop()
    }

    /**
     * 设置上报的基础属性
     */
    fun fillReport(key: String, value: String) {
        SonaReport.addAttribute(key, value)
    }

    /**
     * 上报加载失败
     */
    private fun reportLoadComponentFailed() {
        val enter = acquire(RoomEnterObserver::class.java)
        SonaLogger.log(
            content = if (enter != null) "进入房间失败<1>" else "进入房间失败<2>",
            code = ReportCode.ENTER_ROOM_FAIL_CODE
        )
    }
}