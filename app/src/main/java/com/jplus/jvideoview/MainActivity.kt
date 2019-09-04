package com.jplus.jvideoview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.jplus.jvideoview.persenter.JVideoViewPresenter
import kotlinx.android.synthetic.main.activity_main.*
import android.view.ViewGroup



class MainActivity : AppCompatActivity() {
   private  var presenter:JVideoViewPresenter?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        jv_video_main2.init("https://gss3.baidu.com/6LZ0ej3k1Qd3ote6lo7D0j9wehsv/tieba-smallvideo/607272_bd5ec588760b7d8d2fc15183b95e628a.mp4")

        val map = mapOf("https://gss3.baidu.com/6LZ0ej3k1Qd3ote6lo7D0j9wehsv/tieba-smallvideo/607272_bd5ec588760b7d8d2fc15183b95e628a.mp4" to "test")
        presenter = JVideoViewPresenter(this, jv_video_main2, map)
        presenter?.subscribe()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.unSubscribe()
    }
}
