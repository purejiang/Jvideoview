package com.jplus.jvideoviewtest

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_url.*
import java.io.UnsupportedEncodingException
import java.net.URLDecoder


class UrlActivity : AppCompatActivity() {
    private var mIsPause = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_url)
        showDialog()
        lv_loading.setOnClickListener {
            mIsPause = if(mIsPause){
                lv_loading.pause()
                false
            }else{
                lv_loading.play()
                true
            }
        }
        tv_test.typeface = Typeface.createFromAsset(assets, "iconfont.ttf");
        tv_test.text = resources.getString(R.string.normal_screen)
//        tp_play.setOnClickListener {
//            mIsPause = if(mIsPause){
//                tp_play.pause()
//                false
//            }else{
//                tp_play.play()
//                true
//            }
//        }
//        val text = "\n\u3000\u3000贝斯（金妮弗·古德温 Ginnifer Goodwin 饰）从小的梦想就是能够成为一名家庭主妇，如今她嫁给了罗伯特（山姆·贾格 Sam Jaeger 饰）为妻，总算是实现了理想。没想到第三者的出现将她美好的生活幻影撕成了碎片。\n                                    \n\u3000\u3000社交名媛西蒙尼（刘玉玲 饰）嫁给了非常疼爱她的卡尔（杰克·达文波特 Jack Davenport 饰），哪知道竟然在偶然之中发现卡尔竟然是一名同性恋者，在守住秘密和守住尊严之间，西蒙尼必须做出选择。\n                                    \n\u3000\u3000泰勒（柯尔比·豪威尔-巴普蒂斯特 Kirby Howell-Baptiste 饰）和埃里（瑞德·斯科特 Reid Scott 饰）正在进行一场开放式婚姻，泰勒的情人杰德（亚历珊德拉·达达里奥 Alexandra Daddario 饰）的出现让埃里开始觉得，在他们两个人里面再加一个人似乎也是个不错的主意。\n                        "
//        tv_test.text = text
    }
    @Throws(UnsupportedEncodingException::class)
    fun urlDecode(content:String, charsetName:String ="UTF-8"):String{
        return if(content.isNotEmpty()) URLDecoder.decode(content, charsetName) else ""
    }
    private fun showDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.view_url_dialog, null, false)
        val edt = view?.findViewById<EditText>(R.id.edt_url)
        edt?.setText(
            "https://bili.let-1977cdn.com/20190902/MJZBTF8k/index.m3u8\nhttps://media.w3.org/2010/05/sintel/trailer.mp4\nhttp://www.w3school.com.cn/example/html5/mov_bbb.mp4"
        )

        AlertDialog.Builder(this)
            .setTitle("播放地址")
            .setView(view)
            .setMessage("请输入要播放的地址：")
            .setPositiveButton("确认") { dialog, _ ->
                edt?.text?.let {
                    dialog.dismiss()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("urls", it.toString())
                    startActivity(intent)
                }

            }.setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

    }
    override fun onResume() {
        super.onResume()
        lineAnimator()
    }

    private fun lineAnimator() {
//        val rootView = (this.findViewById(android.R.id.content) as ViewGroup).getChildAt(0)
//        val width = ly_url_test.width
//        val height = ly_url_test.height
//        Log.d(JvCommon.TAG, "rootView:${rootView},height$height")
//
//        val animator = ValueAnimator.ofInt(500, 530, 500)
//        animator.duration = 2000
//        animator.addUpdateListener {
//            val y = it.animatedValue as Int
//            val x = width / 2
//            moveView(lv_loading, x, y)
//        }
//        animator.interpolator = LinearInterpolator()
//        animator.repeatCount = ValueAnimator.INFINITE
//        animator.repeatMode = ValueAnimator.INFINITE
//        animator.start()
//        lv_loading.startAnimator()
    }

    //定义一个修改ImageView位置的方法
    private fun moveView(view: View, rawX: Int, rawY: Int) {
        val left = rawX + view.width / 2
        val top = rawY - view.height
        val width = left + view.width
        val height = top + view.height
        view.layout(left, top, width, height)
    }

}
