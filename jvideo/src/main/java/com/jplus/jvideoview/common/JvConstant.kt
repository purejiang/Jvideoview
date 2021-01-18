package com.jplus.jvideoview.common

/**
 * @author JPlus
 * @date 2019/8/30.
 */

class JvConstant {
    /**
     * 播放器状态
     */
    class PlayState {
        companion object {
            /**
             * 播放错误
             */
            const val STATE_ERROR = -1
            /**
             * 播放器闲置转态
             */
            const val STATE_IDLE = 0
            /**
             * 播放器初始化完成
             */
            const val STATE_INITLIZED = 1
            /**
             * 播放准备中
             */
            const val STATE_PREPARING = 2
            /**
             * 播放准备就绪
             */
            const val STATE_PREPARED = 3
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
    class PlayMode {
        companion object {
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
     * 切换模式
     */
    class SwitchMode {
        companion object {
            /**
             * 普通->全屏->普通模式
             */
            const val SWITCH_FULL_OR_NORMAL = 50
            /**
             * 普通->窗口模式
             */
            const val SWITCH_WINDOW_OR_NORMAL = 51

        }
    }

    /**
     * 调节模式
     */
    class PlayAdjust {
        companion object {
            /**
             * 音量调节
             */
            const val ADJUST_VOLUME = 20
            /**
             * 亮度调节
             */
            const val ADJUST_LIGHT = 21
            /**
             * 快退快进
             */
            const val ADJUST_VIDEO = 22
            /**
             * 不调节
             */
            const val ADJUST_UNKNOWN = 23
        }
    }

    /**
     * 播放器内核
     */
    class PlayBackEngine {
        companion object {
            /**
             * android 自带
             */
            const val PLAYBACK_MEDIA_PLAYER = 30
            /**
             * IjkPlayer
             */
            const val PLAYBACK_IJK_PLAYER = 31
            /**
             * ExoPlayer
             */
            const val PLAYBACK_EXO_PLAYER = 32
        }
    }

    /**
     * 播放形式
     */
    class PlayForm {
        companion object {
            /**
             * 顺序播放
             */
            const val PLAY_FORM_TURN = 40
            /**
             * 单视频循环
             */
            const val PLAY_ONE_LOOP = 41
            /**
             * 单视频播放
             */
            const val PLAY_ONE_END = 42
        }
    }
}