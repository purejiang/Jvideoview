package com.jplus.jvideoview.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import com.jplus.jvideoview.R
import com.jplus.jvideoview.contract.JVideoViewContract
import com.jplus.jvideoview.model.JVideoState
import com.jplus.jvideoview.model.JVideoState.PlayState
import com.jplus.jvideoview.model.JVideoState.PlayAdjust
import com.jplus.jvideoview.utils.JVideoUtil
import kotlinx.android.synthetic.main.layout_controller.view.*
import kotlinx.android.synthetic.main.layout_jvideo.view.*


/**
 * @author JPlus
 * @date 2019/8/30.
 */
@RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class JVideoView : LinearLayout, JVideoViewContract.Views, TextureView.SurfaceTextureListener {

    private var mPresenter: JVideoViewContract.Presenter? = null
    private var mView: View? = null
    private var mContext: Context? = null


    constructor(context: Context) : super(context) {
        initControllerView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initControllerView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initControllerView(context)
    }

    private fun initControllerView(context: Context) {
        mContext = context
        mView = LayoutInflater.from(context).inflate(R.layout.layout_jvideo, this)
    }

    private fun initListener() {
        ttv_video_player.surfaceTextureListener = this
        rl_controller_bar_layout
        mPresenter?.run {
            imb_video_center_play.setOnClickListener {
                Log.d("pipa", "imb_video_center_play, state:${getPlayState()}")
                if (getPlayState() == PlayState.STATE_PLAYING) {
                    pausePlay()
                } else {
                    if (getPlayState() == PlayState.STATE_PAUSED) {
                        continuePlay()
                    } else if (getPlayState() == PlayState.STATE_PREPARED) {
                        startPlay()
                    }
                }
            }
            imb_video_control_play.setOnClickListener {
                Log.d("pipa", "imb_video_control_play,state:${getPlayState()}")
                if (getPlayState() == PlayState.STATE_PLAYING) {
                    pausePlay()
                } else {
                    if (getPlayState() == PlayState.STATE_PAUSED) {
                        continuePlay()
                    } else if (getPlayState() == PlayState.STATE_PREPARED) {
                        startPlay()
                    }
                }
            }
            seek_video_progress?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//                    Log.d("pipa","seekbar,progress:"+progress)
                    mPresenter?.seekBarPlay(seekBar.progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    Log.d("pipa","onStopTrackingTouch,progress:"+seekBar.progress)
                    mPresenter?.seekToPlay(seekBar.progress)
                }

            })
        }
        ly_video_center.setOnTouchListener { v, event ->
            //坐标
            mPresenter?.slideJudge(v, event)
            true
        }
        img_screen_change.setOnClickListener {
            mPresenter?.entrySpecialMode(this)
        }

    }

    override fun setThumbnail(bitmap: Bitmap?) {
        rl_controller_bar_layout.background = BitmapDrawable(null, bitmap)
    }


    override fun setPresenter(presenter: JVideoViewContract.Presenter) {
        mPresenter = presenter
        initListener()
    }

    override fun setTitle(title: String) {
        tv_video_title.text = title
    }

    override fun preparedVideo(videoTime:String, max:Int) {
        showLoading(false)
        hideOrShowController(true)
        tv_video_playing_progress.text = videoTime
        seek_video_progress?.max = max
        if (imb_video_center_play.visibility == GONE) {
            imb_video_center_play.visibility = VISIBLE
        }
        imb_video_control_play.setImageResource(R.drawable.ic_video_pause)
    }

    override fun startVideo(position: Int) {
        seek_video_progress?.progress = position
        rl_controller_bar_layout.setBackgroundResource(0)
        if (imb_video_center_play.visibility == VISIBLE) {
            imb_video_center_play.visibility = GONE
        }
        imb_video_control_play.setImageResource(R.drawable.ic_video_continue)
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


    override fun continueVideo() {
        if (imb_video_center_play.visibility == VISIBLE) {
            imb_video_center_play.visibility = GONE
        }
        imb_video_control_play.setImageResource(R.drawable.ic_video_continue)
    }

    override fun pauseVideo() {
        if (imb_video_center_play.visibility == GONE) {
            imb_video_center_play.visibility = VISIBLE
        }
        imb_video_control_play.setImageResource(R.drawable.ic_video_pause)
    }


    override fun playing(videoTime:String, position: Int) {
        tv_video_playing_progress.text = videoTime
        seek_video_progress?.progress = position
    }



    override fun completedVideo() {

    }

    override fun setLightUi(light: Int) {
        if (tv_progress_center_top.visibility == GONE) {
            tv_progress_center_top.visibility = VISIBLE
        }
        tv_progress_center_top.text = "亮度：$light%"
    }

    override fun setVolumeUi(volumePercent: Int) {
        if (tv_progress_center_top.visibility == GONE) {
            tv_progress_center_top.visibility = VISIBLE
        }
        tv_progress_center_top.text = "音量：$volumePercent%"
    }

    override fun seekToVideo(videoTime:String, position: Int) {
        tv_video_playing_progress.text = videoTime
    }

    override fun slidePlayVideo(videoTime: String, position: Int) {
        if (tv_progress_center_top.visibility == GONE) {
            tv_progress_center_top.visibility = VISIBLE
        }
        tv_progress_center_top.text = "进度：$videoTime"
        tv_video_playing_progress.text = videoTime
        seek_video_progress?.progress = position
    }

    override fun showLoading(isShow:Boolean){
        if (isShow) {
            if (pgb_video_loading.visibility == GONE) {
                pgb_video_loading.visibility = VISIBLE
            }
            if (imb_video_center_play.visibility == VISIBLE) {
                imb_video_center_play.visibility = GONE
            }
        } else {
            if (pgb_video_loading.visibility == VISIBLE) {
                pgb_video_loading.visibility = GONE
            }
        }
    }

    override fun hideAdjustUi() {
        if (tv_progress_center_top.visibility == VISIBLE) {
            tv_progress_center_top.visibility = GONE
        }
    }

    override fun entrySpecialMode(mode: Int) {

    }

    override fun errorVideo() {

    }

    override fun exitMode() {

    }

    override fun hideOrShowController(isShow: Boolean) {
        ly_video_title.visibility = if (isShow) VISIBLE else GONE
        ly_video_controller.visibility = if (isShow) VISIBLE else GONE
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