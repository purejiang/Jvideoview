package com.jplus.jvideoview.contract

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout

/**
 * @author Administrator
 * @date 2019/8/30.
 */
interface JVideoViewContract {
    interface Views {
        /**
         * 设置presenter
         */
        fun setPresenter(presenter: Presenter)
        /**
         * 设置播放标题
         * @param title 标题
         */
        fun setTitle(title:String)
        /**
         * 播放器loading中
         */
        fun loadingVideo()
        /**
         * 缓冲中
         *@param percent 百分比
         */
        fun buffering(percent:Int)
        /**
         * 播放准备就绪
         * @param videoTime 播放时间
         * @param max 最大进度
         */
        fun preparedVideo(videoTime:String, max:Int)
        /**
         * 缩略图
         */
        fun setThumbnail(bitmap:Bitmap?)
        /**
         * 开始播放
         * @param position 可选任意位置，默认为初始位置
         */
        fun startVideo(position: Int = 0)
        /**
         * 播放中
         *  @param videoTime 播放时间
         * @param position 播放位置
         */
        fun playing(videoTime:String, position: Int)
        /**
         * seek到某个位置播放
         * @param videoTime 播放时间
         * @param position 任意位置
         */
        fun seekToVideo(videoTime:String, position: Int)
        /**
         * 手势滑动快进/后退
         * @param videoTime 播放时间
         * @param position 任意位置
         */
        fun slidePlayVideo(videoTime:String, position: Int)
        /**
         * 暂停播放
         */
        fun pauseVideo()
        /**
         * 继续播放
         */
        fun continueVideo()
        /**
         * 播放完成
         */
        fun completedVideo()
        /**
         * 播放错误
         */
        fun errorVideo()
        /**
         * 调节亮度
         * @param light 亮度
         */
        fun setLightUi(light:Int)
        /**
         * 调节音量
         * @param volumePercent 音量百分比
         */
        fun setVolumeUi(volumePercent:Int)
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
        /**
         * 隐藏进度控制ui
         */
        fun hideAdjustUi()

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
         * 继续播放
         */
        fun continuePlay()
        /**
         * 手势判断
         * @param distance 屏幕上滑动的距离
         */
        fun slideJudge(view:View, event:MotionEvent)
        /**
         * 进度条拖动快进/后退
         * @param position 进度条进度
         */
        fun seekBarPlay(position: Int)
//        /**
//         * 调节亮度,不断调用
//         * @param distance 屏幕上滑动的距离
//         */
//        fun setLight(distance: Float)
//        /**
//         * 调节音量,不断调用
//         * @param distance 屏幕上滑动的距离
//         */
//        fun setVolume(distance: Float)
//        /**
//         * 保存调节进度
//         * @param adjustMode 调节的形式
//         */
//        fun saveAdjust(adjustMode: Int)

        /**
         * 进入特殊模式
         * @param view 播放器view
         */
        fun entrySpecialMode(view:LinearLayout)
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
         * 获取亮度
         * @param isMax 可选是否返回最大亮度
         * @return 亮度大小
         */
        fun getLight(isMax:Boolean):Double
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