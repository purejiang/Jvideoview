package com.jplus.jvideoview

import android.app.Activity
import android.content.res.Configuration
import com.jplus.jvideoview.jvideo.JvPresenter
import com.jplus.jvideoview.jvideo.JvView
import com.jplus.jvideoview.common.JvConstant.*
import tv.danmaku.ijk.media.player.AndroidMediaPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

/**
 * @author JPlus
 * @date 2019/10/24.
 */
class JvController(activity: Activity, jvView: JvView, callback:JvCallBack) {
    private var mVideos = mutableListOf<Pair<String,String>>()
    private var presenter: JvPresenter? = null
    private var mPlayForm = PlayForm.PLAYBACK_ONE_END
    private var mPlayBackEngine = PlayBackEngine.PLAYBACK_MEDIA_PLAYER

    init {
        presenter = JvPresenter(activity, jvView, jvView.layoutParams, callback, getPlayEngine(mPlayBackEngine))
        presenter?.subscribe()
    }

    private fun getPlayEngine(playEngine:Int):IMediaPlayer{
       return  when (playEngine) {
            //使用ijkplayer播放引擎
            PlayBackEngine.PLAYBACK_IJK_PLAYER -> IjkMediaPlayer()
            //使用android自带的播放引擎
            PlayBackEngine.PLAYBACK_MEDIA_PLAYER -> AndroidMediaPlayer()
            //使用exoplayer引擎
//            PlayBackEngine.PLAYBACK_EXO_PLAYER ->Exo
            else -> AndroidMediaPlayer()
        }
    }

    //设置播放引擎
    fun setPlayBackEngine(playBackEngine: Int) {
        mPlayBackEngine = playBackEngine
        presenter?.switchPlayEngine(mPlayBackEngine)
    }

    //设置播放循环模式
    fun setPlayForm(playForm: Int) {
        mPlayForm = playForm
    }

    //设置播放列表
    fun playVideos(videos: MutableList<Pair<String, String>>) {
        mVideos =videos
        startPlayLoop(0)
    }

    //顺序播放
    fun startPlayLoop(position:Int) {
        presenter?.startVideo(mVideos[position].first, mVideos[position].second, object : JvPresenter.VideoPlayCallBack {
            override fun videoCompleted() {

                startPlayLoop(position+1)
            }
        })
    }


    fun onPause() {
        presenter?.onPause()
    }

    fun onResume() {
        presenter?.onResume()
    }

    fun onConfigChanged(newConfig: Configuration) {
        presenter?.onConfigChanged(newConfig)
    }

    fun destroy(){
        presenter?.unSubscribe()
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
         * 播放结束
         */
        fun endPlay()
    }
//    companion object {
//        private var INSTANCE: JvController? = null
//
//        fun getInstance(activity: Activity, jvView: JvView): JvController {
//            return INSTANCE ?: JvController(activity,jvView)
//                .apply { INSTANCE = this }
//        }
//        fun destroyInstance() {
//            INSTANCE = null
//        }
//    }
}