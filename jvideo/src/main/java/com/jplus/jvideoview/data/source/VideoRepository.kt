package com.jplus.jvideoview.data.source

import android.util.Log
import com.jplus.jvideoview.data.Video
import com.jplus.jvideoview.jvideo.JVideoCommon
import java.util.*

/**
 * @author JPlus
 * @date 2019/10/10.
 */
class VideoRepository(private val mLocal:VideoDataSource, private val mRemote:VideoDataSource ):VideoDataSource {
    var mVideoList = ArrayList<Video>()
    /**
     * 标记缓存是否无效
     */
    var mIsCacheDirty = false

    override fun getVideos(callback: VideoDataSource.LoadVideosCallback) {
        Log.d(JVideoCommon.TAG, "VideoRepository.getVideos")
        //数据源不为空就返回
        if (mVideoList.isNotEmpty() && !mIsCacheDirty) {
            callback.onVideosLoaded(mVideoList)
            return
        }
        if(mIsCacheDirty){
            //标记缓存无效则从网络获取数据源
              getVideosFromRemoteDataSource(callback)
        }else{
            //标记缓存有效则加载本地数据源
            getVideosFromLocalDataSource(callback)
        }
    }

    override fun getVideo(videoId: String, callback: VideoDataSource.GetVideoCallback) {

    }

    override fun saveVideo(video: Video) {
        cacheAndPerform(video) {
            mRemote.saveVideo(it)
            mLocal.saveVideo(it)
        }
    }

    override fun completeVideos(video: Video) {
        cacheAndPerform(video) {
            mRemote.completeVideos(it)
            mLocal.completeVideos(it)
        }
    }

    override fun completeTask(videoId: Int) {

    }

    override fun activateVideo(video: Video) {
        cacheAndPerform(video) {
            mRemote.activateVideo(it)
            mLocal.activateVideo(it)
        }
    }

    override fun activateVideo(videoId: Int) {

    }

    override fun clearCompletedVideos() {
        //清除所有已播放完成的Video
        mRemote.clearCompletedVideos()
        mLocal.clearCompletedVideos()

        mVideoList = mVideoList.forEach {
            //如果播放进度为100则改为0
            if(it.progress==100) {
                it.progress = 0
            }
        } as ArrayList<Video>
    }

    override fun refreshVideos() {
       mIsCacheDirty = true
    }

    override fun deleteAllVideos() {
        mLocal.deleteAllVideos()
        mRemote.deleteAllVideos()
        mVideoList.clear()
    }

    override fun deleteVideo(videoId: Int) {

    }

    override fun deleteVideo(video: Video) {
        mLocal.deleteVideo(video)
        mRemote.deleteVideo(video)
    }
    private fun getVideosFromLocalDataSource(callback: VideoDataSource.LoadVideosCallback) {
       mLocal.getVideos(object : VideoDataSource.LoadVideosCallback {
           override fun onVideosLoaded(videos: List<Video>) {
               //刷新缓存数据
               refreshCache(videos)
               //回调通知数据加载成功
               callback.onVideosLoaded(mVideoList)
           }

           override fun onDataNotAvailable() {
               //数据获取失败
               callback.onDataNotAvailable()
           }
       })
    }

    private fun  getVideosFromRemoteDataSource(callback: VideoDataSource.LoadVideosCallback) {
        mRemote.getVideos(object : VideoDataSource.LoadVideosCallback {
            override fun onVideosLoaded(videos: List<Video>) {
                //刷新缓存数据
                refreshCache(videos)
                //刷新本地数据
                refreshLocalDataSource(videos)
                //回调通知数据加载成功
                callback.onVideosLoaded(mVideoList)
            }

            override fun onDataNotAvailable() {
                //数据获取失败
                callback.onDataNotAvailable()
            }
        })
    }
    private fun refreshLocalDataSource(videos: List<Video>) {
        mLocal.deleteAllVideos()
        for (video in videos) {
            mLocal.saveVideo(video)
        }
    }
    private fun refreshCache(videos: List<Video>) {
            //刷新数据，清理原始数据
            mVideoList.clear()
            //保存刷新数据
            videos.forEach {
                cacheAndPerform(it) {
                    //在此处参数仍为video也就是it
                }
            }
            mIsCacheDirty = false
    }

    private fun cacheAndPerform(video: Video, function: (Video) -> Unit) {
        mVideoList.add(video)
        //携带方法名在此处调用方法
        function(video)
    }
    companion object {

        private var INSTANCE: VideoRepository? = null

        /**
         * Returns the single instance of this class, creating it if necessary.
         * @param remoteDataSource the backend data source
         * *
         * @param localDataSource  the device storage data source
         * *
         * @return the [VideoRepository] instance
         */
        fun getInstance(remoteDataSource: VideoDataSource,
                                   localDataSource: VideoDataSource): VideoRepository {
            return INSTANCE ?: VideoRepository(localDataSource, remoteDataSource)
                .apply { INSTANCE = this }
        }

        /**
         * Used to force [getInstance] to create a new instance
         * next time it's called.
         */
         fun destroyInstance() {
            INSTANCE = null
        }
    }
}