package com.jplus.jvideoview.persenter

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import com.jplus.jvideoview.contract.JVideoViewContract
import com.jplus.jvideoview.model.JVideoState.PlayMode
import com.jplus.jvideoview.model.JVideoState.PlayState
import com.jplus.jvideoview.utils.JVideoUtil.Companion.dt2progress
import kotlin.math.floor


/**
 * @author Administrator
 * @date 2019/8/30.
 */
class JVideoViewPresenter(
    private val mContext: Context,
    private val mView: JVideoViewContract.View,
    private val mUrlMap: Map<String, String>
) :
    JVideoViewContract.Presenter {


    private var mPlayer: MediaPlayer? = null
    private var mPlayState = PlayState.STATE_IDLE
    private var mPlayMode = PlayMode.MODE_NORMAL
    private var mSurface: Surface? = null
    private var mBufferPercent = 0
    private val mHandler = Handler()
    private var mRunnable: MyRunnable? = null
    private var mParams: LinearLayout.LayoutParams? = null
    private var mAudioManager: AudioManager? = null
    private var mVolumeProgress = 0
    private var mVideoProgress = 0
    private var mLightProgress = 0.0
    
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
        mView.startPlay(position)
        //5秒后执行
        mHandler.postDelayed({ mView.hideOrShowController(true) }, 5000)
        //开始计时
        if (mRunnable == null) {
            mRunnable = MyRunnable()
        }
        mHandler.post(mRunnable)
    }

    override fun seekToPlay(position: Int) {
        Log.d("pipa", "seekToPlay")
        mPlayer?.let {
            it.seekTo(position)
            if (mPlayState == PlayState.STATE_PAUSED) {
                pausePlay()
            } else if (mPlayState == PlayState.STATE_PLAYING) {
                it.start()
            }
        }
        mView.seekToPlay(position)
    }

    override fun pausePlay() {
        Log.d("pipa", "pausePlay")
        if (mPlayState == PlayState.STATE_PLAYING) {
            mPlayState = PlayState.STATE_PAUSED
        } else if (mPlayState == PlayState.STATE_BUFFERING_PLAYING) {
            mPlayState = PlayState.STATE_BUFFERING_PAUSED
        }
        mPlayer?.pause()
        mView.pausePlay()
        mHandler.removeCallbacks(mRunnable)
    }

    override fun setLight(distance: Float) {
        if (mLightProgress == 0.0) {
            mLightProgress = (mContext as AppCompatActivity).window.attributes.screenBrightness.toDouble()
        }
        var light = mLightProgress + dt2progress(distance, 1, (mView as LinearLayout).height, 1.0)
        when {
            light in 0.0..1.0 -> {
            }
            light < 0.0 -> light = 0.0
            else ->  light = 1.0
        }
        val params =(mContext as AppCompatActivity).window.attributes
        params.screenBrightness = light.toFloat()
        mContext.window.attributes = params
        mView.setLight(floor(light*100).toInt())
    }

    override fun setVolume(distance: Float) {
        if (mVolumeProgress == 0) {
            mVolumeProgress = getVolume(false)
        }
        val volume = mVolumeProgress + floor(dt2progress(distance, getVolume(true), (mView as LinearLayout).height, 1.0)).toInt()
        when {
            volume in 0..getVolume(true) -> {
                mAudioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
                mView.setVolume(volume * 100 / getVolume(true))
            }
            volume < 0 -> mView.setVolume(0)
            else -> mView.setVolume(100)
        }

    }

    override fun endLight() {
        mView.hideLight()
        mLightProgress = 0.0
    }

    override fun endVolume() {
        mView.hideVolume()
        mVolumeProgress = 0
    }
    /**
     * 滑动屏幕快进或者后退
     * @param distance 滑动的距离
     */
    override fun forwardOrBackVideo(distance: Float) {
        if (mVideoProgress == 0) {
            mVideoProgress = getPosition()
        }
        val progress = mVideoProgress + floor(dt2progress(distance, getDuration(), (mView as LinearLayout).width, 2.0)).toInt()
        when {
            progress in 0..getDuration() -> {
                seekToPlay(progress)
            }
            progress < 0 -> seekToPlay(0)
            else -> seekToPlay(getDuration())
        }

    }
    override fun endForwardOrBack() {
        mView.hideForwardOrBack()
        mVideoProgress = 0
    }

    override fun entrySpecialMode(mode: Int, view: LinearLayout) {
        if (mode == PlayMode.MODE_FULL_SCREEN) {
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
        } else if (mode == PlayMode.MODE_NORMAL) {
            mPlayMode = PlayMode.MODE_NORMAL
            (mContext as AppCompatActivity).supportActionBar?.show()
            mContext.window.clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            mContext.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            mContext.window.decorView.systemUiVisibility = View.VISIBLE
            view.layoutParams = mParams
        }
    }

    override fun restart() {
        Log.d("pipa", "restart")
        if (mPlayState == PlayState.STATE_PAUSED) {
            mPlayer?.start()
            mPlayState = PlayState.STATE_PLAYING
        } else if (mPlayState == PlayState.STATE_BUFFERING_PAUSED) {
            mPlayer?.start()
            mPlayState = PlayState.STATE_BUFFERING_PLAYING
        } else if (mPlayState == PlayState.STATE_COMPLETED || mPlayState == PlayState.STATE_ERROR) {
            mPlayer?.reset()
            loadVideo(mSurface!!, mUrlMap.keys.toList())
        } else {

        }
        mView.restart()
        //5秒后执行
        mHandler.postDelayed({ mView.hideOrShowController(true) }, 5000)
        //开始计时
        if (mRunnable == null) {
            mRunnable = MyRunnable()
        }
        mHandler.post(mRunnable)
    }

    override fun exitMode(isBackNormal: Boolean) {
        Log.d("pipa", "exitMode")
    }

    override fun openMediaPlayer(surface: SurfaceTexture, width: Int, height: Int) {
        Log.d("pipa", "openMediaPlayer")
        mSurface = mSurface ?: Surface(surface)
        Log.d("pipa", "mSurface:" + mSurface)
        mPlayState = PlayState.STATE_PREPARING
        mView.showLoading(true)
        loadVideo(mSurface!!, mUrlMap.keys.toList())
    }

    private fun loadVideo(surface: Surface, urls: List<String>) {
        mPlayer?.run {
            setDataSource(urls[0])
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
                mView.completedPlay()
            }

            //播放之前的缓冲监听
//            setOnSeekCompleteListener {
//                mPlayState = PlayState.STATE_PREPARED
//            }
            //预加载监听
            setOnPreparedListener {
                Log.d("pipa", "setOnPreparedListener")
                mPlayState = PlayState.STATE_PREPARED
                mView.preparedPlay()
            }
            //相当于缓存进度条
            setOnBufferingUpdateListener { mp, percent ->
                mBufferPercent = percent
                mView.buffering(percent)
            }
            setOnErrorListener { mp, what, extra ->
                Log.d("pipa", "setOnErrorListener")
                mPlayState = PlayState.STATE_ERROR
                mView.errorPlay()
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
                        mView.showLoading(true)
                    }
                    MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                        // 填充缓冲区后，MediaPlayer恢复播放/暂停
                        if (mPlayState == PlayState.STATE_BUFFERING_PLAYING) {
                            mPlayState = PlayState.STATE_PLAYING
                            mView.playing(mPlayer?.currentPosition)
                        }
                        if (mPlayState == PlayState.STATE_BUFFERING_PAUSED) {
                            mPlayState = PlayState.STATE_PAUSED
                            mView.pausePlay()
                        }
                        mView.showLoading(false)
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

    override fun getPlayState(): Int {
        return mPlayState
    }

    override fun getPlayMode(): Int {
        return mPlayMode
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



    inner class MyRunnable : Runnable {
        override fun run() {
            mPlayer?.let {
                if (it.isPlaying) {
                    //更新播放进度
                    mView.playing(it.currentPosition)
                }
            }
            //重复调起自身
            mHandler.postDelayed(this, 200)
        }
    }
}