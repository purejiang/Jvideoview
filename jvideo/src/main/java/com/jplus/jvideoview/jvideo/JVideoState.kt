package com.jplus.jvideoview.jvideo

/**
 * @author JPlus
 * @date 2019/8/30.
 */
class JVideoState {
    /**
     * 播放器状态
     */
    class PlayState{
            companion object{
                /**
                 * 播放错误
                 */
                const val STATE_ERROR = -1
                /**
                 * 播放未开始
                 */
                const val STATE_IDLE = 0
                /**
                 * 播放准备中
                 */
                const val STATE_PREPARING = 1
                /**
                 * 播放准备就绪
                 */
                const val STATE_PREPARED = 2
                /**
                 * 开始播放
                 */
                const val STATE_START = 3
                /**
                 * 正在播放
                 */
                const val STATE_PLAYING = 4
                /**
                 * 暂停播放
                 */
                const val STATE_PAUSED = 5
                /**
                 * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，缓冲区数据足够后恢复播放)
                 */
                const val STATE_BUFFERING_PLAYING = 6
                /**
                 * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，此时暂停播放器，继续缓冲，缓冲区数据足够后恢复暂停
                 */
                const val STATE_BUFFERING_PAUSED = 7
                /**
                 * 播放完成
                 */
                const val STATE_COMPLETED = 8
            }
        }

    /**
     * 播放模式
     */
    class PlayMode{
        companion object{
            /**
             * 普通模式
             */
            const val MODE_NORMAL = 10
            /**
             * 全屏模式
             */
            const val MODE_FULL_SCREEN = 11
            /**
             * 窗口模式
             */
            const val MODE_TINY_WINDOW = 12
        }
    }

    /**
     * 调节模式
     */
    class PlayAdjust{
        companion object{
            /**
             * 音量调节
             */
            const val ADJUST_VOLUME = 13
            /**
             * 亮度调节
             */
            const val ADJUST_LIGHT = 14
            /**
             * 快退快进
             */
            const val ADJUST_VIDEO = 15
        }
    }
}