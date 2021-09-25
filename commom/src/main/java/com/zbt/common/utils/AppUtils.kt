package com.zbt.common.utils

import android.app.Application
import android.os.Build
import android.os.Process
import java.io.BufferedReader
import java.io.FileReader

/**
 * Description: App常用工具集
 * @Author: xuwd
 * Date: 2020/11/23 14:23
 *
 */
object AppUtils {
    /**
     * 获取当前进程名称
     */
    @JvmStatic
    fun getProcessName(context: Application):String{
        if(Build.VERSION.SDK_INT>=28){
            return Application.getProcessName()
        }
        return try {
            BufferedReader(FileReader("/proc/${Process.myPid()}/cmdline")).use {
                it.readLine().trim()
            }
        }catch (e:Exception){
            e.printStackTrace()
            context.packageName
        }
    }
}