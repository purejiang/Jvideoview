package com.jplus.jvideoview.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.jplus.jvideoview.R
import kotlin.math.sqrt

/**
 * @author JPlus
 * @date 2019/11/6.
 */
class TransPlayView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var mPlayPaint: Paint? = null
    private var mPlayPath: Path? = null
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var mIsPause = false

    private val mMinWidth by lazy {
        150
    }
    private val mMinHeight by lazy {
        mMinWidth
    }
    private var mStrokeWidth = 0f

    init {
        initPlayView(context, attrs)
    }

    private fun initPlayView(context: Context, attrs: AttributeSet) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.TransPlayView)
        mStrokeWidth = typeArray.getFloat(R.styleable.TransPlayView_stroke, 0f)
        initPaint(context, typeArray)
        //释放资源
        typeArray.recycle()
    }

    private fun initPaint(context: Context, typeArray:TypedArray) {
        val playColor = typeArray.getColor(
            R.styleable.TransPlayView_color,
            ContextCompat.getColor(context, R.color.video_play_transparent)
        )
        //初始化画笔
        playColor.let{
            mPlayPaint = mPlayPaint?:Paint()
            mPlayPaint?.init(it)
        }

    }
    private fun Paint.init(color: Int) {
        this.let {
            it.isAntiAlias = true
            it.style = Paint.Style.STROKE
            it.strokeWidth = strokeWidth
            it.strokeJoin = Paint.Join.ROUND
            it.strokeCap = Paint.Cap.ROUND
            it.color = color
            it.strokeJoin = Paint.Join.ROUND //结合处为圆角
            it.strokeCap = Paint.Cap.ROUND  // 设置转弯处为圆角
        }
    }

    private fun initDrawView(max: Float) {
        if (mStrokeWidth == 0f) {
            mStrokeWidth = max / 10
            mPlayPaint?.strokeWidth = max / 12f
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (mIsPause) {
            setDrawPlay(getRealWidthAndHeight(mWidth, mHeight) * 1f)
        } else {
            setDrawPause(getRealWidthAndHeight(mWidth, mHeight) * 1f)
        }

        mPlayPath?.let {
            mPlayPaint?.let{paint->
                canvas?.drawPath(it, paint)
            }
        }
    }

    private fun setDrawPlay(max: Float) {
        mPlayPath = Path()
        mPlayPath?.let {
            //播放
            it.moveTo(mWidth / 2f - max / 6f, mHeight / 2f - max / 6) //移动到
            it.lineTo(mWidth / 2f - max / 6f, mHeight / 2f + max / 6)
            it.moveTo(mWidth / 2f + max / 6f, mHeight / 2f - max / 6) //移动到
            it.lineTo(mWidth / 2f + max / 6f, mHeight / 2f + max / 6)
        }
    }

    private fun setDrawPause(r: Float) {
        mPlayPath = Path()
        mPlayPath?.let {
            //暂停
            val ran = r / 4f
            val len = ran * 3 / sqrt(3f)
            it.moveTo(mWidth / 2f - ran / 2f, mHeight / 2f)
            it.quadTo(mWidth / 2f - ran / 2f, mHeight / 2f - len / 2f, mWidth / 2f + ran / 4f, mHeight / 2f - len / 4f)

            it.moveTo(mWidth / 2f - ran / 2f, mHeight / 2f)
            it.quadTo(mWidth / 2f - ran / 2f, mHeight / 2f + len / 2f, mWidth / 2f + ran / 4f, mHeight / 2f + len / 4f)

            it.moveTo(mWidth / 2f + ran / 4f, mHeight / 2f - len / 4f)
            it.quadTo(mWidth / 2f + ran, mHeight / 2f, mWidth / 2f + ran / 4f, mHeight / 2f + len / 4f)
        }
    }


    private fun getRealWidthAndHeight(widthSize: Int, heightSize: Int): Int {
        //获取各个边距的padding值
        return if (widthSize < heightSize) {
            widthSize - paddingLeft - paddingRight
        } else {
            heightSize - paddingTop - paddingBottom
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        //处理wrap_contentde情况
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            mWidth = mMinWidth
            mHeight = mMinHeight
            setMeasuredDimension(mMinWidth, mMinHeight)
        } else if (widthMode == MeasureSpec.AT_MOST) {
            mWidth = mMinWidth
            mHeight = heightSize
            setMeasuredDimension(mMinWidth, heightSize)
        } else if (heightMode == MeasureSpec.AT_MOST) {
            mWidth = widthSize
            mHeight = mMinHeight
            setMeasuredDimension(widthSize, mMinHeight)
        } else {
            mWidth = widthSize
            mHeight = heightSize
        }
        initDrawView(getRealWidthAndHeight(mWidth, mHeight) * 1f)
    }

    fun pause() {
        mIsPause = false
        invalidate()
    }

    fun play() {
        mIsPause = true
        invalidate()
    }


}