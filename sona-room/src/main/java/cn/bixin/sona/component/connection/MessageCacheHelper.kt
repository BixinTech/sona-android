package cn.bixin.sona.component.connection

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import java.util.*

class MessageCacheHelper(
    private var maxSize: Int,
    private var checkDuration: Long,
    private var timeoutDuration: Long, looper: Looper?
) {

    private val messageList = LinkedList<CacheMessage>()
    private val messageSet = HashSet<String>()

    private var handler: Handler? = null

    private var runnable: Runnable = Runnable {
        removeTimeoutMessage()
        postMessage()
    }

    init {
        if (checkDuration > 0) {
            looper?.let {
                handler = Handler(it)
            }
            postMessage()
        }
    }

    fun addMessage(messageId: String?) {
        if (TextUtils.isEmpty(messageId)) {
            return
        }
        if (messageList.size >= maxSize) {
            removeTimeoutMessage()
        }
        if (messageList.size >= maxSize) {
            val pollMessage = messageList.poll()
            messageSet.remove(pollMessage?.messageId ?: "")
        }
        messageList.offer(CacheMessage(messageId!!, System.currentTimeMillis()))
        messageSet.add(messageId)
    }

    fun hasMessage(messageId: String?): Boolean {
        if (TextUtils.isEmpty(messageId)) return false
        if (messageSet.contains(messageId)) {
            return true
        }
        return false
    }

    private fun removeTimeoutMessage() {
        if (messageList.isEmpty().not()) {
            val iterator = messageList.iterator()
            while (iterator.hasNext()) {
                val message = iterator.next()
                if (System.currentTimeMillis() - message.cacheTime >= timeoutDuration) {
                    iterator.remove()
                    messageSet.remove(message.messageId)
                } else {
                    break
                }
            }
        }
    }

    private fun postMessage() {
        handler?.postDelayed(runnable, checkDuration)
    }

    fun release() {
        handler?.removeCallbacksAndMessages(null)
    }

}