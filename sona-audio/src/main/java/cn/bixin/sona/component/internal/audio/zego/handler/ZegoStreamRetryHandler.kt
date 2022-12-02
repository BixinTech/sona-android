package cn.bixin.sona.component.internal.audio.zego.handler

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import cn.bixin.sona.component.ComponentMessage
import cn.bixin.sona.component.audio.AudioError
import cn.bixin.sona.component.audio.AudioStream
import cn.bixin.sona.component.internal.audio.AudioReportCode
import cn.bixin.sona.component.internal.audio.AudioSession
import cn.bixin.sona.component.internal.audio.SteamType

import cn.bixin.sona.component.internal.audio.zego.ZegoAudio
import cn.bixin.sona.util.SonaLogger
import java.util.*

/**
 * 推拉流失败处理
 */
class ZegoStreamRetryHandler(component: ZegoAudio) : BaseZegoHandler(component) {

    companion object {
        /**
         * 拉流重试消息
         */
        private const val PULL_STREAM = 0

        /**
         * 推流重试消息
         */
        private const val PUSH_STREAM = 1

        /**
         * 推流失败重试最大次数
         * 即构官网建议是每隔3秒重试一次，重试5-10次
         */
        private const val PUSH_STREAM_RETRY_MAX = 3

        /**
         * 拉流失败重试最大次数
         * 即构官网建议是每隔3秒重试一次，重试5-10次
         */
        private const val PULL_STREAM_RETRY_MAX = 3

    }

    /**
     * 记录推流失败重试次数
     */
    var mPushStreamRetryTimes = 0

    private val mPullStreamRetryPool = LinkedList<String>()
    private val mPullStreamRetryMap = HashMap<String, PullStreamRetryEntity>()

    /**
     * 延迟处理推拉流失败的handler
     */
    @SuppressLint("HandlerLeak")
    private val mStreamHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                PULL_STREAM -> if (!mPullStreamRetryPool.isEmpty()) {
                    val streamId = mPullStreamRetryPool.poll()
                    streamId ?: return
                    getStreamHandler()?.let {
                        val pullStreamRetryEntity = mPullStreamRetryMap[streamId]
                        if (pullStreamRetryEntity == null) {
                            mPullStreamRetryMap[streamId] =
                                PullStreamRetryEntity(
                                    streamId,
                                    1,
                                    -1,
                                    it.sessionType() === AudioSession.MIX
                                )
                            it.startPullStream(streamId)
                        } else {
                            pullStreamRetryEntity.retryCount++
                            if (pullStreamRetryEntity.isMixStream && isPullMixStreamNeedMaxRetry(
                                    pullStreamRetryEntity.code
                                )
                                || pullStreamRetryEntity.retryCount <= PULL_STREAM_RETRY_MAX
                            ) {
                                it.startPullStream(streamId)
                            } else {
                                // 拉流失败
                                mPullStreamRetryPool.remove(streamId)
                                mPullStreamRetryMap.remove(streamId)
                                it.stopPullStream(streamId)
                                SonaLogger.log(
                                    content = "拉流回调失败 streamId $streamId",
                                    code = if (pullStreamRetryEntity.isMixStream) AudioReportCode.ZEGO_PULL_MIX_STREAM_FAIL_CODE else AudioReportCode.ZEGO_PULL_STREAM_FAIL_CODE,
                                    sdkCode = pullStreamRetryEntity.code
                                )
                                dispatchMessage(
                                    ComponentMessage.ERROR_MSG,
                                    if (pullStreamRetryEntity.isMixStream) AudioError.PULL_MIX_STREAM_ERROR else AudioError.PULL_STREAM_ERROR
                                )
                            }
                        }
                    }
                    sendEmptyMessageDelayed(PULL_STREAM, 500)
                }
                PUSH_STREAM -> if (++mPushStreamRetryTimes <= PUSH_STREAM_RETRY_MAX) {
                    (getStreamHandler()?.acquireStream(SteamType.LOCAL) as? AudioStream)?.let {
                        getComponent().pushStream(it.streamId, null)
                    }
                } else {
                    // 推流失败
                    mPushStreamRetryTimes = 0
                    var pushStreamId = ""
                    var pushErrorCode = -1
                    if (msg.data != null) {
                        pushStreamId = msg.data.getString("streamId") ?: ""
                        pushErrorCode = msg.data.getInt("code")
                    }
                    SonaLogger.log(
                        content = "推流回调失败 streamId $pushStreamId",
                        code = AudioReportCode.ZEGO_PUSH_STREAM_FAIL_CODE,
                        sdkCode = pushErrorCode
                    )
                    dispatchMessage(
                        ComponentMessage.ERROR_MSG,
                        AudioError.PUSH_STREAM_ERROR
                    )
                }
                else -> {
                }
            }
        }
    }

    private fun isPullMixStreamNeedMaxRetry(code: Int): Boolean {
        return code == 12200006 || code == 12200201 || code == 12102001
    }

    fun retryPushStream(streamId: String, code: Int) {
        val pushStreamMessage = Message.obtain()
        pushStreamMessage.what = PUSH_STREAM
        val bundle = Bundle()
        bundle.putString("streamId", streamId)
        bundle.putInt("code", code)
        pushStreamMessage.data = bundle
        mStreamHandler.sendMessage(pushStreamMessage)
    }

    fun addStream(streamId: String, retryEntity: PullStreamRetryEntity) {
        mPullStreamRetryPool.add(streamId)
        if (mPullStreamRetryMap[streamId] == null) {
            mPullStreamRetryMap[streamId] = retryEntity
        }
        if (!mStreamHandler.hasMessages(PULL_STREAM)) {
            mStreamHandler.sendEmptyMessage(PULL_STREAM)
        }
    }

    fun stopPullStream() {
        mStreamHandler.removeMessages(PULL_STREAM)
        mPullStreamRetryPool.clear()
        mPullStreamRetryMap.clear()
    }

    fun removePushStreamMsg() {
        mPushStreamRetryTimes = 0
        mStreamHandler.removeMessages(PUSH_STREAM)
    }

    fun removeStream(streamId: String) {
        mPullStreamRetryPool.remove(streamId)
        mPullStreamRetryMap.remove(streamId)
    }

    override fun unAssembling() {
        mPullStreamRetryMap.clear()
        mPullStreamRetryPool.clear()
        mStreamHandler.removeCallbacksAndMessages(null)
    }
}