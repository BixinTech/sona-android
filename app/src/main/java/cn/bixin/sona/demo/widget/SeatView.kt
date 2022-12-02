package cn.bixin.sona.demo.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import cn.bixin.sona.demo.ChatRoomManager
import cn.bixin.sona.demo.R
import cn.bixin.sona.demo.model.SeatInfoModel
import cn.bixin.sona.demo.util.AvatarUtil
import com.bumptech.glide.Glide
import com.github.penfeizhou.animation.apng.APNGDrawable
import kotlinx.android.synthetic.main.chatroom_layout_seat.view.*

class SeatView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    lateinit var mRootView: View
    private var seatInfoModel: SeatInfoModel? = null

    private var waveAPNGDrawable: APNGDrawable? = null

    init {
        initView()
    }

    private fun initView() {
        mRootView = LayoutInflater.from(context).inflate(R.layout.chatroom_layout_seat, this)
        initRipple()
        initListener()
    }

    private fun initListener() {
        mRootView.ivSeatIcon.setOnClickListener {
            mSeatClickedListener?.onSeatClicked(this)
        }
        mRootView.ivMute.setOnClickListener {
            mSeatClickedListener?.onMyMicClicked(this)
        }
    }

    private fun initRipple() {
        waveAPNGDrawable = getWave()
    }

    private fun getWave(): APNGDrawable {
        return APNGDrawable.fromResource(context, R.raw.chatroom_apng_ripple)
    }

    private fun isMyRoom(): Boolean {
        return false
    }

    private fun isMySeatView(): Boolean {
        return false
    }

    fun startSpeakAnimation() {
        if (waveAPNGDrawable == null) {
            waveAPNGDrawable = getWave()
        } else {
            if ((waveAPNGDrawable ?: return).isRunning) {
                return
            }
        }
        waveAPNGDrawable?.reset()
        waveAPNGDrawable?.setAutoPlay(false)
        waveAPNGDrawable?.setLoopLimit(1)
        waveAPNGDrawable?.clearAnimationCallbacks()

        waveAPNGDrawable?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                waveAPNGDrawable?.stop()
                mRootView.ivRipple.visibility = View.GONE
            }

            override fun onAnimationStart(drawable: Drawable?) {
            }
        })
        mRootView.ivRipple.setImageDrawable(waveAPNGDrawable)
        mRootView.ivRipple.visibility = View.VISIBLE
        waveAPNGDrawable?.start()
    }

    private var mSeatClickedListener: SeatClickedListener? = null

    fun setSeatClickedListener(seatClickedListener: SeatClickedListener) {
        this.mSeatClickedListener = seatClickedListener
    }

    fun bind(seatInfoModel: SeatInfoModel) {
        this.seatInfoModel = seatInfoModel
        if (seatInfoModel.isEmpty()) {
            tvName.text = "我要上麦"
            ivSeatIcon.setImageResource(R.mipmap.chatroom_img_seat_default)
            ivMute.visibility = View.GONE
        } else {
            tvName.text = "用户${seatInfoModel.uid}"
            if (TextUtils.equals(seatInfoModel.uid, ChatRoomManager.myUid)) {
                ivMute.visibility = View.VISIBLE
            } else {
                ivMute.visibility = View.GONE
            }
            Glide.with(context).load(AvatarUtil.getSeatAvatar(seatInfoModel.index)).circleCrop()
                .into(ivSeatIcon)
        }
    }

    fun handleMic(micOpen: Boolean) {
        if (micOpen) {
            ivMute.setImageResource(R.mipmap.chatroom_ic_seat_mic_open)
        } else {
            ivMute.setImageResource(R.mipmap.chatroom_ic_seat_mic_off)
        }
    }

    interface SeatClickedListener {
        fun onSeatClicked(seatView: SeatView)

        /**
         * 点击自己麦克风控制开关
         */
        fun onMyMicClicked(view: View)

    }

}