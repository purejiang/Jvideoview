package com.jplus.jvideoviewtest

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.jplus.jvideoview.data.Video
import com.jplus.jvideoview.data.source.VideoRepository
import com.jplus.jvideoview.data.source.local.LocalVideoDataSource
import com.jplus.jvideoview.data.source.remote.RemoteVideoDataSource
import com.jplus.jvideoview.jvideo.JVideoState
import com.jplus.jvideoview.jvideo.JVideoViewPresenter
import kotlinx.android.synthetic.main.activity_main.*



class MainActivity : AppCompatActivity() {
   private  var presenter: JVideoViewPresenter?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val urls = intent.getStringExtra("urls")
        val s1 = Regex("\n")
        var id = 0
        val list = ArrayList<Video>()
        urls.split(s1).forEach { it1 ->
            Log.d("pipa", "split:"+it1)
            list.add(Video(id, "视频$id", "", it1.replace("\n| ", ""), 0))
            id++
        }
        presenter = JVideoViewPresenter(
            this,
            jv_video_main2,
            VideoRepository.getInstance(RemoteVideoDataSource(list), LocalVideoDataSource()).apply {
                refreshVideos()
            }, JVideoState.PlayBackEngine.PLAYBACK_IJK_PLAYER)

        presenter?.subscribe()
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
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
