package com.jplus.jvideoview.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.LinearLayout
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.max

/**
 * @author JPlus
 * @date 2019/9/2.
 */
object JvUtil {
    /**
     * 播放进度转换为时间
     * @param progress 进度(整型)
     */
    fun progress2Time(progress: Long?): String {
        return if (progress == null) {
            "00:00"
        } else {
            val simpleDate = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
            simpleDate.timeZone = TimeZone.getTimeZone("GMT+00:00")
            val result = simpleDate.format(Date(progress))
            if (result.startsWith("00:")) result.removePrefix("00:") else result
        }
    }

    /**
     * 将滑动的距离转为进度显示
     * @param distance 滑动的距离
     * @param duration 总进度
     * @param proportion 一次屏幕的滑动所占总进度的比例
     * @param all 总高度/总宽度
     */
    fun dt2progress(distance: Float, duration: Long, all: Int, proportion: Double): Double {
        return (distance * duration * proportion / all)
    }

    /**
     * 获取屏幕宽度
     * @param context 上下文
     */
    fun getPhoneDisplayWidth(context: Context): Int {
        return (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.width
    }

    /**
     * 获取屏幕高度
     * @param context 上下文
     */
    fun getPhoneDisplayHeight(context: Context): Int {
        return (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.height
    }

    /**
     * 获取视频流的第一帧图片
     * @param videoUri 在线视频的播放地址/本地视频的uri
     * @return Bitmap
     */
    fun getNetVideoBitmap(videoUri: String): Bitmap? {
        var bitmap: Bitmap? = null
        val retriever = MediaMetadataRetriever()
        try {
            //根据url获取缩略图
            retriever.setDataSource(videoUri, HashMap())
            //获得第一帧图片
            bitmap = retriever.frameAtTime
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        return bitmap
    }

    //按比例改变视频大小适配屏幕宽高, 因为jvView就是继承LinearLayout的，所以要返回LinearLayout.LayoutParams
    fun changeVideoSize(
        phoneWidth: Int,
        phoneHeight: Int,
        playerWidth: Int,
        playerHeight: Int
    ): LinearLayout.LayoutParams {
        Log.d("pipa", "changeVideoSize:phoneWidth:$phoneWidth, phoneHeight:$phoneHeight, playerWidth")
        val defaultWidth = if (phoneWidth < 0) 1920 else phoneWidth

        //根据视频尺寸去计算->视频可以在TextureView中放大的最大倍数。
        val max =
            //竖屏模式下按视频宽度计算放大倍数值
            max(playerHeight * 1.0 / phoneHeight, playerWidth * 1.0 / defaultWidth)
        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
        val videoWidth = ceil(playerWidth * 1.0 / max).toInt()
        val videoHeight = ceil(playerHeight * 1.0 / max).toInt()
        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
        return LinearLayout.LayoutParams(videoWidth, videoHeight)

    }

    /**
     * 获取手机是否开启自动旋转屏幕
     * @param context 上下文
     * @return 是否已开启自动旋转屏幕功能
     */
    fun getIsOpenRotate(context: Context): Boolean {
        return Settings.System.getInt(
            context.contentResolver,
            Settings.System.ACCELEROMETER_ROTATION
        ) == 1
    }

    /**
     * long转文件大小
     * @param size
     * @return 返回B、K、M、G
     */
    fun parseByteSize(size: Float): String {
        return if (size in 0f..1023f) {
            String.format("%.1fB", size)
        } else if (size >= 1024f && size < 1024f * 1024f) {
            String.format("%.1fK", size / 1024f)
        } else if (size >= 1024f * 1024f && size < 1024f * 1024f * 1024f) {
            String.format("%.1fM", size / 1024f / 1024f)
        } else if (size >= 1024f * 1024f * 1024f) {
            String.format("%.1fG", size / 1024f / 1024f / 1024f)
        } else {
            "0B"
        }
    }
}