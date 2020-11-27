package com.jplus.jvideoview.other

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager

/**
 * @author JPlus
 * @date 2020/7/6.
 */
class BatteryReceiver: BroadcastReceiver() {
    private var mOnBatteryChangeListener: OnBatteryChangeListener? = null

    override fun onReceive(context: Context?, intent: Intent) {
        if (Intent.ACTION_BATTERY_CHANGED == intent.action) {
            val level = intent.getIntExtra("level", 0)
            val scale = intent.getIntExtra("scale", 100)
            val battery = level * 100.0 / scale

            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            mOnBatteryChangeListener?.backBattery(battery, status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL)
        }
    }

    fun setOnBatteryChangeListener(onBatteryChangeListener: OnBatteryChangeListener?) {
        mOnBatteryChangeListener = onBatteryChangeListener
    }

    /**
     * 返回电池信息的接口
     */
    interface OnBatteryChangeListener {
        /**
         * 返回的电量、充电状态
         * @param battery 电量信息
         * @param isCharge 是否充电
         */
        fun backBattery(battery: Double, isCharge: Boolean)
    }
}