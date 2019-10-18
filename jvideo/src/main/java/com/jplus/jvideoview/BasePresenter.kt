package com.jplus.jvideoview

/**
 * @author JPlus
 * @date 2019/10/10.
 */
interface BasePresenter {
    /**
     * 实现订阅关系
     */
    fun subscribe()
    /**
     * 移除订阅关系
     */
    fun unSubscribe()
}