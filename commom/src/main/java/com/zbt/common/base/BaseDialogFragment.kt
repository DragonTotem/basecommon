package com.zbt.common.base

import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.annotation.FloatRange
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import me.jessyan.autosize.utils.ScreenUtils

/**
 * author：   HUlq
 * date：     2020/12/10 & 17:41
 * desc       封装一个自定义layoutId  DialogFragment,
 * modify by
 * @param T 传入泛型的 ViewBinding
 * @param layoutId 传入布局用来跳过在子类中初始化传入 inflater
 */
abstract class BaseDialogFragment<T : ViewBinding>(private val layoutId: Int) : DialogFragment(),
        CoroutineScope by MainScope() {

    private var _binding: T? = null

    val binding get() = _binding!!

    private var isNotFullScreen = false
    private var mDimAmount = 0.5f //背景昏暗度
    private var mLocation: IntArray? = null
    private var mShowBottomEnable = false   //是否底部显示
    private var mMargin = 0.0 //左右边距

    private var mAnimStyle = 0 //进入退出动画

    private var mOutCancel = true //点击外部取消

    private var mWidth = 0
    private var mHeight = 0
    private var onDismissListener: OnDismissListener? = null

    interface OnDismissListener {
        fun onDismiss(bundle: Bundle?)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));// .必须设置dialog的window背景为透明颜色，不然圆角无效或者是系统默认的颜色
        // 完成 initView 后改变view的初始化状态为完成
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = initBinding(view)
        initView()
        initData()
    }

    /**
     * 传入对应的 ViewBinding
     */
    abstract fun initBinding(view: View): T

    /**
     * fragment 初始化 view 的方法
     */
    abstract fun initView()

    /**
     * 加载数据
     */
    abstract fun initData()

    override fun onStart() {
        super.onStart()
        initParams()
    }

    //释放数据
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
        cancel()
    }

    @StyleRes
    open fun getDialogStyle(): Int {
        return 0
    }

    open fun initParams() {
        val window: Window? = dialog?.window
        if (window != null) {
            val params = window.attributes
            params.dimAmount = mDimAmount

            //设置dialog显示位置
            if (mShowBottomEnable) {
                params.gravity = Gravity.BOTTOM
            }

            //不全屏显示
            if (isNotFullScreen) {
                params.flags = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
            }

            //标志位置
            if (mLocation != null) {
                params.x = mLocation!![0]
                params.y = mLocation!![1]
                params.gravity = Gravity.TOP
            }

            //设置dialog宽度
            if (mWidth == 0) {
                params.width = ScreenUtils.getScreenSize(context)[0].minus(dp2px(mMargin).times(2))
            } else {
                params.width = mWidth
            }
            //设置dialog高度
            if (mHeight == 0) {
                params.height = WindowManager.LayoutParams.WRAP_CONTENT
            } else {
                params.height = mHeight
            }

            //设置dialog动画
            if (mAnimStyle != 0) {
                window.setWindowAnimations(mAnimStyle)
            }
            window.attributes = params
        }
        isCancelable = mOutCancel
    }

    open fun dp2px(dpValue: Double): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    open fun setLocation(location: IntArray): BaseDialogFragment<T> {
        mLocation = location
        return this
    }

    open fun setNotFullScreen(notFullScreen: Boolean): BaseDialogFragment<T> {
        isNotFullScreen = notFullScreen
        return this
    }

    /**
     * 设置背景昏暗度
     *
     * @param dimAmount
     * @return
     */
    open fun setDimAmout(@FloatRange(from = 0.0, to = 1.0) dimAmount: Float): BaseDialogFragment<T> {
        mDimAmount = dimAmount
        return this
    }

    /**
     * 是否显示底部
     *
     * @param showBottom
     * @return
     */
    open fun setShowBottom(showBottom: Boolean): BaseDialogFragment<T> {
        mShowBottomEnable = showBottom
        return this
    }

    /**
     * 设置宽高
     *
     * @param width
     * @param height
     * @return
     */
    open fun setSize(width: Int, height: Int): BaseDialogFragment<T> {
        mWidth = width
        mHeight = height
        return this
    }

    /**
     * 设置左右margin
     *
     * @param margin
     * @return
     */
    open fun setMargin(margin: Double): BaseDialogFragment<T> {
        mMargin = margin
        return this
    }

    /**
     * 设置进入退出动画
     *
     * @param animStyle
     * @return
     */
    open fun setAnimStyle(@StyleRes animStyle: Int): BaseDialogFragment<T> {
        mAnimStyle = animStyle
        return this
    }

    /**
     * 设置是否点击外部取消
     *
     * @param outCancel
     * @return
     */
    open fun setOutCancel(outCancel: Boolean): BaseDialogFragment<T> {
        mOutCancel = outCancel
        return this
    }

    open fun show(manager: FragmentManager): BaseDialogFragment<T> {
        super.show(manager, System.currentTimeMillis().toString())
        return this
    }


    open fun setOnDismissListener(dismissListener: OnDismissListener?) {
        onDismissListener = dismissListener
    }

    override fun dismiss() {
        super.dismiss()
        onDismissListener?.onDismiss(null)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onDismissListener?.onDismiss(null)
    }
}