package com.zbt.common.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * author：   HUlq
 * date：     2020/12/10 & 17:41
 * desc       封装一个有懒加载的 Fragment
 * modify by
 * @param T 传入泛型的 实体
 * @param K 传入泛型的 ViewBinding
 */
abstract class RecyclerBaseAdapter<T, K : ViewBinding> protected constructor(
        @NonNull private var
        mDataList: MutableList<T>,
) :
        RecyclerView.Adapter<BaseBindingViewHolder<K>>(), IRecyclerAdapter<T> {
    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(view: View?, position: Int)
    }

    private var mClickListener: OnItemClickListener? = null
    private var mLongClickListener: OnItemLongClickListener? = null

    fun <K : ViewBinding> ViewGroup.getViewHolder(
            creator: (inflater: LayoutInflater, root: ViewGroup, attachToRoot: Boolean) -> K,
    ): BaseBindingViewHolder<K> = BaseBindingViewHolder(this, creator)


    override fun onBindViewHolder(holder: BaseBindingViewHolder<K>, position: Int) {
        val p = holder.layoutPosition
        if (mClickListener != null) {
            holder.itemView.setOnClickListener { v -> mClickListener!!.onItemClick(v, p) }
        }
        if (mLongClickListener != null) {
            holder.itemView.setOnLongClickListener { v ->
                mLongClickListener!!.onItemLongClick(v, p)
                true
            }
        }
        bindDataForView(holder, if (mDataList.isNullOrEmpty() || mDataList.size <= p) null else mDataList[p], p)
    }

    protected abstract fun bindDataForView(holder: BaseBindingViewHolder<K>, t: T?, position: Int)

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override val dataList: MutableList<T>?
        get() = mDataList

    override fun setOnItemClickListener(listener: OnItemClickListener?) {
        mClickListener = listener
    }

    override fun setOnItemLongClickListener(listener: OnItemLongClickListener?) {
        mLongClickListener = listener
    }

    override fun getItem(@IntRange(from = 0) position: Int): T? {
        return (if (position <= -1 || mDataList.size <= position) {
            null
        } else mDataList[position])
    }

    override fun insertItems(list: MutableList<T>?, @IntRange(from = 0) startIndex: Int) {

        if (list == null || list.isEmpty()) {
            return
        }
        if (mDataList.containsAll(list)) {
            return
        }
        notifyItemRangeInserted(startIndex, list.size)
        mDataList.addAll(startIndex, list)
        notifyItemRangeChanged(startIndex, mDataList.size)
    }

    override fun insertItems(list: MutableList<T>?) {
        this.insertItems(list, mDataList.size)
    }

    override fun insertItem(t: T, @IntRange(from = 0) position: Int) {
        if (t == null) {
            return
        }
        if (mDataList.contains(t)) {
            return
        }
        notifyItemInserted(position)
        mDataList.add(position, t)
        notifyItemRangeChanged(position, mDataList.size)
    }

    override fun insertItem(t: T) {
        this.insertItem(t, mDataList.size)
    }

    override fun replaceData(list: MutableList<T>?) {
        if (mDataList == null) {
            return
        }
        if (list == null || list.isEmpty()) {
            return
        }
        if (mDataList.containsAll(list)) {
            return
        }
        mDataList = list
        notifyDataSetChanged()
    }

    override fun updateItems(@IntRange(from = 0) positionStart: Int, @IntRange(from = 0) itemCount: Int) {
        notifyItemRangeChanged(positionStart, itemCount)
    }

    override fun updateAll() {
        updateItems(0, mDataList.size)
    }

    override fun removeItem(@IntRange(from = 0) position: Int) {
        if (mDataList == null || mDataList.size == 0 || position <= -1) {
            return
        }
        notifyItemRemoved(position)
        mDataList.removeAt(position)
        notifyItemRangeChanged(position, mDataList.size)
    }

    override fun removeAll() {
        if (mDataList == null || mDataList.size == 0) {
            return
        }
        notifyItemRangeRemoved(0, mDataList.size)
        mDataList.clear()
        notifyItemRangeChanged(0, mDataList.size)
    }

    companion object {
        private val TAG = RecyclerBaseAdapter::class.java.simpleName
    }
}