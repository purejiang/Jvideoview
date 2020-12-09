package com.jplus.jvideoviewtest

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.AppBarLayout
import com.jplus.jvideoview.JvController
import com.jplus.jvideoview.common.JvConstant
import com.jplus.jvideoview.entity.Video
import com.jplus.jvideoview.jvideo.JvCommon
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var mController: JvController? = null
    private var mIsAuto = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val windowManager: WindowManager.LayoutParams = window.attributes
        //异形屏适配，横屏时显示刘海区域
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            windowManager.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
           window.attributes = windowManager
        }


        val urls = intent.getStringExtra("urls")
        val s1 = Regex("\n")
        var id = 0
        val list = mutableListOf<Video>()
        urls.split(s1).forEach { it1 ->
            Log.d(JvCommon.TAG, "split:$it1")
            list.add(Video("视频$id", it1.replace("\n| ", ""), 0, 20000L))
            id++
        }
        var mAppBarParams: AppBarLayout.LayoutParams? = null

        mController = JvController(this, jv_video_play, object : JvController.JvCallBack {
            override fun initSuccess() {
                Log.d(JvCommon.TAG, "initSuccess")
//                val mAppBarChildAt: View = abl_play_top.getChildAt(0)
//                mAppBarParams = mAppBarChildAt.layoutParams as AppBarLayout.LayoutParams
                mController?.playVideos(list)
            }

            override fun startPlay() {
                Log.d(JvCommon.TAG, "startPlay")
//                val mAppBarChildAt: View = abl_play_top.getChildAt(0)
                mAppBarParams?.let {
                    it.scrollFlags = 0
//                    mAppBarChildAt.layoutParams = it
                }
            }

            override fun switchScreen(isFullScreen: Boolean) {

            }

            override fun endPlay() {
                Log.d(JvCommon.TAG, "endPlay")
            }

            override fun pausePlay() {
//                val mAppBarChildAt: View = abl_play_top.getChildAt(0)
                mAppBarParams?.let {
                    it.scrollFlags =
                        AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
//                    mAppBarChildAt.layoutParams = it
                }

            }

            override fun autoToNext() {
                Log.d(JvCommon.TAG, "autoToNext")
            }

            override fun manualToNext() {
                Log.d(JvCommon.TAG, "manualToNext")
            }
        }, JvConstant.PlayBackEngine.PLAYBACK_IJK_PLAYER)
        mController?.supportShowSysTime(true)
        mController?.supportAutoPlay(mIsAuto)

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.d(JvCommon.TAG, "onWindowFocusChanged")
    }

    override fun onStart() {
        super.onStart()
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mController?.onConfigChanged(newConfig)
    }

    override fun onResume() {
        super.onResume()
        mController?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mController?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mController?.destroy()
    }

    override fun onBackPressed() {
        if (mController?.onBackProgress() == false) super.onBackPressed()
    }
}
