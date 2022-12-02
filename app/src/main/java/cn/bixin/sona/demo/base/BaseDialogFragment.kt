package cn.bixin.sona.demo.base

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import cn.bixin.sona.demo.R

abstract class BaseDialogFragment : DialogFragment() {

    companion object {
        const val WINDOW_FULL_SCREEN = 0
        const val WINDOW_FULL_WIDTH = 1
        const val WINDOW_FULL_HEIGHT = 2
        const val WINDOW_DEFAULT = 4
    }

    protected lateinit var mRootView: View

    override fun onStart() {
        super.onStart()
        dialog?.setCanceledOnTouchOutside(canceledOnTouchOutside())
        val window = dialog?.window ?: return
        val windowParams = window.attributes
        windowParams.dimAmount = dimAmount()
        window.setGravity(gravity())
        window.decorView.setPadding(0, 0, 0, 0)
        when (windowMode()) {
            WINDOW_FULL_SCREEN -> {
                window.attributes?.width = matchParent()
                window.attributes?.height = matchParent()
            }
            WINDOW_FULL_WIDTH -> {
                window.attributes?.width = matchParent()
            }
            WINDOW_FULL_HEIGHT -> {
                window.attributes?.height = matchParent()
            }
            else -> {
            }
        }
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.attributes = windowParams
        if (needBottomAnimator()) {
            dialog?.window?.setWindowAnimations(R.style.Chatroom_BottomDialog_Animation)
        }
    }

    private fun matchParent(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }

    protected open fun windowMode(): Int {
        return WINDOW_DEFAULT
    }

    protected open fun gravity(): Int {
        return Gravity.CENTER
    }

    protected open fun dimAmount(): Float {
        return 0.2f
    }

    protected open fun canceledOnTouchOutside(): Boolean {
        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(getLayoutResId(), container, true)
        initView()
        return mRootView
    }

    abstract fun getLayoutResId(): Int

    protected open fun initView() {

    }

    open fun show(fragmentManager: androidx.fragment.app.FragmentManager?) {
        if (fragmentManager == null) {
            return
        }
        try {
            if (!isAdded && !isVisible) {
                this.showNow(fragmentManager, this.javaClass.simpleName)
            }
        } catch (ignored: Exception) {
            ignored.printStackTrace()
        }
    }

    open fun needBottomAnimator(): Boolean {
        return false
    }

    override fun dismiss() {
        if (activity == null || requireActivity().isFinishing) {
            return
        }

        try {
            dismissAllowingStateLoss()
        } catch (ignored: Exception) {
        }

    }

}