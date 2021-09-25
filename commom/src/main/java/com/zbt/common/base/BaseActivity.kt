package com.zbt.common.base

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * author：   HUlq
 * date：     2020/12/10 & 17:41
 * desc
 * modify by
 * @param T 传入泛型的 ViewBinding
 */
abstract class BaseActivity<T : ViewBinding> : AppCompatActivity(),
        CoroutineScope by MainScope() {

    private var _binding: T? = null

    val binding get() = _binding!!

    @JvmField
    protected var fragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        //去除系统重建activity带来的对fragment的影响
        savedInstanceState?.putParcelable("android:support:fragments", null);
        super.onCreate(savedInstanceState)
        initWindowBar()
        _binding = initBinding()
        setContentView(_binding?.root)
        initView(savedInstanceState)
        initData()
    }

    private fun initWindowBar() {
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.WHITE
            navigationBarColor = Color.WHITE
            if (Build.VERSION.SDK_INT >= 30) {
                decorView.windowInsetsController?.apply {
                    setSystemBarsAppearance(
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
                    setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS)
                    systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }


        }

    }

    /**
     * 传入对应的 ViewBinding
     */
    abstract fun initBinding(): T

    /**
     * 初始化控件
     * @param savedInstanceState
     */
    abstract fun initView(savedInstanceState: Bundle?)

    /**
     * 加载数据
     */
    abstract fun initData()

//    /**
////     * 初始化监听
////     */
////    abstract fun initEvent()

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}