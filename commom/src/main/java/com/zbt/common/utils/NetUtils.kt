package com.zbt.common.utils

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import com.zbt.common.permission.PermissionUtil

/**
 * Description:网络工具类
 *
 * @Author: xuwd
 * Date: 2020/10/13 17:35
 */

object NetUtils {


    /**
     * 全局网络状态监听更新
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @JvmStatic
    fun registerDefaultNetworkCallback(context: Context, networkCallback: ConnectivityManager.NetworkCallback) {
        if (PermissionUtil.checkPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            if (Build.VERSION.SDK_INT >= 26) {
                context.getSystemService(ConnectivityManager::class.java)
                        .registerDefaultNetworkCallback(networkCallback)
            } else {
                context.getSystemService(ConnectivityManager::class.java)
                        .registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
            }
        }
    }


    /**检查是否有网络连接 */
    @JvmStatic
    fun isNetworkConnected(context: Context?): Boolean {
        if (context != null) {
            val manager = context.getSystemService(ConnectivityManager::class.java)
            if (PermissionUtil.checkPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
                return manager.activeNetworkInfo?.isConnected ?: false
            }
        }
        return false
    }

    /**检查是否是wifi连接 */
    @JvmStatic
    fun isWifiConnected(context: Context?): Boolean {
        if (context != null) {
            val manager = context.getSystemService(ConnectivityManager::class.java)
            if (PermissionUtil.checkPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
                return !manager.allNetworks.none { network ->
                    manager.getNetworkInfo(network)?.let {
                        it.type == ConnectivityManager.TYPE_WIFI && it.isConnected
                    } ?: false
                }

            }
        }
        return false
    }

    /**检查是否是移动连接 */
    @JvmStatic
    fun isMobileConnected(context: Context?): Boolean {
        if (context != null) {
            val manager = context.getSystemService(ConnectivityManager::class.java)
            if (PermissionUtil.checkPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
                return !manager.allNetworks.none { network ->
                    manager.getNetworkInfo(network)?.let {
                        it.type == ConnectivityManager.TYPE_MOBILE && it.isConnected
                    } ?: false
                }

            }
        }
        return false
    }
}

