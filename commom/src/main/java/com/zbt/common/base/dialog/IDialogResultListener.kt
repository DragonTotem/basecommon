package com.zbt.common.base.dialog

/**
 * author：   HUlq
 * date：     2020/12/10 & 17:41
 * desc       数据回调接口
 * modify by
 */
interface IDialogResultListener<T> {
    fun onDataResult(result: T)
}