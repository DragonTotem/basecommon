package com.zbt.common.utils

import androidx.annotation.FloatRange

/**
 * Description: 动画相关工具库
 * @Author: xuwd
 * Date: 2020/11/27 8:34
 *
 */
object AnimUtils {

    /**
     * 计算初始值变化到目标值的过程值
     * @param start 初始值
     * @param end 目标值
     * @param fraction 过程状态
     * @return 对应状态的过渡值Float
     */
    fun calculateFloatNum(start: Float, end: Float, @FloatRange(from = 0.0, to = 1.0) fraction: Float): Float {
        return start + (end - start) * fraction
    }

    /**
     * 计算初始值变化到目标值的过程值
     * @param start 初始值
     * @param end 目标值
     * @param fraction 过程状态
     * @return 对应状态的过渡值Int
     */
    fun calculateIntNum(start: Int, end: Int, @FloatRange(from = 0.0, to = 1.0) fraction: Float): Int {
        return start + ((end - start) * fraction).toInt()
    }
}