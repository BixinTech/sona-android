package cn.bixin.sona.component.internal.audio.zego.handler

import android.text.TextUtils
import cn.bixin.sona.component.ComponentMessage
import cn.bixin.sona.component.internal.audio.AudioSession
import cn.bixin.sona.component.internal.audio.zego.ZegoAudio
import cn.bixin.sona.data.entity.UserData
import cn.bixin.sona.plugin.config.AudioConfig
import cn.bixin.sona.plugin.entity.SoundLevelInfoEntity
import com.zego.zegoavkit2.mixstream.ZegoSoundLevelInMixStreamInfo
import com.zego.zegoavkit2.mixstream.ZegoStreamMixer
import com.zego.zegoavkit2.soundlevel.IZegoSoundLevelCallback
import com.zego.zegoavkit2.soundlevel.ZegoSoundLevelInfo
import com.zego.zegoavkit2.soundlevel.ZegoSoundLevelMonitor
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * 声音大小回调处理
 */
class ZegoSoundLevelHandler(component: ZegoAudio) : BaseZegoHandler(component) {

    private var publishSubject: PublishSubject<ArrayList<ZegoSoundLevelInMixStreamInfo>>? = null
    private var mMixDisposable: Disposable? = null
    private var zegoStreamMixer: ZegoStreamMixer? = null
    private var mSoundDisposable: Disposable? = null

    fun registerSoundLevelListener(audioSession: AudioSession) {
        if (zegoStreamMixer == null) {
            zegoStreamMixer = ZegoStreamMixer()
        }
        val audioConfig = acquire(AudioConfig::class.java)
        if (audioConfig == null || !audioConfig.soundCapture) return
        if (audioSession === AudioSession.MIX) {
            registerMixThrottling(audioConfig)
            ZegoSoundLevelMonitor.getInstance().stop()
            ZegoSoundLevelMonitor.getInstance().setCallback(null)
            zegoStreamMixer?.setSoundLevelInMixStreamCallback { arrayList: ArrayList<ZegoSoundLevelInMixStreamInfo>? ->
                arrayList ?: return@setSoundLevelInMixStreamCallback
                publishSubject?.onNext(arrayList)
            }
        } else {
            zegoStreamMixer?.setSoundLevelInMixStreamCallback(null)
            var volumeInterval = 500L
            if (audioConfig.voiceVolumeInterval > 0) {
                volumeInterval = audioConfig.voiceVolumeInterval
            }
            ZegoSoundLevelMonitor.getInstance().setCycle(volumeInterval.toInt())
            ZegoSoundLevelMonitor.getInstance().stop()
            ZegoSoundLevelMonitor.getInstance().setCallback(object : IZegoSoundLevelCallback {
                /**
                 * 拉流声浪数据回调，停止拉流之后不会触发此回调
                 * @param zegoSoundLevelInfos - 拉流声浪数据
                 */
                override fun onSoundLevelUpdate(zegoSoundLevelInfos: Array<ZegoSoundLevelInfo>) {
                    handleMultiSoundInfo(zegoSoundLevelInfos)
                }

                override fun onCaptureSoundLevelUpdate(zegoSoundLevelInfo: ZegoSoundLevelInfo) {

                }
            })
            ZegoSoundLevelMonitor.getInstance().start()
        }
    }

    private fun registerMixThrottling(audioConfig: AudioConfig?) {
        mMixDisposable?.dispose()
        publishSubject = PublishSubject.create<ArrayList<ZegoSoundLevelInMixStreamInfo>>()
        var volumeInterval = 500L
        if (audioConfig != null && audioConfig.voiceVolumeInterval > 0) {
            volumeInterval = audioConfig.voiceVolumeInterval
        }
        mMixDisposable = publishSubject?.throttleFirst(volumeInterval, TimeUnit.MILLISECONDS)
            ?.subscribe({ zegoSoundLevelInMixStreamInfos: ArrayList<ZegoSoundLevelInMixStreamInfo> ->
                handleMixSoundInfo(zegoSoundLevelInMixStreamInfos)
            }, { it.printStackTrace() })
    }

    private fun handleMultiSoundInfo(zegoSoundLevelInfos: Array<ZegoSoundLevelInfo>?) {
        zegoSoundLevelInfos ?: return
        val soundLevelInfoList: MutableList<SoundLevelInfoEntity> = ArrayList()
        val audioStreamList = getStreamHandler()?.findAudioStream()
        audioStreamList ?: return

        zegoSoundLevelInfos.forEach { soundLevelInfo ->
            if (soundLevelInfo.soundLevel > 0) {
                audioStreamList.forEach continuing@{
                    if (TextUtils.equals(soundLevelInfo.streamID, it.streamId)) {
                        soundLevelInfoList.add(
                            SoundLevelInfoEntity(it.userId, soundLevelInfo.soundLevel)
                        )
                        return@continuing
                    }
                }
            }
        }

        if (soundLevelInfoList.isNotEmpty()) {
            dispatchMessage(
                ComponentMessage.AUDIO_REV_SOUND_LEVEL_INFO,
                soundLevelInfoList
            )
        }
    }

    private fun handleMixSoundInfo(arrayList: ArrayList<ZegoSoundLevelInMixStreamInfo>?) {
        if (arrayList == null || arrayList.isEmpty()) return
        val soundLevelInfoList = ArrayList<SoundLevelInfoEntity>()
        arrayList.forEach {
            if (it.soundLevel > 0) {
                soundLevelInfoList.add(
                    SoundLevelInfoEntity(
                        it.soundLevelID.toString(), it.soundLevel.toFloat()
                    )
                )
            }
        }
        if (soundLevelInfoList.isNotEmpty()) {
            dispatchMessage(
                ComponentMessage.AUDIO_REV_SOUND_LEVEL_INFO,
                soundLevelInfoList
            )
        }
    }

    /**
     * 监控声音
     *
     * @param capture true 监控，false 取消监控
     */
    fun captureSound(capture: Boolean) {
        mSoundDisposable?.dispose()
        if (capture) {
            val audioConfig = acquire(AudioConfig::class.java)
            val userData = acquire(UserData::class.java)
            val uid = userData?.uid ?: ""

            var volumeInterval = 500L
            audioConfig?.let {
                if (it.voiceVolumeInterval > 0) {
                    volumeInterval = it.voiceVolumeInterval
                }
                if (it.soundCapture) {
                    mSoundDisposable =
                        Observable.interval(volumeInterval, TimeUnit.MILLISECONDS)
                            .subscribe({
                                val volume = getLiveRoom()?.captureSoundLevel ?: 0f
                                if (volume > 0 && !TextUtils.isEmpty(uid)) {
                                    val soundLevelInfoList = ArrayList<SoundLevelInfoEntity>()
                                    soundLevelInfoList.add(
                                        SoundLevelInfoEntity(uid, volume)
                                    )
                                    dispatchMessage(
                                        ComponentMessage.AUDIO_REV_SOUND_LEVEL_INFO,
                                        soundLevelInfoList
                                    )
                                }
                            }) { throwable: Throwable -> throwable.printStackTrace() }
                }
            }
        }
    }

    override fun unAssembling() {
        mMixDisposable?.dispose()
        mSoundDisposable?.dispose()
        zegoStreamMixer?.setSoundLevelInMixStreamCallback(null)
        ZegoSoundLevelMonitor.getInstance().stop()
        ZegoSoundLevelMonitor.getInstance().setCallback(null)
        mMixDisposable = null
        publishSubject = null
        mSoundDisposable = null
        zegoStreamMixer = null
    }
}