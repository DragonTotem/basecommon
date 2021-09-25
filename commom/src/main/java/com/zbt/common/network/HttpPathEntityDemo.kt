package com.zbt.common.network


/**
 * 定义全局使用请求路径实例
 * @author xuwd
 */
object HttpPathEntityDemo {

    //定义示例---------------start
    object ExampleApiPath {
        private val UserApiEntrance = HttpPath.Builder().path("user/api/").build()
        val ThemeList = HttpPath.Builder().path("getThemeList").build()
        val uploadFile = HttpPath.Builder().path("uploader").build()
        val downloadFile = HttpPath.Builder().path("download").method(HttpPath.Method.GET).build()

        /**
         * 登录
         */
        val UserLogin = UserApiEntrance.produce("abc/login")

        /**
         * 注册
         */
        @JvmField
        val Register = UserApiEntrance.produce("register")
    }


}