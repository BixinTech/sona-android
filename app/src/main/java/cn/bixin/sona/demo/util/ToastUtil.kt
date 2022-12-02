package cn.bixin.sona.demo.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.widget.Toast

object ToastUtil {

    private val mainHandler = Handler(Looper.getMainLooper())

    fun showToast(context: Context, msg: String) {
        if (TextUtils.isEmpty(msg)) return
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        } else {
            mainHandler.post {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

}