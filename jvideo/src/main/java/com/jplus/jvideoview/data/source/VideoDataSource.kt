package com.jplus.jvideoview.data.source

import com.jplus.jvideoview.data.Video

/**
 * @author JPlus
 * @date 2019/10/10.
 */
interface VideoDataSource {

    interface LoadVideosCallback {

        fun onVideosLoaded(videos: List<Video>)

        fun onDataNotAvailable()
    }

    interface GetVideoCallback {

        fun onVideoLoaded(video: Video)

        fun onDataNotAvailable()
    }

    fun getVideos(callback: LoadVideosCallback)

    fun getVideo(videoId: String, callback: GetVideoCallback)

    fun saveVideo(video: Video)

    fun completeVideos(video: Video)

    fun completeTask(videoId: Int)

    fun activateVideo(video: Video)

    fun activateVideo(videoId: Int)

    fun clearCompletedVideos()

    fun refreshVideos()

    fun deleteAllVideos()

    fun deleteVideo(videoId: Int)

    fun deleteVideo(video: Video)
}