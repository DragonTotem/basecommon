

package com.zbt.common.permission

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * Description: 权限相关工具kt实现
 * @Author: xuwd
 * Date: 2020/10/12 18:52
 *
 */
object PermissionUtil{
  //  @JvmStatic
  //  internal var context:Activity?=null

    @JvmStatic
    private val permissionFragment= PermissionFragment()

    /**
     * 检查是否有权限
     * @param context
     * @param permission 权限名
     * @return 有权限为true
     */
    @JvmStatic
    fun checkPermission(context: Context?, permission: String?): Boolean {
        if(context == null ||permission==null){
            return false
        }
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }




    /**
     * 申请权限
     * @param activity 需要是[FragmentActivity]的子类 BaseActivity已继承
     * @param permission 权限名可变参数，可连续多个
     * @param callbackAdapter 权限回调判断
     * 示例
     * PermissionUtil.requestPermission(BaseActivity.this,new PermissionFragment.PermissionResultAdapter(){
            @Override
            public void onPermissionGranted(@NotNull String name, boolean isGranted) {
                if(isGrand){
                    //do
                }else{
                    //do
                }
            }

            @Override
            public void permissionNeedHint(@NotNull String name) {
                //do
            }
       },Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE);
     */
    @JvmStatic
    fun requestPermission(activity: FragmentActivity?=null, callbackAdapter: PermissionFragment.PermissionResultAdapter,  permission: Array<out String>) {
        if (activity == null) {
            return
        }

        permissionFragment.permissions= permission
        permissionFragment.permissionResultAdapter=callbackAdapter
        activity.supportFragmentManager.
            beginTransaction().remove(permissionFragment).
            add(permissionFragment,null).commitAllowingStateLoss()



    }


    /**
     * 申请单个权限
     * @param fragment 需要是[Fragment]的子类 BaseFragment已继承
     * @param permission 权限名
     * @param callbackAdapter 权限回调判断
     * 示例
     * PermissionUtil.requestPermission(BaseFragment.this,new PermissionFragment.PermissionResultAdapter(){
            @Override
            public void onPermissionGranted(@NotNull String name, boolean isGranted) {
                if(isGrand){
                    //do
                }else{
                    //do
                }
            }

            @Override
            public void permissionNeedHint(@NotNull String name) {
                //do
            }
       },Manifest.permission.WRITE_EXTERNAL_STORAGE);
     */
    @JvmStatic
    fun requestPermission(fragment: Fragment?, callbackAdapter: PermissionFragment.PermissionResultAdapter, vararg permission: String) {
        if ( fragment==null ) {
            return
        }

        permissionFragment.permissions= permission
        permissionFragment.permissionResultAdapter=callbackAdapter

        fragment.requireActivity(). supportFragmentManager.
            beginTransaction().remove(permissionFragment).
            add(permissionFragment,null).commitAllowingStateLoss()


    }


    /**
     * 示例
     * PermissionUtil.requestPermission(BaseFragment.this,new PermissionFragment.PermissionResultAdapter(){
        @Override
        public void onPermissionGranted(@NotNull String name, boolean isGranted) {
            if(isGrand){
                //do
            }else{
                //do
            }
        }

        @Override
        public void permissionNeedHint(@NotNull String name) {
                //do
        }
        },Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE);
     */
}






