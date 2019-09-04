package com.jplus.jvideoview.model

/**
 * @author Administrator
 * @date 2019/8/30.
 */
class JVideoState {

        class PlayState{
            companion object{
                /**
                 * 播放错误
                 */
                val STATE_ERROR = -1
                /**
                 * 播放未开始
                 */
                val STATE_IDLE = 0
                /**
                 * 播放准备中
                 */
                val STATE_PREPARING = 1
                /**
                 * 播放准备就绪
                 */
                val STATE_PREPARED = 2
                /**
                 * 开始播放
                 */
                val STATE_START = 3
                /**
                 * 正在播放
                 */
                val STATE_PLAYING = 4
                /**
                 * 暂停播放
                 */
                val STATE_PAUSED = 5
                /**
                 * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，缓冲区数据足够后恢复播放)
                 */
                val STATE_BUFFERING_PLAYING = 6
                /**
                 * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，此时暂停播放器，继续缓冲，缓冲区数据足够后恢复暂停
                 */
                val STATE_BUFFERING_PAUSED = 7
                /**
                 * 播放完成
                 */
                val STATE_COMPLETED = 8
            }
        }
    class PlayMode{
        companion object{
            /**
             * 普通模式
             */
            val MODE_NORMAL = 10
            /**
             * 全屏模式
             */
            val MODE_FULL_SCREEN = 11
            /**
             * 窗口模式
             */
            val MODE_TINY_WINDOW = 12
        }
    }
    class PlayAdjust{
        companion object{
            /**
             * 音量调节
             */
            val ADJUST_VOLUME = 13
            /**
             * 亮度调节
             */
            val ADJUST_LIGHT = 14
            /**
             * 快退快进
             */
            val ADJUST_VIDEO = 15
        }
    }
}