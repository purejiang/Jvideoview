package com.jplus.jvideoview.persenter

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.media.*
import android.os.Build
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import com.jplus.jvideoview.contract.JVideoViewContract
import com.jplus.jvideoview.model.JVideoState.*
import com.jplus.jvideoview.utils.JVideoUtil
import com.jplus.jvideoview.utils.JVideoUtil.Companion.dt2progress
import kotlin.math.abs
import kotlin.math.floor
import android.view.MotionEvent
import android.widget.Toast


/**
 * @author Administrator
 * @date 2019/8/30.
 */
class JVideoViewPresenter(
    private val mContext: Context,
    private val mView: JVideoViewContract.Views,
    /**
     * url,title
     */
    private val mUrlMap: Map<String, String>
) :
    JVideoViewContract.Presenter {
    companion object{
        //双击判断
        private const val DOUBLE_TAP_TIMEOUT = 200
    }

    private var mPlayState = PlayState.STATE_IDLE
    private var mPlayMode = PlayMode.MODE_NORMAL
    private var mAdjustWay = PlayAdjust.ADJUST_VIDEO

    private var mPlayer: MediaPlayer? = null
    private var mSurface: Surface? = null
    private var mRunnable: MyRunnable? = null
    private var mParams: LinearLayout.LayoutParams? = null
    private var mAudioManager: AudioManager? = null

    private var mVolumeStart = 0
    private var mVideoStart = 0
    private var mLightStart = 0.0

    private var mVolume = 0
    private var mProgress = 0
    private var mLight = 0.0

    private var mDownX: Float = 0.0f
    private var mDownY: Float = 0.0f
    private var mBufferPercent = 0

    private val mHandler = Handler()
    private var mIsShowControllerView = false


    private var mCurrentDownEvent: MotionEvent? = null
    private var mPreviousUpEvent: MotionEvent? = null

    private fun initMediaPlayer() {
        mPlayer = mPlayer ?: MediaPlayer()
        mView.setPresenter(this)
        Log.d("pipa", "initMediaPlayer:$mPlayer")
        initVolume()
    }

    private fun initVolume() {
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
    }

    override fun subscribe() {
        initMediaPlayer()
    }

    override fun unSubscribe() {

    }

    override fun startPlay(position: Int) {
        Log.d("pipa", "startPlay")
        mPlayer?.let {
            if (it.isPlaying) {
                //如果在播放中，指定视频播放位置
                it.seekTo(position)
            } else {
                //如果不在播放中，指定视频播放位置并开始播放
                it.seekTo(position)
            }
            it.start()
        }
        mView.startVideo(position)
        runVideoTime()
        hideControlDelay(5000)
    }

    override fun seekToPlay(position: Int) {
        mPlayer?.let {
            it.seekTo(position)
            if (mPlayState == PlayState.STATE_PAUSED) {
                pausePlay()
            } else if (mPlayState == PlayState.STATE_PLAYING) {
                it.start()
            }
        }
    }

    override fun pausePlay() {
        Log.d("pipa", "pausePlay")
        if (mPlayState == PlayState.STATE_PLAYING) {
            mPlayState = PlayState.STATE_PAUSED
        } else if (mPlayState == PlayState.STATE_BUFFERING_PLAYING) {
            mPlayState = PlayState.STATE_BUFFERING_PAUSED
        }
        mPlayer?.pause()
        mView.pauseVideo()
        stopVideoTime()
    }


    override fun seekBarPlay(position: Int) {
        mView.seekToVideo(getVideoTimeStr(position), position)
    }

    override fun slideJudge(view: View, event: MotionEvent) {
        val ex = event.x
        val ey = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = ex
                mDownY = ey
                val width = JVideoUtil.getPhoneDisplayWidth(mContext)
                if (mPreviousUpEvent != null
                    && mCurrentDownEvent != null
                    && isConsideredDoubleTap(mPreviousUpEvent!!, event)) {
                    mAdjustWay  = 0
                    Toast.makeText(mContext, "doubleclick", Toast.LENGTH_SHORT).show()
                }else {
                    mAdjustWay = when {
                        //通过起始点坐标判断滑动是 快进/后退、亮度调节、音量调节
                        ex >= 0.8 * width -> {
                            mVolumeStart = getVolume(false)
                            PlayAdjust.ADJUST_VOLUME
                        }
                        ex <= 0.2 * width -> {
                            mLightStart = getLight(false)
                            PlayAdjust.ADJUST_LIGHT
                        }
                        else -> {
                            mVideoStart = getPosition()
                            PlayAdjust.ADJUST_VIDEO
                        }
                    }
                }
                mCurrentDownEvent = MotionEvent.obtain(event)
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(event.x - mDownX) < 5 && abs(event.y - mDownY) < 5) {

                } else {
                    val deltaX = ex - mDownX
                    val deltaY = ey - mDownY
                    when (mAdjustWay) {
                        PlayAdjust.ADJUST_VOLUME -> {
                            //音量调节，从下往上为加，所以需要加上负号
                            setVolume(mVolumeStart, -deltaY)
                        }
                        PlayAdjust.ADJUST_LIGHT -> {
                            // 亮度调节
                            setLight(mLightStart, -deltaY)
                        }
                        PlayAdjust.ADJUST_VIDEO-> {
                            //快进/后退
                            slidePlay(mVideoStart, deltaX)
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mPreviousUpEvent = MotionEvent.obtain(event)
                //通过判断是点击、双击还是滑动
                if (abs(event.x - mDownX) < 5 && abs(event.y - mDownY) < 5) {
                    mIsShowControllerView = !mIsShowControllerView
                    mView.hideOrShowController(mIsShowControllerView)
                } else {
                    saveAdjust(mAdjustWay)
                    mView.hideAdjustUi()
                }

            }
        }
    }
    private fun isConsideredDoubleTap(
        firstUp: MotionEvent, secondDown: MotionEvent
    ): Boolean {
        if (secondDown.eventTime - firstUp.eventTime > DOUBLE_TAP_TIMEOUT) {
            return false
        }
        val deltaX = firstUp.x.toInt() - secondDown.x.toInt()
        val deltaY = firstUp.y.toInt() - secondDown.y.toInt()
        return deltaX * deltaX + deltaY * deltaY < 10000
    }
    private fun saveAdjust(adjustMode: Int) {
        when (adjustMode) {
            PlayAdjust.ADJUST_LIGHT -> {

            }
            PlayAdjust.ADJUST_VOLUME -> {

            }
            PlayAdjust.ADJUST_VIDEO -> {
                seekToPlay(mProgress)
            }
        }
    }

    override fun entrySpecialMode(view: LinearLayout) {
        if (getPlayMode() == PlayMode.MODE_FULL_SCREEN) {
            //进入普通模式
            mParams?.let {
                mPlayMode = PlayMode.MODE_NORMAL
                (mContext as AppCompatActivity).supportActionBar?.show()
                mContext.window.clearFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
                mContext.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                mContext.window.decorView.systemUiVisibility = View.VISIBLE
                view.layoutParams = it
            }
        } else if (getPlayMode() == PlayMode.MODE_NORMAL) {
            //进入全屏模式
            mPlayMode = PlayMode.MODE_FULL_SCREEN
            mParams = LinearLayout.LayoutParams(view.layoutParams)
            // 隐藏ActionBar、状态栏，并横屏
            (mContext as AppCompatActivity).supportActionBar?.hide()
            mContext.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            mContext.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            mContext.window.decorView.systemUiVisibility = View.INVISIBLE
            view.layoutParams = params
        }
    }

    override fun continuePlay() {
        Log.d("pipa", "restart")
        if (mPlayState == PlayState.STATE_PAUSED) {
            mPlayer?.start()
            mPlayState = PlayState.STATE_PLAYING
        } else if (mPlayState == PlayState.STATE_BUFFERING_PAUSED) {
            mPlayer?.start()
            mPlayState = PlayState.STATE_BUFFERING_PLAYING
        }
//        else if (mPlayState == PlayState.STATE_COMPLETED || mPlayState == PlayState.STATE_ERROR) {
//            mPlayer?.reset()
//            loadVideo(mSurface!!, mUrlMap.keys.toList())
//        } else {
//
//        }
        mView.continueVideo()
        hideControlDelay(5000)
    }

    private fun hideControlDelay(delayTime: Long) {
        //5秒后执行
        mHandler.postDelayed({ mView.hideOrShowController(false) }, delayTime)
    }
    private fun runVideoTime(){
        //开始计时
        if (mRunnable == null) {
            mRunnable = MyRunnable()
        }
        mHandler.post(mRunnable)
    }
    private fun stopVideoTime(){
        //停止计时
        mHandler.removeCallbacks(mRunnable)
    }
    override fun exitMode(isBackNormal: Boolean) {
        Log.d("pipa", "exitMode")
    }

    override fun openMediaPlayer(surface: SurfaceTexture, width: Int, height: Int) {
        Log.d("pipa", "openMediaPlayer")
        mSurface = mSurface ?: Surface(surface)
        mPlayState = PlayState.STATE_PREPARING
        val firstUrl = mUrlMap.keys.toList()[0]
        loadVideo(mSurface!!, firstUrl, mUrlMap[firstUrl])
    }

    override fun getPlayState(): Int {
        return mPlayState
    }

    override fun getPlayMode(): Int {
        return mPlayMode
    }

    override fun getLight(isMax: Boolean): Double {
        return (mContext as AppCompatActivity).window.attributes.screenBrightness.toDouble()
    }

    override fun getVolume(isMax: Boolean): Int {
        return if (isMax) {
            mAudioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        } else {
            mAudioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)
        } ?: 0
    }

    override fun getDuration(): Int {
        return mPlayer?.duration ?: 0
    }

    override fun getPosition(): Int {
        return mPlayer?.currentPosition ?: 0
    }

    override fun getBufferPercent(): Int {
        return mBufferPercent
    }

    override fun getNetSpeed(): Float {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        mProgress = position
        Log.d("pipa", "mProgress:$mProgress")
        mView.slidePlayVideo(getVideoTimeStr(position), position)
    }

    private fun setLight(startLight: Double, distance: Float) {
        var light = startLight + dt2progress(distance, 1, (mView as LinearLayout).height, 1.0)
        when {
            light in 0.0..1.0 -> {
            }
            light <= 0.0 -> light = 0.0
            else -> light = 1.0
        }
        val params = (mContext as AppCompatActivity).window.attributes
        params.screenBrightness = light.toFloat()
        mContext.window.attributes = params
        mLight = light
        Log.d("pipa", "light:$mLight")
        mView.setLightUi(floor(light * 100).toInt())
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
        mView.setVolumeUi(volume * 100 / getVolume(true))
        Log.d("pipa", "volume:$mVolume")
}

