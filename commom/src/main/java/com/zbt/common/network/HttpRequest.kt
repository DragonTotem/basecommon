package com.zbt.common.network

import android.os.Build
import androidx.annotation.FloatRange
import com.zbt.common.log.LogUtils

import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.net.URLEncoder

/**
 * Description: 网络请求基础类
 * @Author: xuwd
 * Date: 2020/11/2 15:47
 *
 */
class HttpRequest constructor(path: HttpPath, requestCallback: RequestCallback<*>?) {
    private val Log = LogUtils(this)

    private var httpPath: HttpPath = path
    private var callback: RequestCallback<*>? = requestCallback

    @PublishedApi
    internal var mSuccessBlock: Any?.() -> Unit = {}
    internal var mFailBlock: Any?.() -> Unit = {}

    constructor(path: HttpPath) : this(path, null)


    init {
        if (callback == null) {
            callback = object : RequestCallback<JsonElement>() {
                override fun success(request: HttpRequest, responseEntity: BaseResponseEntity<out JsonElement>) {
                    mType?.run {
                        var data: Any? = null
                        kotlin.runCatching {
                            data = gson.fromJson(responseEntity.data, mType)
                        }.onFailure {
                            it.printStackTrace()
                            ResponseErrorEntity(errorCode = ResponseErrorEntity.JSONError, errorMsg = "json type error").apply {
                                fail(this)
                            }
                            return@run
                        }

                        data.run {
                            mSuccessBlock()
                        }

                    }

                }

                override fun fail(responseErrorEntity: ResponseErrorEntity) {
                    responseErrorEntity.run {
                        mFailBlock()
                    }
                }

            }
        }
    }

    @PublishedApi
    internal var mType: Type? = null


    interface ActualType<T>


    inline fun <reified T> onSuccess(noinline block: T?.() -> Unit): HttpRequest {
        mType = (object : ActualType<T> {}.javaClass.genericInterfaces[0] as ParameterizedType).actualTypeArguments[0]
        mSuccessBlock = block as Any?.() -> Unit
        return this
    }

    fun onFail(block: ResponseErrorEntity.() -> Unit): HttpRequest {
        mFailBlock = block as Any?.() -> Unit
        return this
    }


    companion object Header {
        /**
         * 设备品牌
         */
        const val BRAND = "Brand"

        /**
         * 设备型号
         */
        const val MODEL = "Model"

        /**
         * android系统sdk版本 int 如 android 11 表示为30
         */
        const val ANDROIDVERSION = "AndroidVersion"


        val header = hashMapOf<String, String>(
                BRAND to URLEncoder.encode(Build.BOARD, "utf-8"),
                MODEL to URLEncoder.encode(Build.MODEL, "utf-8"),
                ANDROIDVERSION to Build.VERSION.SDK_INT.toString(),
        )
        var enableLog = false
        var host = ""

        /**
         * 初始化请求域名
         */
        fun initHost(url: String) {
            host = url
        }

        /**
         * 初始化请求header
         */
        fun initHeader(map: HashMap<String, String>) {
            header.putAll(map)
        }

        /**
         * 全局错误拦截器回调
         */
        var globalFailCall: GlobalFailCall? = null

        @JvmStatic
        fun createJsonFromMap(map: Map<String, Any>?): JSONObject? {
            return map?.let {
                val obj = JSONObject()
                it.forEach { (t, u) ->
                    obj.putOpt(t, u)
                }
                obj
            }
        }
    }


    private var uploadFiles: Array<File>? = null
    private var downloadFile: File? = null
    private val pathRaw = httpPath?.path ?: ""
    private val powerUrl = httpPath?.powerUrl ?: ""


    val method = httpPath?.method
    open var url = (if (powerUrl.isEmpty()) host else powerUrl).let {
        if (it.lastOrNull() != '/') {
            it.plus('/')
        }
        it.plus(pathRaw)
    }


    var httpSuccess = false
    var responseCode = ResponseErrorEntity.NoNetError
    var isRunning = false
    var params: HashMap<String, String>? = null
    var requestBody: JSONObject? = null

    private fun reset() {
        responseCode = ResponseErrorEntity.NoNetError
        httpSuccess = false
        uploadFiles = null
        downloadFile = null
        params = null
        requestBody = null
    }

    /**
     * 该方法在kotlin调用 发送请求
     */
    suspend fun sendRequest(params: HashMap<String, String>?) {
        if (isRunning) return
        isRunning = true
        withContext(Dispatchers.IO) {
            reset()
            this@HttpRequest.params = params
            if (enableLog) {
                Log.d("----->http_request:$url")
            }
            RetrofitUtil.startRequest(this@HttpRequest)
        }

    }

    /**
     * 该方法在java调用 发送请求 回调不在主线程
     */
    fun sendRequestAsy(params: HashMap<String, String>?) {
        if (isRunning) return
        isRunning = true
        reset()
        this@HttpRequest.params = params
        RetrofitUtil.startRequestAsy(this@HttpRequest)

    }

    /**
     * 该方法在kotlin调用 发送请求
     * 仅能用于post请求
     */
    suspend fun sendRequest(body: JSONObject?) {
        if (isRunning) return
        isRunning = true
        withContext(Dispatchers.IO) {
            reset()
            requestBody = body
            if (enableLog) {
                Log.d("----->http_request:$url")
            }
            RetrofitUtil.startRequest(this@HttpRequest)
        }

    }

