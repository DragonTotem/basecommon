package com.zbt.common.app;


import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import com.zbt.common.entity.ImageEntity
import com.zbt.common.view.ImagePreviewView

/**
 * Description: 图片预览容器
 * @Author: xuwd
 * Date: 2020/12/1 17:21
 *
 */
class ImagePreviewFragment : CommonBaseFragment() {
    companion object {
        const val ImageList = "imageList"
        const val CloseTouchScale = "closeTouchScale"
    }

    private lateinit var mImagePreviewView: ImagePreviewView
    private val mImageList = mutableListOf<ImageEntity>()
    private val mInsetMap = mutableMapOf<Int, Boolean>()
    private var mTouchScaleClose = false

    override fun initView() {
        if (rootView == null) {
            rootView = ImagePreviewView(requireContext()).apply {
                setBackgroundColor(Color.BLACK)
                mImagePreviewView = this
                mTouchScaleAble = !mTouchScaleClose
                closeImageClick = true
            }
        }
        mImagePreviewView.setImageList(mImageList)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.decorView.apply {
            if (Build.VERSION.SDK_INT >= 30) {
                windowInsetsController?.apply {
                    mInsetMap[WindowInsets.Type.statusBars()] = rootWindowInsets.isVisible(WindowInsets.Type.statusBars())
                    mInsetMap[WindowInsets.Type.navigationBars()] = rootWindowInsets.isVisible(WindowInsets.Type.navigationBars())
                    mInsetMap[WindowInsets.Type.captionBar()] = rootWindowInsets.isVisible(WindowInsets.Type.captionBar())
                    hide(WindowInsets.Type.systemBars())
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (Build.VERSION.SDK_INT >= 30) {
            requireActivity().window.decorView.windowInsetsController?.apply {
                mInsetMap.forEach { (type, visible) ->
                    if (visible) {
                        show(type)
                    }
                }
            }
        }

    }

    override fun initModel() {
        arguments?.apply {
            get(ImageList).let {
                if (it is List<*> && it.isNotEmpty() && it.first() is ImageEntity) {
                    mImageList.clear()
                    mImageList.addAll(it as List<ImageEntity>)
                }
            }
            mTouchScaleClose = getBoolean(CloseTouchScale, false)
        }
    }
}