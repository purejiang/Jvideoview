package com.jplus.jvideoview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt


/**
 * @author JPlus
 * @date 2019/8/21.
 */
class JVideoView : LinearLayout, TextureView.SurfaceTextureListener {
    private var mSurfaceTexture: TextureView? = null
    private var mPlayer: MediaPlayer? = null
    private var mControllerLayout: LinearLayout? = null
    private var mTitleLayout: LinearLayout? = null
    private var mVideoLayout: LinearLayout? = null
    private var mPlayVideoImb: ImageButton? = null
    private var mBackImb: ImageView? = null
    private var mVideoUrl: String? = null
    private var mContext: Context? = null
    private var mVideoView: View? = null
    private var mSurface: Surface? = null
    private var mProgress: ProgressBar? = null
    private var mIsPlaying = false
    private var mSeekBar: SeekBar? = null
    private var mPlayingTime: TextView? = null
    private var mAllTime: TextView? = null
    private var mHandler = Handler()
    private var mRunnable: MyRunnable? = null
    private var mIsShowController = true
    private var mVideoProgress = 0
    private var startX = 0.0f
    private var startY = 0.0f

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    fun init(videoUrl: String) {
        mVideoUrl = videoUrl
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView(context: Context) {
        mContext = context
        mVideoView = LayoutInflater.from(context).inflate(R.layout.view_jvideo, this)
        mBackImb = mVideoView?.findViewById(R.id.img_video_back)
        mPlayVideoImb = mVideoView?.findViewById(R.id.imb_video_play)
        mProgress = mVideoView?.findViewById(R.id.pgb_video_loading)
        mSeekBar = mVideoView?.findViewById(R.id.seek_video_progress)
        mSurfaceTexture = mVideoView?.findViewById(R.id.ttv_video_player)
        mPlayingTime = mVideoView?.findViewById(R.id.tv_video_playing_progress)
        mAllTime = mVideoView?.findViewById(R.id.tv_video_all_progress)
        mTitleLayout = mVideoView?.findViewById(R.id.ly_video_title)
        mVideoLayout = mVideoView?.findViewById(R.id.ly_video_controller)
        mControllerLayout = mVideoView?.findViewById(R.id.ly_video_center)

        mProgress?.visibility = View.VISIBLE
        mSurfaceTexture?.surfaceTextureListener = this

        mPlayVideoImb?.setOnClickListener {
            if (mIsPlaying) {
                pauseVideo()
            } else {
                startVideo(false, 0)
            }
        }
        mBackImb?.setOnClickListener {
            (mContext as Activity).finish()
        }
        mSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                //拖动时改变时间
                val simpleDate = SimpleDateFormat("mm:ss", Locale.CHINA)
                simpleDate.timeZone = TimeZone.getTimeZone("GMT+00:00")
                mPlayingTime?.text = simpleDate.format(Date(seekBar.progress.toLong()))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //拖动进度条开始
                pauseVideo()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //如拖动进度条结束
                startVideo(true, seekBar.progress)
            }

        })
        //控制view的手势识别
        mControllerLayout?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //绝对坐标
                    startX = event.rawX
                    startY = event.rawY
                    Log.d("pipa", "action_down")
                }
                MotionEvent.ACTION_UP -> {
                    if (abs(event.x - startX) > 80) {
                        forwardOrBackVideo(event.x - startX)
                    }else{
                        if (mIsShowController) {
                            hideControllerBar()
                        } else {
                            showControllerBar()
                        }
                    }
                }
                MotionEvent.ACTION_MOVE ->{
                    Log.d("pipa", "action_move:X=" + event.rawX + ",y:" + event.rawY)

                }
            }
            true
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 滑动屏幕快进或者后退
     * @param distance 滑动的距离
     */
    private fun forwardOrBackVideo(distance: Float) {
        Log.d("pipa", "distance:$distance")
        pauseVideo()
        val progress = mVideoProgress + dp2progress(distance, mPlayer!!.duration, 0.3)
        when {
            progress >= mPlayer!!.duration -> startVideo(true,  mPlayer!!.duration)
            progress <= 0 -> startVideo(true, 0)
            else -> startVideo(true,  progress)
        }
    }