    /**
     * 该方法在java调用 发送请求 回调不在主线程
     */
    fun sendRequestAsy(body: JSONObject?) {
        if (isRunning) return
        isRunning = true
        reset()
        requestBody = body
        RetrofitUtil.startRequestAsy(this@HttpRequest)

    }


    /**
     * 该方法在kotlin/java调用 多文件上传
     */
    fun uploadRequestAsy(params: HashMap<String, String>?, files: Array<File>?) {
        if (files == null || files.isEmpty()) return
        if (isRunning) return
        isRunning = true
        reset()
        this.params = params
        RetrofitUtil.uploadRequestAsy(this, files)
    }

    /**
     * 该方法在kotlin/java调用 单文件下载
     * @param useJava 在java中调用时传递true为异步下载， false则在当前线程直接请求
     */
    fun downloadFileAsy(params: HashMap<String, String>?, file: File?, useJava: Boolean = false) {
        if (file == null) return
        if (isRunning) return
        isRunning = true
        reset()
        this.params = params
        RetrofitUtil.downLoadRequestAsy(this, file, useJava)
    }


    suspend fun handleResponse(response: String?, httpSuccess: Boolean) {
        Log.d(response)
        isRunning = false
        this@HttpRequest.httpSuccess = httpSuccess

        callback?.let {
            if (httpSuccess) {
                val obj = it.createObj(response)
                withContext(Dispatchers.Main) {
                    if (obj.success) {
                        callback!!.success(this@HttpRequest, obj)
                    } else {
                        val responseErrorEntity = ResponseErrorEntity(obj.code, obj.msg)
                        if (globalFailCall?.fail(responseErrorEntity) == true) return@withContext
                        callback!!.fail(responseErrorEntity)
                    }
                }

            } else {
                val error = ResponseErrorEntity(responseCode, response)
                withContext(Dispatchers.Main) {
                    if (globalFailCall?.fail(error) == true) return@withContext
                    callback!!.fail(error)
                }

            }

        }

    }

    fun handleResponseAsy(response: String?, httpSuccess: Boolean) {
        isRunning = false
        Log.d(response)
        this@HttpRequest.httpSuccess = httpSuccess

        callback?.let {
            if (httpSuccess) {
                var obj = it.createObj(response)
                if (obj.success) {
                    callback!!.success(this, obj)
                } else {
                    val error = ResponseErrorEntity(obj.code, obj.msg)
                    if (globalFailCall?.fail(error) == true) return
                    callback!!.fail(error)
                }
            } else {
                val error = ResponseErrorEntity(responseCode, response)
                if (globalFailCall?.fail(error) == true) return
                callback!!.fail(error)
            }

        }

    }


    /**
     * 回调文件上传和下载进度，批量上传时进度为单个文件0-1 完成后下一文件往复
     */
    fun handProgressAsy(@FloatRange(from = 0.0, to = 1.0) progress: Float, file: File?) {
        callback?.progressHandle(progress, file)
    }


    abstract class RequestCallback<T> {
        companion object {
            var gson = Gson()
        }

        fun createObj(response: String?): BaseResponseEntity<Nothing> {
            try {
                return gson.fromJson(response, getGsonType())
            } catch (e: Exception) {
                e.printStackTrace()
                return BaseResponseEntity<Nothing>().apply {
                    code = -4
                    success = false
                    msg = "json type error"
                }
            }
        }

        private fun getGsonType(): Type {
            var type = javaClass.genericSuperclass
            if (type is ParameterizedType) {
                type = ParameterizedTypeImpl(BaseResponseEntity::class.java, type.actualTypeArguments)
            }
            return type
        }

        abstract fun success(request: HttpRequest, responseEntity: BaseResponseEntity<out T>)
        abstract fun fail(responseErrorEntity: ResponseErrorEntity)
        open fun progressHandle(@FloatRange(from = 0.0, to = 1.0) process: Float, file: File? = null) {}


        private inner class ParameterizedTypeImpl(private val raw: Class<*>, types: Array<Type>?) : ParameterizedType {
            private val types: Array<Type> = types ?: arrayOf()
            override fun getActualTypeArguments(): Array<Type> {
                return types
            }

            override fun getRawType(): Type {
                return raw
            }

            override fun getOwnerType(): Type? {
                return null
            }

        }
    }

    class ResponseErrorEntity constructor(val errorCode: Int = NoNetError, val errorMsg: String?) {

        /**
         * 本地网络请求错误类型定义
         */
        companion object {
            /**
             * 无网络或未知网络错误
             */
            const val NoNetError = -1

            /**
             * 网络超时
             */
            const val TimeoutError = -2

            /**
             * 无网络权限
             */
            const val PermissionError = -3

            /**
             * https证书类型错误
             */
            const val SSLError = -4

            /**
             * Json类型解析错误
             */
            const val JSONError = -5
        }

        override fun toString(): String {
            return "{errorCode=${errorCode}----$errorMsg}"
        }
    }

    /**
     * 全局错误接口，如果需要拦截特定错误，拦截后处理返回true则错误不再向发起请求的客户端回调错误
     * 返回false则继续由请求发起端处理
     */
    interface GlobalFailCall {
        fun fail(responseErrorEntity: ResponseErrorEntity): Boolean
    }
}