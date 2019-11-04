package com.jplus.jvideoview.data.source.remote

import com.jplus.jvideoview.data.Video
import com.jplus.jvideoview.data.source.VideoDataSource

/**
 * @author JPlus
 * @date 2019/10/15.
 */
class RemoteVideoDataSource(var mDataList: List<Video>?) : VideoDataSource {

    override fun getVideos(callback: VideoDataSource.LoadVideosCallback) {
        //这里模拟网络请求拿到数据并存储
        if (mDataList == null) {
            mDataList = listOf(
                (Video(
                    id = 1,
                    videoName = "测试视频",
                    videoUrl = "https://cn3.download05.com/hls/20190721/91ae4a55f7630577b9c58697f94fb2ea/1563674859/index.m3u8",
                    videoPicUrl = "",
                    progress = 0
                ))
            )
        }
        mDataList?.let {
            callback.onVideosLoaded(it)
        }

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