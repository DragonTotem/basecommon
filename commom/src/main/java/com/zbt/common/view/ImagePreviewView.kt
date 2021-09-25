package com.zbt.common.view


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.children
import androidx.core.view.setPadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewpager.widget.ViewPager
import com.zbt.common.R
import com.zbt.common.aop.DisableFastCall
import com.zbt.common.app.uiinterface.IReset
import com.zbt.common.entity.ImageEntity
import com.zbt.common.image.ImageLoader
import com.zbt.common.log.LogUtils
import com.zbt.common.utils.AnimUtils.calculateFloatNum
import com.zbt.common.utils.AnimUtils.calculateIntNum
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Description: 图片列表预览容器
 * @Author: xuwd
 * Date: 2020/11/26 20:02
 *
 */
class ImagePreviewView
@JvmOverloads
constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr), LifecycleObserver {


    companion object {
        private const val sAnimTime = 400L
    }

    private val mCardView = CardView(context!!).apply {
        cardElevation = 0f
        radius = 0f
    }

    private val mViewPager = ViewPager(context!!).apply {
        addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            private var mLastPage = 0
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {

                if (!mShowAnim.isRunning) {
                    mPageChangeListener?.onPageSelected(position)
                }
                mNumText.text = "${position + 1}/${mDefaultAdapter.count}"
                mLastPage = position
            }

            override fun onPageScrollStateChanged(state: Int) {

                mPageChangeListener?.onPageScrollStateChanged(state)
                if (mTouchScaleAble && state == ViewPager.SCROLL_STATE_IDLE) {
                    this@apply.children.forEach { v ->
                        if (v is IReset) {
                            val rect = Rect()
                            v.getLocalVisibleRect(rect)
                            if (rect.left < 0 || rect.left >= rect.width()) {
                                v.resetInit()
                            }
                        }

                    }
                }

            }

        })
    }


    var mCurrentRect: Rect = Rect()
    private var mCurrentRadius = 0f
    var mScaleType = ImageView.ScaleType.FIT_CENTER
    var mTouchScaleAble = false
    var closeImageClick = false
    private val mShowAnim = ValueAnimator.ofFloat(0f).apply {
        addUpdateListener { anim ->
            mCardView.apply {

                translationX = calculateFloatNum(mCurrentRect.left.toFloat(), 0f, anim.animatedFraction)
                translationY = calculateFloatNum(mCurrentRect.top.toFloat(), 0f, anim.animatedFraction)

                layoutParams.apply {
                    width = calculateIntNum(mCurrentRect.width(), this@ImagePreviewView.width, anim.animatedFraction)
                    height = calculateIntNum(mCurrentRect.height(), this@ImagePreviewView.height, anim.animatedFraction)
                    layoutParams = this
                }

            }

        }
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                mCardView.apply {
                    translationY = 0f
                    translationX = 0f
                    radius = 0f
                    layoutParams.apply {
                        width = this@ImagePreviewView.width
                        height = this@ImagePreviewView.height
                        layoutParams = this
                    }
                }

            }

            override fun onAnimationStart(animation: Animator?) {
                mCardView.apply {

                    translationY = mCurrentRect.top.toFloat()
                    translationX = mCurrentRect.left.toFloat()

                    radius = mCurrentRadius
                    layoutParams.apply {
                        width = mCurrentRect.width()
                        height = mCurrentRect.height()
                        layoutParams = this
                    }
                }


            }
        })
        duration = sAnimTime

    }

    private val mHideAnim = ValueAnimator.ofFloat(0f).apply {
        addUpdateListener { anim ->
            mCardView.apply {
                translationX = calculateFloatNum(0f, mCurrentRect.left.toFloat(), anim.animatedFraction)
                translationY = calculateFloatNum(0f, mCurrentRect.top.toFloat(), anim.animatedFraction)
                layoutParams.apply {
                    width = calculateIntNum(this@ImagePreviewView.width, mCurrentRect.width(), anim.animatedFraction)
                    height = calculateIntNum(this@ImagePreviewView.height, mCurrentRect.height(), anim.animatedFraction)
                    layoutParams = this
                }
            }

        }
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                mCardView.apply {
                    translationY = mCurrentRect.top.toFloat()
                    translationX = mCurrentRect.left.toFloat()
                    layoutParams.apply {
                        width = mCurrentRect.width()
                        height = mCurrentRect.height()
                        layoutParams = this
                    }
                }
                if (Build.VERSION.SDK_INT >= 30) {
                    windowInsetsController?.apply {
                        systemBarsBehavior = lastSystemBarsBehavior
                        insetMap.forEach { (type, visible) ->
                            if (visible) {
                                show(type)
                            }
                        }

                        (context as Activity).findViewById<ViewGroup>(android.R.id.content)?.getChildAt(0)?.setPadding(0)
                    }
                }

                this@ImagePreviewView.parent?.let {
                    (it as ViewGroup).removeView(this@ImagePreviewView)
                }

            }

            override fun onAnimationStart(animation: Animator?) {
                mCardView.radius = mCurrentRadius
            }
        })
        duration = sAnimTime
    }


    var mPageChangeListener: ViewPager.OnPageChangeListener? = null

    private val mDefaultAdapter = object : CommonPagerAdapter<ScaleImageView>() {
        private val colorList = listOf(R.color.common_ui_load_bg1, R.color.common_ui_load_bg2, R.color.common_ui_load_bg3,
                R.color.common_ui_load_bg4, R.color.common_ui_load_bg5)

        var mImageList: List<ImageEntity>? = ArrayList()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getCount(): Int {
            return mImageList?.size ?: 0
        }


        override fun initView(view: ScaleImageView?, position: Int): View {
            return (view ?: ScaleImageView(context!!)).apply {
                layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                scaleType = mScaleType
                val place = colorList.random()
                val img = mImageList?.getOrNull(position)
                setBackgroundColor(Color.BLACK)
                ImageLoader.loadImageNoCenter(context, img?.url, this, place, place)
                mTouchable = mTouchScaleAble
                setOnClickListener {
                    if (!closeImageClick) {
                        exitPreview()
                    }
                }
            }
        }


    }
    private var mNumText: TextView
    private val Log = LogUtils(this)

    init {
        setOnClickListener {}
        mCardView.addView(mViewPager, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(mCardView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(TextView(context).apply {
            mNumText = this
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            addRule(CENTER_HORIZONTAL)
            addRule(ALIGN_PARENT_BOTTOM)
            bottomMargin = ViewUtils.dp2px(context, 17)
        })
        mViewPager.adapter = mDefaultAdapter
        setOnKeyListener { v, keyCode, event ->
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (event.action == KeyEvent.ACTION_UP) {
                        exitPreview()
                    }
                    true
                }
            }
            false
        }


        isFocusable = true
        isFocusableInTouchMode = true
        if (context is LifecycleOwner) {
            context.lifecycle.addObserver(this)
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) return true
        return super.onKeyDown(keyCode, event)
    }

    private var lastSystemBarsBehavior = 0
    private val insetMap = mutableMapOf<Int, Boolean>()

    @SuppressLint("SetTextI18n")
    @DisableFastCall
    fun showImage(images: List<ImageEntity>, position: Int = 0, rect: Rect = Rect(), radius: Float = 0f) {
        if (images.isEmpty()) return
        if (mShowAnim.isRunning || mHideAnim.isRunning) {
            return
        }
        mCurrentRect = rect
        mCurrentRadius = radius
        if (context is Activity) {
            (context as Activity).findViewById<ViewGroup>(android.R.id.content)?.apply {
                addView(this@ImagePreviewView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                if (Build.VERSION.SDK_INT >= 30) {
                    windowInsetsController?.apply {
                        insetMap[WindowInsets.Type.statusBars()] = rootWindowInsets.isVisible(WindowInsets.Type.statusBars())
                        insetMap[WindowInsets.Type.navigationBars()] = rootWindowInsets.isVisible(WindowInsets.Type.navigationBars())
                        insetMap[WindowInsets.Type.captionBar()] = rootWindowInsets.isVisible(WindowInsets.Type.captionBar())
                        getChildAt(0)?.apply {
                            var top = 0
                            var bottom = 0
                            if (rootWindowInsets.isVisible(WindowInsets.Type.navigationBars())) {
                                top = rootWindowInsets.getInsets(WindowInsets.Type.statusBars()).top
                            }
                            if (rootWindowInsets.isVisible(WindowInsets.Type.navigationBars())) {
                                bottom = rootWindowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom
                            }
                            setPadding(0, top, 0, bottom)

                        }
                        hide(WindowInsets.Type.systemBars())
                        lastSystemBarsBehavior = systemBarsBehavior
                        systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                }

                showAnim()
                setImageList(images, position)


            }
        }
        requestFocus()
    }


    fun setImageList(images: List<ImageEntity>, position: Int = 0) {
        mDefaultAdapter.mImageList = images
        mViewPager.currentItem = position
        mNumText.text = "${mViewPager.currentItem + 1}/${mDefaultAdapter.count}"
    }


    private fun showAnim() {

        if (!mShowAnim.isRunning) {
            mShowAnim.start()
        }

    }

    private fun hideAnim() {
        if (!mHideAnim.isRunning) {
            mHideAnim.start()
        }
    }

    private fun exitPreview() {
        if (mShowAnim.isRunning || mHideAnim.isRunning) {
            return
        }

        hideAnim()

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun clean() {
        if (context is LifecycleOwner) {
            (context as LifecycleOwner).lifecycle.removeObserver(this)
        }
        mPageChangeListener = null
    }

    @SuppressLint("ClickableViewAccessibility")
    class ScaleImageView @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
        : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr), IReset {


        companion object {
            private const val MinScale = 1f
            private const val MaxScale = 4f
            private const val NONE = 0 //正常模式
            private const val DRAG = 1 //拖动模式
            private const val ZOOM = 2 //缩放模式
        }

        private val mInitRectF = RectF()
        private val mCurrentRectF = RectF()
        private val mCurrentMatrix = Matrix()
        private val mSaveMatrix = Matrix()


        private var mCurrentMode = NONE

        private val mStartPoint = PointF()
        private val mMidPoint = PointF()
        var mClickable = false
        private var mSecondDistance = 0f
        var mTouchable = false
        private var mClickListener: OnClickListener? = null

        override fun setOnClickListener(listener: OnClickListener?) {
            mClickListener = listener
            super.setOnClickListener(listener)
        }

        init {

            setOnTouchListener { v, event ->
                var handleTouch = true
                when (event.action.and(MotionEvent.ACTION_MASK)) {
                    MotionEvent.ACTION_DOWN -> {
                        mClickable = true
                        mStartPoint.set(event.x, event.y)
                        mCurrentMode = DRAG
                        mSaveMatrix.set(mCurrentMatrix)
                        if (parent is ViewGroup) {
                            parent.requestDisallowInterceptTouchEvent(mTouchable)
                        }
                    }

                    MotionEvent.ACTION_POINTER_DOWN -> {
                        if (mTouchable) {
                            mClickable = false
                            mSecondDistance = spacing(event)
                            midPoint(mMidPoint, event)
                            mCurrentMode = ZOOM
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        if (mClickable) {
                            mClickListener?.onClick(this)
                        }
                        mCurrentMode = NONE
                        center()
                    }

                    MotionEvent.ACTION_POINTER_UP -> {
                        mCurrentMode = NONE
                    }


                    MotionEvent.ACTION_MOVE -> {
                        if (abs(event.x - mStartPoint.x) > 4 || abs(event.y - mStartPoint.y) > 4) {
                            mClickable = false
                        }
                        if (mTouchable) {
                            when (mCurrentMode) {
                                DRAG -> {
                                    mCurrentMatrix.set(mSaveMatrix)
                                    mCurrentMatrix.mapRect(mCurrentRectF, mInitRectF)
                                    countTranslate(mCurrentMatrix, mCurrentRectF, event)
                                    imageMatrix = mCurrentMatrix
                                    mCurrentMatrix.mapRect(mCurrentRectF, mInitRectF)
                                    if (parent is ViewGroup) {
                                        val intercept = mCurrentRectF.left >= 0 || mCurrentRectF.right <= width
                                        parent.requestDisallowInterceptTouchEvent(!intercept)
                                        if (!intercept) {
                                            handleTouch = false
                                        }
                                    }

                                }

                                ZOOM -> {
                                    val scale = spacing(event) / mSecondDistance
                                    mCurrentMatrix.set(mSaveMatrix)
                                    mCurrentMatrix.postScale(scale, scale, mMidPoint.x, mMidPoint.y)

                                }
                            }
                        }


                    }
                }
                imageMatrix = mCurrentMatrix

                handleTouch
            }
        }

        override fun resetInit() {
            mCurrentMatrix.reset()
            imageMatrix = mCurrentMatrix
            center()
        }


        private fun center() {
            val scale = checkScale(mCurrentMatrix)
            if (scale != 1f) {
                mCurrentMatrix.postScale(scale, scale, mMidPoint.x, mMidPoint.y)
            }
            mCurrentMatrix.mapRect(mCurrentRectF, mInitRectF)

            var transX = 0f
            var transY = 0f
            transY = when {
                mCurrentRectF.height() < height -> (height - mCurrentRectF.height()) / 2 - mCurrentRectF.top
                mCurrentRectF.top > 0           -> -mCurrentRectF.top
                mCurrentRectF.bottom < height   -> height - mCurrentRectF.bottom
                else                            -> transY
            }
            transX = when {
                mCurrentRectF.width() < width -> (width - mCurrentRectF.width()) / 2 - mCurrentRectF.left
                mCurrentRectF.left > 0        -> -mCurrentRectF.left
                mCurrentRectF.right < width   -> width - mCurrentRectF.right
                else                          -> transX
            }
            mCurrentMatrix.postTranslate(transX, transY)
            imageMatrix = mCurrentMatrix

        }

        private fun countTranslate(matrix: Matrix, rect: RectF, event: MotionEvent) {
            var translateX = event.x - mStartPoint.x
            var translateY = event.y - mStartPoint.y
            translateY = when {
                rect.top >= 0 && rect.bottom <= height -> 0f
                rect.top + translateY >= 0             -> -rect.top
                rect.bottom + translateY <= height     -> height - rect.bottom
                else                                   -> translateY
            }

            translateX = when {
                rect.left >= 0 && rect.right <= width -> 0f
                rect.left + translateX >= 0           -> -rect.left
                rect.right + translateX <= width      -> width - rect.right
                else                                  -> translateX
            }
            matrix.postTranslate(translateX, translateY)

        }

        private fun checkScale(matrix: Matrix): Float {
            val value = FloatArray(9) { 0f }
            matrix.getValues(value)

            return when {
                value[0] < MinScale -> MinScale / value[0]
                value[0] > MaxScale -> MaxScale / value[0]
                else                -> 1f
            }

        }

        private fun spacing(event: MotionEvent): Float {
            val x = event.getX(0) - event.getX(1)
            val y = event.getY(0) - event.getY(1)
            return sqrt(x * x + y * y)
        }


        private fun midPoint(point: PointF, event: MotionEvent) {
            val x = event.getX(0) + event.getX(1)
            val y = event.getY(0) + event.getY(1)
            point.set(x / 2, y / 2)
        }


        override fun setImageDrawable(drawable: Drawable?) {
            super.setImageDrawable(drawable)
            drawable?.apply {
                if (mTouchable) {
                    if (bounds.width() > 0 && bounds.height() > 0) {
                        mInitRectF.set(bounds)
                        scaleType = ScaleType.MATRIX
                        resetInit()
                    }
                }

            }
        }
    }
}