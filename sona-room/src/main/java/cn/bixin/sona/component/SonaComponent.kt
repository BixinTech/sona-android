package cn.bixin.sona.component

import cn.bixin.sona.base.pattern.IProvider
import cn.bixin.sona.base.pattern.Observable
import cn.bixin.sona.driver.MessageDispatcher

/**
 * component基类
 * 所有的component都需要继承该类
 * 另外所有的实现类都需要提供无参构造函数
 *
 * @Author luokun
 * @Date 2020/3/25
 */
abstract class SonaComponent : ComponentBasic,
    IProvider {

    private var dispatcher: MessageDispatcher? = null
    private var provider: IProvider? = null
    private var certified = false

    fun isCertified(): Boolean {
        return certified
    }

    fun setCertified(certified: Boolean) {
        this.certified = certified
    }

    fun setMessageDispatcher(dispatcher: MessageDispatcher) {
        this.dispatcher = dispatcher
    }

    fun setProvider(provider: IProvider) {
        this.provider = provider
    }

    override fun dispatchMessage(roomMessage: ComponentMessage, message: Any?) {
        dispatcher?.dispatchMessage(roomMessage, message)
    }

    override fun provide(obj: Any) {
        provider?.provide(obj)
    }

    override fun <T : Any?> acquire(clz: Class<T>?): T? {
        return provider?.acquire(clz)
    }

    override fun <T : Any?> remove(clz: Class<T>?) {
        provider?.remove(clz)
    }

    override fun clear() {
        provider?.clear()
    }

    override fun <T : Any?> observe(clz: Class<T>?): Observable<T>? {
        return provider?.observe(clz)
    }
}