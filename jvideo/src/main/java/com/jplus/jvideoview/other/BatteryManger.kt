package com.jplus.jvideoview.other

import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * @author JPlus
 * @date 2020/7/6.
 */
class BatteryManger {
    companion object {
        private var batteryReceiver: BatteryReceiver? = null

        fun bindAutoBattery(context: Context, onBatteryChangeListener: BatteryReceiver.OnBatteryChangeListener?): BatteryReceiver? {
            batteryReceiver = BatteryReceiver()
            context.registerReceiver(
                batteryReceiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            batteryReceiver?.setOnBatteryChangeListener(onBatteryChangeListener)
            return batteryReceiver
        }

        /**
         * 注销广播
         * @param context 上下文
         */
        fun unbindAutoBattery(context: Context) {
            if (batteryReceiver != null) {
                context.unregisterReceiver(batteryReceiver)
                batteryReceiver = null
            }
        }
    }
}