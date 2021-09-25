package com.basecommon.bn.app

import com.zbt.common.app.CommonApplication
import com.zbt.common.log.LogUtils
import com.zbt.common.network.HttpRequest
import dagger.hilt.android.HiltAndroidApp

/**
 * Description:
 * @Author: xuwd
 * Date: 2020/11/19 19:30
 *
 */
@HiltAndroidApp
class MyApplication :CommonApplication(){
    override fun onCreate() {
        super.onCreate()
        LogUtils.setDebugAble(true)
        HttpRequest.enableLog = true
    }
}