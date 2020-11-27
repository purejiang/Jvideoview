package com.jplus.jvideoviewtest

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.AppBarLayout
import com.jplus.jvideoview.JvController
import com.jplus.jvideoview.common.JvConstant
import com.jplus.jvideoview.entity.Video
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var mController: JvController? = null
    private var mIsAuto = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val urls = intent.getStringExtra("urls")
        val s1 = Regex("\n")
        var id = 0
        val list = mutableListOf<Video>()
        urls.split(s1).forEach { it1 ->
            Log.d("jv", "split:$it1")
            list.add(Video("视频$id", it1.replace("\n| ", ""), 0, 20000L))
            id++
        }
        var mAppBarParams: AppBarLayout.LayoutParams? = null

        mController = JvController(this, jv_video_play, object : JvController.JvCallBack {
            override fun initSuccess() {
                Log.d("jv", "initSuccess")
//                val mAppBarChildAt: View = abl_play_top.getChildAt(0)
//                mAppBarParams = mAppBarChildAt.layoutParams as AppBarLayout.LayoutParams
                mController?.playVideos(list)
            }

            override fun startPlay() {
                Log.d("jv", "startPlay")
//                val mAppBarChildAt: View = abl_play_top.getChildAt(0)
                mAppBarParams?.let {
                    it.scrollFlags = 0
//                    mAppBarChildAt.layoutParams = it
                }
            }

            override fun toNext() {
                Log.d("jv", "toNext")
            }

            override fun switchScreen(isFullScreen: Boolean) {

            }

            override fun endPlay() {
                Log.d("jv", "endPlay")
            }

            override fun pausePlay() {

//                val mAppBarChildAt: View = abl_play_top.getChildAt(0)
                mAppBarParams?.let {
                    it.scrollFlags =
                        AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
//                    mAppBarChildAt.layoutParams = it
                }

            }
        }, JvConstant.PlayBackEngine.PLAYBACK_IJK_PLAYER)
        mController?.supportShowSysTime(true)
        mController?.supportAutoPlay(mIsAuto)

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.d("jv", "onWindowFocusChanged")
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
