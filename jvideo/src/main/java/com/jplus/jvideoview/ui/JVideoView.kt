package com.jplus.jvideoview.ui

import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import com.jplus.jvideoview.R
import com.jplus.jvideoview.contract.JVideoViewContract
import com.jplus.jvideoview.model.JVideoState
import com.jplus.jvideoview.model.JVideoState.PlayState
import com.jplus.jvideoview.utils.JVideoUtil
import kotlinx.android.synthetic.main.layout_controller.view.*
import kotlinx.android.synthetic.main.layout_jvideo.view.*
import kotlin.math.abs


/**
 * @author JPlus
 * @date 2019/8/30.
 */
@RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class JVideoView : LinearLayout, JVideoViewContract.View, TextureView.SurfaceTextureListener {


    private var mPresenter: JVideoViewContract.Presenter? = null
    private var mIsShowControllerView = false
    private var mView: View? = null
    private var mContext: Context? = null
    private var mAdjustWay = JVideoState.PlayAdjust.ADJUST_VIDEO
    private var mDownX: Float = 0.0f
    private var mDownY: Float = 0.0f

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    private fun initView(context: Context) {
        mContext = context
        mView = LayoutInflater.from(context).inflate(R.layout.layout_jvideo, this)
    }

    private fun initListener() {
        ttv_video_player.surfaceTextureListener = this
        mPresenter?.run {

            img_video_back.setOnClickListener {
                Log.d("pipa", "img_video_back.setOnClickListener")
                mPresenter?.exitMode(true)
            }

            imb_video_play.setOnClickListener {
                Log.d("pipa", "state:${getPlayState()}")
                if (getPlayState() == PlayState.STATE_PLAYING) {
                    pausePlay()
                } else {
                    if (getPlayState() == PlayState.STATE_PAUSED) {
                        restart()
                    } else if (getPlayState() == PlayState.STATE_PREPARED) {
                        startPlay()
                    }
                }
            }
            seek_video_progress?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
//                    mPresenter?.pausePlay()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    mPresenter?.seekToPlay(seekBar.progress)
                    hideForwardOrBack()
                }

            })
        }
        ly_video_center.setOnTouchListener { v, event ->
            //坐标
            val ex = event.x
            val ey = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mDownX = ex
                    mDownY = ey
                    val width = JVideoUtil.getPhoneDisplayWidth(mContext!!)
                    mAdjustWay = when {
                        ex >= 0.8 * width -> {
                            JVideoState.PlayAdjust.ADJUST_VOLUME
                        }
                        ex <= 0.2 * width -> {
                            JVideoState.PlayAdjust.ADJUST_LIGHT
                        }
                        else -> JVideoState.PlayAdjust.ADJUST_VIDEO
                    }
                }
                MotionEvent.ACTION_MOVE -> {
//                    Log.d("pipa", "action_move:X=" + event.x + ",y:" + event.y)
                    if (abs(event.x - mDownX) < 5 && abs(event.y - mDownY) < 5) {

                    } else {
                        val deltaX = ex - mDownX
                        val deltaY = ey - mDownY
                        when (mAdjustWay) {
                            JVideoState.PlayAdjust.ADJUST_VOLUME -> {
                                //音量调节
                                mPresenter?.setVolume(-deltaY)
                            }
                            JVideoState.PlayAdjust.ADJUST_LIGHT -> {
                                // 亮度调节
                                mPresenter?.setLight(-deltaY)
                            }
                            else -> {
                                //快进/后退
                                mPresenter?.forwardOrBackVideo(deltaX)
                            }
                        }
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    //通过起始点坐标判断滑动是 快进/后退、亮度调节、音量调节
                    if (abs(event.x - mDownX) < 5 && abs(event.y - mDownY) < 5) {
                        hideOrShowController(!mIsShowControllerView)
                    } else {
                        when (mAdjustWay) {
                            JVideoState.PlayAdjust.ADJUST_VOLUME -> {
                                //音量调节结束
                                mPresenter?.endVolume()
                            }
                            JVideoState.PlayAdjust.ADJUST_LIGHT -> {
                                // 亮度调节结束
                                mPresenter?.endLight()
                            }
                            else -> {
                                //快进/后退
                                mPresenter?.endForwardOrBack()
                            }
                        }
                    }

                }
            }
            true
        }
        img_screen_change.setOnClickListener {
            val state = mPresenter?.getPlayMode()
            if (state == JVideoState.PlayMode.MODE_NORMAL) {
                mPresenter?.entrySpecialMode(JVideoState.PlayMode.MODE_FULL_SCREEN, this)
            } else if (state == JVideoState.PlayMode.MODE_FULL_SCREEN) {
                mPresenter?.entrySpecialMode(JVideoState.PlayMode.MODE_NORMAL, this)
            }

        }

    }


    private fun playStateChanged() {
        when (mPresenter?.getPlayState()) {
            PlayState.STATE_IDLE -> {
            }
            PlayState.STATE_PREPARING -> {

            }
            PlayState.STATE_PREPARED -> {

            }
            PlayState.STATE_PLAYING -> {

            }
            PlayState.STATE_PAUSED -> {

            }
        }
    }

    override fun hideOrShowController(isShow: Boolean) {
        mIsShowControllerView = isShow
        ly_video_title.visibility = if (isShow) GONE else VISIBLE
        ly_video_controller.visibility = if (isShow) GONE else VISIBLE
        imb_video_play.visibility = if (isShow) GONE else VISIBLE
    }

    override fun setPresenter(presenter: JVideoViewContract.Presenter) {
        mPresenter = presenter
        initListener()
    }

    override fun startPlay(position: Int) {
        imb_video_play?.setImageResource(R.drawable.ic_video_pause)
        seek_video_progress?.progress = position
    }


    override fun preparedPlay() {
        showLoading(false)
        tv_video_playing_progress.text =
            JVideoUtil.progress2Time(null) + "/" + JVideoUtil.progress2Time(mPresenter?.getDuration())
        seek_video_progress?.max = mPresenter?.getDuration() ?: 0
        imb_video_play.setImageResource(R.drawable.ic_video_play)
    }

    override fun buffering(percent: Int) {
        seek_video_progress.secondaryProgress = if (percent == 100) {
            0
        } else {
            (seek_video_progress.max) * percent / 100
        }
        mPresenter?.let {
            if (it.getPlayState() == PlayState.STATE_BUFFERING_PLAYING) {

            } else if (it.getPlayState() == PlayState.STATE_BUFFERING_PAUSED) {

            }
        }
    }

    override fun showLoading(isShow: Boolean) {
        Log.d("pipa", "loading...")
        if (isShow) {
            imb_video_play.visibility = GONE
            pgb_video_loading.visibility = VISIBLE
        } else {
            imb_video_play.visibility = VISIBLE
            pgb_video_loading.visibility = GONE
        }
    }

    override fun restart() {
        imb_video_play?.setImageResource(R.drawable.ic_video_pause)
    }

    override fun seekToPlay(position: Int) {
        tv_video_playing_progress.text =
            JVideoUtil.progress2Time(position) + "/" + JVideoUtil.progress2Time(mPresenter?.getDuration())
        seek_video_progress?.progress = position
        if (tv_progress_center_top.visibility == GONE) {
            tv_progress_center_top.visibility = VISIBLE
        }
        mPresenter?.let {
            tv_progress_center_top.text =
                "进度：" + JVideoUtil.progress2Time(position) + "/" + JVideoUtil.progress2Time(mPresenter?.getDuration())
        }
    }

    override fun playing(position: Int?) {
        tv_video_playing_progress.text =
            JVideoUtil.progress2Time(position) + "/" + JVideoUtil.progress2Time(mPresenter?.getDuration())
        seek_video_progress?.progress = position ?: 0
    }

    override fun pausePlay() {
        imb_video_play?.setImageResource(R.drawable.ic_video_play)
    }

    override fun completedPlay() {

    }

    override fun setLight(light: Int) {
        if (tv_progress_center_top.visibility == GONE) {
            tv_progress_center_top.visibility = VISIBLE
        }
        mPresenter?.let {
            tv_progress_center_top.text = "亮度：$light%"
        }
    }

    override fun setVolume(volumePercent: Int) {
        if (tv_progress_center_top.visibility == GONE) {
            tv_progress_center_top.visibility = VISIBLE
        }
        mPresenter?.let {
            tv_progress_center_top.text = "音量：$volumePercent%"
        }
    }

    override fun hideLight() {
        if (tv_progress_center_top.visibility == VISIBLE) {
            tv_progress_center_top.visibility = GONE
        }
    }

    override fun hideVolume() {
        if (tv_progress_center_top.visibility == VISIBLE) {
            tv_progress_center_top.visibility = GONE
        }
    }

    override fun hideForwardOrBack() {
        if (tv_progress_center_top.visibility == VISIBLE) {
            tv_progress_center_top.visibility = GONE
        }
    }

    override fun entrySpecialMode(mode: Int) {

    }

    override fun errorPlay() {

    }

    override fun exitMode() {

    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        Log.d("pipa", "onSurfaceTextureSizeChanged")
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
//        Log.d("pipa", "onSurfaceTextureUpdated")
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        Log.d("pipa", "onSurfaceTextureDestroyed")
        mPresenter?.releasePlay(false)
        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        Log.d("pipa", "onSurfaceTextureAvailable")
        mPresenter?.openMediaPlayer(surface, width, height)
    }
}