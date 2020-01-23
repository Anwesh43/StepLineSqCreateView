package com.anwesh.uiprojects.steplinesqbouncyview

/**
 * Created by anweshmishra on 24/01/20.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.app.Activity
import android.content.Context

val nodes : Int = 5
val lines : Int = 4
val scGap : Float = 0.02f / lines
val strokeFactor : Int = 90
val foreColor : Int = Color.parseColor("#3F51B5")
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20
val fullDeg : Float = 360f
val sizeFactor : Float = 2.9f
val miniSquareFactor : Float = 3f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(Math.PI * this).toFloat()

fun Canvas.drawStepLineSquare(i : Int, scale : Float, size : Float, paint : Paint) {
    val sf = scale.sinify().divideScale(i, lines)
    val deg : Float = fullDeg / nodes
    val m : Float = size / miniSquareFactor
    val x : Float = 2 * size * sf
    save()
    rotate(deg * i)
    drawLine(-size, -size, x, -size, paint)
    save()
    translate(-size + x, -size)
    drawRect(RectF(-m, -m, m, m), paint)
    restore()
    restore()
}

fun Canvas.drawStepLineSquares(scale : Float, size : Float, paint : Paint) {
    for (j in 0..(lines - 1)) {
        drawStepLineSquare(j, scale, size, paint)
    }
}

fun Canvas.drawSLSNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    translate(w / 2, gap * (i + 1))
    drawStepLineSquares(scale, size, paint)
    restore()
}
