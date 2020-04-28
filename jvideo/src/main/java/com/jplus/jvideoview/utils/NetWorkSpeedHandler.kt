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
        return JvUtil.parseByteSize(speed1)
    }



    interface OnNetWorkSpeedListener {
        /**
         * 返回实时网速
         * @param speed 实时网速
         */
        fun netWorkSpeed(speed: String)
    }
}