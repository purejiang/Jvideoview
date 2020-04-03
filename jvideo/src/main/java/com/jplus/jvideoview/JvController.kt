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






    fun setCallBack(){

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