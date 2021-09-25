package com.zbt.common.ktx;

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider


/**
 * Description: viewmodel辅助懒加载
 * @Author: xuwd
 * Date: 2020/12/28 16:20
 *
 */
@MainThread
inline fun <reified VM : ViewModel> Fragment.fragmentViewModels(
        noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null,
): Lazy<VM> {
    val factoryPromise = factoryProducer ?: {
        defaultViewModelProviderFactory
    }

    return ViewModelLazy(VM::class, { viewModelStore }, factoryPromise)
}

@MainThread
inline fun <reified VM : ViewModel> Fragment.activityViewModels(
        noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null,
): Lazy<VM> {
    val factoryPromise = factoryProducer ?: {
        requireActivity().defaultViewModelProviderFactory
    }

    return ViewModelLazy(VM::class, { requireActivity().viewModelStore }, factoryPromise)
}