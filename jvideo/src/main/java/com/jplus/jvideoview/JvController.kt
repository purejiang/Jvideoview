package com.jplus.jvideoview

import android.app.Activity
import android.content.res.Configuration
import com.jplus.jvideoview.common.JvConstant.PlayBackEngine
import com.jplus.jvideoview.common.JvConstant.PlayForm
import com.jplus.jvideoview.entity.Video
import com.jplus.jvideoview.jvideo.JvListener
import com.jplus.jvideoview.jvideo.JvPresenter
import com.jplus.jvideoview.jvideo.JvView
import tv.danmaku.ijk.media.player.AndroidMediaPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

/**
 * @author JPlus
 * @date 2019/10/24.
 */
class JvController(
    private val mActivity: Activity,
    private val mView: JvView,
    private val mCallBack: JvCallBack,
    private var mPlayEngine: Int = PlayBackEngine.PLAYBACK_MEDIA_PLAYER
) {
    private var mVideos = mutableListOf<Video>()
    private var mPresenter: JvPresenter? = null
    private var mPlayForm = PlayForm.PLAY_ONE_END
    private var mPosition = -1

    init {
        mPresenter = JvPresenter(mActivity, mView, getPlayEngine(mPlayEngine))
        mPresenter?.subscribe()
        initListener()
    }

    private fun initListener() {
        mPresenter?.setJvListener(object : JvListener {
            override fun onInitSuccess() {
                mCallBack.initSuccess()
            }

            override fun onPrepared() {
            }

            override fun onReset() {

            }

            override fun onBuffering() {

            }

            override fun onStartPlay() {

            }

            override fun onReStart() {

            }

            override fun onPlaying() {

            }

            override fun onPausePlay() {

            }

            override fun onCompleted() {
                mPosition += 1
                if (mPosition >= mVideos.size) {
                    mCallBack.endPlay()
                    return
                }
                mCallBack.toNext()
                play(mVideos[mPosition])
            }

            override fun onError() {

            }

            override fun onFullScreen() {

            }

            override fun onNormalScreen() {

            }

        })
    }

    private fun getPlayEngine(playEngine: Int): IMediaPlayer {
        return when (playEngine) {
            //使用ijkplayer播放器内核
            PlayBackEngine.PLAYBACK_IJK_PLAYER -> IjkMediaPlayer()
            //使用android自带的播放器内核
            PlayBackEngine.PLAYBACK_MEDIA_PLAYER -> AndroidMediaPlayer()
            //使用exoplayer内核
//            PlayBackEngine.PLAYBACK_EXO_PLAYER ->Exo
            else -> AndroidMediaPlayer()
        }
    }

    //切换播放器内核
    fun switchPlayEngine(playEngine: Int) {
        mPlayEngine = playEngine
        mPresenter?.switchPlayEngine(getPlayEngine(playEngine))
    }

    fun getPlayProgress(): Long? {
        return mPresenter?.getPosition()
    }

    fun supportShowSpeed() {
        mPresenter?.setIsSupShowSpeed(true)
    }

    fun supportShowSysTime(isShow: Boolean) {
        mPresenter?.setIsSupSysTime(isShow)
    }

    fun supportAutoPlay(isAuto: Boolean) {
        mPresenter?.setIsSupAutoPlay(isAuto)
    }

    fun onBackProgress(): Boolean {
        return mPresenter?.onBackProcess() ?: false
    }

    //设置播放循环模式
    fun setPlayForm(playForm: Int) {
        mPresenter?.setPlayForm(playForm)
    }

    //设置播放列表
    fun playVideos(videos: MutableList<Video>) {
        mVideos = videos
        if (videos.size > 0) {
            play(videos[0])
            mPosition = 0
        }
    }

    private fun play(video: Video) {
        mPresenter?.startVideo(video)
    }

    fun onPause() {
        mPresenter?.onPause()
    }

    fun onResume() {
        mPresenter?.onResume()
    }

    fun onConfigChanged(newConfig: Configuration) {
        mPresenter?.onConfigChanged(newConfig)
    }

    fun destroy() {
        mPresenter?.unSubscribe()
    }

    interface JvCallBack {
        /**
         * 播放器初始化完成
         */
        fun initSuccess()

        /**
         * 开始播放
         */
        fun startPlay()

        /**
         * 播放下一集
         */
        fun toNext()

        /**
         * 全部播放完成
         */
        fun endPlay()
    }
}