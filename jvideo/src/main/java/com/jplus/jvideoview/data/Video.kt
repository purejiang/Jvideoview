package com.jplus.jvideoview.data

/**
 * @author JPlus
 * @date 2019/10/10.
 */
data class Video(
    /**
     * 视频编号
     */
    val id :Int,
    /**
     * 视频名称
     */
    var videoName:String?,
    /**
     * 视频海报的地址
     */
    var videoPicUrl:String,
    /**
     * 视频的地址
     */
    var videoUrl:String,
    /**
     * 视频播放进度
     */
    var progress:Int,
    /**
     * 视频大小
     */
    var videoSize:String,
    /**
     * 视频格式
     */
    var videoType:String
)