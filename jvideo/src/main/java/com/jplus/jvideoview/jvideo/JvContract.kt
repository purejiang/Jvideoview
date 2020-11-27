package com.jplus.jvideoview.jvideo

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.view.MotionEvent
import android.view.TextureView
import tv.danmaku.ijk.media.player.IMediaPlayer

/**
 * @author JPlus
 * @date 2019/8/30.
 */
interface JvContract {

    interface View{
        /**
         * 设置播放标题
         * @param title 标题
         */
        fun setTitle(title:String)
        /**
         * 显示系统时间
         * @param isShow 是否显示
         */
        fun showSysInfo(isShow:Boolean)
        /**
         * 开启播放器loading
         * @param text 提示文字
         */
        fun showLoading(text:String)
        /**
         * 关闭播放器loading
         * @param text 提示文字
         */
        fun closeLoading(text:String)
        /**
         * 显示中间播放控制按钮
         */
        fun showCenterPlayView()
        /**
         * 关闭中间播放控制按钮
         */
        fun closeCenterPlayView()
        /**
         * 显示中间提示信息view
         */
        fun showCenterHintView()
        /**
         * 关闭中间提示信息view
         */
        fun closeCenterHintView()
        /**
         * 播放准备就绪
         * @param videoTime 播放时间
         * @param start 可选任意位置，默认为初始位置 初始进度
         * @param max 最大进度
         */
        fun preparedVideo(videoTime:String, start:Int, max:Int)
        /**
         * 播放器reset
         */
        fun reset()
        /**
         * 播放器引擎切换
         * @param isSupport 是否支持
         */
        fun showSwitchEngine(isSupport: Boolean)
        /**
         * 缓冲中
         *@param percent 百分比
         */
        fun showBuffering(percent:Int)
        /**
         * 静音/正常状态的切换
         * @param isMute 是否静音状态
         */
        fun setVolumeMute(isMute:Boolean)
        /**
         * 缩略图
         */
        fun setThumbnail(bitmap:Bitmap?)
        /**
         * 开始播放
         */
        fun startVideo()
        /**
         * 显示实时网速
         * @param speed 实时网速
         */
        fun showNetSpeed(speed: String)
        /**
         * 播放中
         *  @param videoTime 播放时间
         * @param position 播放位置
         */
        fun playing(videoTime:String, position: Long)
        /**
         * seek滑动到某个位置的UI显示
         * @param videoTime 播放时间
         */
        fun seekingVideo(videoTime:String, position: Long, isSlide: Boolean)
        /**
         * 暂停播放
         */
        fun pauseVideo()
        /**
         * 继续播放
         */
        fun continueVideo()
        /**
         * 显示提示消息
         * @param message 显示的内容
         * @param isShow 是否显示重新播放按钮
         */
        fun showMessagePrompt(message:String, isShow: Boolean)
        /**
         * 调节亮度
         * @param light 亮度
         */
        fun setLightUi(light:Int)
        /**
         * 调节音量
         * @param volumePercent 音量百分比
         */
        fun setVolumeUi(volumePercent:Double)
        /**
         * 进入全屏模式
         */
        fun entryFullMode()
        /**
         * 屏幕旋转处理
         */
        fun onConfigChanged()
        /**
         * 退出当前模式，恢复普通模式
         */
        fun exitMode()
        /**
         * 显示功能控制栏
         */
        fun showController()
        /**
         * 隐藏功能控制栏
         */
        fun hideController()
        /**
         * 隐藏进度控制ui
         */
        fun hideTopAdjustUi()
        /**
         * 显示电池信息
         * @param battery 电池电量
         * @param isCharge 是否充电
         */
        fun showBattery(battery:Double, isCharge: Boolean)

        fun setPresenter(t: Presenter)
    }

