package cn.bixin.sona.api

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 *
 * @Author luokun
 * @Date 2019-12-30
 */

open class ApiRegister {

    private val mCompositeDisposable = CompositeDisposable()

    fun unRegister(disposable: Disposable) {
        this.mCompositeDisposable.remove(disposable)
    }

    fun register(disposable: Disposable) {
        this.mCompositeDisposable.add(disposable)
    }

    fun clear() {
        this.mCompositeDisposable.clear()
    }
}