package com.zbt.common.base.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * author：   HUlq
 * date：     2020/12/11 0027 16:04
 * version    1.0
 * desc       recycler adapter 的基类,封装了ViewBinding ViewHolder
 * modify by
 */
open class BaseBindingViewHolder<K : ViewBinding> private constructor(private var _binding: K) :
        RecyclerView.ViewHolder(_binding.root) {

    val binding get() = _binding

    constructor(
            parent: ViewGroup,
            creator: (inflater: LayoutInflater, root: ViewGroup, attachToRoot: Boolean) -> K,
    ) : this(creator(LayoutInflater.from(parent.context), parent, false))
}