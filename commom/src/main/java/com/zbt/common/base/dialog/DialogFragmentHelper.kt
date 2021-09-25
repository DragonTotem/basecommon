package com.zbt.common.base.dialog

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.text.InputType
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.zbt.common.R
import com.zbt.common.base.CommonDialogFragment
import com.zbt.common.base.CommonDialogFragment.OnCallDialog
import java.util.*

/**
 * author：   HUlq
 * date：     2020/12/10 & 17:41
 * desc       封装一个DialogFragment
 * modify by
 */
object DialogFragmentHelper {
    private const val DIALOG_POSITIVE = "确定"
    private const val DIALOG_NEGATIVE = "取消"

    private val TAG_HEAD = DialogFragmentHelper::class.java.simpleName

    /**
     * 加载中的弹出窗
     */
    private val PROGRESS_THEME: Int = R.style.BaseDialog
    private val PROGRESS_TAG = "$TAG_HEAD:progress"

    fun showProgress(fragmentManager: FragmentManager?, message: String?): CommonDialogFragment? {
        return showProgress(fragmentManager, message, true, null)
    }

    fun showProgress(fragmentManager: FragmentManager?, message: String?, cancelable: Boolean): CommonDialogFragment? {
        return showProgress(fragmentManager, message, cancelable, null)
    }

    @Suppress("DEPRECATION")
    fun showProgress(
            fragmentManager: FragmentManager?, message: String?, cancelable: Boolean,
            cancelListener: CommonDialogFragment.OnDismissListener?,
    ): CommonDialogFragment? {
        val dialogFragment = CommonDialogFragment.newInstance(object : OnCallDialog {
            override fun getDialog(context: Context?): Dialog {
                val progressDialog = ProgressDialog(context, PROGRESS_THEME)
                progressDialog.setMessage(message)
                return progressDialog
            }
        }, cancelable, cancelListener)
        dialogFragment.show(fragmentManager!!, PROGRESS_TAG)
        return dialogFragment
    }

    /**
     * 简单提示弹出窗
     */
    private val TIPS_THEME: Int = R.style.BaseDialog
    private val TIPS_TAG = "$TAG_HEAD:tips"

    fun showTips(fragmentManager: FragmentManager?, message: String?) {
        showTips(fragmentManager, message, true, null)
    }

    fun showTips(fragmentManager: FragmentManager?, message: String?, cancelable: Boolean) {
        showTips(fragmentManager, message, cancelable, null)
    }

    private fun showTips(
            fragmentManager: FragmentManager?, message: String?, cancelable: Boolean,
            cancelListener: CommonDialogFragment.OnDismissListener?,
    ) {
        val dialogFragment = CommonDialogFragment.newInstance(object : OnCallDialog {
            override fun getDialog(context: Context?): Dialog {
                val builder: AlertDialog.Builder = AlertDialog.Builder(context!!, TIPS_THEME)
                builder.setMessage(message)
                return builder.create()
            }
        }, cancelable, cancelListener)
        dialogFragment.show(fragmentManager!!, TIPS_TAG)
    }


    /**
     * 确定取消框
     */
    private val CONFIRM_THEME: Int = R.style.BaseDialog
    private val CONfIRM_TAG = "$TAG_HEAD:confirm"

    fun showConfirmDialog(
            fragmentManager: FragmentManager?, message: String?, listener: IDialogResultListener<Int?>?,
            cancelable: Boolean, cancelListener: CommonDialogFragment.OnDismissListener?,
    ) {
        val dialogFragment = CommonDialogFragment.newInstance(object : OnCallDialog {
            override fun getDialog(context: Context?): Dialog {
                val builder: AlertDialog.Builder = AlertDialog.Builder(context!!, CONFIRM_THEME)
                builder.setMessage(message)
                builder.setPositiveButton(DIALOG_POSITIVE, DialogInterface.OnClickListener { _, which ->
                    listener?.onDataResult(which)
                })
                builder.setNegativeButton(DIALOG_NEGATIVE, DialogInterface.OnClickListener { _, which ->
                    listener?.onDataResult(which)
                })
                return builder.create()
            }
        }, cancelable, cancelListener)
        dialogFragment.show(fragmentManager!!, CONfIRM_TAG)
    }

    /**
     * 带列表的弹出窗
     */
    private val LIST_THEME: Int = R.style.BaseDialog
    private val LIST_TAG = "$TAG_HEAD:list"

    fun showListDialog(
            fragmentManager: FragmentManager?, title: String?, items: Array<String?>?,
            resultListener: IDialogResultListener<Int?>?, cancelable: Boolean,
    ): DialogFragment? {
        val dialogFragment = CommonDialogFragment.newInstance(object : OnCallDialog {
            override fun getDialog(context: Context?): Dialog {
                val builder: AlertDialog.Builder = AlertDialog.Builder(context!!, LIST_THEME)
                builder.setTitle(title)
                builder.setItems(items, DialogInterface.OnClickListener { dialog, which ->
                    resultListener?.onDataResult(which)
                })
                return builder.create()
            }
        }, cancelable, null)
        dialogFragment.show(fragmentManager!!, LIST_TAG)
        return dialogFragment
    }

