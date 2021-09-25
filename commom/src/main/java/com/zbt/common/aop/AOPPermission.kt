package com.zbt.common.aop

import android.content.pm.PackageManager
import androidx.fragment.app.FragmentActivity
import com.zbt.common.app.CommonApplication
import com.zbt.common.permission.BiometricsUtils
import com.zbt.common.permission.PermissionFragment
import com.zbt.common.permission.PermissionUtil
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature

/**
 * Description: 权限AOP方式
 * @Author: xuwd
 * Date: 2020/11/24 12:16
 *
 */
@Aspect
class AOPPermission {
    @Pointcut("execution(@com.zbt.common.aop.CheckPermission * *(..))")
    fun checkPermission() {
    }

    @Pointcut("execution(@com.zbt.common.aop.DisableFastCall * *(..))")
    fun checkClick() {
    }

    @Pointcut("execution(@com.zbt.common.aop.CheckBiometrics * *(..))")
    fun checkBiometrics() {
    }

    @Around("checkClick()")
    fun aroundClick(joinPoint: ProceedingJoinPoint) {
        // 取出方法的注解
        val methodSignature = joinPoint.signature as MethodSignature

        val method = methodSignature.method
        if (!method.isAnnotationPresent(DisableFastCall::class.java)) {
            return
        }

        if(canClickAgain(joinPoint.target.javaClass.name)){
            joinPoint.proceed()
        }
    }


    @Around("checkPermission()")
    fun aroundPermission(joinPoint: ProceedingJoinPoint) {
        // 取出方法的注解
        val methodSignature = joinPoint.signature as MethodSignature

        val method = methodSignature.method
        if (!method.isAnnotationPresent(CheckPermission::class.java)) {
            return
        }

        val permission = method.getAnnotation(CheckPermission::class.java)
        CommonApplication.application.getCurrentActivity()?.let{
            val list=permission?.permission?.filter {p->
                !PermissionUtil.checkPermission(it,p)
            }
            if(list.isNullOrEmpty()){
                joinPoint.proceed()
                return
            }
            if(it is FragmentActivity){
                PermissionUtil.requestPermission(it as FragmentActivity,object : PermissionFragment.PermissionResultAdapter() {
                    override fun onPermissionsResult(permissions: Array<String>, grantResults: IntArray, shouldShowRequestPermissionRationale: BooleanArray) {

                        for(i in permissions.indices){
                            if(grantResults[i]== PackageManager.PERMISSION_DENIED){
                                permissionCall(joinPoint,permissions[i],!shouldShowRequestPermissionRationale[i])
                                return
                            }
                        }
                        joinPoint.proceed()
                    }

                },list.toTypedArray() )
            }
        }
    }

    private fun permissionCall(joinPoint: ProceedingJoinPoint,permission: String,hint:Boolean){

        joinPoint.target.javaClass.declaredMethods.find {
            m ->
            m.isAnnotationPresent(PermissionDeniedCallBack::class.java) &&
                    m.parameterTypes.let {
                        m.isAccessible = true
                        it.size == 2 && "java.lang.String".equals(it[0].name, true)
                                && "boolean".equals(it[1].name, true)

                    }
        }?.invoke(joinPoint.target, permission, hint)

    }

    @Around("checkBiometrics()")
    fun aroundBiometrics(joinPoint: ProceedingJoinPoint) {
        val methodSignature = joinPoint.signature as MethodSignature

        val method = methodSignature.method
        if (!method.isAnnotationPresent(CheckBiometrics::class.java)) {
            return
        }


        val type = method.getAnnotation(CheckBiometrics::class.java)
        if (type != null) {
            CommonApplication.application.getCurrentActivity()?.let {
                BiometricsUtils.requestBiometrics(it as FragmentActivity, type.type, object : BiometricsUtils.BiometricsCallback {
                    override fun success() {
                        joinPoint.proceed()
                    }

                    override fun fail() {

                    }

                })
            }

        }
    }


}

/**
 * 添加此注解的方法会检查相应权限，获得权限则运行方法，否则不执行
 *
 * class A{
 *     @CheckPermission
 *     fun needCheckMethod(){}
 *
 *     @PermissionDeniedCallBack
 *     fun doSome(permission:String,needHint:Boolean){}
 * }
 */

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CheckPermission(
        val permission:Array<String>
)

/**
 * 添加此注释的方法会在权限被拒绝后调用 方法必须含有参数String Boolean
 * 注释多个方法时只会执行第一个
 * 且与检查权限的注解[CheckPermission]方法在同一个类中
 * 被拒绝后只会调用一次，传入值为第一个（如果是权限组）被拒绝的权限名String，和是否需要强制提示boolean，
 * true表示无法再次申请该权限,需要提示引导用户去应用设置开启相应权限
 * false表示普通申请流程被拒绝
 *
 * class A{
 *     @CheckPermission
 *     fun needCheckMethod(){}
 *
 *     @PermissionDeniedCallBack
 *     fun doSome(permission:String,needHint:Boolean){}
 * }
 *
 * ...
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class PermissionDeniedCallBack()

/**
 * 禁用方法连续执行 间隔1秒，如禁用快速点击
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class DisableFastCall()
private object Time{
    const val interval=1000
    val timeMap= mutableMapOf<String,Long>()
}

private fun canClickAgain(name: String): Boolean {
    val cur: Long = System.currentTimeMillis()
    val last = Time.timeMap.getOrDefault(name, 0)
    val should = cur - last > Time.interval
    if (should) {
        Time.timeMap[name] = cur
    }
    return should
}


/**
 * 添加此注解的方法需通过键盘锁或生物认证后执行
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CheckBiometrics(val type: BiometricsUtils.Type)

