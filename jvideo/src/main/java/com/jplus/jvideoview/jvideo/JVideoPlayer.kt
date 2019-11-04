package com.jplus.jvideoview.jvideo

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.SurfaceHolder
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import tv.danmaku.ijk.media.player.MediaInfo
import tv.danmaku.ijk.media.player.misc.IMediaDataSource
import tv.danmaku.ijk.media.player.misc.ITrackInfo
import java.io.FileDescriptor
import java.lang.Exception


/**
 * @author JPlus
 * @date 2019/10/24.
 */
class JVideoPlayer(private val mPlayerType: Int){
    private var mPlayer: MediaPlayer? = null
    private var mIjkPlayer: IjkMediaPlayer? = null
    private var mPlayerIsIjk = false
    private var mCallback: JVideoPlayer.JVideoPlayerCallback? = null
    private var mIjkCallback: JVideoPlayer.IjkJVideoPlayerCallback? = null

    init {
        mPlayerIsIjk = if (mPlayerType == JVideoState.PlayBackEngine.PLAYBACK_IJK_PLAYER) {
            mIjkPlayer = IjkMediaPlayer()
            true
        } else {
            mPlayer = MediaPlayer()
            false
        }
    }

    fun setIjkJVideoCallback(callback: IjkJVideoPlayerCallback) {
        if (mPlayerIsIjk) {
            mIjkPlayer?.let {
                it.setOnCompletionListener {
                    callback.setOnCompletionListener()
                }
                it.setOnPreparedListener {
                    callback.setOnPreparedListener()
                }
                it.setOnBufferingUpdateListener { iMediaPlayer, i ->
                    callback.setOnBufferingUpdateListener(iMediaPlayer, i)
                }
                it.setOnErrorListener { iMediaPlayer, i, i2 ->
                    callback.setOnErrorListener(iMediaPlayer, i, i2)
                }
                it.setOnInfoListener { iMediaPlayer, i, i2 ->
                    callback.setOnInfoListener(iMediaPlayer, i, i2)
                }
                it.setOnVideoSizeChangedListener { iMediaPlayer, i, i2, i3, i4 ->
                    callback.setOnVideoSizeChangedListener(iMediaPlayer, i, i2, i3, i4)
                }
                it.setOnSeekCompleteListener {
                    callback.setOnSeekCompleteListener()
                }
            }
        } else {
            throw Exception("not IjkPlayer")
        }
    }

    fun setJVideoCallback(callback: JVideoPlayerCallback) {
        if (!mPlayerIsIjk) {
            mPlayer?.let {
                it.setOnCompletionListener {
                    callback.setOnCompletionListener()
                }
                it.setOnPreparedListener {
                    callback.setOnPreparedListener()
                }
                it.setOnBufferingUpdateListener { mediaPlayer, i ->
                    callback.setOnBufferingUpdateListener(mediaPlayer, i)
                }

            }
        } else {
            throw Exception("not MediaPlayer")
        }
    }

    fun setOption(category: Int, name: String, value: String) {
        mIjkPlayer?.setOption(category, name, value)
    }
    fun setOption(category: Int, name: String, value: Long) {
        mIjkPlayer?.setOption(category, name, value)
    }
     fun isLooping(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun getDuration(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun getDataSource(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun getCurrentPosition(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun setOnPreparedListener(p0: IMediaPlayer.OnPreparedListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun setDisplay(p0: SurfaceHolder?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun setOnVideoSizeChangedListener(p0: IMediaPlayer.OnVideoSizeChangedListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun start() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun setVolume(p0: Float, p1: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun getVideoSarDen(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun setDataSource(p0: Context?, p1: Uri?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun setDataSource(p0: Context?, p1: Uri?, p2: MutableMap<String, String>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun setDataSource(p0: FileDescriptor?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun setDataSource(p0: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun setDataSource(p0: IMediaDataSource?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun setKeepInBackground(p0: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun getMediaInfo(): MediaInfo {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun setOnSeekCompleteListener(p0: IMediaPlayer.OnSeekCompleteListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun setOnErrorListener(p0: IMediaPlayer.OnErrorListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun prepareAsync() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun setAudioStreamType(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun getVideoWidth(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun setLooping(p0: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun getVideoHeight(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun getVideoSarNum(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun setSurface(p0: Surface?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun stop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun setOnBufferingUpdateListener(p0: IMediaPlayer.OnBufferingUpdateListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun isPlayable(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun setOnInfoListener(p0: IMediaPlayer.OnInfoListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun setOnCompletionListener(p0: IMediaPlayer.OnCompletionListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun seekTo(p0: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun setLogEnabled(p0: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun getIjkTrackInfo(): Array<out ITrackInfo> {
        return mIjkPlayer?.trackInfo!!
    }


     fun reset() {
        if (mPlayerIsIjk) {
            mIjkPlayer?.reset()
        } else {
            mPlayer?.reset()
        }
    }

     fun setOnTimedTextListener(p0: IMediaPlayer.OnTimedTextListener?) {
        if (mPlayerIsIjk) {
            mIjkPlayer?.setOnTimedTextListener(p0)
        }
    }

     fun setWakeMode(p0: Context, p1: Int) {
        if (mPlayerIsIjk) {
            mIjkPlayer?.setWakeMode(p0, p1)
        } else {
            mPlayer?.setWakeMode(p0, p1)
        }
    }

     fun isPlaying(): Boolean {
        return if (mPlayerIsIjk) {
            mIjkPlayer?.isPlaying!!
        } else {
            mPlayer?.isPlaying!!
        }
    }

     fun pause() {
        if (mPlayerIsIjk) {
            mIjkPlayer?.pause()
        } else {
            mPlayer?.pause()
        }
    }

     fun setScreenOnWhilePlaying(p0: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun getAudioSessionId(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     fun release() {
        if (mPlayerIsIjk) {
            mIjkPlayer?.release()
        } else {
            mPlayer?.release()
        }
    }


    interface JVideoPlayerCallback {
        fun setOnCompletionListener()
        fun setOnSeekCompleteListener()
        fun setOnPreparedListener()
        fun setOnBufferingUpdateListener(mediaPlayer: MediaPlayer, i: Int)
        fun setOnErrorListener()
        fun setOnInfoListener()
        fun setOnVideoSizeChangedListener()
    }

    interface IjkJVideoPlayerCallback {
        fun setOnCompletionListener()
        fun setOnSeekCompleteListener()
        fun setOnPreparedListener()
        fun setOnBufferingUpdateListener(iMediaPlayer: IMediaPlayer, i: Int)
        fun setOnErrorListener(iMediaPlayer: IMediaPlayer, var2: Int, var3: Int): Boolean
        fun setOnInfoListener(iMediaPlayer: IMediaPlayer, var2: Int, var3: Int): Boolean
        fun setOnVideoSizeChangedListener(iMediaPlayer: IMediaPlayer, i: Int, i2: Int, i3: Int, i4: Int)
    }
}