package cn.bixin.sona.component.internal.audio.tencent

import cn.bixin.sona.component.audio.IAudioPlayer
import cn.bixin.sona.component.audio.IAudioPlayerCallback
import cn.bixin.sona.component.internal.audio.AudioReportCode
import cn.bixin.sona.util.SonaLogger
import com.tencent.liteav.audio.TXAudioEffectManager
import com.tencent.liteav.audio.TXAudioEffectManager.TXMusicPlayObserver
import com.tencent.trtc.TRTCCloud

/**
 * 腾讯背景音乐播放器
 * @Author luokun
 * @Date 2020-03-05
 */
class TencentPlayer(private val mTRTCCloud: TRTCCloud, index: IAudioPlayer.Index) : IAudioPlayer {
    private var musicId = DEFAULT_MUSIC_ID

    companion object {
        const val DEFAULT_MUSIC_ID = 100
        const val DEFAULT_MUSIC_ID_ONE = 101
        const val DEFAULT_MUSIC_ID_TWO = 102
        const val DEFAULT_MUSIC_ID_THREE = 103
    }

    init {
        musicId = when (index) {
            IAudioPlayer.Index.NONE -> DEFAULT_MUSIC_ID
            IAudioPlayer.Index.ONE -> DEFAULT_MUSIC_ID_ONE
            IAudioPlayer.Index.TWO -> DEFAULT_MUSIC_ID_TWO
            IAudioPlayer.Index.THREE -> DEFAULT_MUSIC_ID_THREE
            else -> DEFAULT_MUSIC_ID
        }
    }

    private var mPattern: IAudioPlayer.PlayPattern? = null
    private var mVolume: Int = mTRTCCloud.audioPlayoutVolume
    private var mStatus = IAudioPlayer.Status.IDLE
    private var mCurrentPosition: Long = 0

    private var mPlayerCallback: IAudioPlayerCallback? = null

    private var repeat: Boolean = false

    override fun play(url: String, pattern: IAudioPlayer.PlayPattern) {
        if (mStatus != IAudioPlayer.Status.IDLE) {
            mTRTCCloud.audioEffectManager.stopPlayMusic(musicId)
            mStatus = IAudioPlayer.Status.IDLE
        }

        mPattern = pattern
        mCurrentPosition = 0

        mTRTCCloud.audioEffectManager.setMusicObserver(musicId, object : TXMusicPlayObserver {
            override fun onStart(id: Int, errCode: Int) {
                if (errCode == 0) {
                    mStatus = IAudioPlayer.Status.PLAY
                    mPlayerCallback?.onStart()
                } else {
                    mPlayerCallback?.onError(errCode)
                    SonaLogger.log(
                        content = "TencentPlayer播放错误",
                        sdkCode = errCode,
                        code = AudioReportCode.TENCENT_MUSIC_PLAY_ERROR_CODE
                    )
                }
            }

            override fun onPlayProgress(id: Int, curPtsMS: Long, durationMS: Long) {
                mCurrentPosition = curPtsMS
                mPlayerCallback?.onProcess(curPtsMS)
            }

            override fun onComplete(id: Int, errCode: Int) {
                if (errCode == 0) {
                    mPlayerCallback?.onComplete()
                    mCurrentPosition = 0
                    mStatus = IAudioPlayer.Status.IDLE
                } else {
                    mCurrentPosition = 0
                    mStatus = IAudioPlayer.Status.IDLE
                    mPlayerCallback?.onError(errCode)
                }
            }

        })

        val audioMusicParam = TXAudioEffectManager.AudioMusicParam(musicId, url)
        when (pattern) {
            IAudioPlayer.PlayPattern.LOCAL -> {
                audioMusicParam.publish = false
                mTRTCCloud.audioEffectManager.setMusicPublishVolume(musicId, 0)
            }
            IAudioPlayer.PlayPattern.REMOTE -> {
                audioMusicParam.publish = true
                mTRTCCloud.audioEffectManager.setMusicPublishVolume(musicId, mVolume)
            }
        }
        if (repeat) {
            audioMusicParam.loopCount = Int.MAX_VALUE
        } else {
            audioMusicParam.loopCount = 0
        }
        mTRTCCloud.audioEffectManager.startPlayMusic(audioMusicParam)
    }

    override fun play(url: String, startPosition: Long, pattern: IAudioPlayer.PlayPattern) {
        play(url, pattern)
    }

    override fun playRepeat(url: String, pattern: IAudioPlayer.PlayPattern) {
        repeat = true
        play(url, pattern)
    }

    override fun playRepeat(url: String, startPosition: Long, pattern: IAudioPlayer.PlayPattern) {
        playRepeat(url, pattern)
    }

    override fun resume() {
        if (mStatus == IAudioPlayer.Status.PAUSE) {
            mTRTCCloud.audioEffectManager.resumePlayMusic(musicId)
            mStatus = IAudioPlayer.Status.PLAY
            mPlayerCallback?.onResume()
        }
    }

    override fun pause() {
        if (mStatus == IAudioPlayer.Status.PLAY) {
            mTRTCCloud.audioEffectManager.pausePlayMusic(musicId)
            mStatus = IAudioPlayer.Status.PAUSE
            mPlayerCallback?.onPause()
        }
    }

    override fun stop() {
        if (mStatus != IAudioPlayer.Status.IDLE) {
            repeat = false
            mTRTCCloud.audioEffectManager.stopPlayMusic(musicId)
            mStatus = IAudioPlayer.Status.IDLE
            mPlayerCallback?.onStop()
        }
    }

    override fun seek(position: Long) {
        if (mStatus != IAudioPlayer.Status.IDLE) {
            mTRTCCloud.audioEffectManager.seekMusicToPosInMS(musicId, position.toInt())
        }
    }

    override fun setVolume(volume: Int) {
        mTRTCCloud.audioEffectManager.setMusicPlayoutVolume(musicId, volume)
        if (mPattern == IAudioPlayer.PlayPattern.REMOTE) {
            mTRTCCloud.audioEffectManager.setMusicPublishVolume(musicId, volume)
        }
        mVolume = volume
    }

    override fun getDuration(): Long {
        return mTRTCCloud.audioEffectManager.getMusicDurationInMS(null)
    }

    override fun getMaxVolume(): Int {
        return 100
    }

    override fun getCurrentPosition(): Long {
        return mCurrentPosition
    }

    override fun getStatus(): IAudioPlayer.Status {
        return mStatus
    }

    override fun getVolume(): Int {
        return 0
    }

    override fun setCallback(callback: IAudioPlayerCallback?) {
        mPlayerCallback = callback
    }

    fun release() {
        if (mStatus != IAudioPlayer.Status.IDLE) {
            mTRTCCloud.audioEffectManager.stopPlayMusic(musicId)
            mStatus = IAudioPlayer.Status.IDLE
        }
        mTRTCCloud.audioEffectManager.setMusicObserver(musicId, null)
        mPlayerCallback = null
    }
}