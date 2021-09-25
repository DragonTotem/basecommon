package com.zbt.common.view

import android.content.Context
import android.database.DataSetObserver
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.zbt.common.R


/**
 * Description: 导航点控件
 * @Author: xuwd
 * Date: 2020/10/14 14:47
 *
 */
class SlidingPointView
@JvmOverloads
constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        RelativeLayout(context, attrs, defStyleAttr), LifecycleObserver {
    private val innerLayout: LinearLayout = LinearLayout(context)
    var baseMargin = ViewUtils.dp2px(context,6)

    private val defaultFactory: PointViewFactory = object : PointViewFactory() {
        override fun createView(context: Context): View {
            return View(context).apply {
                setBackgroundResource(R.drawable.sliding_point)
                layoutParams = LinearLayout.LayoutParams(ViewUtils.dp2px(context, 6), ViewUtils.dp2px(context, 6)).apply {
                    if (innerLayout.orientation == LinearLayout.HORIZONTAL) {
                        topMargin = 0
                        marginStart = baseMargin
                    } else {
                        topMargin = baseMargin
                        marginStart = 0
                    }

                }
            }
        }
    }
    private var rawAdapter: PagerAdapter? = null
    var pointViewFactory: PointViewFactory? = defaultFactory;
    private val dataObserver = object : DataSetObserver() {
        override fun onChanged() {
            setSlidCount(rawAdapter?.count ?: 0)
        }
    }

    init {
        innerLayout.orientation = LinearLayout.HORIZONTAL
        addView(innerLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        if (context is LifecycleOwner) {
            context.lifecycle.addObserver(this)
        }
    }

    //滑动点个数
    private var count = 0
    var currentSelect: Int = -1
        private set
    private var curViewPager: ViewPager? = null



    fun setSlidCount(num: Int) {
        var temp = num
        if (temp < 1) {
            temp = 0;
        }
        if (count != temp) {
            count = temp
            resetCurrentPoint()
        }
    }

    /**
     * 设置横竖切换
     */
    fun setOrientation(@LinearLayoutCompat.OrientationMode orientation: Int) {
        innerLayout.orientation = orientation
    }

    private fun resetCurrentPoint() {
        if (count == innerLayout.childCount) {
            return
        }
        if (count > innerLayout.childCount) {
            for (i in innerLayout.childCount until count) {
                val view = createPointView()
                if (i == 0) {
                    (view.layoutParams as? LinearLayout.LayoutParams)?.apply {
                        topMargin = 0
                        marginStart = 0
                    }
                }
                innerLayout.addView(view)

            }
        } else {
            innerLayout.removeViews(count, innerLayout.childCount - count)
        }
        moveToPoint(currentSelect)
    }


    fun moveToPoint(index: Int) {
        var temp = index
        if (temp < 0) {
            temp = 0
        }
        temp %= innerLayout.childCount

        if (currentSelect == temp) {
            return
        }


        innerLayout.getChildAt(currentSelect)?.isSelected = false
        innerLayout.getChildAt(temp)?.isSelected = true
        currentSelect = temp

    }

    /**
     * 滑动点生成工厂
     */
    private fun createPointView(): View {
        return pointViewFactory?.createView(context) ?: defaultFactory.createView(context)

    }


    fun bindViewPager(pager: ViewPager?, adapter: PagerAdapter?) {
        curViewPager?.removeOnPageChangeListener(defaultPageChangeListener)
        pager?.apply {
            addOnPageChangeListener(defaultPageChangeListener)
            curViewPager = this
        }
        rawAdapter?.unregisterDataSetObserver(dataObserver)
        adapter?.apply {
            registerDataSetObserver(dataObserver)
            rawAdapter = this
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroyView() {
        if (context is LifecycleOwner) {
            (context as LifecycleOwner).lifecycle.removeObserver(this)
        }
        curViewPager?.removeOnPageChangeListener(defaultPageChangeListener)
        rawAdapter?.unregisterDataSetObserver(dataObserver)

    }

    abstract class PointViewFactory {
        abstract fun createView(context: Context): View
    }


    private val defaultPageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            moveToPoint(position)
        }
    }


}