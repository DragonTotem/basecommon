package com.zbt.common.view

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group

/**
 * Description: View 相关工具类
 *
 * @Author: xuwd
 * Date: 2020/9/29 14:49
 */
object ViewUtils {
    private var DENSITY = 0f


    @JvmStatic fun dp2px(context: Context?, dp: Int): Int {
        if (DENSITY == 0f) {
            DENSITY = getDensity(context)
        }
        if(DENSITY==0f){
            return  dp
        }
        return (dp * DENSITY).toInt()
    }

    @JvmStatic fun px2dp(context: Context?, px: Int): Int {
        if (DENSITY == 0f) {
            DENSITY = getDensity(context)
        }
        if(DENSITY==0f){
            return  px
        }
        return (px / DENSITY).toInt()
    }

    /**
     * 获取屏幕逻辑密度
     */
    @JvmStatic fun getDensity(context: Context?): Float {
        return context?.resources?.displayMetrics?.density?:0f
    }

    /**
     * 获取屏幕实际宽度
     */
    @JvmStatic
    fun getPhoneWidth(context: Context?): Int {
        return context?.resources?.displayMetrics?.widthPixels ?: 0
    }

    /**
     * 获取屏幕实际高度
     */
    @JvmStatic
    fun getPhoneHeight(context: Context?): Int {
        return context?.resources?.displayMetrics?.heightPixels ?: 0
    }


    /**
     * 收起输入法
     */
    @JvmStatic
    fun hideIme(context: Activity?) {
        context?.apply {
            getSystemService(InputMethodManager::class.java)?.hideSoftInputFromWindow(context.currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }

    }
}

fun Group.setOnClickToViews(click: (View?) -> Unit) {

    val parent = this.parent
    if (parent != null && parent is ConstraintLayout) {
        referencedIds?.forEach { id ->
            parent.findViewById<View>(id).setOnClickListener { click(it) }
        }
    }

}