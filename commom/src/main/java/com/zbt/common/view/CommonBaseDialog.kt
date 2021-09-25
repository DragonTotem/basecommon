package com.zbt.common.view


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.zbt.common.R


/**
 * Description: 基础弹窗
 * @Author: xuwd
 * Date: 2020/10/10 15:04
 *
 */
abstract class CommonBaseDialog constructor(context: Context) : DialogFragment(), LifecycleObserver {
    private val mRootView: View? by lazy { initView() }
    private var mDialogGravity = Gravity.BOTTOM
    private var mMarginY = ViewUtils.dp2px(context, 8)
    protected val mContext: Context = context

    init {
        if (mContext is LifecycleOwner) {
            (mContext as LifecycleOwner).lifecycle.addObserver(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun destroy() {
        if (mContext is LifecycleOwner) {
            (mContext as LifecycleOwner).lifecycle.removeObserver(this)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return Dialog(requireContext(), R.style.BaseDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return mRootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.apply {
            gravity = mDialogGravity
            y = mMarginY
        }
    }

    abstract fun initView(): View?

    /**
     * 设置弹窗至于底部或顶部
     * @param gravity 参考 [Gravity.TOP] [Gravity.BOTTOM] [Gravity.CENTER]
     * @param margin 根据gravity设定偏移px 负值无效
     */
    final fun setGravityAndY(gravity: Int, margin: Int = 0) {
        mDialogGravity = gravity
        mMarginY = margin
    }

    override fun dismiss() {
        mRootView?.apply {
            if (parent is ViewGroup) {
                (parent as ViewGroup).removeView(this)
            }
        }
        dismissAllowingStateLoss()
    }

}