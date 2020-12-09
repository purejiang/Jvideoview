package com.jplus.jvideoview.jvideo

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.LinearLayout
import com.jplus.jvideoview.common.JvConstant.*
import com.jplus.jvideoview.entity.Video
import com.jplus.jvideoview.other.BatteryManger
import com.jplus.jvideoview.other.BatteryReceiver
import com.jplus.jvideoview.utils.JvUtil
import com.jplus.jvideoview.utils.JvUtil.dt2progress
import com.jplus.jvideoview.utils.JvUtil.getIsOpenRotate
import com.jplus.jvideoview.utils.NetWorkSpeedHandler
import tv.danmaku.ijk.media.player.AndroidMediaPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import kotlin.math.floor
import java.io.IOException as IOException1


/**
 * @author JPlus
 * @date 2019/8/30.
 */
class JvPresenter(
    //所在的上下文
    private val mActivity: Activity,
    //播放器view
    private val mView: JvView,
    //默认播放器内核
    private var mPlayer: IMediaPlayer
) :
    JvContract.Presenter {
    //播放界面，相当于一块幕布
    private var mSurface: Surface? = null

    //播放视图界面，相当于放映的画面所在的区域
    private var mTextureView: TextureView? = null

    //音频管理器
    private var mAudioManager: AudioManager? = null
    private var mRunnable: Runnable? = null

    //是否在onResume后进行播放操作
    private var mIsBackContinue: Boolean? = null

    //播放状态
    private var mPlayState = PlayState.STATE_IDLE

    //播放模式
    private var mPlayMode = PlayMode.MODE_NORMAL

    //初始音量
    private var mDefaultVolume = 0

    //滑动初始进度
    private var mStartPosition = 0L

    //初始亮度
    private var mStartLight = 0

    //播放进度
    private var mPosition = 0L

    //调节的音量
    private var mVolume = 0

    //调节的亮度
    private var mLight = 0

    //缓存进度
    private var mBufferPercent = 0

    //loadingID
    private var mLoadingNums = mutableSetOf<Int>()

    //滑动功能模式
    private var mAdjustWay = -1

    //是否第一次按下，用于滑动判断
    private var mIsFirstDown = true

    //中间的控制view是否显示中
    private var mCenterControlViewIsShow = false

    //是否加载中
    private var mIsLoading = false

    //是否静音
    private var mIsVolumeMute = false

    //是否强制翻转屏幕
    private var mIsForceScreen = false

    //是否显示系统时间
    private var mIsShowSysTime = false

    //网速获取器
    private var mNetWorkSpeedHandler: NetWorkSpeedHandler? = null

    //播放器状态回调
    private var mJvListener: JvListener? = null

    //是否自动播放
    private var mIsAutoPlay = false

    //默认params
    private var mDefaultParams: ViewGroup.LayoutParams? = null

    private val mHandler by lazy {
        Handler()
    }

    //播放的视频
    private var mVideo: Video? = null

    private val mHideRunnable: Runnable by lazy {
        //延时后执行
        Runnable {
            mView.hideController()
            Log.d(JvCommon.TAG, " mView.hideController()")
            mCenterControlViewIsShow = false
        }
    }

    init {
        mDefaultParams = mView.layoutParams
        mView.setPresenter(this)
        mView.setOnTouchListener { _, _ -> true }
        //默认显示网速
        setIsSupShowSpeed(false, 2000L)
        //保存普通状态下的布局参数
        Log.d(JvCommon.TAG, "orientation:" + mActivity.requestedOrientation)
        BatteryManger.bindAutoBattery(mActivity, object :BatteryReceiver.OnBatteryChangeListener{
            override fun backBattery(battery: Double, isCharge: Boolean) {
                mView.showBatteryInfo(battery, isCharge)
            }
        })
    }


    override fun switchPlayEngine(player: IMediaPlayer) {
        mPlayer = player
        reStartPlay()
    }

    override fun subscribe() {
        showLoading("音频初始化中...", 1)
        initAudio()
        closeLoading("音频初始化完成", 1)
    }

    override fun unSubscribe() {
        BatteryManger.unbindAutoBattery(mActivity)
    }

    //初始化Media和volume
    private fun initAudio() {
        mAudioManager = mActivity.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //8.0以上需要响应音频焦点的状态改变
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            /*
            AUDIOFOCUS_GAIN  的使用场景：应用需要聚焦音频的时长会根据用户的使用时长改变，属于不确定期限。例如：多媒体播放或者播客等应用。
            AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK  的使用场景：应用只需短暂的音频聚焦，来播放一些提示类语音消息，或录制一段语音。例如：闹铃，导航等应用。
            AUDIOFOCUS_GAIN_TRANSIENT  的使用场景：应用只需短暂的音频聚焦，但包含了不同响应情况，例如：电话、QQ、微信等通话应用。
            AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE  的使用场景：同样您的应用只是需要短暂的音频聚焦。未知时长，但不允许被其它应用截取音频焦点。例如：录音软件。
            */
            val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener {
                } // Need to implement listener
                .build()
            mAudioManager?.requestAudioFocus(audioFocusRequest)
        } else {
            mAudioManager?.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        //初始音量值
        mDefaultVolume = getVolume(false)
        //初始亮度值
        mStartLight = getLight(false)
    }

    //初始化播放器监听
     override fun initPlayerListener() {
        mPlayer.let { player ->
            //设置是否循环播放，默认可不写
            player.isLooping = false
            //设置播放类型
            if (mPlayer is AndroidMediaPlayer) {
                Log.d(JvCommon.TAG, "AndroidMediaPlayer")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val attributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                        .build()
                    (player as AndroidMediaPlayer).internalMediaPlayer.setAudioAttributes(attributes)
                } else {
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC)
                }
            } else {
                player.setAudioStreamType(AudioManager.STREAM_MUSIC)
            }

            //播放完成监听
            player.setOnCompletionListener {
                completedPlay(null)
            }

            //seekTo()调用并实际查找完成之后
            player.setOnSeekCompleteListener {
                // mPlayState = PlayState.STATE_IDLE
                Log.d(JvCommon.TAG, "setOnSeekCompleteListener")
                seekCompleted()
            }

            //预加载监听
            player.setOnPreparedListener {
                preparedPlay()
            }

            //相当于缓存进度条
            player.setOnBufferingUpdateListener { _, percent ->
                buffering(percent)
            }

            //播放错误监听
            player.setOnErrorListener { _, what, extra ->
                errorPlay(what, extra, "播放错误，请重试~")
                true
            }

            //播放信息监听
            player.setOnInfoListener { _, what, _ ->
                when (what) {
                    MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                        // 播放器开始渲染
                        mPlayState = PlayState.STATE_PLAYING
                        Log.d(JvCommon.TAG, "MEDIA_INFO_VIDEO_RENDERING_START")
                    }
                    MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                        bufferStart()
                    }
                    IMediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                        bufferEnd()
                    }
                    MediaPlayer.MEDIA_INFO_NOT_SEEKABLE -> {
                        //无法seekTo
                        notSeek()
                    }
                }
                true
            }
            //播放尺寸
            player.setOnVideoSizeChangedListener { _, _, _, _, _ ->
                //这里是视频的原始尺寸大小
                Log.d(JvCommon.TAG, "setOnVideoSizeChangedListener")
                mTextureView?.layoutParams = JvUtil.changeVideoSize(
                    mView.width,
                    mView.height,
                    player.videoWidth,
                    player.videoHeight
                )
            }
            //设置IjkMediaPlayer Option
            if (player is IjkMediaPlayer) {
                //关闭播放器缓冲，这个必须关闭，否则会出现播放一段时间后，一直卡主，控制台打印 FFP_MSG_BUFFERING_START
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1)
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L)
                /*
                结束
                 */
                player.setOption(
                    IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                    "enable-accurate-seek",
                    1
                )//防止某些视频在SeekTo的时候，（FFMPEG不兼容）会跳回到拖动前的位置
                player.setOption(
                    IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                    "start-on-prepared",
                    0
                )
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1)
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0)
                player.setOption(
                    IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                    "buffer-size",
                    1024 * 1024 * 5
                )//缓冲大小,单位b
                player.setOnNativeInvokeListener(IjkMediaPlayer.OnNativeInvokeListener { _, _ ->
                    true
                })

                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "reconnect", 5)
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1)
                //硬解码
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1)
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
                player.setOption(
                    IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                    "mediacodec-handle-resolution-change",
                    1
                )
            }

            //设置是否保持屏幕常亮
            player.setScreenOnWhilePlaying(true)
        }
    }

    fun startVideo(video: Video) {
        //播放视频
        mSurface?.let {
            loadVideo(it, video)
        }
    }

    //视频播放准备
    private fun loadVideo(surface: Surface, video: Video) {
        Log.d(JvCommon.TAG, "entryVideo:$video")
        mVideo = video
        showLoading("视频预加载...", 4)
        //设置title
        mView.setTitle(if (video.name.isEmpty()) "未知视频" else video.name)
        mPlayer.let {
            Log.d(JvCommon.TAG, "mPlayState:$mPlayState")
            //如果不是IDLE状态就改变播放器状态
            if (mPlayState != PlayState.STATE_IDLE) {
                resetPlay()
            }
            try {
                it.dataSource = (video.url)
                //加载url之后为播放器初始化完成状态
                mPlayState = PlayState.STATE_INITLIZED
                //设置渲染画板
                it.setSurface(surface)
                //初始化播放器监听
                initPlayerListener()
                //异步的方式装载流媒体文件
                it.prepareAsync()
            } catch (e: IOException1) {
                e.printStackTrace()
                errorPlay(0, 0, "视频路径有误或者地址失效~")
            }
        }
    }

    override fun setJvListener(listener: JvListener) {
        mJvListener = listener
    }

    /*
    设置区
     */
    override fun setIsSupShowSpeed(isShow: Boolean, frequency: Long) {
        if (isShow) {
            if (mNetWorkSpeedHandler == null) {
                //网速获取器
                mNetWorkSpeedHandler = NetWorkSpeedHandler(mActivity, frequency)
            }
        } else {
            mNetWorkSpeedHandler = null
        }
    }

    override fun setIsSupSysTime(isShow: Boolean) {
        mIsShowSysTime = isShow
    }

    override fun setIsSupPlayEngine(isSupport: Boolean) {
        mView.showSwitchEngine(isSupport)
    }

    override fun setIsSupAutoPlay(isAuto: Boolean) {
        mIsAutoPlay = isAuto
    }

    override fun setPlayForm(playForm: Int) {
        Log.d(JvCommon.TAG, "setPlayForm:$playForm")
//        mPlayForm = playForm
    }

    /*
    播放器状态区
     */
    override fun textureReady(surface: SurfaceTexture, textureView: TextureView) {
        Log.d(JvCommon.TAG, "textureReady")
        if (mSurface == null) {
            mSurface = Surface(surface)
        }
        mTextureView = mTextureView ?: textureView
//        mPlayState = PlayState.STATE_PREPARING
        mJvListener?.onInitSuccess()
    }

    override fun preparedPlay() {
        mJvListener?.onPrepared()
        //预加载完成状态
        mPlayState = PlayState.STATE_PREPARED

        closeLoading("预加载完成", 4)
        Log.d(JvCommon.TAG, "setOnPreparedListener")
        mView.setOnTouchListener { _, _ -> false }

        //预加载后先播放再暂停，1：防止播放错误-38(未开始就停止) 2：可以显示第一帧画面
        mView.preparedVideo(getVideoTimeStr(mVideo?.progress), mVideo?.progress?.toInt() ?: 0, mPlayer.duration.toInt())

        //如果开启自动播放的话就直接播放,否则直接滑动到初始位置
        if (mIsAutoPlay) startPlay(mVideo?.progress ?: 0)
        showControlUi(false)
    }

    //开始播放
    override fun startPlay(position: Long) {
        mJvListener?.onStartPlay()
        if (mPlayState == PlayState.STATE_PREPARED) {
            Log.d(JvCommon.TAG, "startPlay:$position")
            mPlayer.let {
                //如果不在播放中，指定视频播放位置并开始播放
                if (position != 0L) soughtTo(position)
                it.start()
                mPlayState = PlayState.STATE_PLAYING
            }
            mView.startVideo()
            runVideoTime()
        }
    }

    override fun nextPlay() {
        mJvListener?.onNextPlay()
    }

    //暂停播放
    override fun pausePlay() {
        mJvListener?.onPausePlay()
        Log.d(JvCommon.TAG, "pausePlay")
        mPlayState =
            if (mPlayState == PlayState.STATE_PLAYING || mPlayState == PlayState.STATE_PREPARED) {
                PlayState.STATE_PAUSED
            } else if (mPlayState == PlayState.STATE_BUFFERING_PLAYING) {
                PlayState.STATE_BUFFERING_PAUSED
            } else {
                return
            }
        mPlayer.pause()
        stopVideoTime()
        stopHideControlUi()
        showControlUi(false)
        mView.pauseVideo()
    }

    override fun continuePlay() {
        Log.d(JvCommon.TAG, "continuePlay")
        when (mPlayState) {
            PlayState.STATE_PAUSED, PlayState.STATE_COMPLETED -> {
                mPlayer.start()
                mPlayState = PlayState.STATE_PLAYING
            }
            PlayState.STATE_BUFFERING_PAUSED -> {
                mPlayer.start()
                mPlayState = PlayState.STATE_BUFFERING_PLAYING
            }
            PlayState.STATE_ERROR -> {
                reStartPlay()
            }
        }
        mView.continueVideo()
        runVideoTime()
    }

    override fun resetPlay() {
        Log.d(JvCommon.TAG, "resetPlay")
        mJvListener?.onReset()
        mView.reset()
        mView.closeMessagePrompt()
        mPlayer.reset()
        mPlayState = PlayState.STATE_IDLE
        mView.setOnTouchListener { _, _ -> true }
    }

    override fun reStartPlay() {
        mJvListener?.onReStart()

        mView.closeMessagePrompt()
        resetPlay()
        mVideo?.let {
            startVideo(it)
        }
    }

    private fun soughtTo(position: Long) {
        //loading
        showLoading("seek中....", 5)
        mPlayer.seekTo(position)
        //缓冲开始时显示网速
        mNetWorkSpeedHandler?.bindHandler(object : NetWorkSpeedHandler.OnNetWorkSpeedListener {
            override fun netWorkSpeed(speed: String) {
                mView.showNetSpeed("$speed/s")
            }
        })
    }

    private fun seekCompleted() {
        Log.d(JvCommon.TAG, "seekCompleted")
        closeLoading("seek完成", 5)
        if(mPlayState==PlayState.STATE_BUFFERING_PAUSED||mPlayState==PlayState.STATE_BUFFERING_PLAYING){
            closeLoading("缓冲UI停止", 3)
            showLoading("缓冲中....", 3)
            //缓冲开始时绑定实时网速获取器
            mNetWorkSpeedHandler?.bindHandler(object : NetWorkSpeedHandler.OnNetWorkSpeedListener {
                override fun netWorkSpeed(speed: String) {
                    mView.showNetSpeed("$speed/s")
                }
            })
        }
        //缓冲结束解绑实时网速获取器
        mNetWorkSpeedHandler?.unbindHandler()
    }

    override fun seekCompletePlay(position: Long) {
        Log.d(JvCommon.TAG, "seekToPlay:$position")
        mPlayer.let {
            if (mPlayState == PlayState.STATE_PAUSED || mPlayState == PlayState.STATE_BUFFERING_PAUSED) {
                soughtTo(position)
                continuePlay()//拖动时播放一秒再暂停
                pausePlay()
            } else if (mPlayState == PlayState.STATE_PLAYING || mPlayState == PlayState.STATE_BUFFERING_PLAYING) {
                soughtTo(position)
                continuePlay()
            } else if (mPlayState == PlayState.STATE_PREPARED) {
                startPlay(position)
            }
        }
        mPosition = position
    }

    override fun seekingPlay(position: Long, isSlide: Boolean) {
        mView.seekingVideo(getVideoTimeStr(position), position, isSlide)
    }

    private fun bufferStart() {
        //loading
        showLoading("缓冲中....", 3)

        Log.d(JvCommon.TAG, "bufferStart:state$mPlayState")
        // MediaPlayer暂时不播放，以缓冲更多的数据
        mPlayState =
            when (mPlayState) {
                PlayState.STATE_PAUSED -> {
                    PlayState.STATE_BUFFERING_PAUSED
                }
                PlayState.STATE_PLAYING -> {
                    PlayState.STATE_BUFFERING_PLAYING
                }
                else -> {
                    return
                }
            }
        //缓冲开始时绑定实时网速获取器
        mNetWorkSpeedHandler?.bindHandler(object : NetWorkSpeedHandler.OnNetWorkSpeedListener {
            override fun netWorkSpeed(speed: String) {
                mView.showNetSpeed("$speed/s")
            }
        })
    }

    private fun buffering(percent: Int) {
        mJvListener?.onBuffering()
        if (percent != 0) {
            mBufferPercent = percent
        }
        mView.showBuffering(percent)
    }

    private fun bufferEnd() {
        Log.d(JvCommon.TAG, "buffered")
        // 填充缓冲区后，MediaPlayer恢复播放/暂停
        if (mPlayState == PlayState.STATE_BUFFERING_PLAYING) {
            continuePlay()
        } else if (mPlayState == PlayState.STATE_BUFFERING_PAUSED) {
            pausePlay()
        }
        closeLoading("缓冲完成", 3)
        //缓冲结束解绑网速获取器
        mNetWorkSpeedHandler?.unbindHandler()
    }

    override fun completedPlay(videoUrl: String?) {
        mJvListener?.onCompleted()
        //播放完成状态
        mPlayState = PlayState.STATE_COMPLETED
        Log.d(JvCommon.TAG, "completedPlay")
    }

    override fun errorPlay(what: Int, extra: Int, message: String) {
        mJvListener?.onError()

        Log.d(JvCommon.TAG, "setOnErrorListener:$what, $message")
        mPlayState = PlayState.STATE_ERROR
        //播放错误时记录下时间点
        mVideo?.progress = if (getPosition() != 0L) {
            getPosition()
        } else {
            if (mStartPosition != 0L) {
                mStartPosition
            } else {
                mPosition
            }
        }
        setMessagePromptInCenter(message, true)
    }

    override fun releasePlay(destroyUi: Boolean) {
        mSurface?.release()
        mHandler.removeCallbacks(mRunnable)
        mPlayer.stop()
        mPlayer.release()//调用release()方法来释放资源，资源可能包括硬件加速组件的单态固件
        mSurface = null
    }

    /*
信息获取方法区
 */
    override fun getPlayState(): Int {
        return mPlayState
    }

    override fun getPlayMode(): Int {
        return mPlayMode
    }

    override fun getLight(isMax: Boolean): Int {
        var nowBrightnessValue = 0
        try {
            nowBrightnessValue =
                Settings.System.getInt(mActivity.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Exception) {

        }
        return nowBrightnessValue
    }

    override fun getVolume(isMax: Boolean): Int {
        return if (isMax) {
            mAudioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        } else {
            mAudioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)
        } ?: 0
    }

    override fun getDuration(): Long {
        return mPlayer.duration
    }

    override fun getPosition(): Long {
        return mPlayer.currentPosition
    }

    override fun getBufferPercent(): Int {
        return mBufferPercent
    }

    /*
    播放器参数调节方法区
     */
    // 1.创建一个手势监听回调
    private val listener: SimpleOnGestureListener = object : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            Log.d(JvCommon.TAG, "onDown")
            return super.onDown(e)
        }

        override fun onShowPress(e: MotionEvent) {
            Log.d(JvCommon.TAG, "onShowPress")
            super.onShowPress(e)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            Log.d(JvCommon.TAG, "onDoubleTap")
            mPlayer.let {
                if (mPlayState == PlayState.STATE_PLAYING || mPlayState == PlayState.STATE_BUFFERING_PLAYING) {
                    pausePlay()
                } else if (mPlayState == PlayState.STATE_BUFFERING_PAUSED || mPlayState == PlayState.STATE_PAUSED) {
                    continuePlay()
                } else if (mPlayState == PlayState.STATE_PREPARED) {
                    startPlay()
                }
            }
            return super.onDoubleTap(e)
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            Log.d(JvCommon.TAG, "onSingleTapConfirmed")
            if (mPlayState == PlayState.STATE_ERROR) return true
            if (mCenterControlViewIsShow) hideControlUi() else showControlUi(true)
            return true
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (mIsFirstDown) {
                mIsFirstDown = false
                mAdjustWay = getAdjustMode(e1)
            }
            //水平滑动的距离
            val distX = e2.x - e1.x
            //竖直滑动的距离
            val distY = e2.y - e1.y
            Log.d(JvCommon.TAG, "e1.x:${e1.x},e1.y:${e1.y},  e2.x:${e2.x}, e2.y:${e2.y}")
            //从手指落下时判断滑动时改变的模式
            when (mAdjustWay) {
                PlayAdjust.ADJUST_VOLUME -> {
                    //音量调节，从下往上为加，所以需要加上负号
                    if (!mIsVolumeMute) {
                        setVolume(mDefaultVolume, -distY)
                    }
                }
                PlayAdjust.ADJUST_LIGHT -> {
                    // 亮度调节
                    setLight(mStartLight, -distY)
                }
                PlayAdjust.ADJUST_VIDEO -> {
                    //快进/后退
                    slidePlay(mStartPosition, distX)
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY)
        }
    }

    // 2.创建一个检测器
    private val detector = GestureDetector(mActivity, listener)
    //====================================================================================

    override fun slideJudge(view: View, event: MotionEvent) {
        detector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                endAdjust()
                mView.hideTopAdjustUi()
            }
        }
    }

    private fun getAdjustMode(event: MotionEvent): Int {
        //调整前获取调整的模式
        Log.d(JvCommon.TAG, "getAdjustMode")
        val width = mView.width
        return when {
            //通过起始点坐标判断滑动是 快进/后退、亮度调节、音量调节
            event.x >= 0.8 * width -> {
                PlayAdjust.ADJUST_VOLUME
            }
            event.x <= 0.2 * width -> {
                PlayAdjust.ADJUST_LIGHT
            }
            else -> {
                mStartPosition = getPosition()
                stopVideoTime()
                PlayAdjust.ADJUST_VIDEO
            }
        }
    }

    private fun endAdjust() {
        //调整结束后保存结果
        Log.d(JvCommon.TAG, "endAdjust")
        mIsFirstDown = true
        when (mAdjustWay) {
            PlayAdjust.ADJUST_LIGHT -> {
                //保存亮度
                mStartLight = mLight
            }
            PlayAdjust.ADJUST_VOLUME -> {
                //保存音量
                mDefaultVolume = mVolume
            }
            PlayAdjust.ADJUST_VIDEO -> {
                //保存并跳到指定位置播放
                mStartPosition = mPosition
                seekCompletePlay(mStartPosition)
            }
        }
        mAdjustWay = -1
    }

    override fun switchVolumeMute() {
        //设置静音和恢复静音前音量
        mIsVolumeMute = !mIsVolumeMute
        mAudioManager?.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            if (mIsVolumeMute) 0 else mVolume,
            0
        )
        mView.setVolumeMute(mIsVolumeMute)
    }

    /**
     * 滑动屏幕快进或者后退
     * @param distance
     */
    private fun slidePlay(startProgress: Long, distance: Float) {
        if (mPlayState == PlayState.STATE_COMPLETED || mPlayState == PlayState.STATE_IDLE || mPlayState == PlayState.STATE_INITLIZED || mPlayState == PlayState.STATE_ERROR) {
            //播放状态为初始前，初始化完成以及加载完毕和错误时不能滑动播放
            Log.d(JvCommon.TAG, "can't to slide play ,state${mPlayState}")
            return
        }
        var position =
            startProgress + floor(
                dt2progress(
                    distance,
                    getDuration(),
                    (mView as LinearLayout).width,
                    0.3
                )
            ).toLong()
        when {
            position > getDuration() -> position = getDuration()
            position < 0 -> position = 0
            else -> mPosition = position //保存进度
        }
        mView.seekingVideo(getVideoTimeStr(position), position, true)
    }

    private fun setLight(startLight: Int, distance: Float) {
        Log.d(JvCommon.TAG, "startLight:$startLight, distance$distance")
        var light = startLight + floor(
            dt2progress(
                distance,
                255,
                (mView as LinearLayout).height,
                0.5
            )
        ).toInt()
        when {
            light >= 255 -> light = 255
            light <= 0 -> light = 0
            else -> mLight = light //保存亮度
        }
        //设置当前activity的亮度
        val params = mActivity.window.attributes
        params.screenBrightness = light / 255f
        mActivity.window.attributes = params

        mView.setLightUi(floor(light / 255f * 100).toInt())
    }

    private fun setVolume(startVolume: Int, distance: Float) {

        var volume =
            startVolume +
                dt2progress(
                    distance,
                    getVolume(true).toLong(),
                    (mView as LinearLayout).height,
                    1.0
                )
        when {
            volume <= 0.0 -> volume = 0.0
            volume >= getVolume(true)*1.0 -> volume = getVolume(true)*1.0
        }
        mAudioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, volume.toInt(), 0)
        mVolume = volume.toInt()
        mView.setVolumeUi(volume * 100.0 / getVolume(true))
    }

    /*
    生命周期方法
     */
    override fun onPause() {
        //取消屏幕常亮
        mActivity.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mIsBackContinue =
            if (mPlayState == PlayState.STATE_PAUSED || mPlayState == PlayState.STATE_BUFFERING_PAUSED) {
                false
            } else if (mPlayState == PlayState.STATE_PLAYING || mPlayState == PlayState.STATE_BUFFERING_PLAYING) {
                true
            } else {
                //播放器初始化前、初始化中、初始化后或者播放完成、播放错误时中不做任何操作
                mIsBackContinue = null
                return
            }
        pausePlay()
    }

    override fun onResume() {
        //设置屏幕常亮
        mActivity.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //播放器初始化中不做任何操作
        mIsBackContinue?.let {
            if (it) continuePlay() else pausePlay()
        }
    }

    override fun onConfigChanged(newConfig: Configuration) {
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            autoEntryPortraitScreen()
            Log.d(JvCommon.TAG, "Configuration.ORIENTATION_PORTRAIT")
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            autoEntryFullScreen()
            Log.d(JvCommon.TAG, "Configuration.ORIENTATION_LANDSCAPE")
        }
    }

    override fun onBackProcess(): Boolean {
        if (mPlayMode == PlayMode.MODE_FULL_SCREEN) {
            exitFullOrWindowMode(isBackNormal = true, isRotateScreen = true)
            return true
        }
        return false
    }

    override fun switchFullOrWindowMode(switchMode: Int, isRotateScreen: Boolean) {
        Log.d(JvCommon.TAG, "playMode$mPlayMode")
        when (mPlayMode) {
            PlayMode.MODE_NORMAL -> {
                if (switchMode == SwitchMode.SWITCH_FULL_OR_NORMAL) {
                    //进入全屏模式（在dialog的模式下似乎会有适配问题）
                    mPlayMode = PlayMode.MODE_FULL_SCREEN
                    mJvListener?.onFullScreen()
                    //没有开启旋转的情况下要强制转屏来达到全屏效果
                    mActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                    if(getIsOpenRotate(mActivity)) {
                        //开启旋转的情况下可以在转屏后恢复到默认状态， 屏幕旋转时指定默认的屏幕方向不然会转不过来...
                        mActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                }
            }
            PlayMode.MODE_FULL_SCREEN -> {
                exitFullOrWindowMode(true, isRotateScreen)
            }
        }
    }

    /*
    模式切换方法
     */
    /**
     * 根据onConfigChanged自动切换横屏
     */
    private fun autoEntryFullScreen() {
        //显示手机状态
        if (mIsShowSysTime) {
            mView.showSysInfo(true)
        }
        //该方案只适合父容器为linearLayout且根布局中没有滑动控件，其他父容器下适配捉急
        //============直接设置根布局改为横屏，然后View宽高改为MATCH_PARENT来实现=======
        mActivity.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        //设置为充满父布局
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        mView.layoutParams = params
        mActivity.window.decorView.apply {
            //隐藏导航栏，状态栏，并且全屏， 粘性沉浸式（PS:与沉浸式的区别在于会自动收起且不改变原始布局）
            systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
        }

        //全屏直接使用手机大小,此时未翻转的话，高宽对调
        val phoneWidth = JvUtil.getPhoneDisplayWidth(mActivity)
        val phoneHeight = JvUtil.getPhoneDisplayHeight(mActivity)
        mTextureView?.layoutParams = JvUtil.changeVideoSize(
            if (phoneHeight > phoneWidth) phoneHeight else phoneWidth,
            if (phoneHeight > phoneWidth) phoneWidth else phoneHeight,
            mPlayer.videoWidth,
            mPlayer.videoHeight
        )
        mView.entryFullMode()
    }
    /**
     * 根据onConfigChanged自动切换竖屏
     */
    private fun autoEntryPortraitScreen() {
        if (mIsShowSysTime) {
            mView.showSysInfo(false)
        }
        //进入普通模式
        mDefaultParams?.let {
            mPlayMode = PlayMode.MODE_NORMAL
            mActivity.window.clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            mActivity.window.decorView.systemUiVisibility = View.VISIBLE

            mView.layoutParams = it
            mTextureView?.layoutParams =
                JvUtil.changeVideoSize(it.width, it.height, mPlayer.videoWidth, mPlayer.videoHeight)
        }
        mView.exitMode()
    }

    override fun exitFullOrWindowMode(isBackNormal: Boolean, isRotateScreen: Boolean) {
        Log.d(JvCommon.TAG, "exitMode")
        if (getPlayMode() != PlayMode.MODE_NORMAL && isBackNormal) {
            //屏幕方向改为竖屏
            mActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            if(getIsOpenRotate(mActivity)) {
                //开启旋转的情况下可以在转屏后恢复到默认状态，确保下次能够旋转屏幕
                mActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
            mJvListener?.onNormalScreen()
        }
    }

    /*
     线程相关方法
     */
    //播放进度开始计时
    private fun runVideoTime() {
        Log.d(JvCommon.TAG, "runVideoTime")
        mRunnable = mRunnable ?: Runnable {
            mPlayer.let {
                if (it.isPlaying) {
                    //更新播放进度showCenterControlView
                    mView.playing(
                        getVideoTimeStr(it.currentPosition),
                        it.currentPosition
                    )
                }
            }
            //重复调起自身
            mHandler.postDelayed(mRunnable, 200)
        }
        mHandler.post(mRunnable)
        runHideControlUi(5000)
    }

    //播放进度停止计时
    private fun stopVideoTime() {
        mHandler.removeCallbacks(mRunnable)
    }

    /*
    UI调整方法
     */
    override fun controlPlay() {
        when (mPlayState) {
            PlayState.STATE_PLAYING, PlayState.STATE_BUFFERING_PLAYING -> pausePlay()
            PlayState.STATE_PAUSED, PlayState.STATE_BUFFERING_PAUSED -> continuePlay()
            PlayState.STATE_PREPARED -> startPlay(mVideo?.progress ?: 0L)
        }
    }

    private fun runHideControlUi(delayMillis: Long) {
        Log.d(JvCommon.TAG, "runHideControlUi")
        if (mPlayState == PlayState.STATE_PLAYING || mPlayState == PlayState.STATE_BUFFERING_PLAYING) {
            stopHideControlUi()
            mHandler.postDelayed(mHideRunnable, delayMillis)
        }
    }

    private fun showControlUi(autoHide: Boolean) {
        mCenterControlViewIsShow = true
        if (!mIsLoading) { //不在加载中则显示中心按钮
            mView.showCenterPlayView()
        }
        mView.showController()
        if (autoHide) {
            runHideControlUi(5000)
        }
    }

    private fun hideControlUi() {
        stopHideControlUi() // 去掉自动隐藏
        mCenterControlViewIsShow = false
        mView.hideController()
    }

    private fun stopHideControlUi() {
        mHandler.removeCallbacks(mHideRunnable)
    }

    private fun showLoading(content: String, loadingNum: Int) {
        if (loadingNum in mLoadingNums) {
            Log.e(JvCommon.TAG, "loading- show[$content, $loadingNum] is exist.")
        } else {
            mLoadingNums.add(loadingNum)
            mView.showLoading(content)
            mIsLoading = true
            Log.d(JvCommon.TAG, "loading-show[$content, $loadingNum]")
        }
    }

    private fun closeLoading(content: String, loadingNum: Int) {
        if (loadingNum in mLoadingNums) {
            mLoadingNums.remove(loadingNum)
            mView.closeLoading(content)
            mIsLoading = false
            Log.d(JvCommon.TAG, "loading-close[$content, $loadingNum]")
        } else {
            Log.e(JvCommon.TAG, "loading- close[$content, $loadingNum] is not exist.")
        }
    }

    private fun closeAllLoading() {
        mLoadingNums.clear()
        mView.closeLoading("closeAll")
        mIsLoading = false
    }

    override fun setMessagePromptInCenter(message: String, isShowReset: Boolean) {
        closeAllLoading()
        mView.closeCenterPlayView()
        mView.showMessagePrompt(message, isShowReset)
    }

    /*
    其他方法区
     */
    private fun notSeek() {

    }

    private fun getVideoTimeStr(position: Long?): String {
        return JvUtil.progress2Time(position) + "&" + JvUtil.progress2Time(getDuration())
    }

}