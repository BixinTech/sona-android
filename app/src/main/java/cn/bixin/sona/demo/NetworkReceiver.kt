package cn.bixin.sona.demo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import java.util.concurrent.ConcurrentHashMap

class NetworkReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
            val conManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = conManager.activeNetworkInfo
            if (networkInfo != null) {
                val isConnected = networkInfo.isConnected
                if (isConnected) {
                    onNetworkReady.forEach {
                        it.value.invoke()
                    }
                }
            }
        }
    }


    companion object {
        private val networkReceiver: NetworkReceiver by lazy {
            NetworkReceiver()
        }
        private var onNetworkReady = ConcurrentHashMap<String, () -> Unit>()

        fun addNetworkListener(key: String, listener: () -> Unit) {
            if (!onNetworkReady.containsKey(key)) {
                onNetworkReady[key] = listener
            }
        }

        fun removeNetworkListener(key: String) {
            if (onNetworkReady.contains(key)) {
                onNetworkReady.remove(key)
            }
        }


        @Synchronized
        fun register(context: Context) {
            val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            context.registerReceiver(
                networkReceiver,
                filter
            )
        }

        fun unregister(context: Context) {
            context.unregisterReceiver(networkReceiver)
        }
    }
}