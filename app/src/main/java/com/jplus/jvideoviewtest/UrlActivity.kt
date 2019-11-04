package com.jplus.jvideoviewtest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.jplus.jvideoview.data.Video

class UrlActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_url)
        showDialog()
    }
    private fun showDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.view_url_dialog, null, false)
        val edt = view?.findViewById<EditText>(R.id.edt_url)
        edt?.setText(
                "https://cn5.download05.com/hls/20190804/ebfd96741e2e6e854144b2e012c30755/1564883639/index.m3u8\n" +
                "https://cn5.download05.com/hls/20190811/fa9d03cdb6881fb674fe5d85ae55efa5/1565487765/index.m3u8\n")

        AlertDialog.Builder(this)
            .setTitle("播放地址")
            .setView(view)
            .setMessage("请输入要播放的地址：")
            .setPositiveButton("确认") { dialog, _ ->
                edt?.text?.let{
                    dialog.dismiss()
                    val intent =Intent(this, MainActivity::class.java)
                    intent.putExtra("urls", it.toString())
                    startActivity(intent)
                }
            }.setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }.show()

    }
}
