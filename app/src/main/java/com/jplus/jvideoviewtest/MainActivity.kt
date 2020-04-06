package com.jplus.jvideoviewtest

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.jplus.jvideoview.JvController
import com.jplus.jvideoview.data.Video

import com.jplus.jvideoview.jvideo.JvState
import com.jplus.jvideoview.jvideo.JvPresenter
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var presenter: JvPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onStart() {
        super.onStart()
        val urls = intent.getStringExtra("urls")
        val s1 = Regex("\n")
        var id = 0
        val list = ArrayList<Video>()
        urls.split(s1).forEach { it1 ->
            Log.d("pipa", "split:" + it1)
            list.add(Video(id, "视频$id", "", it1.replace("\n| ", ""), 0, "", ""))
            id++
        }
//        JvController.getInstance(this, jv_video_main2)
        presenter = JvPresenter(this, jv_video_main2, JvState.PlayBackEngine.PLAYBACK_IJK_PLAYER, object :JvPresenter.JVideoCallBack{
            override fun initSuccess() {
                presenter?.start(list)
            }
        })
        presenter?.subscribe()
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        presenter?.onConfigChanged(newConfig)
    }

    override fun onResume() {
        super.onResume()
        presenter?.onResume()
    }

    override fun onPause() {
        super.onPause()
        presenter?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.unSubscribe()
    }
}
