package cn.bixin.sona.demo.gift

import android.graphics.drawable.Drawable
import android.util.SparseIntArray
import android.widget.ImageView
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import cn.bixin.sona.demo.MainApplication
import cn.bixin.sona.demo.R
import com.github.penfeizhou.animation.apng.APNGDrawable
import java.util.*

class GiftAnimationHelper {

    private var giftAnimationList = LinkedList<Int>()

    private val giftResMap = SparseIntArray().apply {
        put(1, R.raw.chatroom_apng_gift_reward)
        put(2, R.raw.chatroom_apng_gift_reward)
        put(3, R.raw.chatroom_apng_gift_reward)
        put(4, R.raw.chatroom_apng_gift_reward)
    }

    private var giftView: ImageView? = null

    private var isPlaying = false

    fun bind(giftView: ImageView) {
        this.giftView = giftView
    }

    fun play(giftId: Int) {
        if (isPlaying) {
            giftAnimationList.add(giftId)
        } else {
            isPlaying = true
            internalPlay(giftId, {
                isPlaying = false
                if (giftAnimationList.isEmpty()) {
                    return@internalPlay
                }
                play(giftAnimationList.poll() ?: 0)
            }, {
                isPlaying = false
                if (giftAnimationList.isEmpty()) {
                    return@internalPlay
                }
                play(giftAnimationList.poll() ?: 0)
            })
        }
    }

    private fun internalPlay(giftId: Int, onPlayEnd: () -> Unit, onPlayFail: () -> Unit) {
        if (giftResMap[giftId] == 0) {
            onPlayFail.invoke()
            return
        }
        val giftApngDrawable =
            APNGDrawable.fromResource(MainApplication.getContext(), giftResMap[giftId])
        giftApngDrawable.setAutoPlay(false)
        giftApngDrawable.setLoopLimit(1)
        giftApngDrawable.clearAnimationCallbacks()
        giftApngDrawable.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                giftApngDrawable.stop()
                giftView?.setImageDrawable(null)
                onPlayEnd.invoke()
            }

            override fun onAnimationStart(drawable: Drawable?) {
            }
        })
        this.giftView?.setImageDrawable(giftApngDrawable)
        giftApngDrawable.start()
    }

}