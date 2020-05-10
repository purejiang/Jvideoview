package com.jplus.jvideoviewtest

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.jplus.jvideoview.JvController
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
            Log.d("pipa", "split:" + it1)
            list.add(Video("视频$id", it1.replace("\n| ", ""), 55306L))
            id++
        }

        mController = JvController(this, jv_video_main2, object : JvController.JvCallBack {
            override fun initSuccess() {
                Log.d("pipa", "initSuccess" )
                mController?.playVideos(list)
            }

            override fun startPlay() {
                Log.d("pipa", "startPlay")
            }

            override fun toNext() {
                Log.d("pipa", "toNext")
            }

            override fun endPlay() {
                Log.d("pipa", "endPlay")
            }

        })
        mController?.supportShowSysTime(true)
        mController?.supportAutoPlay(true)
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
