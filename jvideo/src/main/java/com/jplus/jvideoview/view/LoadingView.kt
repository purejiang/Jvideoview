package com.jplus.jvideoview.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.jplus.jvideoview.R


/**
 * @author Jplus
 * @date 2019/10/29.
 */
class LoadingView(context: Context?, attrs: AttributeSet?) : View(context, attrs),ValueAnimator.AnimatorUpdateListener {
//    private var

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }


    override fun onAnimationUpdate(animation: ValueAnimator?) {

    }

}

