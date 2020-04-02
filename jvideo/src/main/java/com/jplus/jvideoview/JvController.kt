package com.jplus.jvideoview

import android.app.Activity
import android.util.Log
import com.jplus.jvideoview.data.Video
import com.jplus.jvideoview.jvideo.JvCommon
import com.jplus.jvideoview.jvideo.JvPresenter
import com.jplus.jvideoview.jvideo.JvView

/**
 * @author JPlus
 * @date 2019/10/24.
 */
class JvController(private val activity: Activity, private val jvView: JvView) {
    private val mVideos = mutableListOf<Video>()
    private var presenter: JvPresenter? = null





//    private fun loadVideos(videos: Collection<Video>){
//
//    }
//
//
//    private fun startVideoLoop() {
//        //播放后如果有顺序播放，则重新开始播放列表
//
//        mSurface?.let {
//            Log.d(JvCommon.TAG, "mVideoIndex:$mVideoIndex")
//            mView.buffering(0) //清空前一集的缓存进度条
//            entryVideo(it, mVideoList[mVideoIndex])
//        }
//    }
//
//
//
//


    companion object {
        private var INSTANCE: JvController? = null

        fun getInstance(activity: Activity, jvView: JvView): JvController {
            return INSTANCE ?: JvController(activity,jvView)
                .apply { INSTANCE = this }
        }
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}