package com.jplus.jvideoview.jvideo

/**
 * 播放器状态回调
 * @author JPlus
 * @date 2020/4/28.
 */
interface JvListener {
    /**
     * 播放器初始化完成
     */
    fun onInitSuccess()
    /**
     * 预加载完成
     */
    fun onPrepared()
    /**
     * 播放器重启
     */
    fun onReset()
    /**
     * 缓冲中
     */
    fun onBuffering()
    /**
     * 播放开始
     */
    fun onStartPlay()
    /**
     * 重播
     */
    fun onReStart()
    /**
     * 播放下一个
     */
    fun onNextPlay()
    /**
     * 播放中
     */
    fun onPlaying()
    /**
     * 播放暂停
     */
    fun onPausePlay()
    /**
     * 播放完成
     */
    fun onCompleted()
    /**
     * 播放失败
     */
    fun onError()
    /**
     * 全屏状态
     */
    fun onFullScreen()
    /**
     * 普通状态
     */
    fun onNormalScreen()


}