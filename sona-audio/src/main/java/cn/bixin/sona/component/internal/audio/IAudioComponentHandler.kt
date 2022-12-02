package cn.bixin.sona.component.internal.audio

import cn.bixin.sona.component.ComponentMessage

interface IAudioComponentHandler<out C : AudioComponentWrapper> {

    fun getComponent(): C

    fun <T> acquire(clazz: Class<T>): T?

    fun dispatchMessage(roomMessage: ComponentMessage, message: Any?)

    fun assembling()

    fun unAssembling()

}