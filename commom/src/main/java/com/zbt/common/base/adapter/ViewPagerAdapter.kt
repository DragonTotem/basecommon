package com.zbt.common.base.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.zbt.common.base.BaseFragment

/**
 * author：   HUlq
 * date：     2020/12/10 & 17:41
 * desc
 * modify by
 */
class ViewPagerAdapter constructor(
        private val fragmentManager: FragmentManager,
        // 注意看这个参数
        private val behavior: Int,
        private val fragmentList: List<BaseFragment<*>>,
) :
        FragmentPagerAdapter(fragmentManager, behavior) {

    override fun getItem(position: Int): Fragment = fragmentList[position]

    override fun getCount(): Int = fragmentList.size
}