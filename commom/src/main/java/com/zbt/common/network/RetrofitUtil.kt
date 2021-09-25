package com.zbt.common.network


import com.zbt.common.log.LogUtils
import com.zbt.common.utils.FileUtils.createFileParent
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.BufferedSink
import okio.Okio
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Description: 网络请求框架kt方式
 * @Author: xuwd
 * Date: 2020/11/2 10:40
 *
 */
object RetrofitUtil {
    val Log = LogUtils(this)
    private val okHttpClient = OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS).build()

    private var retrofit = Retrofit.Builder().baseUrl("http://www.zbt.com")
            .client(okHttpClient)
            .callbackExecutor(ThreadPoolExecutor(1, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, SynchronousQueue<Runnable>()))
            .build()
    fun setRetrofit(retrofit: Retrofit){
        this.retrofit=retrofit
        apiServer=retrofit.create(ApiServer::class.java)
    }

    private var apiServer = retrofit.create(ApiServer::class.java)

    interface ApiServer {
        @GET
        fun getResponse(@Url path: String, @HeaderMap headers: Map<String, String>, @QueryMap param: Map<String, String>): Call<ResponseBody>

        @POST
        fun postResponse(@Url path: String?, @HeaderMap headers: Map<String, String>?, @Body requestBody: RequestBody): Call<ResponseBody>

        @FormUrlEncoded
        @POST
        fun postFormResponse(@Url path: String?, @HeaderMap headers: Map<String, String>?, @FieldMap param: Map<String, String>): Call<ResponseBody>

        @Multipart
        @POST
        fun uploadResponse(@Url path: String?, @HeaderMap headers: Map<String, String>?, @PartMap param: HashMap<String, RequestBody>): Call<ResponseBody>

        @Streaming
        @POST
        fun downloadResponse(@Url path: String?, @HeaderMap headers: Map<String, String>?, @QueryMap param: Map<String, String>): Call<ResponseBody>
    }

    object Empty {
        val emptyParam = mapOf<String, String>()
    }

    /**
     * 普通请求
     *
     * @param httpRequest
     */
    suspend fun startRequest(httpRequest: HttpRequest?) {
        if (httpRequest == null) return
        val url = httpRequest.url
        if (url.isNullOrEmpty()) return
        val call: Call<ResponseBody>
        val header = HttpRequest.header
        call = when (httpRequest.method) {
            HttpPath.Method.POST -> {
                if (httpRequest.params != null) {
                    apiServer.postFormResponse(url, header, httpRequest.params
                            ?: Empty.emptyParam)
                } else {
                    apiServer.postResponse(url, header, RequestBody.create(MediaType.parse("application/json"), httpRequest.requestBody?.toString()
                            ?: ""))
                }

            }
            else                 -> apiServer.getResponse(url, header, httpRequest.params
                    ?: Empty.emptyParam)
        }

        try {
            val response = call.execute()
            handleResponse(httpRequest, response)
        } catch (e: Exception) {
            handleFail(e, null, httpRequest)
        }


    }

    /**
     * 普通请求 异步模式
     *
     * @param httpRequest
     */
    fun startRequestAsy(httpRequest: HttpRequest?) {
        if (httpRequest == null) return
        val url = httpRequest.url
        if (url.isNullOrEmpty()) return
        val call: Call<ResponseBody>
        val header = HttpRequest.header
        call = when (httpRequest.method) {
            HttpPath.Method.POST -> {
                apiServer.postResponse(url, header, RequestBody.create(MediaType.parse("application/json"), httpRequest.requestBody?.toString()
                        ?: ""))
            }
            else                 -> apiServer.getResponse(url, header, httpRequest.params
                    ?: Empty.emptyParam)
        }
        try {
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    handleResponseAsy(httpRequest, response)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    handleFailAsy(null, t, httpRequest)
                }
            })
        } catch (e: Exception) {
            handleFailAsy(e, null, httpRequest)
        }

    }

    private suspend fun handleResponse(httpRequest: HttpRequest, response: Response<ResponseBody>) {
        val content: String? = if (response.isSuccessful) {
            response.body()?.string()
        } else {
            response.errorBody()?.string()
        }
        httpRequest.responseCode = response.code()
        httpRequest.handleResponse(content, response.isSuccessful)
    }

    private fun handleResponseAsy(httpRequest: HttpRequest, response: Response<ResponseBody>) {
        val content: String? = if (response.isSuccessful) {
            response.body()?.string()
        } else {
            response.errorBody()?.string()
        }
        httpRequest.responseCode = response.code()
        httpRequest.handleResponseAsy(content, response.isSuccessful)
    }

    /**
     * 上传文件特殊请求 同步模式
     *
     * @param httpRequestBase
     * @param files
     */
    fun uploadRequestAsy(httpRequest: HttpRequest?, files: Array<File>?) {
        if (httpRequest == null) return
        val url = httpRequest.url
        if (url.isNullOrEmpty()) return
        if (files == null) return
        val call: Call<ResponseBody>
        val header = HttpRequest.header
        val map: HashMap<String, RequestBody> = HashMap()
        for (file in files) {
            val requestFile = FileRequestBody(MediaType.parse("multipart/form-data"), file, httpRequest)
            map["file\"; filename=\"${file.name}"] = requestFile
        }
        httpRequest.params?.forEach { (key, value) -> map[key] = RequestBody.create(MediaType.parse("text/plain"), value) }
        call = apiServer.uploadResponse(url, header, map)
        try {
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    var content: String? = if (response.isSuccessful) {
                        response.body()?.string()
                    } else {
                        response.errorBody()?.string()
                    }
                    httpRequest.responseCode = response.code()
                    httpRequest.handleResponseAsy(content, response.isSuccessful)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    handleFailAsy(null, t, httpRequest)
                }
            })
        } catch (e: Exception) {
            handleFailAsy(e, null, httpRequest)
        }
    }

    /**
     * 下载文件请求 同步请求
     *
     * @param httpRequestBase
     */
    fun downLoadRequestAsy(httpRequest: HttpRequest?, file: File?, useJava: Boolean = false) {
        if (httpRequest == null) return
        val url = httpRequest.url
        if (url.isNullOrEmpty()) return
        if (file == null) return
        val call: Call<ResponseBody>
        val header = HttpRequest.header
        call = apiServer.downloadResponse(url, header, httpRequest.params ?: Empty.emptyParam)
        if (useJava) {
            call.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                    try {
                        handleDownResponse(httpRequest, file, response)
                    } catch (e: Exception) {
                        handleFailAsy(e, null, httpRequest)
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    handleFailAsy(null, t, httpRequest)
                }
            })
        } else {
            try {
                val response = call.execute()
                handleDownResponse(httpRequest, file, response)
            } catch (e: Exception) {
                handleFailAsy(e, null, httpRequest)
            }
        }


    }

    private fun handleDownResponse(httpRequest: HttpRequest, file: File?, response: Response<ResponseBody?>) {
        var content: String? = ""
        if (response.isSuccessful) {
            if (response.body() != null) {
                var currentLength: Long = 0
                val inputStream = response.body()!!.byteStream()
                val totalLength = response.body()!!.contentLength()

                inputStream.use {
                    createFileParent(file)
                    val outputStream = FileOutputStream(file)
                    outputStream.use {
                        var len: Int
                        val buff = ByteArray(1024 * 8)
                        while (inputStream.read(buff).also { len = it } != -1) {
                            outputStream.write(buff, 0, len)
                            currentLength += len.toLong()
                            //计算当前下载百分比，currentLength / totalLength
                            httpRequest.handProgressAsy(currentLength * 1f / totalLength, file)
                        }
                    }
                }

                content = "{\"data\":true,\"message\":\"\",\"code\":0,\"success\":true}"

            }
        } else {
            content = response.errorBody()?.string()
        }
        httpRequest.responseCode = response.code()
        httpRequest.handleResponseAsy(content, response.isSuccessful)
    }

    /**
     *
     * 请求发送失败时处理，通常只是超时和SSL错误
     */
    private suspend fun handleFail(exception: Exception? = null, throwable: Throwable? = null, httpRequestBase: HttpRequest?) {
        if (httpRequestBase == null) {
            return
        }
        if (throwable == null && exception == null) {
            httpRequestBase.handleResponse("unknown", false)
        }
        var temp = ""
        temp = throwable?.toString() ?: exception?.toString() ?: ""

        if (temp.contains("TimeoutException")) {

            httpRequestBase.responseCode = HttpRequest.ResponseErrorEntity.TimeoutError
        } else if (temp.contains("verified")
                || temp.contains("SSLHandshakeException")
                || temp.contains("CertPathValidatorException")) {

            httpRequestBase.responseCode = HttpRequest.ResponseErrorEntity.SSLError

        } else if (temp.contains("Permission denied")) {
            //应用无网络权限

            httpRequestBase.responseCode = HttpRequest.ResponseErrorEntity.PermissionError
        }
        httpRequestBase.handleResponse(temp, false)
    }

    /**
     *
     * 请求发送失败时处理，通常只是超时和SSL错误
     */
    private fun handleFailAsy(exception: Exception? = null, throwable: Throwable? = null, httpRequestBase: HttpRequest?) {
        if (httpRequestBase == null) {
            return
        }
        if (throwable == null && exception == null) {
            httpRequestBase.handleResponseAsy("unknown", false)
        }
        var temp = ""
        temp = throwable?.toString() ?: exception?.toString() ?: ""
        //var errorMsg = ""
        if (temp.contains("TimeoutException")) {
            httpRequestBase.responseCode = HttpRequest.ResponseErrorEntity.TimeoutError
        } else if (temp.contains("verified")
                || temp.contains("SSLHandshakeException")
                || temp.contains("CertPathValidatorException")) {
            httpRequestBase.responseCode = HttpRequest.ResponseErrorEntity.SSLError

        } else if (temp.contains("Permission denied")) {
            //应用无网络权限
            httpRequestBase.responseCode = HttpRequest.ResponseErrorEntity.PermissionError
        }
        httpRequestBase.handleResponseAsy(temp, false)
    }

    class FileRequestBody constructor(private val contentType: MediaType?, val file: File, private val httpRequest: HttpRequest?) : RequestBody() {
        private val contentLength = file?.length() ?: 0
        override fun contentType(): MediaType? {
            return contentType
        }

        override fun contentLength(): Long {
            return file.length()
        }


        override fun writeTo(sink: BufferedSink) {

            Okio.source(file).use { source ->
                val bufferSize = 1024L
                var len = contentLength
                while (len > bufferSize) {
                    sink.write(source, bufferSize)
                    len -= bufferSize
                    httpRequest?.handProgressAsy(1 - len * 1f / contentLength, file)
                }
                if (len > 0) {
                    sink.write(source, len)
                }
                httpRequest?.handProgressAsy(1f, file)

            }
        }

    }
}