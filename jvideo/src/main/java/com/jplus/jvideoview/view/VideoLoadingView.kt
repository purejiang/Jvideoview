package com.jplus.jvideoview.view

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.jplus.jvideoview.R
import com.jplus.jvideoview.jvideo.JVideoCommon


/**
 * @author JPlus
 * @date 2019/12/6.
 */
class VideoLoadingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var mStrokeWidth = 0f
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private val mMinWidth by lazy {
        150
    }
    private val mMinHeight by lazy {
        mMinWidth
    }
    private var mPaint: Paint? = null

    private var mOneFiveBarTop = 0f
    private var mTwoFourBarTop = 0f
    private var mThreeBarTop = 0f
    private var mRate = 0L

    private var mAnimatorSet:AnimatorSet?=null
    init {
        initLoadingView(context, attrs)
    }

    private fun initLoadingView(context: Context, attrs: AttributeSet) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.VideoLoadingView)
        val color = typeArray.getColor(
            R.styleable.VideoLoadingView_lines_color,
            ContextCompat.getColor(context, R.color.video_play_color)
        )
        mRate = typeArray.getInt(R.styleable.VideoLoadingView_rate,
            1000).toLong()
        mPaint = Paint()
        //第一个进度条画笔
        mPaint?.initPaint(color)

        //释放资源
        typeArray.recycle()
    }

    fun setColor(color: Int) {
        mPaint?.initPaint(color)
    }
    private fun Paint.initPaint(color: Int) {
        this.let {
            it.color = color
            it.isAntiAlias = true
            it.style = Paint.Style.STROKE
            it.strokeWidth = mStrokeWidth
            it.strokeJoin = Paint.Join.ROUND  //画笔结合处为圆弧
            it.strokeCap = Paint.Cap.ROUND  //画笔始末端（线帽）样式为圆形
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
        Log.d(JVideoCommon.TAG, "mWidth:$mWidth, mHeight:$mHeight,mRate$mRate")
        if (mStrokeWidth == 0f) {
            mStrokeWidth = mWidth / 9f
            mPaint?.strokeWidth = mStrokeWidth
        }
        start()
    }

    private fun start(){
        val oneHeight = mHeight * 3f / 7f

        val valueAnimator = ValueAnimator.ofFloat(0f, oneHeight - mStrokeWidth)
        valueAnimator.duration = mRate
        valueAnimator.addUpdateListener {
            val value = it.animatedValue as Float
            mThreeBarTop = value
            postInvalidate()
        }
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.repeatMode = ValueAnimator.REVERSE
        valueAnimator.repeatCount = ValueAnimator.INFINITE

        val valueAnimator2 = ValueAnimator.ofFloat(0f, oneHeight - mStrokeWidth)
        valueAnimator2.duration = mRate
        valueAnimator2.startDelay =  (mRate/2.5).toLong()
        valueAnimator2.addUpdateListener {
            val value = it.animatedValue as Float
            mTwoFourBarTop = value
            postInvalidate()
        }

        valueAnimator2.interpolator = LinearInterpolator()
        valueAnimator2.repeatMode = ValueAnimator.REVERSE
        valueAnimator2.repeatCount = ValueAnimator.INFINITE

        val valueAnimator3 = ValueAnimator.ofFloat(0f, oneHeight - mStrokeWidth)
        valueAnimator3.duration = mRate
        valueAnimator3.startDelay = (mRate/1.25).toLong()
        valueAnimator3.addUpdateListener {
            val value = it.animatedValue as Float
            mOneFiveBarTop = value
            postInvalidate()
        }

        valueAnimator3.interpolator = LinearInterpolator()
        valueAnimator3.repeatMode = ValueAnimator.REVERSE
        valueAnimator3.repeatCount = ValueAnimator.INFINITE

        mAnimatorSet = AnimatorSet()
        mAnimatorSet?.playTogether(valueAnimator, valueAnimator2, valueAnimator3)//同时执行
        mAnimatorSet?.start()
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val space = (mWidth - 6 * mStrokeWidth) / 4
        mPaint?.let { paint ->
            canvas?.drawLine(
                mStrokeWidth,
                mOneFiveBarTop + mStrokeWidth,
                mStrokeWidth,
                mHeight - mOneFiveBarTop - mStrokeWidth,
                paint
            )
            canvas?.drawLine(
                2 * mStrokeWidth + space,
                mTwoFourBarTop + mStrokeWidth,
                2 * mStrokeWidth + space,
                mHeight - mStrokeWidth - mTwoFourBarTop,
                paint
            )
            canvas?.drawLine(
                3 * mStrokeWidth + 2 * space,
                mThreeBarTop + mStrokeWidth,
                3 * mStrokeWidth + 2 * space,
                mHeight - mThreeBarTop - mStrokeWidth,
                paint
            )
            canvas?.drawLine(
                4 * mStrokeWidth + 3 * space,
                mTwoFourBarTop + mStrokeWidth,
                4 * mStrokeWidth + 3 * space,
                mHeight - mTwoFourBarTop - mStrokeWidth,
                paint
            )
            canvas?.drawLine(
                5 * mStrokeWidth + 4 * space,
                mOneFiveBarTop + mStrokeWidth,
                5 * mStrokeWidth + 4 * space,
                mHeight - mOneFiveBarTop - mStrokeWidth,
                paint
            )
        }
    }


    /**
     * 避免长时间重绘导致内存泄漏
     */
    fun close() {
        mAnimatorSet?.cancel()
    }

}