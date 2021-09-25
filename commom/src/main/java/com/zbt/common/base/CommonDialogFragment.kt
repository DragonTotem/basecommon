package com.zbt.common.base

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

/**
 * author：   HUlq
 * date：     2020/12/10 & 17:41
 * desc       封装一个公共DialogFragment,
 * modify by
 */
class CommonDialogFragment : DialogFragment() {
    /**
     * 监听弹出窗是否被取消
     */
    private var onDismissListener: OnDismissListener? = null

    /**
     * 回调获得需要显示的 dialog
     */
    private var mOnCallDialog: OnCallDialog? = null

    interface OnDismissListener {
        fun onDismiss(bundle: Bundle?)
    }

    interface OnCallDialog {
        fun getDialog(context: Context?): Dialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (null == mOnCallDialog) {
            super.onCreate(savedInstanceState)
        }
        return mOnCallDialog!!.getDialog(activity)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val window = getDialog()!!.window
            val windowParams = window!!.attributes
            windowParams.dimAmount = 0.0f
            window.attributes = windowParams
        }
    }

    override fun dismiss() {
        super.dismiss()
        onDismissListener?.onDismiss(null)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onDismissListener?.onDismiss(null)
    }

    companion object {
        @JvmOverloads
        fun newInstance(
                callDialog: OnCallDialog?, cancelable: Boolean,
                dismissListener: OnDismissListener? =
                        null,
        ): CommonDialogFragment {
            val instance = CommonDialogFragment()
            instance.isCancelable = cancelable
            instance.onDismissListener = dismissListener
            instance.mOnCallDialog = callDialog
            return instance
        }
    }
}