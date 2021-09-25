package com.zbt.common.view


import android.content.Context
import android.database.DataSetObserver
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.zbt.common.entity.ImageEntity
import com.zbt.common.image.ImageLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Description: 标准banner控件
 * @Author: xuwd
 * Date: 2020/10/15 8:43
 *
 */
class BannerView
@JvmOverloads
constructor(context: Context?, attrs: AttributeSet?=null, defStyleAttr: Int=0):
        RelativeLayout(context, attrs, defStyleAttr),LifecycleObserver {
    var autoScroll = true
        set(value) {
            if (value != field) {
                if (value) {
                    scrollJob?.start()
                } else {
                    scrollJob?.cancel()
                }
            }
            field = value
        }
    private var closeScroll = false
    private val slidingPointView = SlidingPointView(context)
    private val marginBottom = ViewUtils.dp2px(context, -12).toFloat()
    private var rawAdapter: PagerAdapter? = null
    private var scrollJob: Job? = null
    private var inTouching = false
    private val viewPager: ViewPager = object : ViewPager(context!!) {
        init {
            clipToPadding = false
            setPadding(ViewUtils.dp2px(context, 20), 0, ViewUtils.dp2px(context, 20), 0)
        }

        override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
            parent?.requestDisallowInterceptTouchEvent(true)
            return super.dispatchTouchEvent(ev)
        }

        override fun onTouchEvent(ev: MotionEvent?): Boolean {
            if (closeScroll) return false
            inTouching = when (ev?.action) {
                MotionEvent.ACTION_DOWN -> true
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> false
                else -> true
            }
            return super.onTouchEvent(ev)
        }

        override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
            if (closeScroll) return false
            return super.onInterceptTouchEvent(ev)
        }

    }

    private val dataObserver = object : DataSetObserver() {
        override fun onChanged() {
            closeScroll = rawAdapter?.count ?: 0 < 2
            delegateAdapter.notifyDataSetChanged()
        }
    }

    /**
     * 设置滑动点距离底部边距
     */
    fun setSlidingMargin(marginBottom: Int) {
        slidingPointView.translationY = -ViewUtils.dp2px(context, marginBottom).toFloat()
    }

    /**
     * 设置是否显示滑动点
     */
    fun setSlidingHide(hide: Boolean) {
        slidingPointView.visibility = if (hide) GONE else VISIBLE
    }

    fun setDefaultImageList(list: ArrayList<ImageEntity>) {
        defaultAdapter.imageList = list
        viewPager.offscreenPageLimit = 4.coerceAtMost(list.size)

    }

    fun setSlidingFactory(factory: SlidingPointView.PointViewFactory) {
        slidingPointView.pointViewFactory = factory
    }


    fun setAdapter(adapter: PagerAdapter) {
        rawAdapter?.unregisterDataSetObserver(dataObserver)
        rawAdapter = adapter.apply { registerDataSetObserver(dataObserver) }
        slidingPointView.bindViewPager(viewPager, rawAdapter)
    }


    fun addOnPageChangeListener(listener: ViewPager.OnPageChangeListener) {
        viewPager.addOnPageChangeListener(listener)
    }

    fun setCurrentItem(item: Int, smoothScroll: Boolean = false) {
        viewPager.setCurrentItem(item, smoothScroll)
    }

    fun getCurrentItem(): Int {
        return viewPager.currentItem
    }


    private val defaultAdapter = object : CommonPagerAdapter<RoundImageView>() {
        var imageList: ArrayList<ImageEntity>? = ArrayList()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getCount(): Int {
            return imageList?.size ?: 0
        }

        override fun initView(view: RoundImageView?, position: Int): View {
            return (view ?: RoundImageView(context!!)).apply {
                round = ViewUtils.dp2px(context, 12).toFloat()
                layoutParams = LayoutParams(ViewUtils.dp2px(context, 320), ViewUtils.dp2px(context, 120))
                ImageLoader.loadImageNoCenter(context, imageList?.getOrNull(position)?.url, this)
            }
        }


    }

    private val delegateAdapter = object : PagerAdapter() {
        override fun getCount(): Int {
            return if (rawAdapter?.count ?: 0 > 2) {
                Int.MAX_VALUE
            } else {
                rawAdapter?.count ?: 0
            }

        }


        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            return rawAdapter?.instantiateItem(container, position % rawAdapter!!.count)
                    ?: container
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            rawAdapter?.destroyItem(container, position % rawAdapter?.count!!, obj)

        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return obj == view
        }

    }

    init {

        viewPager.pageMargin = ViewUtils.dp2px(context, 20)

        addView(viewPager, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        val layoutParams = LayoutParams(-2, -2).apply {
            addRule(ALIGN_PARENT_BOTTOM)
            addRule(CENTER_HORIZONTAL)
        }
        addView(slidingPointView, layoutParams)
        slidingPointView.translationY = marginBottom
        setAdapter(defaultAdapter)
        viewPager.adapter = delegateAdapter

        if (context is LifecycleOwner) {
            context.lifecycle.addObserver(this)
        }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun removeLife() {
        if (context is LifecycleOwner) {
            (context as LifecycleOwner).lifecycle.removeObserver(this)
        }
        rawAdapter?.unregisterDataSetObserver(dataObserver)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun startAutoScroll() {
        if (autoScroll) {
            if (context is CoroutineScope) {
                scrollJob = (context as CoroutineScope).launch() {
                    repeat(100000) {
                        delay(5000)
                        if (rawAdapter?.count!! > 1 && !inTouching) {
                            viewPager.setCurrentItem(viewPager.currentItem + 1, true)
                        }

                    }
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun stopAutoScroll() {
        if (autoScroll) {
            scrollJob?.cancel()
        }
    }

}