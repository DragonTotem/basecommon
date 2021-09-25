package com.zbt.common.app

import android.app.Activity
import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.FragmentActivity
import com.alibaba.android.arouter.launcher.ARouter
import com.zbt.common.entity.NetStateEntity
import com.zbt.common.log.LogUtils
import com.zbt.common.permission.BiometricsUtils
import com.zbt.common.permission.PermissionUtil
import com.zbt.common.storage.BNKV
import com.zbt.common.utils.AppUtils
import com.zbt.common.utils.NetUtils
import me.jessyan.autosize.AutoSize
import me.jessyan.autosize.AutoSizeConfig
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

open class CommonApplication : Application() {

    private lateinit var lifecycleCallbacks:ActivityLifecycleCallbacks
    private val activityList= mutableListOf<Activity>()

    companion object{
        public lateinit var application:CommonApplication
    }


    override fun onCreate() {
        super.onCreate()
        application = this
        if (packageName == AppUtils.getProcessName(this)) {
            initMainProcess()
        } else {
            initOtherProcess()
        }
    }

    /**
     * 主进程初始化流程 onCreate中执行 和[initOtherProcess]不会同时触发
     */
    @CallSuper
    open fun initMainProcess() {
        initDefaultProcess()
    }

    /**
     * 其他线程初始化流程onCreate中执行和[initMainProcess]不会同时触发
     */
    @CallSuper
    open fun initOtherProcess() {
        initDefaultProcess()
    }

    /**
     * 获取当前activity
     */
    open fun getCurrentActivity(): Activity? {
        return activityList.lastOrNull()
    }

    private fun initDefaultProcess() {

        LogUtils.initLog(this)
        LogUtils.setDebugAble(false)
        AutoSize.initCompatMultiProcess(this)
        AutoSizeConfig.getInstance().setExcludeFontScale(true).setLog(false)
        BNKV.initMMkv(this)
        ARouter.init(this)
        lifecycleCallbacks = object : ActivityLifecycleCallbacksAdapter() {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activityList.add(activity)
            }

            override fun onActivityDestroyed(activity: Activity) {
                LogUtils.appenderFlush()
                activityList.remove(activity)
            }
        }
        registerActivityLifecycleCallbacks(lifecycleCallbacks)

        NetUtils.registerDefaultNetworkCallback(this, networkCallback)

        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            e.apply {
                StringWriter().use {
                    printStackTrace(PrintWriter(it))
                    it.flush()
                    LogUtils.globalE(it.toString())
                    LogUtils.appenderClose()
                }
            }

        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            val netType = when {
                NetUtils.isWifiConnected(this@CommonApplication) -> {
                    NetStateEntity.NetType.Wifi
                }
                NetUtils.isMobileConnected(this@CommonApplication) -> {
                    NetStateEntity.NetType.Mobile
                }
                else -> {
                    NetStateEntity.NetType.Other
                }
            }

            NetStateEntity.mNetState.postValue(NetStateEntity.NetState(true, netType))
        }

        override fun onLost(network: Network) {
            NetStateEntity.mNetState.postValue(NetStateEntity.NetState(false, NetStateEntity.NetType.None))
        }

    }


    enum class ExitMode {
        ClearTask, KillApp
    }

    /**
     * 退出App
     * @param mode 退出模式 枚举量，默认清除堆栈模式[ExitMode.ClearTask]，可选清除堆栈并关闭进程模式 [ExitMode.KillApp]
     *
     */
    open fun exitApp(mode:ExitMode=ExitMode.ClearTask){
       when(mode){
           ExitMode.ClearTask ->{
               clearTask()
           }
           ExitMode.KillApp ->{
               clearTask()
               unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
               LogUtils.appenderClose()
               exitProcess(0)
           }
       }
    }
    private fun clearTask(){
        activityList.forEach {activity ->
            activity.finish()
        }
    }



    open class ActivityLifecycleCallbacksAdapter : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

        }

        override fun onActivityStarted(activity: Activity) {

        }

        override fun onActivityResumed(activity: Activity) {

        }

        override fun onActivityPaused(activity: Activity) {

        }

        override fun onActivityStopped(activity: Activity) {

        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

        }

        override fun onActivityDestroyed(activity: Activity) {

        }

    }
}