package com.jplus.jvideoview.jvideo

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
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
import androidx.appcompat.app.AppCompatActivity
import com.jplus.jvideoview.data.Video
import com.jplus.jvideoview.data.source.VideoDataSource
import com.jplus.jvideoview.data.source.VideoRepository
import com.jplus.jvideoview.jvideo.JVideoState.*
import com.jplus.jvideoview.utils.JVideoUtil
import com.jplus.jvideoview.utils.JVideoUtil.dt2progress
import tv.danmaku.ijk.media.player.AndroidMediaPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.io.IOException
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max


/**
 * @author JPlus
 * @date 2019/8/30.
 */
class JVideoViewPresenter(
    private val mContext: Context,
    private val mView: JVideoViewContract.Views,
    private val mVideoRepository: VideoRepository,
    private val mPlayerType: Int
) :
    JVideoViewContract.Presenter {


    private var mPlayState = PlayState.STATE_IDLE
    private var mPlayMode = PlayMode.MODE_NORMAL
    private var mPlayForm = PlayForm.PLAYBACK_ONE_END
    private var mAdjustWay = 0

    private var mPlayer: IMediaPlayer? = null
    private var mSurface: Surface? = null
    private var mTextureView: TextureView? = null
    private var mParams: LinearLayout.LayoutParams? = null
    private var mAudioManager: AudioManager? = null
    private var mRunnable: Runnable? = null
    private var mHideRunnable: Runnable? = null

    private var mStartVolume = 0
    private var mStartPosition = 0
    private var mStartLight = 0
    private var mPosition = 0
    private var mVolume = 0
    private var mLight = 0
    private var mIsFirstDown = true
    private var mBufferPercent = 0
    private var mIsBackContinue = false

    private val mHandler = Handler()
    private var mIsShowControllerView = false
    private var mVolumeMute = false
    private var mVideoList = ArrayList<Video>()
    private var mVideoIndex = 0


    init {
        mView.setPresenter(this)
        //保存普通状态下的布局参数
        mParams = LinearLayout.LayoutParams((mView as View).layoutParams)
        //延时后执行
        mHideRunnable = Runnable {
            mView.hideOrShowController(false)
            mIsShowControllerView = false
        }
    }

    override fun subscribe() {
        mView.showLoading(true, "播放器初始化中...")
        mPlayer = if (mPlayerType == PlayBackEngine.PLAYBACK_IJK_PLAYER) {
            mPlayer ?: IjkMediaPlayer()
        } else {
            mPlayer ?: AndroidMediaPlayer()
        }
        Log.d("pipa", "initMediaPlayer:$mPlayer")
        initAudio()
    }

    override fun unSubscribe() {

    }

    //初始化Media和volume
    private fun initAudio() {
        mAudioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

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
            mAudioManager?.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        //初始音量值
        mStartVolume = getVolume(false)
        //初始亮度值
        mStartLight = getLight(false)
    }

    //初始化播放器监听
    private fun initPlayerListener() {
        mPlayer?.let { it ->
            //设置是否循环播放，默认可不写
            it.isLooping = false
            //设置播放类型
            if (mPlayer is AndroidMediaPlayer) {
                Log.d("pipa", "AndroidMediaPlayer")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val attributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                        .build()
                    (it as AndroidMediaPlayer).internalMediaPlayer.setAudioAttributes(attributes)
                } else {
                    it.setAudioStreamType(AudioManager.STREAM_MUSIC)
                }
            } else {
                it.setAudioStreamType(AudioManager.STREAM_MUSIC)
            }

            //播放完成监听
            it.setOnCompletionListener {
                completedPlay(null)
            }

            //seekTo()调用并实际查找完成之后
            it.setOnSeekCompleteListener {
                // mPlayState = PlayState.STATE_IDLE
                Log.d("pipa", "setOnSeekCompleteListener")
            }

            //预加载监听
            it.setOnPreparedListener {
                mView.showLoading(false, "初始化完成")
                preparedPlay()
            }

            //相当于缓存进度条
            it.setOnBufferingUpdateListener { mp, percent ->
                if (percent != 0) {
                    mBufferPercent = percent
                }
                mView.buffering(mBufferPercent)
            }

            //播放错误监听
            it.setOnErrorListener { mp, what, extra ->
                errorPlay(what, extra, "播放错误，请重试~")
                true
            }

            //播放信息监听
            it.setOnInfoListener { mp, what, extra ->
                when (what) {
                    MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                        Log.d("pipa", "MEDIA_INFO_VIDEO_RENDERING_START")
                        // 播放器开始渲染
                        mPlayState = PlayState.STATE_PLAYING
                    }
                    MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                        //loading
                        mView.showLoading(true, "缓冲中...")
                        if (it is IjkMediaPlayer) {
                            mView.showNetSpeed("" + (it as IjkMediaPlayer).tcpSpeed / 1024 + "K/s")
                        }
                        Log.d("pipa", "MEDIA_INFO_BUFFERING_START")
                        // MediaPlayer暂时不播放，以缓冲更多的数据
                        mPlayState =
                            if (mPlayState == PlayState.STATE_PAUSED || mPlayState == PlayState.STATE_BUFFERING_PAUSED) {
                                PlayState.STATE_BUFFERING_PAUSED
                            } else {
                                PlayState.STATE_BUFFERING_PLAYING
                            }

                    }
                    IMediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                        mView.showLoading(false, "缓冲完成")
                        Log.d("pipa", "MEDIA_INFO_BUFFERING_END")
                        // 填充缓冲区后，MediaPlayer恢复播放/暂停
                        if (mPlayState == PlayState.STATE_BUFFERING_PLAYING) {
                            mPlayState = PlayState.STATE_PLAYING
                            mView.playing(
                                getVideoTimeStr(it.currentPosition.toInt()),
                                it.currentPosition.toInt()
                            )
                        }
                        if (mPlayState == PlayState.STATE_BUFFERING_PAUSED) {
                            mPlayState = PlayState.STATE_PAUSED
                            mView.pauseVideo()
                        }
                    }
                    MediaPlayer.MEDIA_INFO_NOT_SEEKABLE -> {
                        //无法seekTo
                    }
                }
                true
            }
            //播放尺寸
            it.setOnVideoSizeChangedListener { mp, var1, var2, width, height ->
                //这里是视频的原始尺寸大小
                Log.d("pipa", "setOnVideoSizeChangedListener")
                changeVideoSize((mView as View).width, (mView as View).height)
            }
            //设置Option
            if (mPlayerType == PlayBackEngine.PLAYBACK_IJK_PLAYER) {
                (it as IjkMediaPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0)
            }

            //设置是否保持屏幕常亮
            it.setScreenOnWhilePlaying(true)
        }
    }

    //开始播放
    override fun startPlay(position: Int) {
        Log.d("pipa", "startPlay:$position")
        mPlayer?.let {
            //如果不在播放中，指定视频播放位置并开始播放
            it.seekTo(position.toLong())
            it.start()
            mPlayState == PlayState.STATE_PLAYING
        }
        mView.startVideo(position)
        runVideoTime()
    }

    override fun seekCompletePlay(position: Int) {
        Log.d("pipa", "seekToPlay:$position")
        mPosition = position
        mPlayer?.let {
            if (mPlayState == PlayState.STATE_PAUSED || mPlayState == PlayState.STATE_BUFFERING_PAUSED) {
                it.seekTo(position.toLong())
                pausePlay()
            } else if (mPlayState == PlayState.STATE_PLAYING || mPlayState == PlayState.STATE_BUFFERING_PLAYING) {
                it.seekTo(position.toLong())
                continuePlay()
            } else if (mPlayState == PlayState.STATE_PREPARED) {
                startPlay(position)
            }
        }
    }

    override fun seekingPlay(position: Int, isSlide: Boolean) {
        mView.seekingVideo(getVideoTimeStr(position), position, isSlide)
    }

    //暂停播放
    override fun pausePlay() {
        Log.d("pipa", "pausePlay")
        if (mPlayState == PlayState.STATE_PLAYING || mPlayState == PlayState.STATE_PREPARED) {
            mPlayState = PlayState.STATE_PAUSED
        } else if (mPlayState == PlayState.STATE_BUFFERING_PLAYING) {
            mPlayState = PlayState.STATE_BUFFERING_PAUSED
        }
        mPlayer?.pause()
        mView.pauseVideo()
        stopVideoTime()
    }

    override fun completedPlay(videoUrl: String?) {
        Log.d("pipa", "completedPlay")
        mPlayState = PlayState.STATE_COMPLETED
        when (mPlayForm) {
            PlayForm.PLAYFORM_TURN -> {
                //播放结束后的显示
                mView.completedVideo("即将播放下个视频~")
                //播放结束后，重新设置播放器，避免再次调用setDataSource()时出现IllegalStateException
                mVideoIndex++
                Handler().postDelayed(
                    {
                        entryVideoLoop()
                    }, 3000
                )
            }
            PlayForm.PLAYBACK_ONE_LOOP -> {
                //播放结束后的显示
                mView.completedVideo("即将重新播放该视频~")
                Handler().postDelayed(
                    {
                        entryVideoLoop()
                    }, 3000
                )
            }
            else -> //播放结束后的显示
                mView.completedVideo("播放结束~")
        }
    }

    override fun preparedPlay() {
        Log.d("pipa", "setOnPreparedListener")
        mPlayer?.let {
            mPlayState = PlayState.STATE_PREPARED
            //预加载后先播放再暂停，1：防止播放错误-38(未开始就停止) 2：可以显示第一帧画面
            it.start()
            it.pause()
            mView.preparedVideo(getVideoTimeStr(null), it.duration.toInt())
        }
        showOrHideControlDelay(isShow = true, isClick = false)
    }

    override fun resetPlay() {
        mPlayer?.reset()
        mPlayState = PlayState.STATE_IDLE
    }

    override fun errorPlay(what: Int, extra: Int, message: String) {
        Log.d("pipa", "setOnErrorListener:$what")
        mPlayState = PlayState.STATE_ERROR
        mView.showMessagePrompt(message)
    }

    override fun continuePlay() {
        Log.d("pipa", "continuePlay")
        when (mPlayState) {
            PlayState.STATE_PAUSED -> {
                mPlayer?.start()
                mPlayState = PlayState.STATE_PLAYING
            }
            PlayState.STATE_BUFFERING_PAUSED -> {
                mPlayer?.start()
                mPlayState = PlayState.STATE_BUFFERING_PLAYING
            }
            PlayState.STATE_ERROR -> {
                entryVideoLoop()
            }
            PlayState.STATE_COMPLETED -> {
                entryVideoLoop()
            }
        }
        mView.continueVideo()
        runVideoTime()
    }


    override fun onPause() {
        if (mPlayState == PlayState.STATE_PAUSED || mPlayState == PlayState.STATE_BUFFERING_PAUSED) {
            mIsBackContinue = false
        } else if (mPlayState == PlayState.STATE_PLAYING || mPlayState == PlayState.STATE_BUFFERING_PLAYING) {
            mIsBackContinue = true
        } else if (mPlayState == PlayState.STATE_PREPARING) {
            //播放器初始化中不做任何操作
            return
        }
        pausePlay()
    }

    override fun onResume() {
        //播放器初始化中不做任何操作
        if (mPlayState == PlayState.STATE_PREPARING || mPlayState == PlayState.STATE_IDLE) {
            return
        }
        if (!mIsBackContinue) {
            pausePlay()
            Log.e("pipa", "pause+play:" + mPlayState)
        } else {
            continuePlay()
        }
    }

    //====================================================================================
    // 1.创建一个手势监听回调
    private val listener: SimpleOnGestureListener = object : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            Log.d("pipa", "onDown")
            return super.onDown(e)
        }

        override fun onShowPress(e: MotionEvent) {
            Log.d("pipa", "onShowPress")
            super.onShowPress(e)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            Log.d("pipa", "onDoubleTap")
            mPlayer?.let {
                if (it.isPlaying) {
                    pausePlay()
                } else if (mPlayState == PlayState.STATE_PREPARED) {
                    startPlay()
                } else if (!it.isPlaying) {
                    continuePlay()
                }
            }
            return super.onDoubleTap(e)
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            Log.d("pipa", "onSingleTapConfirmed")
            mIsShowControllerView = !mIsShowControllerView
            showOrHideControlDelay(isShow = mIsShowControllerView, isClick = true)
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            if (mIsFirstDown) {
                mAdjustWay = getAdjustMode(e1)
                mIsFirstDown = false
            }
            val distX = e2.x - e1.x
            val distY = e2.y - e1.y
            when (mAdjustWay) {
                PlayAdjust.ADJUST_VOLUME -> {
                    //音量调节，从下往上为加，所以需要加上负号
                    if (!mVolumeMute) {
                        setVolume(mStartVolume, -distY)
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
    private val detector = GestureDetector(mContext, listener)
    //====================================================================================

    override fun slideJudge(view: View, event: MotionEvent) {
        detector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (mAdjustWay != 0) {
                    endAdjust()
                    mView.hideAdjustUi()
                }
            }
        }
    }

    private fun getAdjustMode(event: MotionEvent): Int {
        //调整前获取调整的模式
        Log.d("pipa", "getAdjustMode")
        val width = (mView as LinearLayout).width
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

    override fun setPlayForm(playForm: Int) {
        Log.d("pipa", "setPlayForm:$playForm")
        mPlayForm = playForm
    }

    override fun setVolumeMute(isMute: Boolean) {
        if (isMute) {
            mAudioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
        } else {
            mAudioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, mVolume, 0)
        }
        mVolumeMute = isMute
    }

    private fun endAdjust() {
        //调整结束后保存结果
        Log.d("pipa", "endAdjust")
        mIsFirstDown = true
        when (mAdjustWay) {
            PlayAdjust.ADJUST_LIGHT -> {
                mStartLight = mLight
            }
            PlayAdjust.ADJUST_VOLUME -> {
                mStartVolume = mVolume
            }
            PlayAdjust.ADJUST_VIDEO -> {
                mStartPosition = mPosition
                seekCompletePlay(mStartPosition)
            }
        }
        mAdjustWay = 0
    }

    override fun setSpecialMode(playMode: Int) {
        Log.d("pipa", "playMode" + playMode)
        if (mPlayMode == PlayMode.MODE_NORMAL) {
            //进入全屏模式
            mPlayMode = PlayMode.MODE_FULL_SCREEN
            // 隐藏ActionBar、状态栏，并横屏
            (mContext as AppCompatActivity).supportActionBar?.hide()
            mContext.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            mContext.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            //设置为充满父布局
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            //隐藏虚拟按键，并且全屏
            mContext.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN
            (mView as LinearLayout).layoutParams = params
            //全屏直接使用手机大小,此时未翻转的话，高宽对调
            val phoneWidth = JVideoUtil.getPhoneDisplayWidth(mContext)
            val phoneHeight = JVideoUtil.getPhoneDisplayHeight(mContext)
            changeVideoSize(
                if (phoneHeight > phoneWidth) phoneHeight else phoneWidth,
                if (phoneHeight > phoneWidth) phoneWidth else phoneHeight
            )
        }
        mView.entrySpecialMode(mPlayMode)
    }

    override fun exitMode(isBackNormal: Boolean) {
        Log.d("pipa", "exitMode")
        if (getPlayMode() != PlayMode.MODE_NORMAL) {
            //进入普通模式
            mParams?.let {
                mPlayMode = PlayMode.MODE_NORMAL
                (mContext as AppCompatActivity).supportActionBar?.show()
                mContext.window.clearFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
                mContext.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                mContext.window.decorView.systemUiVisibility = View.VISIBLE
                (mView as LinearLayout).layoutParams = it
                changeVideoSize(it.width, it.height)
            }
        }
        mView.exitMode()
    }

    private fun showOrHideControlDelay(isShow: Boolean, isClick: Boolean) {
        Log.d("pipa", "showOrHideControlDelay:show:$isShow, mIsShowControllerView:$mIsShowControllerView")
        if (isShow) {
            mView.hideOrShowController(true)
            mIsShowControllerView = true
            if(mPlayState == PlayState.STATE_PLAYING){
                mHandler.postDelayed(mHideRunnable, 5000)
            }
        } else {
            if(isClick){
                mHandler.removeCallbacks(mHideRunnable)
                mView.hideOrShowController(false)
                mIsShowControllerView = false
            }
        }
    }

    private fun runVideoTime() {
        //开始计时
        mRunnable = mRunnable ?: Runnable {
            mPlayer?.let {
                if (it.isPlaying) {
                    //更新播放进度
                    mView.playing(getVideoTimeStr(it.currentPosition.toInt()), it.currentPosition.toInt())
                }
            }
            //重复调起自身
            mHandler.postDelayed(mRunnable, 200)
        }
        mHandler.post(mRunnable)

        if(mIsShowControllerView){
            mHandler.postDelayed(mHideRunnable, 5000)
        }
    }

    private fun stopVideoTime() {
        //停止计时
        mHandler.removeCallbacks(mRunnable)

        if(mIsShowControllerView) {
            mHandler.removeCallbacks(mHideRunnable)
        }
    }


    override fun textureReady(surface: SurfaceTexture, textureView: TextureView) {
        Log.d("pipa", "textureReady")
        mSurface = mSurface ?: Surface(surface)
        mTextureView = mTextureView ?: textureView
        mPlayState = PlayState.STATE_PREPARING
        loadVideosData()
    }

    //获取数据源
    override fun loadVideosData() {
        Log.d("pipa", "loadVideosData")

        mVideoRepository.getVideos(object : VideoDataSource.LoadVideosCallback {
            override fun onVideosLoaded(videos: List<Video>) {
                mVideoList.addAll(videos)
                if (mVideoList.isEmpty()) {
                    mView.showMessagePrompt("数据源获取为空~")
                    return
                }
                entryVideoLoop()
            }

            override fun onDataNotAvailable() {
                mView.showMessagePrompt("数据源获取失败~")
            }
        })
    }

    override fun entryVideoLoop() {
        if (mVideoIndex >= mVideoList.size) {
            mView.showMessagePrompt("所有视频已播放结束~")
            mPlayer?.stop()
            return
        }
        //播放后如果有顺序播放，则重新开始播放列表
        mSurface?.let {
            Log.d("pipa", "mVideoIndex:$mVideoIndex")

            entryVideo(it, mVideoList[mVideoIndex])
        }
    }

    override fun getPlayState(): Int {
        return mPlayState
    }

    override fun getPlayMode(): Int {
        return mPlayMode
    }

    override fun getLight(isMax: Boolean): Int {
        var nowBrightnessValue = 0
        try {
            nowBrightnessValue = Settings.System.getInt(mContext.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
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

    override fun getDuration(): Int {
        return mPlayer?.duration?.toInt() ?: 0
    }

    override fun getPosition(): Int {
        return mPlayer?.currentPosition?.toInt() ?: 0
    }

    override fun getBufferPercent(): Int {
        return mBufferPercent
    }

    override fun releasePlay(destroyUi: Boolean) {
        mSurface?.release()
        mHandler.removeCallbacks(mRunnable)
        mPlayer?.stop()
        mPlayer?.release()//调用release()方法来释放资源，资源可能包括硬件加速组件的单态固件
        mSurface = null
        mPlayer = null
    }


    /**
     * 滑动屏幕快进或者后退
     * @param distance
     */
    private fun slidePlay(startProgress: Int, distance: Float) {
        var position =
            startProgress + floor(dt2progress(distance, getDuration(), (mView as LinearLayout).width, 0.2)).toInt()
        when {
            position in 0..getDuration() -> {

            }
            position < 0 -> position = 0
            else -> position = getDuration()
        }
        mPosition = position
        mView.seekingVideo(getVideoTimeStr(position), position, true)
    }

    private fun setLight(startLight: Int, distance: Float) {
        Log.d("pipa", "startLight:$startLight, distance$distance")
        var light = startLight + floor(dt2progress(distance, 255, (mView as LinearLayout).height, 1.0)).toInt()
        when {
            light in 0..255 -> {
            }
            light <= 0 -> light = 0
            else -> light = 255
        }
        val params = (mContext as AppCompatActivity).window.attributes
        params.screenBrightness = light / 255f
        mContext.window.attributes = params
        //保存亮度
        mLight = light
        mView.setLightUi(floor(light / 255f * 100).toInt())
    }

    private fun setVolume(startVolume: Int, distance: Float) {
        var volume =
            startVolume + floor(dt2progress(distance, getVolume(true), (mView as LinearLayout).height, 1.0)).toInt()
        when {
            volume in 0..getVolume(true) -> {

            }
            volume <= 0 -> volume = 0
            else -> volume = getVolume(true)
        }
        mAudioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
        mVolume = volume
        mView.setVolumeUi(volume * 100 / getVolume(true))
    }

    //进入视频播放准备
    private fun entryVideo(surface: Surface, video: Video) {
        Log.d("pipa", "entryVideo:${video.videoUrl}")
        //设置title
        mView.setTitle(video.videoName ?: "未知视频")
        mPlayer?.let {
            //如果不是IDLE状态就改变播放器状态
            if (mPlayState != PlayState.STATE_IDLE) {
                resetPlay()
            }
            try {
                it.dataSource = (video.videoUrl)
                //加载url之后为播放器初始化完成状态
                mPlayState = PlayState.STATE_INITLIZED
                //设置渲染画板
                it.setSurface(surface)
                //初始化播放器监听
                initPlayerListener()
                //异步的方式装载流媒体文件
                it.prepareAsync()
            } catch (e: IOException) {
                e.printStackTrace()
                errorPlay(0, 0, "视频路径有误或者地址失效~")
            }
        }
    }

    //按比例改变视频大小适配屏幕宽高
    private fun changeVideoSize(mJVideoWidth: Int, mJVideoHeight: Int) {
        val jWidth = if (mJVideoWidth < 0) 1080 else mJVideoWidth
        mPlayer?.let {
            val videoWidth = it.videoWidth
            val videoHeight = it.videoHeight
            //根据视频尺寸去计算->视频可以在TextureView中放大的最大倍数。
            val max =
                //竖屏模式下按视频宽度计算放大倍数值
                max(videoHeight * 1.0 / mJVideoHeight, videoWidth * 1.0 / jWidth)
            //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
            val videoWidth2 = ceil(videoWidth * 1.0 / max).toInt()
            val videoHeight2 = ceil(videoHeight * 1.0 / max).toInt()
            Log.d(
                "pipa",
                "mPlayer:$videoWidth - $videoHeight， jvideo：$jWidth- $mJVideoHeight, changed:$videoWidth2-$videoHeight2"
            )
            //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
            mTextureView?.layoutParams = LinearLayout.LayoutParams(videoWidth2, videoHeight2)
        }

    }


    private fun getVideoTimeStr(position: Int?): String {
        return JVideoUtil.progress2Time(position) + "/" + JVideoUtil.progress2Time(getDuration())
    }

}