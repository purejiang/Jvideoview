package com.jplus.jvideoview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import com.jplus.jvideoview.R
import android.view.View
import androidx.core.content.ContextCompat
import android.graphics.RectF
import kotlin.math.sqrt


/**
 * @author Jplus
 * @date 2019/10/29.
 */
class VideoLoadingProgress(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var mPaint:Paint?=null
    private var mPath: Path?=null

    init {
        initView()
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        //位置
        super.layout(l, t, r, b)
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //宽高
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
    override fun onDraw(canvas: Canvas?) {
        //绘制
        super.onDraw(canvas)
        val left = 400f
        val right = 600f - 50f*sqrt(2f)

        mPath?.moveTo(left, 300f - 50f*sqrt(2f))
        mPath?.quadTo(left + 25f*sqrt(2f), 100f, left + 100f*sqrt(2f), 300f - 50f*sqrt(2f))

        mPath?.moveTo(left, 300f - 50f*sqrt(2f))
        mPath?.quadTo(left + 25f*sqrt(2f), 100f, left + 100f*sqrt(2f), 300f - 50f*sqrt(2f))

        val rectF = RectF(400f, 200f, 600f, 400f)
        mPath?.addArc(rectF, -45f, 270f)
        //绘制
        canvas?.drawPath(mPath, mPaint!!)
    }

    private fun initView() {
        mPaint = Paint()
        mPath = Path()

        mPaint?.let{
            it.isAntiAlias = true
            it.style = Paint.Style.STROKE
            it.strokeWidth = 20f
            it.strokeJoin = Paint.Join.ROUND
            it.strokeCap = Paint.Cap.ROUND
            it.color = ContextCompat.getColor(context, R.color.seek_bar_progress)
            it.strokeJoin = Paint.Join.ROUND //结合处为圆角
            it.strokeCap = Paint.Cap.ROUND  // 设置转弯处为圆角
        }


    }
}