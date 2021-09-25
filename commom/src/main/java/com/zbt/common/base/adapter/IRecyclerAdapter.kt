package com.zbt.common.base.adapter

/**
 * author：   HUlq
 * date：     2020/12/10 & 17:41
 * desc       封装一个有懒加载的 Fragment
 * modify by
 */
interface IRecyclerAdapter<T> {
    /**
     * 设置item的单击事件
     *
     * @param listener 单击监听
     */
    fun setOnItemClickListener(listener: RecyclerBaseAdapter.OnItemClickListener?)

    /**
     * 设置item的长按时间
     *
     * @param listener 长按监听
     */
    fun setOnItemLongClickListener(listener: RecyclerBaseAdapter.OnItemLongClickListener?)

    /**
     * 获取第 position 个数据
     *
     * @param position 位置
     * @return T
     */
    fun getItem(position: Int): T?

    /**
     * 获取全部数据
     *
     * @return
     */
    val dataList: MutableList<T>?

    /**
     * 插入一系列数据
     *
     * @param list       数据集
     * @param startIndex 开始位置
     */
    fun insertItems(list: MutableList<T>?, startIndex: Int)

    /**
     * 追加一系列数据
     *
     * @param list 数据集
     */
    fun insertItems(list: MutableList<T>?)

    /**
     * 插入单个数据
     *
     * @param t        数据
     * @param position 开始位置
     */
    fun insertItem(t: T, position: Int)

    /**
     * 追加单个数据
     *
     * @param t 数据
     */
    fun insertItem(t: T)

    /**
     * 替换整个数据
     *
     * @param list 数据集
     */
    fun replaceData(list: MutableList<T>?)

    /**
     * 通知更新
     *
     * @param positionStart 开始位置
     * @param itemCount     更新的个数
     */
    fun updateItems(positionStart: Int, itemCount: Int)

    /**
     * 通知更新
     */
    fun updateAll()

    /**
     * 移除一个item
     *
     * @param position 位置
     */
    fun removeItem(position: Int)

    /**
     * 移除全部
     */
    fun removeAll()
}