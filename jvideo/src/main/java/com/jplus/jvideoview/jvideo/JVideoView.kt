package com.jplus.jvideoview.jvideo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import com.jplus.jvideoview.R
import com.jplus.jvideoview.jvideo.JVideoState.PlayMode
import com.jplus.jvideoview.jvideo.JVideoState.PlayState
import com.jplus.jvideoview.jvideo.JVideoState.SwitchMode
import kotlinx.android.synthetic.main.layout_control_bottom.view.*
import kotlinx.android.synthetic.main.layout_control_center.view.*
import kotlinx.android.synthetic.main.layout_control_slide.view.*
import kotlinx.android.synthetic.main.layout_controller.view.*
import kotlinx.android.synthetic.main.layout_controller_top.view.*
import kotlinx.android.synthetic.main.layout_jvideo.view.*
import kotlinx.android.synthetic.main.layout_line_progress.view.*


/**
 * @author JPlus
 * @date 2019/8/30.
 */
@RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class JVideoView : LinearLayout, JVideoViewContract.Views, TextureView.SurfaceTextureListener {


    private var mPresenter: JVideoViewContract.Presenter? = null
    private var mView: View? = null
    private lateinit var mContext: Context

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
        initView()
    }

    private fun initView() {
        ly_video_title.setOnTouchListener { _, _ -> true }

    }

    private fun initListener() {
        mPresenter?.setPlayForm(JVideoState.PlayForm.PLAYFORM_TURN)
        //设置Texture监听
        ttv_video_player.surfaceTextureListener = this

        //控件的点击、拖动、滑动事件
        vpv_video_center_play.setOnClickListener {
            mPresenter?.controlPlay()
            Log.d("pipa", "imb_video_center_play, state:${mPresenter?.getPlayState()}")
        }
        vpv_video_control_play.setOnClickListener {
            mPresenter?.controlPlay()
            Log.d("pipa", "imb_video_control_play,state:${mPresenter?.getPlayState()}")
        }
        seek_video_progress?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                //seekBar滑动中的回调
                mPresenter?.seekingPlay(seekBar.progress, false)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //seekBar开始滑动的回调
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //seekBar滑动结束的回调
                mPresenter?.seekCompletePlay(seekBar.progress)
            }

        })

        //预加载完成前禁止拖动seekBar
        seek_video_progress.setOnTouchListener { _, _ -> true }
        //预加载完成前禁止点击bottom的控制按钮
        vpv_video_control_play.setOnTouchListener{_,_ -> true}
        ly_video_center.setOnTouchListener { v, event ->
            //屏幕滑动设置播放参数
            mPresenter?.slideJudge(v, event)
            true
        }
        img_screen_change.setOnClickListener {
            mPresenter?.switchSpecialMode(SwitchMode.SWITCH_TO_FULL)
        }
        tv_video_refresh.setOnClickListener {
            //重新播放
            hideCenterHintUi()
            mPresenter?.continuePlay()
        }
        img_video_volume_open.setOnClickListener {
            mPresenter?.let {
                //音量ui的显示
                if (it.getVolume(false) == 0) {
                    img_video_volume_open.setImageResource(R.mipmap.ic_video_volume_open)
                    it.setVolumeMute(false)
                } else {
                    img_video_volume_open.setImageResource(R.mipmap.ic_video_volume_close)
                    it.setVolumeMute(true)
                }
            }
        }
        imb_video_back.setOnClickListener {
            mPresenter?.exitMode(true)
        }
    }


    override fun setThumbnail(bitmap: Bitmap?) {
        rl_controller_layout.background = BitmapDrawable(null, bitmap)
    }

    override fun showNetSpeed(speed: String) {
        if (tv_video_loading.visibility == VISIBLE) {
            tv_video_loading.text = speed
        }
    }

    override fun setPresenter(t: JVideoViewContract.Presenter) {
        mPresenter = t
        initListener()
    }

    override fun setTitle(title: String) {
        tv_video_title.text = title
    }

    override fun showMessagePrompt(message: String) {
        showMessageUi(message)
    }

    override fun preparedVideo(videoTime: String, max: Int) {
        hideCenterHintUi()
        tv_video_playing_progress.text = videoTime
        //加载完成后可以拖动seekBar
        seek_video_progress.setOnTouchListener { _, _ -> false }
        //加载完成后可以点击控制按钮
        vpv_video_control_play.setOnTouchListener{_,_ -> false}
        seek_video_progress?.max = max
        seek_video_progress?.progress = 0

        pgb_video_line_progress?.max = max
        pgb_video_line_progress?.progress = 0

        showCenterPlayUi()
        vpv_video_control_play.pause()
        vpv_video_center_play.pause()
    }

    override fun startVideo(position: Int) {
        rl_controller_layout.setBackgroundResource(0)
        seek_video_progress?.progress = position
        hideCenterPlayUi()
        hideCenterHintUi()
        vpv_video_control_play.play()
        vpv_video_center_play.play()
    }


    override fun buffering(percent: Int) {
//        Log.d("pipa", "缓冲百分比:${percent / 100.0}")
        seek_video_progress.secondaryProgress = (seek_video_progress.max) * percent / 100
        if (ly_video_line.visibility == VISIBLE) {
            pgb_video_line_progress?.secondaryProgress = (seek_video_progress.max) * percent / 100
        }
//        mPresenter?.let {
//            if (it.getPlayState() == PlayState.STATE_BUFFERING_PLAYING) {
//
//            } else if (it.getPlayState() == PlayState.STATE_BUFFERING_PAUSED) {
//
//            }
//        }
    }

    override fun continueVideo() {
        hideCenterPlayUi()
        hideLoadingUi()
        vpv_video_control_play.play()
        vpv_video_center_play.play()
    }

    override fun pauseVideo() {
        showCenterPlayUi()
        hideLoadingUi()
        vpv_video_control_play.pause()
        vpv_video_center_play.pause()
    }


    override fun playing(videoTime: String, position: Int) {
        tv_video_playing_progress.text = videoTime
        seek_video_progress?.progress = position
        if (ly_video_line.visibility == VISIBLE) {
            pgb_video_line_progress?.progress = position
        }
    }


    override fun setLightUi(light: Int) {
        showTopAdjustUi("亮度：$light%")
    }

    override fun setVolumeUi(volumePercent: Int) {
        img_video_volume_open.setImageResource(R.mipmap.ic_video_volume_open)
        showTopAdjustUi("音量：$volumePercent%")
    }

    override fun seekingVideo(videoTime: String, position: Int, isSlide: Boolean) {
        tv_video_playing_progress.text = videoTime
        if (isSlide) {
            showTopAdjustUi("进度：$videoTime")
            seek_video_progress?.progress = position
            if (ly_video_line.visibility == VISIBLE) {
                pgb_video_line_progress?.progress = position
            }
        }
    }


    override fun showLoading(isShow: Boolean, text: String) {
        if (isShow) {
            showLoadingUi(text)
            hideCenterPlayUi()
            hideCenterHintUi()
        } else {
            hideLoadingUi()
        }
    }

    private fun showBottomLineUi() {
        if (ly_video_line.visibility == GONE) {
            ly_video_line.visibility = VISIBLE
        }
    }

    private fun hideBottomLineUi() {
        if (ly_video_line.visibility == VISIBLE) {
            ly_video_line.visibility = GONE
        }
    }

    private fun showLoadingUi(text: String) {
        if (ly_video_loading.visibility == GONE) {
            tv_video_loading.text = text
            ly_video_loading.visibility = VISIBLE
        }
    }

    private fun hideLoadingUi() {
        if (ly_video_loading.visibility == VISIBLE) {
            ly_video_loading.visibility = GONE
        }
    }

    private fun hideControlUi() {
        if (ly_video_bottom_controller.visibility == VISIBLE) {
            ly_video_bottom_controller.visibility = GONE
        }
        if (ly_video_title.visibility == VISIBLE) {
            ly_video_title.visibility = GONE
        }
    }

    private fun showControlUi() {
        if (ly_video_bottom_controller.visibility == GONE) {
            ly_video_bottom_controller.visibility = VISIBLE
        }
        if (ly_video_title.visibility == GONE) {
            ly_video_title.visibility = VISIBLE
        }
    }

    private fun hideCenterPlayUi() {
        if (ly_video_play.visibility == VISIBLE) {
            ly_video_play.visibility = GONE
        }
    }

    private fun showCenterPlayUi() {
        if (ly_video_play.visibility == GONE) {
            ly_video_play.visibility = VISIBLE
        }
    }

    private fun showTopAdjustUi(text: String) {
        if (ly_video_slide.visibility == GONE) {
            ly_video_slide.visibility = VISIBLE
        }
        tv_slide_top.text = text
    }

    private fun hideTopAdjustUi() {
        if (ly_video_slide.visibility == VISIBLE) {
            ly_video_slide.visibility = GONE
        }
    }

    private fun showMessageUi(text: String) {
        if (ly_video_hint.visibility == GONE) {
            ly_video_hint.visibility = VISIBLE
        }
        hideCenterPlayUi()
        hideLoadingUi()
        tv_video_refresh.text = "重新播放"
        tv_video_hint1.text = text
    }

    private fun hideCenterHintUi() {
        if (ly_video_hint.visibility == VISIBLE) {
            ly_video_hint.visibility = GONE
        }
    }

    override fun hideAdjustUi() {
        hideTopAdjustUi()
    }

    override fun entryFullMode() {
        img_screen_change.setImageResource(R.mipmap.ic_video_shrink)
        //显示返回键
        if (imb_video_back.visibility == GONE) {
            imb_video_back.visibility = VISIBLE
        }
    }

    override fun exitMode() {
        img_screen_change.setImageResource(R.mipmap.ic_video_arrawsalt)
        //隐藏返回键
        if (imb_video_back.visibility == VISIBLE) {
            imb_video_back.visibility = GONE
        }

    }

    override fun hideOrShowController(isShow: Boolean) {
        if (isShow) {
            showControlUi()
            hideBottomLineUi()
        } else {
            hideControlUi()
            showBottomLineUi()
        }
    }

    override fun hideOrShowCenterPlay(isShow: Boolean) {
        if (isShow) {
            showCenterPlayUi()
        } else {
            hideCenterPlayUi()
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        //这里是改变后的画布大小
        Log.d("pipa", "onSurfaceTextureSizeChanged:$width - $height")
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
//        Log.d("pipa", "onSurfaceTextureUpdated")
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        Log.d("pipa", "onSurfaceTextureDestroyed")
        mPresenter?.releasePlay(false)
//        ldv_video_loading.close()
        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        //这里是原始画布大小
        Log.d("pipa", "onSurfaceTextureAvailable:$width - $height")
        mPresenter?.textureReady(surface, ttv_video_player)
    }
}