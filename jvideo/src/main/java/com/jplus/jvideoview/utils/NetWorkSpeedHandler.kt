package com.jplus.jvideoview.utils

import android.content.Context
import android.net.TrafficStats
import android.os.Handler

/**
 * @author JPlus
 * @date 2020/4/21.
 */
class NetWorkSpeedHandler (private val context: Context,private val  frequency:Long){

    private val mHandler by lazy {
        Handler()
    }
    private var mRunnable: Runnable? = null
    private var mLastTime = 0L
    private var mLastRxBytes = 0L


     fun bindHandler(onNetWorkSpeedListener: OnNetWorkSpeedListener) {
        mLastTime = System.currentTimeMillis()
        mLastRxBytes = getTotalRxBytes(context)
        //开始计时
        if (mRunnable == null) {
            mRunnable = Runnable{
                onNetWorkSpeedListener.netWorkSpeed(getNetWorkSpeed())
                mHandler.postDelayed(mRunnable, frequency)
            }
        }
        mHandler.post(mRunnable)
    }

     fun unbindHandler() { //暂停计时
        mHandler.removeCallbacks(mRunnable)
        if (mRunnable != null) {
            mRunnable = null
        }
    }

    private fun getTotalRxBytes(context: Context): Long { //获取流量总量，转为KB
        return if (TrafficStats.getUidRxBytes(context.applicationInfo.uid) == TrafficStats.UNSUPPORTED.toLong()) 0 else TrafficStats.getTotalRxBytes() / 1024
    }

    private fun getNetWorkSpeed(): String {
        val nowTime = System.currentTimeMillis()
        val nowTotalRxBytes = getTotalRxBytes(context)
        val speed1 = ((nowTotalRxBytes - mLastRxBytes) * 1f) / ((nowTime - mLastTime) * 1f/ 1000f)*1024
        mLastRxBytes = nowTotalRxBytes
        return parseByteSize(speed1)
    }

    /**
     * long转文件大小
     * @param size
     * @return 返回B、K、M、G
     */
    private fun parseByteSize(size: Float): String {
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

    interface OnNetWorkSpeedListener {
        /**
         * 返回实时网速
         * @param speed 实时网速
         */
        fun netWorkSpeed(speed: String)
    }
}