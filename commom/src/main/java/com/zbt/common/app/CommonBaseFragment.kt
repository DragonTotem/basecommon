package com.zbt.common.app

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.zbt.common.app.uiinterface.Initialize
import com.zbt.common.log.LogUtils
import com.zbt.common.storage.BNKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel


abstract class CommonBaseFragment : Fragment(), Initialize, CoroutineScope by MainScope() {
    @JvmField
    protected val bnkv= BNKV.getBNKV()
    @JvmField
    protected var rootView: View? = null

    @JvmField
    protected var Log = LogUtils(this)
    protected val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT
    protected val WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT



    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initView()

        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initModel()
    }

    override fun onDetach() {
        super.onDetach()
        cancel()
    }


}