private fun loadVideo(surface: Surface, url: String, title: String?) {
    //设置title
    mView.setTitle(title ?: "未知视频")
    //获取第一帧图片
    mView.setThumbnail(getNetVideoBitmap(url))

    mPlayer?.run {
        setDataSource(url)
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
            Log.d("pipa", "setOnCompletionListener")
            mPlayState = PlayState.STATE_COMPLETED
            mView.completedVideo()
        }

        //播放之前的缓冲监听
//            setOnSeekCompleteListener {
//                mPlayState = PlayState.STATE_PREPARED
//            }
        //预加载监听
        setOnPreparedListener {
            Log.d("pipa", "setOnPreparedListener")
            mPlayState = PlayState.STATE_PREPARED
            mView.preparedVideo(getVideoTimeStr(null), duration)
        }
        //相当于缓存进度条
        setOnBufferingUpdateListener { mp, percent ->
            mBufferPercent = percent
            mView.buffering(percent)
        }
        setOnErrorListener { mp, what, extra ->
            Log.d("pipa", "setOnErrorListener")
            mPlayState = PlayState.STATE_ERROR
            mView.errorVideo()
            true
        }
        setOnInfoListener { mp, what, extra ->
            when (what) {
                MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    // 播放器开始渲染
                    mPlayState = PlayState.STATE_PLAYING

                }
                MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    // MediaPlayer暂时不播放，以缓冲更多的数据
                    mPlayState =
                        if (mPlayState == PlayState.STATE_PAUSED || mPlayState == PlayState.STATE_BUFFERING_PAUSED) {
                            PlayState.STATE_BUFFERING_PAUSED

                        } else {
                            PlayState.STATE_BUFFERING_PLAYING
                        }
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                    // 填充缓冲区后，MediaPlayer恢复播放/暂停
                    if (mPlayState == PlayState.STATE_BUFFERING_PLAYING) {
                        mPlayState = PlayState.STATE_PLAYING
                        mView.playing(getVideoTimeStr(mPlayer?.currentPosition), mPlayer?.currentPosition ?: 0)
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
        //设置是否保持屏幕常亮
        setScreenOnWhilePlaying(true)
        //异步的方式装载流媒体文件
        prepareAsync()
    }
}


private fun getNetVideoBitmap(videoUrl: String): Bitmap? {
    var bitmap: Bitmap? = null
    val retriever = MediaMetadataRetriever()
    try {
        //根据url获取缩略图
        retriever.setDataSource(videoUrl, HashMap())
        //获得第一帧图片
        bitmap = retriever.frameAtTime
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
    } finally {
        retriever.release()
    }
    return bitmap
}

private fun getVideoTimeStr(position: Int?): String {
    return JVideoUtil.progress2Time(position) + "/" + JVideoUtil.progress2Time(getDuration())
}

inner class MyRunnable : Runnable {
    override fun run() {
        mPlayer?.let {
            if (it.isPlaying) {
                //更新播放进度
                mView.playing(getVideoTimeStr(it.currentPosition), it.currentPosition)
            }
        }
        //重复调起自身
        mHandler.postDelayed(this, 200)
    }
}

}