package com.zbt.common.app

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.zbt.common.app.uiinterface.Initialize
import com.zbt.common.log.ILogInterface
import com.zbt.common.log.LogUtils
import com.zbt.common.storage.BNKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel


abstract class CommonBaseActivity : AppCompatActivity(), Initialize, CoroutineScope by MainScope() {
    @JvmField
    protected val bnkv = BNKV.getBNKV()

    @JvmField
    protected var Log: ILogInterface = LogUtils(this)


    @JvmField
    protected var fragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        //去除系统重建activity带来的对fragment的影响
        savedInstanceState?.putParcelable("android:support:fragments", null);
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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




        initView()
        initModel()
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        onCreate(savedInstanceState)
    }


    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    /**
     * 执行初始化view相关方法
     *
     *     fun initView(){
     *          setContentView(R.layout.id)
     *          findViewById....
     *     }
     *
     *
     */
    abstract override fun initView()


    /**
     * 执行初始化viewModel方法
     *
     *     fun initModel(){
     *          viewModel = ViewModelProvider(this).get(xxx::class.java)
     *          viewModel.xxxx
     *     }
     */
    abstract override fun initModel()


}