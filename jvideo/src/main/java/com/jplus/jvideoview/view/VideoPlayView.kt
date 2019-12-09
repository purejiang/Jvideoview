package com.jplus.jvideoview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.jplus.jvideoview.R
import kotlin.math.sqrt

/**
 * @author JPlus
 * @date 2019/11/6.
 */
class VideoPlayView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var mPlayPaint: Paint? = null
    private var mPlayPath: Path? = null
    private var mCirclePath: Path? = null
    private var mCirclePaint: Paint? = null
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
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.VideoPlayView)
        val circleColor = typeArray.getColor(
            R.styleable.VideoPlayView_circle_color,
            ContextCompat.getColor(context, R.color.video_play_color)
        )
        val playColor = typeArray.getColor(
            R.styleable.VideoPlayView_play_color,
            ContextCompat.getColor(context, R.color.video_play_color)
        )
        val strokeWidth = typeArray.getFloat(R.styleable.VideoPlayView_stroke_width, 0f)
        mStrokeWidth = strokeWidth
        mPlayPaint = Paint()
        mPlayPaint?.let {
            it.isAntiAlias = true
            it.style = Paint.Style.STROKE
            it.strokeWidth = strokeWidth
            it.strokeJoin = Paint.Join.ROUND
            it.strokeCap = Paint.Cap.ROUND
            it.color = playColor
            it.strokeJoin = Paint.Join.ROUND //结合处为圆角
            it.strokeCap = Paint.Cap.ROUND  // 设置转弯处为圆角
        }
        mCirclePaint = Paint()
        mCirclePaint?.let {
            it.isAntiAlias = true
            it.style = Paint.Style.STROKE
            it.strokeWidth = strokeWidth
            it.strokeJoin = Paint.Join.ROUND
            it.strokeCap = Paint.Cap.ROUND
            it.color = circleColor
            it.strokeJoin = Paint.Join.ROUND //结合处为圆角
            it.strokeCap = Paint.Cap.ROUND  // 设置转弯处为圆角
        }

    }

    private fun initDrawView(r: Float) {
        if (mStrokeWidth == 0f) {
            mStrokeWidth = r/10
            mCirclePaint?.strokeWidth = r / 10f
            mPlayPaint?.strokeWidth = r / 10f
        }
        setDrawCircle(r, 120f, 300f)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(mIsPause){
            setDrawPlay(getRealWidthAndHeight(mWidth, mHeight) * 1f)
        }else{
            setDrawPause(getRealWidthAndHeight(mWidth, mHeight) * 1f)
        }
        canvas?.drawPath(mCirclePath!!, mPlayPaint!!)
        canvas?.drawPath(mPlayPath!!, mPlayPaint!!)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun setDrawPlay(r: Float) {
        mPlayPath = Path()
        mPlayPath?.let {
            //播放
            it.moveTo(mWidth / 2f - r / 6f, mHeight / 2f - r / 6) //移动到
            it.lineTo(mWidth / 2f - r / 6f, mHeight / 2f + r / 6)
            it.moveTo(mWidth / 2f + r / 6f, mHeight / 2f - r / 6) //移动到
            it.lineTo(mWidth / 2f + r / 6f, mHeight / 2f + r / 6)
        }
    }

    private fun setDrawPause(r: Float) {
        Log.d("pipa", "setDrawPause:$r")
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

    private fun setDrawCircle(r: Float, start: Float, sweep: Float) {
        Log.d("pipa", "setDrawCircle:$r")
        mCirclePath = Path()
        val rectF1 =
            RectF(
                mWidth / 2f - r / 2f + mStrokeWidth / 2f,
                mHeight / 2f - r / 2f + mStrokeWidth / 2f,
                mWidth / 2f + r / 2f - mStrokeWidth / 2f,
                mHeight / 2f + r / 2f - mStrokeWidth / 2f
            )
        mCirclePath?.addArc(rectF1, start, sweep)
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
        }else{
            mWidth = widthSize
            mHeight = heightSize
        }
        Log.d("pipa", "mWidth:$mWidth, mHeight:$mHeight")
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