package com.zbt.common.storage

import android.content.Context
import android.os.Parcelable
import android.util.Log
import com.zbt.common.log.LogUtils
import com.tencent.mmkv.MMKV
import com.tencent.mmkv.MMKVHandler
import com.tencent.mmkv.MMKVLogLevel
import com.tencent.mmkv.MMKVRecoverStrategic


/**
 * Description: 本地key value储存业务代替sharedPreferences
 * 使用前需要初始化
 * @Author: xuwd
 * Date: 2020/11/18 15:03
 *
 */
class BNKV private constructor(name: String?, mode: Int = 1, key: String? = cryptKey) {
    private var mmkv: MMKV? = null

    init {
        if (!mNeedInit) {
            mmkv = name?.let { MMKV.mmkvWithID(name, mode, key) }
                    ?: MMKV.defaultMMKV(mode, cryptKey)
        }

    }


    companion object {
        private var mNeedInit = true
        private val mmkvHandler = object : MMKVHandler {
            override fun onMMKVCRCCheckFail(p0: String?): MMKVRecoverStrategic {
                return MMKVRecoverStrategic.OnErrorDiscard
            }

            override fun onMMKVFileLengthError(p0: String?): MMKVRecoverStrategic {
                return MMKVRecoverStrategic.OnErrorDiscard
            }

            override fun wantLogRedirecting(): Boolean {
                return true
            }

            override fun mmkvLog(level: MMKVLogLevel?, file: String?, line: Int, func: String?, message: String?) {
                val log = "<${file}:${line}::${func}>${message}"
                when (level) {
                    MMKVLogLevel.LevelDebug -> {
                        LogUtils.mmkvW(log)
                    }
                    MMKVLogLevel.LevelInfo -> {
                    }
                    MMKVLogLevel.LevelWarning -> {
                        LogUtils.mmkvW(log)
                    }
                    MMKVLogLevel.LevelError -> {
                        LogUtils.mmkvW(log)
                    }
                    MMKVLogLevel.LevelNone -> {
                    }
                }
            }

        }


        /**
         * 初始化MMkv 需要在application中调用
         */
        @JvmStatic
        fun initMMkv(context: Context) {
            MMKV.initialize(context, MMKVLogLevel.LevelWarning)
            MMKV.registerHandler(mmkvHandler)
            mNeedInit = false
        }

        const val MULTI_PROCESS_MODE = MMKV.MULTI_PROCESS_MODE
        private const val cryptKey = "abcdef"

        /**
         * 获取储存空间 默认当前所有空间共享
         * 例如应用范围内 bnkv=BNKV.getBNKV() bnkv.putInt("a",1) bnkv.getInt("a")=1通用
         * @param name 传入name 则独立使用空间 例
         * bnkv=BNKV.getBNKV("name1") bnkv.putInt("a",1) bnkv.getInt("a")=1
         * bnkv=BNKV.getBNKV("name2") bnkv.getInt("a")=0
         * bnkv=BNKV.getBNKV() bnkv.getInt("a")=0
         * @param mode 需要多进程共享时 传入mode { MULTI_PROCESS_MODE}
         */
        @JvmStatic
        fun getBNKV(name: String? = null, mode: Int = 1, key: String? = cryptKey):BNKV {
            if (mNeedInit) {
                Log.w("com.zbt.common.storage.BNKV", "需要在application中执行初始化方法")
            }
            return BNKV(name, mode, key)
        }
    }

    fun getBoolean(key: String, default: Boolean = false):Boolean {
        return mmkv?.getBoolean(key, default) ?: false
    }

    fun putBoolean(key: String, default: Boolean) {
        mmkv?.putBoolean(key, default)
    }

    fun getFloat(key: String, default: Float = 0f) :Float{
        return mmkv?.getFloat(key, default) ?: 0f
    }

    fun putFloat(key: String, default: Float) {
        mmkv?.putFloat(key, default)
    }

    fun getInt(key: String, default: Int = 0) :Int{
        return mmkv?.getInt(key, default) ?: 0
    }

    fun putInt(key: String, default: Int) {
        mmkv?.putInt(key, default)
    }

    fun getString(key: String, default: String = ""):String {
        return mmkv?.decodeString(key, default) ?: ""
    }

    fun putString(key: String, default: String) {
        mmkv?.encode(key, default)
    }

    fun getLong(key: String, default: Long = 0):Long {
        return mmkv?.getLong(key, default) ?: 0
    }

    fun putLong(key: String, default: Long){
        mmkv?.putLong(key, default)
    }

    fun getBytes(key: String, default: ByteArray = byteArrayOf()):ByteArray {
        return mmkv?.getBytes(key, default) ?: byteArrayOf()
    }

    fun putBytes(key: String, default: ByteArray) {
        mmkv?.putBytes(key, default)
    }

    fun getDouble(key: String, default: Double = 0.0): Double {
        return mmkv?.decodeDouble(key, default) ?: 0.0
    }

    fun putDouble(key: String, default: Double) {
        mmkv?.encode(key, default)
    }

    fun <T : Parcelable> getParcelable(key: String, type: Class<T>): T? {
        return mmkv?.decodeParcelable(key, type)
    }

    fun putParcelable(key: String, default: Parcelable) {
        mmkv?.encode(key, default)
    }

    fun getStringSet(key: String, default: Set<String> = setOf()): MutableSet<String>? {
        return mmkv?.getStringSet(key, default)
    }

    fun putStringSet(key: String, default: Set<String>) {
        mmkv?.putStringSet(key, default)
    }

    fun cleanAll() {
        mmkv?.clearAll()
    }

    fun removeValueForKey(key: String) {
        mmkv?.removeValueForKey(key)
    }
}