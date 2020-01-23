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

class StepLineSqBouncyView(ctx : Context) : View(ctx) {


    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SLSBNode(var i : Int, private val state : State = State()) {

        private var next : SLSBNode? = null
        private var prev : SLSBNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = SLSBNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSLSNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SLSBNode {
            var curr : SLSBNode? = next
            if (dir == -1) {
                curr = prev
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class StepBouncyLine(var i : Int) {

        private val root : SLSBNode = SLSBNode(0)
        private var curr : SLSBNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }


    data class Renderer(var view : StepLineSqBouncyView) {

        private val animator : Animator = Animator(view)
        private val sbl : StepBouncyLine = StepBouncyLine(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            sbl.draw(canvas, paint)
            animator.animate {
                sbl.update {
                    animator.stop()
                }
            }
        }

        fun handleTap(cb : () -> Unit) {
            sbl.startUpdating {
                animator.start()
            }
        }
    }
}