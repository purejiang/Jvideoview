package com.jplus.jvideoview.data.source.local

import com.jplus.jvideoview.data.Video
import com.jplus.jvideoview.data.source.VideoDataSource

/**
 * @author JPlus
 * @date 2019/10/15.
 */
class LocalVideoDataSource:VideoDataSource {
    override fun getVideos(callback: VideoDataSource.LoadVideosCallback) {
        
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