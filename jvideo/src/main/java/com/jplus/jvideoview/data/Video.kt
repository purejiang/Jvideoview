package com.jplus.jvideoview.data

/**
 * @author JPlus
 * @date 2019/10/10.
 */
data class Video(
    val id :Int,
    var videoName:String?,
    var videoPicUrl:String,
    var videoUrl:String,
    var progress:Int
)