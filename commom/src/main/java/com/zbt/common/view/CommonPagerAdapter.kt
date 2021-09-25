package com.zbt.common.view

import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.viewpager.widget.PagerAdapter


/**
 * Description: 抽象pageradpter
 * @Author: xuwd
 * Date: 2020/12/3 10:31
 *
 */
abstract class CommonPagerAdapter<T : View> : PagerAdapter() {
    /**
     * 当方法 [destroyItem]执行时，移出view保存到列表中，
     * 再次执行[instantiateItem]时使用已创建过的view数组
     */
    private val mPagerViewList = mutableListOf<T>()
    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return obj == view
    }

    @CallSuper
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = initView(mPagerViewList.removeFirstOrNull(), position)
        container.addView(view)
        return view
    }

    /**
     * 初始化要显示在pager上的view
     * @param view 当view非空时直接设置属性，当view为空时需要创建新的view对象
     *
     *示例：
     *
     *      fun initView(view:LinearLayout?):View{
     *          if(view==null){
     *             view=LinearLayout(context)
     *          }
     *
     *          //dosome
     *          return view
     *      }
     */
    abstract fun initView(view: T?, position: Int): View


    @CallSuper
    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        if (obj is View) {
            container.removeView(obj)
            mPagerViewList.add(obj as T)
        }
    }


}