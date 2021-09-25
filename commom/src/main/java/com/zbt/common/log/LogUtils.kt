package com.zbt.common.log

import android.app.Application
import android.os.Looper
import android.os.Process
import android.util.Log
import com.zbt.common.utils.AppUtils


class LogUtils : ILogInterface {
    private constructor() {}

    private var tag = "simple"

    constructor(obj: Any?) {
        if (obj != null) {
            tag = obj.javaClass.toString()
        }
    }


    override fun i(msg: String?) {
        if (checkInit()) {
            return
        }
        Xlog.logWrite2(LEVEL_INFO, tag, processName, "",
                0, Process.myPid(), Process.myPid().toLong(), Looper.getMainLooper().thread.id, msg)
    }

    override fun d(msg: String?) {
        if (checkInit()) {
            return
        }
        Xlog.logWrite2(LEVEL_DEBUG, tag, processName, "",
                0, Process.myPid(), Process.myPid().toLong(), Looper.getMainLooper().thread.id, msg)
    }

    override fun w(msg: String?) {
        if (checkInit()) {
            return
        }
        Xlog.logWrite2(LEVEL_WARNING, tag, processName, "",
                0, Process.myPid(), Process.myPid().toLong(), Looper.getMainLooper().thread.id, msg)
    }

    override fun e(msg: String?) {
        if (checkInit()) {
            return
        }
        Xlog.logWrite2(LEVEL_ERROR, tag, processName, "",
                0, Process.myPid(), Process.myPid().toLong(), Looper.getMainLooper().thread.id, msg)
    }

    companion object {
        private var mNeedInit = true
        private val xlog = Xlog()

        /**
         * 初始化方法在Application中执行
         */
        internal fun initLog(context: Application) {
            Xlog.logInit(context)
            processName = AppUtils.getProcessName(context)
            mNeedInit = false
        }

        private var processName: String? = null

        private fun checkInit(): Boolean {
            if (mNeedInit) {
                Log.w("com.zbt.common.log.LogUtils", "未执行初始化方法，无法输出日志")
                return true
            }
            return false
        }

        /**
         * 默认关闭debug模式
         * 可在application初始化时打开
         * @param debug true表示打开debug模式，控制台输出日志，release时关闭
         */
        @JvmStatic
        fun setDebugAble(debug: Boolean) {
            Xlog.setConsoleLogOpen(debug)
            Xlog.setLogLevel(if (debug) LEVEL_DEBUG else LEVEL_INFO)
        }
        const val LEVEL_VERBOSE = 0
        const val LEVEL_DEBUG = 1
        const val LEVEL_INFO = 2
        const val LEVEL_WARNING = 3
        const val LEVEL_ERROR = 4
        const val LEVEL_FATAL = 5
        fun appenderFlush(){
            if (checkInit()) return
            xlog.appenderFlush(false)
        }
        fun appenderClose() {
            if (checkInit()) return
            xlog.appenderClose()
        }

        internal fun globalE(msg: String?) {
            if (checkInit()) return
            Xlog.logWrite2(LEVEL_ERROR, "global", processName, "",
                    0, Process.myPid(), Process.myPid().toLong(), Looper.getMainLooper().thread.id, msg)
        }

        internal fun mmkvW(msg: String?) {
            if (checkInit()) return
            Xlog.logWrite2(LEVEL_ERROR, "MMKV", processName, "",
                    0, Process.myPid(), Process.myPid().toLong(), Looper.getMainLooper().thread.id, msg)
        }
    }
}