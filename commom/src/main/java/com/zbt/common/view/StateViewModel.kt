package com.zbt.common.view

import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Description: ViewModel状态基类
 * @Author: xuwd
 * Date: 2020/11/13 14:08
 *
 */
abstract class StateViewModel : ViewModel() {
    protected val viewState = MutableLiveData<ViewState>()

    private val mViewActionMap = hashMapOf<String, () -> Unit>()

    companion object State {
        const val Normal = 0//正常情况
        const val Loading = 1//加载中
        const val NetError = 2//请求网络错误
        const val NoContent = 3//无数据
    }

    /**
     * 页面状态
     * @param state  对应State
     * @param errorCode errorMsg 状态为NetError时才有用
     */
    data class ViewState(val state: Int, val errorCode: Int = 0, val errorMsg: String? = "")


    /**
     * 提供可观察状态给页面布局
     */
    fun getViewState(): LiveData<ViewState> {
        return viewState
    }

    fun putAction(actionName: String, action: () -> Unit) {
        mViewActionMap[actionName] = action
    }

    fun invokeAction(actionName: String) {
        mViewActionMap[actionName]?.invoke()
    }

    @CallSuper
    override fun onCleared() {
        mViewActionMap.clear()
    }
}