package com.zbt.common.permission

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.zbt.common.R
import com.zbt.common.log.LogUtils


/**
 * Description:手势解锁，密码解锁，指纹解锁，人脸识别工具
 * @Author: xuwd
 * Date: 2020/12/14 9:57
 *
 */
object BiometricsUtils {
    val Log = LogUtils(this)

    enum class Type {
        Keyguard, FingerPrint, Face
    }

    const val mKeyguardCode = 100

    private val biometricsFragment = BiometricsFragment()


    /**
     * 调起键盘锁界面，如果未设置解盘锁则跳转设置键盘锁。如果无法设置键盘锁则方法无效
     * @param title 解锁界面标题
     * @param des 解锁说明
     */
    fun checkKeyguard(fragment: Fragment, title: String = "", des: String = "") {
        fragment.requireActivity().let { context ->
            val manager = context.getSystemService(KeyguardManager::class.java)
            if (manager.isDeviceSecure) {
                fragment.startActivityForResult(manager.createConfirmDeviceCredentialIntent(title, des), mKeyguardCode)
            } else {
                Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                    if (this.resolveActivity(context.packageManager) != null) {
                        fragment.startActivity(this)
                    }
                }
            }
        }

    }


    /**
     * 进行生物识别判断
     * @param type 使用的识别方式见[Type]
     * @param callback 识别成功或者失败的回调
     * @param title 识别界面提示标题
     * @param des 识别界面提示说明
     */
    fun requestBiometrics(activity: FragmentActivity, type: Type, callback: BiometricsCallback, title: String = activity.getString(R.string.common_biometrics_title), des: String = "") {
        when (type) {
            Type.Keyguard -> {
                activity.supportFragmentManager.beginTransaction()
                        .remove(biometricsFragment)
                        .add(biometricsFragment.apply {
                            mBiometricsCallback = callback
                            mTitle = title
                            mDes = des
                        }, null)
                        .commitAllowingStateLoss()
            }
            Type.FingerPrint, Type.Face -> {
                checkBiometrics(activity, type, callback, title, des)
            }

        }

    }

    /**
     * 调起生物锁界面，如果无法使用则方法无效
     * @param title 解锁界面标题
     * @param des 解锁说明
     */
    private fun checkBiometrics(activity: FragmentActivity, type: Type, callback: BiometricsCallback, title: String = activity.getString(R.string.common_biometrics_title), des: String = "") {
        if (title.isEmpty()) {
            Toast.makeText(activity, activity.getString(R.string.common_title_cannot_empty), Toast.LENGTH_SHORT).show()
            return
        }

        if ((Build.VERSION.SDK_INT >= 30 && !PermissionUtil.checkPermission(activity, Manifest.permission.USE_BIOMETRIC))
                || (Build.VERSION.SDK_INT < 30 && !PermissionUtil.checkPermission(activity, Manifest.permission.USE_FINGERPRINT))) {
            Toast.makeText(activity, activity.getString(R.string.common_cannot_use_keyguard), Toast.LENGTH_SHORT).show()
            return
        }

        val manager = BiometricManager.from(activity)
        when (manager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
            -> {
                Toast.makeText(activity, activity.getString(R.string.common_cannot_use_keyguard), Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_SUCCESS -> {
                BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        callback.success()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        callback.fail()
                    }
                }).authenticate(BiometricPrompt.PromptInfo.Builder()
                        .setTitle(title)
                        .setDescription(des)
                        .setNegativeButtonText(activity.getString(R.string.common_biometrics_cancel))
                        .build())
            }

        }


    }


    interface BiometricsCallback {
        fun success()
        fun fail()
    }

    class BiometricsFragment : Fragment() {
        var mBiometricsCallback: BiometricsCallback? = null
        var mTitle = ""
        var mDes = ""

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            retainInstance = true
        }

        override fun onAttach(context: Context) {
            super.onAttach(context)
            checkKeyguard(this, mTitle, mDes)
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            when (requestCode) {
                mKeyguardCode -> {
                    if (resultCode == Activity.RESULT_OK) {
                        mBiometricsCallback?.success()
                    } else {
                        mBiometricsCallback?.fail()
                    }
                }
            }
            exit()

        }

        private fun exit() {
            requireActivity().supportFragmentManager.beginTransaction().remove(this).commitAllowingStateLoss()
        }
    }
}