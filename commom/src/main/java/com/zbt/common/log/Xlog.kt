package com.zbt.common.log

import android.app.Application
import androidx.annotation.Keep
import com.zbt.common.utils.AppUtils

/**
 * Description: wx xlog
 * @Author: xuwd
 * Date: 2020/11/23 10:37
 *
 */
@Keep
internal class Xlog {

    @Keep
    internal class XLoggerInfo {
        var level = 0
        var tag: String? = null
        var filename: String? = null
        var funcname: String? = null
        var line = 0
        var pid: Long = 0
        var tid: Long = 0
        var maintid: Long = 0
    }

    companion object{
        private val key="47bd4350be28cfac3e794ed0101f720001a6fb4c0cb5de4a98703eddfdb574c1e7e3a0c837b6f38b39f34784c57c152764bfc4a089f0e4ee750170f0ce553743"
        init {
            System.loadLibrary("c++_shared")
            System.loadLibrary("marsxlog")
        }
        fun logInit(context: Application){

            appenderOpen(LogUtils.LEVEL_DEBUG, 0,
                    context.filesDir.absolutePath.plus(cacheDir),
                    context.filesDir.absolutePath.plus(logDir),
                    AppUtils.getProcessName(context), 0, "")
        }

        const val cacheDir="/Log/logCache"
        const val logDir="/Log/rawLog"
        @JvmStatic
        external fun setLogLevel(logLevel: Int)
        @JvmStatic
        external fun logWrite(logInfo: XLoggerInfo?, log: String?)
        @JvmStatic
        external fun appenderOpen(level: Int, mode: Int, cacheDir: String, logDir: String, nameprefix: String, cacheDays: Int, pubkey: String)
        @JvmStatic
        external fun setAppenderMode(mode: Int)
        @JvmStatic
        external fun setConsoleLogOpen(isOpen: Boolean) //set whether the console prints log

        @JvmStatic
        external fun setErrLogOpen(isOpen: Boolean) //set whether the  prints err log into a separate file


        @JvmStatic
        external fun setMaxFileSize(size: Long)
        /**
         * should be called before appenderOpen to take effect
         * @param duration alive seconds
         */
        @JvmStatic
        external fun setMaxAliveTime(duration: Long)
        @JvmStatic
        external fun logWrite2(level: Int, tag: String?, filename: String?, funcname: String?, line: Int, pid: Int, tid: Long, maintid: Long, log: String?)


    }
    external fun appenderClose()

    external fun appenderFlush(isSync: Boolean)

}