    /**
     * 选择日期
     */
    private val DATE_THEME: Int = R.style.BaseDialog
    private val DATE_TAG = "$TAG_HEAD:date"

    fun showDateDialog(
            fragmentManager: FragmentManager?, title: String?, calendar: Calendar,
            resultListener: IDialogResultListener<Calendar?>, cancelable: Boolean,
    ): DialogFragment? {
        val dialogFragment = CommonDialogFragment.newInstance(object : OnCallDialog {
            override fun getDialog(context: Context?): Dialog {
                val datePickerDialog = DatePickerDialog(context!!, DATE_THEME, OnDateSetListener { view, year, month, dayOfMonth ->
                    calendar[year, month] = dayOfMonth
                    resultListener.onDataResult(calendar)
                }, calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH])
                datePickerDialog.setTitle(title)
                datePickerDialog.setOnShowListener {
                    datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).text = DIALOG_POSITIVE
                    datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).text = DIALOG_NEGATIVE
                }
                return datePickerDialog
            }
        }, cancelable, null)
        dialogFragment.show(fragmentManager!!, DATE_TAG)
        return dialogFragment
    }


    /**
     * 选择时间
     */
    private val TIME_THEME: Int = R.style.BaseDialog
    private val TIME_TAG = "$TAG_HEAD:time"
    fun showTimeDialog(manager: FragmentManager?, title: String?, calendar: Calendar, resultListener: IDialogResultListener<Calendar?>?, cancelable: Boolean) {
        val dialogFragment = CommonDialogFragment.newInstance(object : OnCallDialog {
            override fun getDialog(context: Context?): Dialog {
                val dateDialog = TimePickerDialog(context, TIME_THEME, OnTimeSetListener { view, hourOfDay, minute ->
                    if (resultListener != null) {
                        calendar[Calendar.HOUR_OF_DAY] = hourOfDay
                        calendar[Calendar.MINUTE] = minute
                        resultListener.onDataResult(calendar)
                    }
                }, calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE], true)
                dateDialog.setTitle(title)
                dateDialog.setOnShowListener {
                    dateDialog.getButton(DialogInterface.BUTTON_POSITIVE).text = DIALOG_POSITIVE
                    dateDialog.getButton(DialogInterface.BUTTON_NEGATIVE).text = DIALOG_NEGATIVE
                }
                return dateDialog
            }
        }, cancelable, null)
        dialogFragment.show(manager!!, DATE_TAG)
    }

    /**
     * 带输入框的弹出窗
     */
    private val INSERT_THEME: Int = R.style.BaseDialog
    private val INSERT_TAG = "$TAG_HEAD:insert"

    fun showInsertDialog(manager: FragmentManager?, title: String?, resultListener: IDialogResultListener<String?>?, cancelable: Boolean) {
        val dialogFragment = CommonDialogFragment.newInstance(object : OnCallDialog {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            override fun getDialog(context: Context?): Dialog {
                val editText = EditText(context)
                editText.background = null
                editText.setPadding(60, 40, 0, 0)
                val builder: AlertDialog.Builder = AlertDialog.Builder(context!!, INSERT_THEME)
                builder.setTitle(title)
                builder.setView(editText)
                builder.setPositiveButton(DIALOG_POSITIVE, DialogInterface.OnClickListener { dialog, which ->
                    resultListener?.onDataResult(editText.text.toString())
                })
                builder.setNegativeButton(DIALOG_NEGATIVE, null)
                return builder.create()
            }
        }, cancelable, null)
        dialogFragment.show(manager!!, INSERT_TAG)
    }


    /**
     * 带输入密码框的弹出窗
     */
    private val PASSWORD_INSER_THEME: Int = R.style.BaseDialog
    private val PASSWORD_INSERT_TAG = "$TAG_HEAD:insert"

    fun showPasswordInsertDialog(manager: FragmentManager?, title: String?, resultListener: IDialogResultListener<String?>?, cancelable: Boolean) {
        val dialogFragment = CommonDialogFragment.newInstance(object : OnCallDialog {
            override fun getDialog(context: Context?): Dialog {
                val editText = EditText(context)
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                editText.isEnabled = true
                val builder: AlertDialog.Builder = AlertDialog.Builder(context!!,
                        PASSWORD_INSER_THEME)
                builder.setTitle(title)
                builder.setView(editText)
                builder.setPositiveButton(DIALOG_POSITIVE, DialogInterface.OnClickListener { dialog, which ->
                    resultListener?.onDataResult(editText.text.toString())
                })
                builder.setNegativeButton(DIALOG_NEGATIVE, null)
                return builder.create()
            }
        }, cancelable, null)
        dialogFragment.show(manager!!, INSERT_TAG)
    }
}