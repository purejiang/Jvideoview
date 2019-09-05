package com.jplus.jvideoview.contract

import android.graphics.SurfaceTexture
import android.widget.LinearLayout

/**
 * @author Administrator
 * @date 2019/8/30.
 */
interface JVideoViewContract {
    interface View {
        /**
         * 设置presenter
         */
        fun setPresenter(presenter: Presenter)
        /**
         * 缓冲加载中/播放准备中
         * @param isShow 是否显示
         */
        fun showLoading(isShow:Boolean)
        /**
         * 缓冲中
         *@param percent 百分比
         */
        fun buffering(percent:Int)
        /**
         * 播放准备就绪
         */
        fun preparedPlay()
        /**
         * 开始播放
         * @param position 可选任意位置，默认为初始位置
         */
        fun startPlay(position: Int = 0)
        /**
         * 播放中
         * @param position 播放位置
         */
        fun playing(position: Int? = 0)
        /**
         * seek到某个位置继续播放
         * @param position 任意位置
         */
        fun seekToPlay(position: Int)
        /**
         * 暂停播放
         */
        fun pausePlay()
        /**
         * 播放完成
         */
        fun completedPlay()
        /**
         * 重新播放
         */
        fun restart()
        /**
         * 播放错误
         */
        fun errorPlay()
        /**
         * 调节亮度
         * @param light 亮度
         */
        fun setLight(light:Int)
        /**
         * 调节音量
         * @param volumePercent 音量百分比
         */
        fun setVolume(volumePercent:Int)
        /**
         * 调节亮度结束
         */
        fun hideLight()
        /**
         * 调节音量结束
         */
        fun hideVolume()
        /**
         * 调节进度结束
         */
        fun hideForwardOrBack()
        /**
         * 进入特殊模式
         * @param mode 全屏/窗口
         */
        fun entrySpecialMode(mode:Int)
        /**
         * 退出当前模式，恢复普通模式
         */
        fun exitMode()
        /**
         * 弹出/隐藏控制栏
         * @param isShow 是否显示
         */
        fun hideOrShowController(isShow:Boolean)

    }

    interface Presenter {
        /**
         * 实现订阅关系
         */
        fun subscribe()
        /**
         * 移除订阅关系
         */
        fun unSubscribe()
        /**
         * 开始播放
         * @param position 可选任意位置，默认为初始位置
         */
        fun startPlay(position: Int = 0)
        /**
         * seek到某个位置继续播放
         * @param position 任意位置
         */
        fun seekToPlay(position: Int)
        /**
         * 暂停播放
         */
        fun pausePlay()
        /**
         * 快进或者后退
         * @param distance 屏幕上滑动的距离
         */
        fun forwardOrBackVideo(distance: Float)
        /**
         * 重新播放
         */
        fun restart()
        /**
         * 调节亮度,不断调用
         * @param distance 屏幕上滑动的距离
         */
        fun setLight(distance: Float)
        /**
         * 调节音量,不断调用
         * @param distance 屏幕上滑动的距离
         */
        fun setVolume(distance: Float)
        /**
         * 调节亮度结束
         */
        fun endLight()
        /**
         * 调节音量结束
         */
        fun endVolume()
        /**
         * 调节进度结束
         */
        fun endForwardOrBack()
        /**
         * 进入特殊模式
         * @param mode 全屏/窗口
         */
        fun entrySpecialMode(mode:Int, view:LinearLayout)
        /**
         * 退出当前模式
         * @param isBackNormal  是否恢复普通模式
         */
        fun exitMode(isBackNormal:Boolean)
        /**
         * 通过url加载视频
         * @param surface 幕布
         * @param width 视频宽度
         * @param height 视频高度
         */
        fun openMediaPlayer(surface: SurfaceTexture, width: Int, height: Int)
        /**
         * 获取播放状态
         * @return 播放的九种状态
         */
        fun getPlayState():Int
        /**
         * 获取播放器模式
         * @return 返回 全屏/窗口/普通 模式
         */
        fun getPlayMode():Int
        /**
         * 获取音量
         * @param isMax 可选是否返回最大音量
         * @return 音量大小
         */
        fun getVolume(isMax:Boolean):Int
        /**
         * 获取视频总时长
         * @return 视频时长
         */
        fun getDuration():Int
        /**
         * 获取当前播放位置
         * @return 当前视频进度
         */
        fun getPosition():Int
        /**
         * 获取当前缓冲百分比
         * @return 1~100
         */
        fun getBufferPercent():Int
        /**
         * 当前网络速度
         * @return
         */
        fun getNetSpeed():Float

        /**
         * 释放资源
         * @param destroyUi 释放MediaPlayer资源后是否退出当前模式
         */
        fun releasePlay(destroyUi:Boolean)
    }
}