    interface Presenter{
        /**
         * 初始化播放器监听
         */
        fun initPlayerListener()
        /**
         * 设置播放器状态回调
         */
        fun setJvListener(listener:JvListener)
        /**
         * 切换播放器内核
         * @param player 播放器内核
         */
        fun switchPlayEngine(player: IMediaPlayer)
        /**
         * 是否支持切换播放器内核
         * @param isSupport 是否支持切换
         */
        fun setIsSupPlayEngine(isSupport:Boolean)
        /**
         * 是否支持显示实时网速
         * @param isShow 是否显示实时网速
         * @param frequency 显示频率
         */
        fun setIsSupShowSpeed(isShow:Boolean, frequency:Long = 2000L)
        /**
         * 是否支持显示系统时间
         * @param isShow 是否显示系统时间
         */
        fun setIsSupSysTime(isShow:Boolean)
        /**
         * 是否支持自动开始播放
         * @param isAuto 是否自动开始播放
         */
        fun setIsSupAutoPlay(isAuto:Boolean)
        /**
         * 设置中间提示
         * @param message 提示消息
         * @param isShowReset 是否显示重新播放按钮
         */
        fun setMessagePromptInCenter(message:String, isShowReset:Boolean)
        /**
         * 播放控制
         */
        fun controlPlay()
        /**
         * 开始播放
         * @param position 可选任意位置，默认为初始位置
         */
        fun startPlay(position: Long = 0)
        /**
         * 暂停播放
         */
        fun pausePlay()
        /**
         * 播放完成
         * @param videoUrl 接下来的视频Url
         */
        fun completedPlay(videoUrl:String?)
        /**
         * 静音/正常状态的切换
         */
        fun switchVolumeMute()
        /**
         * 继续播放
         */
        fun continuePlay()
        /**
         * 恢复IDLE
         */
        fun resetPlay()
        /**
         * 重新播放
         */
        fun reStartPlay()
        /**
         * 播放错误
         */
        fun errorPlay(what: Int, extra: Int, message:String)
        /**
         * 手势判断
         * @param view View
         * @param event 手势事件
         */
        fun slideJudge(view: android.view.View, event:MotionEvent)
        /**
         * 滑动中
         * @param position 进度条进度
         * @param isSlide 是否为手势滑动
         */
        fun seekingPlay(position: Long, isSlide: Boolean)
        /**
         * 滑动完成
         * @param position 进度条进度
         */
        fun seekCompletePlay(position: Long)
        /**
         * 切换播放模式（全屏与普通模式）
         * @param switchMode 切换的模式
         * @param isRotateScreen 是否旋转屏幕
         */
        fun switchSpecialMode(switchMode:Int, isRotateScreen:Boolean)
        /**
         * 退出当前模式
         * @param isBackNormal  是否恢复普通模式
         * @param isRotateScreen 是否旋转屏幕
         */
        fun exitMode(isBackNormal:Boolean, isRotateScreen: Boolean)
        /**
         * 设置播放顺序
         * @param playForm
         */
        fun setPlayForm(playForm:Int)
        /**
         * 幕布准备就绪,播放器初始化完成
         * @param surface 表面
         * @param textureView 幕布View
         */
        fun textureReady(surface: SurfaceTexture, textureView: TextureView)
        /**
         * 预加载完成
         */
        fun preparedPlay()
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
         * @return 亮度大小0~255
         */
        fun getLight(isMax:Boolean):Int
        /**
         * 生命周期onPause()
         */
        fun onPause()
        /**
         * 生命周期onResume()
         */
        fun onResume()
        /**
         * 按退出按钮时的逻辑
         * @return 是否已消费
         */
        fun onBackProcess():Boolean
        /**
         * 屏幕旋转处理
         * @param newConfig
         */
        fun onConfigChanged(newConfig: Configuration)
        /**
         * 获取视频总时长
         * @return 视频时长
         */
        fun getDuration():Long
        /**
         * 获取当前播放位置
         * @return 当前视频进度
         */
        fun getPosition():Long
        /**
         * 获取当前缓冲百分比
         * @return 1~100
         */
        fun getBufferPercent():Int
        /**
         * 释放资源
         * @param destroyUi 释放MediaPlayer资源后是否退出当前模式
         */
        fun releasePlay(destroyUi:Boolean)

        fun subscribe()

        fun unSubscribe()
    }
}