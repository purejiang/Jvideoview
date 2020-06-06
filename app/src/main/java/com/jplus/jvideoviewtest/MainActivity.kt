package com.jplus.jvideoviewtest

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.jplus.jvideoview.JvController
import com.jplus.jvideoview.common.JvConstant
import com.jplus.jvideoview.entity.Video
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var mController: JvController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val urls = intent.getStringExtra("urls")
        val s1 = Regex("\n")
        var id = 0
        val list = mutableListOf<Video>()
        urls.split(s1).forEach { it1 ->
            Log.d("jv", "split:" + it1)
            list.add(Video("视频$id", it1.replace("\n| ", ""), 565306L))
            id++
        }

        mController = JvController(this, jv_video_main2, object : JvController.JvCallBack {
            override fun initSuccess() {
                Log.d("jv", "initSuccess" )
                mController?.playVideos(list)
            }

            override fun startPlay() {
                Log.d("jv", "startPlay")
            }

            override fun toNext() {
                Log.d("jv", "toNext")
            }

            override fun endPlay() {
                Log.d("jv", "endPlay")
            }

        }, JvConstant.PlayBackEngine.PLAYBACK_IJK_PLAYER)
        mController?.supportShowSysTime(true)
        mController?.supportAutoPlay(false)
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
