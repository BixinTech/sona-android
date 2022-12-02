package cn.bixin.sona.component.internal.audio.zego

import android.graphics.Bitmap
import cn.bixin.sona.component.audio.IAudioPlayer
import cn.bixin.sona.component.audio.IAudioPlayerCallback
import cn.bixin.sona.component.internal.audio.AudioReportCode
import cn.bixin.sona.report.SonaReport
import cn.bixin.sona.report.SonaReportEvent
import cn.bixin.sona.util.SonaLogger
import com.zego.zegoavkit2.IZegoMediaPlayerWithIndexCallback
import com.zego.zegoavkit2.ZegoMediaPlayer

/**
 * 即构背景音乐播放器
 * @Author luokun
 * @Date 2020-03-05
 */
class ZegoPlayer(index: IAudioPlayer.Index) : IAudioPlayer {

    private val mMediaPlayer: ZegoMediaPlayer = ZegoMediaPlayer()
    private var mPlayerCallback: IAudioPlayerCallback? = null
    private var mStatus: IAudioPlayer.Status = IAudioPlayer.Status.IDLE

    init {
        val playIndex = when (index) {
            IAudioPlayer.Index.NONE -> ZegoMediaPlayer.PlayerIndex.First
            IAudioPlayer.Index.ONE -> ZegoMediaPlayer.PlayerIndex.Second
            IAudioPlayer.Index.TWO -> ZegoMediaPlayer.PlayerIndex.Third
            IAudioPlayer.Index.THREE -> ZegoMediaPlayer.PlayerIndex.Fourth
        }
        mMediaPlayer.init(ZegoMediaPlayer.PlayerTypeAux, playIndex)
        mMediaPlayer.setProcessInterval(1000L)
        mMediaPlayer.setEventWithIndexCallback(object : IZegoMediaPlayerWithIndexCallback {
            override fun onReadEOF(p0: Int) {
            }

            override fun onPlayStart(p0: Int) {
                mStatus = IAudioPlayer.Status.PLAY
                mPlayerCallback?.onStart()
            }

            override fun onLoadComplete(p0: Int) {
            }

            override fun onPlayPause(p0: Int) {
                mStatus = IAudioPlayer.Status.PAUSE
                mPlayerCallback?.onPause()
            }

            override fun onVideoBegin(p0: Int) {
            }

            override fun onSeekComplete(p0: Int, p1: Long, p2: Int) {
            }

            override fun onBufferEnd(p0: Int) {
                mPlayerCallback?.onBufferEnd()
            }

            override fun onPlayStop(p0: Int) {
                mStatus = IAudioPlayer.Status.IDLE
                mPlayerCallback?.onStop()
            }

            override fun onProcessInterval(p0: Long, p1: Int) {
                mPlayerCallback?.onProcess(p0)
            }

            override fun onPlayEnd(p0: Int) {
                mPlayerCallback?.onComplete()
                mStatus = IAudioPlayer.Status.IDLE
            }

            override fun onPlayError(p0: Int, p1: Int) {
                mStatus = IAudioPlayer.Status.IDLE
                mPlayerCallback?.onError(p0)
                val sonaReportEvent = SonaReportEvent.Builder()
                    .setCode(AudioReportCode.ZEGO_MUSIC_PLAY_ERROR_CODE)
                    .setSdkCode(p0)
                    .setContent("zego播放器播放错误")
                    .setType(SonaReportEvent.LOG or SonaReportEvent.LOGAN)
                    .build()
                SonaReport.report(sonaReportEvent)
            }

            override fun onSnapshot(p0: Bitmap?, p1: Int) {
            }

            override fun onPlayResume(p0: Int) {
                mStatus = IAudioPlayer.Status.PLAY
                mPlayerCallback?.onResume()
            }

            override fun onBufferBegin(p0: Int) {
                mPlayerCallback?.onBufferBegin()
            }

            override fun onAudioBegin(p0: Int) {
            }
        })
    }

    override fun play(url: String, pattern: IAudioPlayer.PlayPattern) {
        SonaLogger.print("zego play $mStatus $pattern")
        stop()
        if (pattern == IAudioPlayer.PlayPattern.LOCAL) {
            mMediaPlayer.setPlayerType(ZegoMediaPlayer.PlayerTypePlayer)
        } else {
            mMediaPlayer.setPlayerType(ZegoMediaPlayer.PlayerTypeAux)
        }
        mMediaPlayer.start(url, false)
    }

    override fun play(url: String, startPosition: Long, pattern: IAudioPlayer.PlayPattern) {
        SonaLogger.print("zego play $mStatus $pattern startPosition = $startPosition")
        stop()
        if (pattern == IAudioPlayer.PlayPattern.LOCAL) {
            mMediaPlayer.setPlayerType(ZegoMediaPlayer.PlayerTypePlayer)
        } else {
            mMediaPlayer.setPlayerType(ZegoMediaPlayer.PlayerTypeAux)
        }
        mMediaPlayer.start(url, false, startPosition)
    }

    override fun playRepeat(url: String, pattern: IAudioPlayer.PlayPattern) {
        SonaLogger.print("zego play $mStatus $pattern")
        stop()
        if (pattern == IAudioPlayer.PlayPattern.LOCAL) {
            mMediaPlayer.setPlayerType(ZegoMediaPlayer.PlayerTypePlayer)
        } else {
            mMediaPlayer.setPlayerType(ZegoMediaPlayer.PlayerTypeAux)
        }
        mMediaPlayer.start(url, true)
    }

    override fun playRepeat(url: String, startPosition: Long, pattern: IAudioPlayer.PlayPattern) {
        SonaLogger.print("zego play $mStatus $pattern startPosition = $startPosition")
        stop()
        if (pattern == IAudioPlayer.PlayPattern.LOCAL) {
            mMediaPlayer.setPlayerType(ZegoMediaPlayer.PlayerTypePlayer)
        } else {
            mMediaPlayer.setPlayerType(ZegoMediaPlayer.PlayerTypeAux)
        }
        mMediaPlayer.start(url, true, startPosition)
    }

    override fun resume() {
        if (mStatus == IAudioPlayer.Status.PAUSE) {
            mMediaPlayer.resume()
        }
    }

    override fun pause() {
        if (mStatus == IAudioPlayer.Status.PLAY) {
            mMediaPlayer.pause()
        }
    }

    override fun stop() {
        mMediaPlayer.stop()
    }

    override fun seek(position: Long) {
        if (mStatus != IAudioPlayer.Status.IDLE) {
            mMediaPlayer.seekTo(position)
        }
    }

    override fun setVolume(volume: Int) {
        mMediaPlayer.setVolume(volume)
    }

    override fun getDuration(): Long {
        return if (mStatus != IAudioPlayer.Status.IDLE) {
            mMediaPlayer.duration
        } else 0
    }

    override fun getMaxVolume(): Int {
        return 200
    }

    override fun getCurrentPosition(): Long {
        return if (mStatus != IAudioPlayer.Status.IDLE) {
            mMediaPlayer.currentDuration
        } else 0
    }

    override fun getStatus(): IAudioPlayer.Status {
        return mStatus
    }

    override fun getVolume(): Int {
        return mMediaPlayer.playVolume
    }

    override fun setCallback(callback: IAudioPlayerCallback?) {
        mPlayerCallback = callback
    }

    fun release() {
        stop()
        mMediaPlayer.uninit()
        mPlayerCallback = null
    }

}