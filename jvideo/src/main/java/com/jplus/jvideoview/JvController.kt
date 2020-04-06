package com.jplus.jvideoview

import android.app.Activity
import android.content.res.Configuration
import com.jplus.jvideoview.data.Video
import com.jplus.jvideoview.jvideo.*

/**
 * @author JPlus
 * @date 2019/10/24.
 */
class JvController(private val activity: Activity, private val jvView: JvView) {
    private var mVideos = mutableListOf<Video>()
    private var presenter: JvPresenter? = null
    private var mPlayForm = PlayForm.PLAYBACK_ONE_END
    private var mPlayBackEngine = PlayBackEngine.PLAYBACK_MEDIA_PLAYER

    init{
        presenter = JvPresenter(activity, jvView, jvView.layoutParams, )
    }

    //设置播放引擎
    fun setPlayBackEngine(playBackEngine: Int){
        mPlayBackEngine = playBackEngine
        presenter?.switchPlaybackEngine(mPlayBackEngine)
    }

    //设置播放循环
    fun setPlayForm(playForm:Int){
        mPlayForm = playForm
    }
    //设置播放队列
    fun playVideos(videos:MutableList<Video>){
        mVideos = videos
    }

    //播放队列
    fun playLoop(){
        presenter?.startVideo()
    }


    fun setCallBack(){

    }
    fun onPause(){
        presenter?.onPause()
    }
    fun onResume(){
        presenter?.onResume()
    }
    fun onConfigChanged(newConfig: Configuration){
        presenter?.onConfigChanged(newConfig)
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