//    /**
//     * 滑动屏幕后退
//     * @param distance 滑动的距离
//     */
//    private fun backVideo(distance: Float) {
//        pauseVideo()
//        startVideo(true, mVideoProgres - dp2progress(distance, mPlayer!!.duration, 0.8))
//    }

    /**
     * 将屏幕宽度转为进度显示
     * @param distance 滑动的距离
     * @param duration 总进度
     * @param proportion 一次屏幕的滑动所占总进度的比例
     */
    private fun dp2progress(distance: Float, duration: Int, proportion: Double): Int {
        return (distance * duration * proportion / getPhoneDisplay().defaultDisplay.width).roundToInt()
    }


    private fun getPhoneDisplay(): WindowManager {
        return mContext?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    /**
     * 暂停播放
     */
    private fun pauseVideo() {
        mIsPlaying = false
        mPlayVideoImb?.setImageResource(R.drawable.ic_video_play)
        //暂停播放
        mPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
            mVideoProgress = it.currentPosition
        }
        mHandler.removeCallbacks(mRunnable)
        showControllerBar()
    }

    /**
     * 播放视频
     * @param isSeek 是否是拖动的进度
     * @param progress 指定进度
     */
    private fun startVideo(isSeek: Boolean, progress: Int) {
        mIsPlaying = true
        Log.d("pipa", "startVideo:$progress")
        mPlayVideoImb?.setImageResource(R.drawable.ic_video_pause)
        //开始播放
        mPlayer?.let {
            if (isSeek) {
                if (it.isPlaying) {
                    //如果在播放中，指定视频播放位置
                    it.seekTo(progress)
                } else {
                    //如果不在播放中，指定视频播放位置并开始播放
                    it.seekTo(progress)
                }
            }
            it.start()
        }
        //开始计时
        if (mRunnable == null) {
            mRunnable = MyRunnable()
        }
        mHandler.post(mRunnable)

        //两秒后自动隐藏控制栏
        mHandler.postDelayed({
            hideControllerBar()
        }, 5000)

    }

    private fun showControllerBar() {
        mIsShowController = true
        mTitleLayout?.visibility = VISIBLE
        mVideoLayout?.visibility = VISIBLE
        mPlayVideoImb?.visibility = VISIBLE
    }

    private fun hideControllerBar() {
        mIsShowController = false
        mTitleLayout?.visibility = GONE
        mVideoLayout?.visibility = GONE
        mPlayVideoImb?.visibility = GONE
    }

    /**
     * 释放资源
     */
    private fun jVideoToDestroy() {
        mHandler.removeCallbacks(mRunnable)
        mSurface = null
        mPlayer?.stop()
        mPlayer?.release()//调用release()方法来释放资源，资源可能包括硬件加速组件的单态固件
        mPlayer = null
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        Log.d("pipa", "onSurfaceTextureAvailable")
        mSurface = Surface(surface)
        if (mPlayer == null) {
            mPlayer = MediaPlayer()
        }
        initPlaySetting(mVideoUrl, mSurface)
    }

    /**
     * 初始化MediaPlayer播放器
     */
    private fun initPlaySetting(videoUrl: String?, surface: Surface?) {
        //设置播放资源(可以是应用的资源文件／url／sdcard路径)
        mPlayer?.run {
            setDataSource(videoUrl)
            //设置渲染画板
            setSurface(surface)
            //设置播放类型
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val attributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                        .build()
                setAudioAttributes(attributes)
            } else {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
            }
            //播放完成监听
            setOnCompletionListener {

            }

            //播放之前的缓冲监听
            setOnSeekCompleteListener {
                Log.d("pipa", "缓冲完成")

            }
            //预加载监听
            setOnPreparedListener {
                Log.d("pipa", "mPlayer.start")
                //加载完成
                mProgress?.visibility = View.GONE
                mPlayVideoImb?.visibility = View.VISIBLE
                mPlayVideoImb?.setImageResource(R.drawable.ic_video_play)
                //设置总进度
                mSeekBar?.max = it.duration
                val simpleDate = SimpleDateFormat("mm:ss", Locale.CHINA)
                simpleDate.timeZone = TimeZone.getTimeZone("GMT+00:00")
                mAllTime?.text = simpleDate.format(Date(it.duration.toLong()))
            }
            //相当于缓存进度条
            setOnBufferingUpdateListener { mp, percent ->
                mSeekBar?.secondaryProgress = if(percent==100){
                    0
                }else{
                    (mSeekBar?.max?:0) * percent/100
                }
                Log.d("pipa", "缓存:$percent")
            }
            //设置是否保持屏幕常亮
            setScreenOnWhilePlaying(true)
            //异步的方式装载流媒体文件
            prepareAsync()
        }

    }

    inner class MyRunnable : Runnable {
        override fun run() {
            mPlayer?.let {
                if (it.isPlaying) {
                    //更新播放进度
                    mSeekBar?.progress = it.currentPosition
                    val simpleDate = SimpleDateFormat("mm:ss", Locale.CHINA)
                    simpleDate.timeZone = TimeZone.getTimeZone("GMT+00:00")
                    mPlayingTime?.text = simpleDate.format(Date(it.currentPosition.toLong()))
                }
            }
            //重复调起自身
            mHandler.postDelayed(this, 200)
        }
    }

}