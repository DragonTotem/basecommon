package com.zbt.common.permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * Description: 权限封装类用于权限请求
 *
 * @Author: xuwd
 * Date: 2020/10/13 10:37
 */
class PermissionFragment : Fragment() {

    var permissions: Array<out String>?=null
    var permissionResultAdapter: PermissionResultAdapter?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

     override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != PERMISSIONS_REQUEST_CODE) return
        val shouldShowRequestPermissionRationale = BooleanArray(permissions.size)
        for (i in permissions.indices) {
            shouldShowRequestPermissionRationale[i] = shouldShowRequestPermissionRationale(permissions[i])
        }
         permissionResultAdapter?.onPermissionsResult(permissions,grantResults, shouldShowRequestPermissionRationale)
         exit()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        requestPermission()
    }

    private fun requestPermission() {
        if(permissions.isNullOrEmpty()){
            exit()
        }else{
            requestPermissions(permissions!!, PERMISSIONS_REQUEST_CODE)
        }

    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 633
    }

    /**
     * 处理完权限需要回调退出fragment
     */
    private fun exit(){
        permissions=null;
        requireActivity().supportFragmentManager.beginTransaction().remove(this).commitAllowingStateLoss()
    }
    /**
     * 权限回调类
     * */
    open class PermissionResultAdapter{

        /**
         * @param permissions 回调的权限名数组
         * @param grantResults 对应的权限授权结果数组，对应权限[PackageManager.PERMISSION_GRANTED]或[PackageManager.PERMISSION_DENIED]
         * @param shouldShowRequestPermissionRationale 对应权限提示，需结合[grantResults]使用，当对应权限为拒绝且此数组中的对应值为false时，
         * 表示申请权限被拒绝权限后（包括勾选不在提醒），无法再次申请，只能通过应用设置权限时回调
         * 需要弹窗提示用户到应用设置中开启权限
         */
        open fun onPermissionsResult(permissions: Array<String>, grantResults: IntArray, shouldShowRequestPermissionRationale: BooleanArray) {
            }
    }
}