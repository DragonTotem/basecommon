package com.zbt.common.view

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.CallSuper
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import kotlin.math.abs

/**
 * Description: 滑动刷新基础抽象控件
 * @Author: xuwd
 * Date: 2020/12/15 12:32
 *
 */
abstract class SlideRefreshLayout
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr), NestedScrollingParent3 {

    companion object {
        const val TOP = 1
        const val BOTTOM = 1.shl(1)
        const val LEFT = 1.shl(2)
        const val RIGHT = 1.shl(3)
        const val POSITION = 0b1111
        const val ScrollTouch = 0
        const val ScrollFlip = 1
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(TOP, BOTTOM, LEFT, RIGHT)
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE, AnnotationTarget.PROPERTY)
    annotation class PositionMode

    private var mSlideLoading = false
    private var mIsOnTouch = false
    private val mParentHelper = NestedScrollingParentHelper(this)


    data class RefreshViews(
            val top: RefreshSubView? = null,
            val bottom: RefreshSubView? = null,
            val left: RefreshSubView? = null,
            val right: RefreshSubView? = null,
    )

    private var mRefreshViews = RefreshViews()
    private var mRequest = mutableMapOf<Int, () -> Unit>()
    private var mCurrentView: RefreshSubView? = null


    private var mCurrentApply = 0

    private var mScrollChild: RelativeLayout

    init {
        super.addView(RelativeLayout(context).apply {
            mScrollChild = this
        }, 0, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

    }

    private val mAnimObject = AnimObject().apply {
        mScrollLayout = mScrollChild
    }
    private var mAnimX = PropertyValuesHolder.ofFloat("translationX", 0f)
    private var mAnimY = PropertyValuesHolder.ofFloat("translationY", 0f)
    private var mAnim = ObjectAnimator().apply {
        duration = 200
        target = mAnimObject

    }

    private class AnimObject() {
        var mScrollLayout: View? = null
        var mRefreshSubView: RefreshSubView? = null
        var translationX = 0f
            get() {

                field = mScrollLayout?.translationX ?: 0f
                return field
            }
            set(value) {
                mScrollLayout?.translationX = value
                mRefreshSubView?.translationX = value
                field = value
            }
        var translationY = 0f
            get() {

                field = mScrollLayout?.translationY ?: 0f
                return field
            }
            set(value) {
                mScrollLayout?.translationY = value
                mRefreshSubView?.translationY = value
                field = value
            }
    }


    /**
     * 设置刷新请求
     * @param mode 方向
     * @param request 操作
     */
    fun setRefreshRequest(@PositionMode mode: Int, request: () -> Unit) {
        mRequest[mode] = request
    }

    /**
     * 停止当前刷新
     * @param success 请求成功与否
     */
    fun stopRequest(success: Boolean) {
        mCurrentView?.apply {
            endLoading(success)
            if (translationY != 0f || translationX != 0f) {
                mAnimY.setFloatValues(0f)
                mAnimX.setFloatValues(0f)
                mAnim.setValues(mAnimX, mAnimY)
                post {
                    mAnim.start()
                }

            }
        }
        mSlideLoading = false
    }


    fun setFreshSubView(views: RefreshViews) {
        mCurrentApply = 0
        mRefreshViews = views
        views.top?.apply {
            mCurrentApply = mCurrentApply.or(TOP)

            super.addView(getSubView(), 0, LayoutParams(LayoutParams.MATCH_PARENT, mBaseScrollDistance).apply {

                addRule(ALIGN_PARENT_TOP)
            })
            mPosition = TOP
        }
        views.bottom?.apply {
            mCurrentApply = mCurrentApply.or(BOTTOM)

            super.addView(getSubView(), 0, LayoutParams(LayoutParams.MATCH_PARENT, mBaseScrollDistance).apply {
                addRule(ALIGN_PARENT_BOTTOM)
            })
            mPosition = BOTTOM
        }
        views.left?.apply {

            mCurrentApply = mCurrentApply.or(LEFT)
            super.addView(getSubView(), 0, LayoutParams(mBaseScrollDistance, LayoutParams.MATCH_PARENT).apply {
                addRule(ALIGN_PARENT_START)
            })
            mPosition = LEFT
        }
        views.right?.apply {

            mCurrentApply = mCurrentApply.or(RIGHT)
            super.addView(getSubView(), 0, LayoutParams(mBaseScrollDistance, LayoutParams.MATCH_PARENT).apply {
                addRule(ALIGN_PARENT_END)
            })
            mPosition = RIGHT
        }
    }


    private fun handlePreScroll(dx: Int, dy: Int, consumed: IntArray, type: Int) {
        var useX = 0
        var useY = 0

        if (TOP == mCurrentApply.and(TOP)) {
            val y = mAnimObject.translationY
            if (y > 0 && dy > 0) {
                mAnimObject.translationY = 0.toFloat().coerceAtLeast(y - dy)
                useY = (y - mAnimObject.translationY).toInt()
                mRefreshViews.top?.apply {
                    countAndSetProgress(mScrollChild.translationY)
                }
            }

        }
        if (BOTTOM == mCurrentApply.and(BOTTOM)) {
            val y = mAnimObject.translationY
            if (y < 0 && dy < 0) {
                mAnimObject.translationY = 0.toFloat().coerceAtMost(y - dy)
                useY = (y - mAnimObject.translationY).toInt()
                mRefreshViews.bottom?.apply {
                    countAndSetProgress(mScrollChild.translationY)

                }
            }

        }
        if (LEFT == mCurrentApply.and(LEFT)) {
            val x = mAnimObject.translationX
            if (x > 0 && dx > 0) {
                mAnimObject.translationX = 0.toFloat().coerceAtLeast(x - dx)
                useX = (x - mAnimObject.translationX).toInt()
                mRefreshViews.left?.apply {
                    countAndSetProgress(mScrollChild.translationX)

                }
            }
        }
        if (RIGHT == mCurrentApply.and(RIGHT)) {
            val x = mAnimObject.translationX
            if (x < 0 && dx < 0) {
                mAnimObject.translationX = 0.toFloat().coerceAtMost(x - dx)
                useX = (x - mAnimObject.translationX).toInt()
                mRefreshViews.right?.apply {
                    countAndSetProgress(mScrollChild.translationX)

                }
            }
        }

        consumed[0] = useX
        consumed[1] = useY
    }

    private fun handleOnScroll(dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        if (mSlideLoading || type == ScrollTouch) {

            if (TOP == mCurrentApply.and(TOP)) {
                handleTopRefresh(dyUnconsumed)
            }
            if (BOTTOM == mCurrentApply.and(BOTTOM)) {
                handleBottomRefresh(dyUnconsumed)
            }
            if (LEFT == mCurrentApply.and(LEFT)) {
                handleLeftRefresh(dxUnconsumed)
            }
            if (RIGHT == mCurrentApply.and(RIGHT)) {
                handleRightRefresh(dxUnconsumed)
            }

        }
    }

    private fun handleBottomRefresh(dyUnconsumed: Int) {
        if (dyUnconsumed > 0) {
            mRefreshViews.bottom?.apply {
                if (mIsLoading != mSlideLoading) {
                    return@apply
                }
                mAnimObject.mRefreshSubView = this
                var y = mAnimObject.translationY.toInt()
                val max = mBaseScrollDistance.takeIf { mSlideLoading }
                        ?: (mBaseScrollDistance.coerceAtLeast(getMaxScrollDistance()))
                y = (-max).coerceAtLeast(y - dyUnconsumed)
                mAnimObject.translationY = y.toFloat()
                countAndSetProgress(mScrollChild.translationY)

            }
        }
    }

    private fun handleTopRefresh(dyUnconsumed: Int) {
        if (dyUnconsumed < 0) {
            mRefreshViews.top?.apply {
                if (mIsLoading != mSlideLoading) {
                    return@apply
                }
                mAnimObject.mRefreshSubView = this
                var y = mAnimObject.translationY.toInt()
                val max = mBaseScrollDistance.takeIf { mSlideLoading }
                        ?: (mBaseScrollDistance.coerceAtLeast(getMaxScrollDistance()))
                y = max.coerceAtMost(y - dyUnconsumed)
                mAnimObject.translationY = y.toFloat()
                countAndSetProgress(mScrollChild.translationY)

            }
        }
    }

    private fun handleLeftRefresh(dxUnconsumed: Int) {
        if (dxUnconsumed < 0) {
            mRefreshViews.left?.apply {
                if (mIsLoading != mSlideLoading) {
                    return@apply
                }
                mAnimObject.mRefreshSubView = this
                var x = mAnimObject.translationX.toInt()
                val max = mBaseScrollDistance.takeIf { mSlideLoading }
                        ?: (mBaseScrollDistance.coerceAtLeast(getMaxScrollDistance()))
                x = max.coerceAtMost(x - dxUnconsumed)
                mAnimObject.translationX = x.toFloat()
                countAndSetProgress(mScrollChild.translationX)

            }
        }
    }

    private fun handleRightRefresh(dxUnconsumed: Int) {
        if (dxUnconsumed > 0) {
            mRefreshViews.right?.apply {
                if (mIsLoading != mSlideLoading) {
                    return@apply
                }
                mAnimObject.mRefreshSubView = this
                var x = mAnimObject.translationX.toInt()
                val max = mBaseScrollDistance.takeIf { mSlideLoading }
                        ?: (mBaseScrollDistance.coerceAtLeast(getMaxScrollDistance()))
                x = (-max).coerceAtLeast(x - dxUnconsumed)
                mAnimObject.translationX = x.toFloat()
                countAndSetProgress(mScrollChild.translationX)

            }
        }
    }

    private fun checkRefresh(view: RefreshSubView?) {
        view?.apply {
            if (mIsReady) {
                startLoading()
                mSlideLoading = true
                mCurrentView = this
                mRequest[mPosition]?.invoke()
            }
        }
    }

    private fun handleStopTouch() {
        if (TOP == mCurrentApply.and(TOP)) {
            checkRefresh(mRefreshViews.top)
        }
        if (BOTTOM == mCurrentApply.and(BOTTOM)) {
            checkRefresh(mRefreshViews.bottom)
        }
        if (LEFT == mCurrentApply.and(LEFT)) {
            checkRefresh(mRefreshViews.left)
        }
        if (RIGHT == mCurrentApply.and(RIGHT)) {
            checkRefresh(mRefreshViews.right)
        }
    }


    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return mCurrentApply.and(POSITION) != 0 && axes != 0 && !mAnim.isRunning
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        mParentHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        mParentHelper.onStopNestedScroll(target, type)
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) {
        handleOnScroll(dxUnconsumed, dyUnconsumed, type)
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        handleOnScroll(dxUnconsumed, dyUnconsumed, type)

    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {

        handlePreScroll(dx, dy, consumed, type)
    }

    override fun getNestedScrollAxes(): Int {
        return mParentHelper.nestedScrollAxes
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                mIsOnTouch = !mSlideLoading && !mAnim.isRunning

            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                if (mIsOnTouch) {
                    handleStopTouch()
                    if (mSlideLoading) {
                        mCurrentView?.apply {
                            when (mPosition) {
                                TOP, LEFT -> {
                                    if (translationY.takeIf { TOP == mPosition } ?: translationX > mBaseScrollDistance) {

                                        (mAnimY.takeIf { TOP == mPosition } ?: mAnimX).apply {
                                            setFloatValues(mBaseScrollDistance.toFloat())
                                            mAnim.setValues(this)
                                        }
                                        mAnim.start()
                                    }
                                }
                                BOTTOM, RIGHT -> {
                                    if (translationY.takeIf { BOTTOM == mPosition } ?: translationX < -mBaseScrollDistance) {
                                        (mAnimY.takeIf { BOTTOM == mPosition } ?: mAnimX).apply {
                                            setFloatValues(-mBaseScrollDistance.toFloat())
                                            mAnim.setValues(this)
                                        }

                                        mAnim.start()
                                    }
                                }

                            }
                        }
                    } else {
                        mAnimX.setFloatValues(0f)
                        mAnimY.setFloatValues(0f)
                        mAnim.setValues(mAnimX, mAnimY)
                        mAnim.start()
                    }


                }
                mIsOnTouch = false

            }

        }


        return super.dispatchTouchEvent(ev)
    }


    abstract class RefreshSubView {
        @PositionMode
        var mPosition = 0
            set(value) {
                field = value
                mBaseTransCount = when (value) {
                    TOP, LEFT -> -mBaseScrollDistance.toFloat()

                    BOTTOM, RIGHT -> mBaseScrollDistance.toFloat()

                    else -> 0f
                }
                translationX = 0f
                translationY = 0f
            }
        var mIsReady = false
            protected set
        var mIsLoading = false
            protected set
        var mBaseScrollDistance = 0
            private set
            get() = getBaseScrollDistance()


        private var mBaseTransCount = 0f

        /**
         * 基础刷新滑动距离
         */
        abstract fun getBaseScrollDistance(): Int

        /**
         * 最大滑动距离
         */
        open fun getMaxScrollDistance(): Int {
            return mBaseScrollDistance
        }

        internal var translationX = 0f
            set(value) {

                when (mPosition) {
                    LEFT -> {
                        getSubView().translationX = (value + mBaseTransCount).coerceIn(mBaseTransCount, 0f)
                    }
                    RIGHT -> {

                        getSubView().translationX = (value + mBaseTransCount).coerceIn(0f, mBaseTransCount)
                    }

                }
                field = value
            }
        internal var translationY = 0f
            set(value) {

                when (mPosition) {
                    TOP -> {
                        getSubView().translationY = (value + mBaseTransCount).coerceIn(mBaseTransCount, 0f)
                    }
                    BOTTOM -> {

                        getSubView().translationY = (value + mBaseTransCount).coerceIn(0f, mBaseTransCount)
                    }
                }

                field = value
            }


        fun countAndSetProgress(translation: Float) {
            setProgress(mBaseScrollDistance.takeIf { it > 0 }?.let {
                abs(1.toFloat().coerceAtMost(translation / mBaseScrollDistance))
            } ?: 0f)
        }

        /**
         * 设置滑动模型比例
         */
        abstract fun setProgress(@FloatRange(from = 0.0, to = 1.0) progress: Float)

        /**
         * 滑动模型显示的View
         */
        abstract fun getSubView(): View

        @CallSuper
        open fun endLoading(success: Boolean) {
            mIsLoading = false
            mIsReady = false
        }

        @CallSuper
        open fun startLoading() {
            mIsReady = false
            mIsLoading = true
        }

    }


    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        mScrollChild.addView(child, index, params)
    }

    /**
     * 初始化刷新模型
     *
     * override fun initRefreshView(){
     *      setFreshSubView(SlideRefreshLayout.RefreshViews(top= object:SlideRefreshLayout.RefreshSubView(){
     *       var view=TextView(this@MainActivity).apply {
    setBackgroundColor(Color.BLUE)
    gravity=Gravity.CENTER
    }


    override fun getBaseScrollDistance(): Int {
    return ViewUtils.dp2px(this@MainActivity,50)
    }



    override fun setProgress(progress: Float) {
    if(!mIsLoading){
    mIsReady=progress>0.75f
    view.text= when{
    mIsReady->"准备加载"
    else->"等待加载"
    }
    }



    }
    override fun startLoading() {
    super.startLoading()
    view.text="加载中"
    }
    override fun getSubView(): View {
    return view
    }



    }
     *
     * }
     *
     *
     */
    abstract fun initRefreshView()
}