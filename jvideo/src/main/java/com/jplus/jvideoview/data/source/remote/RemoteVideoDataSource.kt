package com.jplus.jvideoview.data.source.remote

import com.jplus.jvideoview.data.Video
import com.jplus.jvideoview.data.source.VideoDataSource

/**
 * @author JPlus
 * @date 2019/10/15.
 */
class RemoteVideoDataSource:VideoDataSource {
    override fun getVideos(callback: VideoDataSource.LoadVideosCallback) {
          //这里模拟网络请求拿到数据并存储
        val videoList = listOf((Video(id=1, videoName = "测试视频", videoUrl = "https://gss3.baidu.com/6LZ0ej3k1Qd3ote6lo7D0j9wehsv/tieba-smallvideo/607272_bd5ec588760b7d8d2fc15183b95e628a.mp4", videoPicUrl = "", progress = 0)))
        callback.onVideosLoaded(videoList)
    }

    override fun getVideo(videoId: String, callback: VideoDataSource.GetVideoCallback) {
          
    }

    override fun saveVideo(video: Video) {
          
    }

    override fun completeVideos(video: Video) {
          
    }

    override fun completeTask(videoId: Int) {
          
    }

    override fun activateVideo(video: Video) {
          
    }

    override fun activateVideo(videoId: Int) {
          
    }

    override fun clearCompletedVideos() {
          
    }

    override fun refreshVideos() {
          
    }

    override fun deleteAllVideos() {
          
    }

    override fun deleteVideo(videoId: Int) {
          
    }

    override fun deleteVideo(video: Video) {
          
    }
}