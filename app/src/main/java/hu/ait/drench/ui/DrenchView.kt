package hu.ait.drench.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import hu.ait.drench.MainActivity
import hu.ait.drench.R
import hu.ait.drench.model.DrenchModel

class DrenchView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var size = DrenchModel.size

    private var paintBox = Paint()
    private var paintWinRect = Paint()
    private var paintLoseRect = Paint()
    private var paintText = Paint()

    init {
        paintBox.color = Color.parseColor("#FFFFFF")
        paintBox.style = Paint.Style.FILL

        paintWinRect.color = Color.parseColor("#00FF00")
        paintWinRect.style = Paint.Style.FILL

        paintLoseRect.color = Color.parseColor("#FF0000")
        paintLoseRect.style = Paint.Style.FILL

        paintText.color = Color.parseColor("#FFFFFF")
        paintText.textSize = 100f
        paintText.textAlign = Paint.Align.CENTER
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val d = if (w == 0) h else if (h == 0) w else if (w < h) w else h
        setMeasuredDimension(d, d)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawGameArea(canvas!!)
        if (DrenchModel.gameWon) {
            drawWinMessage(canvas)
        } else if (DrenchModel.gameOver) {
            drawLoseMessage(canvas)
        }
    }

    private fun drawGameArea(canvas: Canvas) {
        for (i in 0 until size) {
            for (j in 0 until size) {
                val box = DrenchModel.getBox(i, j)
                paintBox.color = (context as MainActivity).colors[box.color]
                canvas.drawRect(
                    (i * width / size).toFloat(),
                    (j * height / size).toFloat(),
                    ((i + 1) * width / size).toFloat(),
                    ((j + 1) * height / size).toFloat(),
                    paintBox
                )
            }
        }
    }

    private fun drawWinMessage(canvas: Canvas) {
        canvas.drawRect(
            0f + width.toFloat()/10f,
            0f + height.toFloat()/10f,
            width.toFloat() * 0.9f,
            height.toFloat() * 0.9f,
            paintWinRect
        )
        val textDrawHeight = height.toFloat()/2f + paintText.textSize/2f
        canvas.drawText(
            context.getString(R.string.click_here),
            width.toFloat()/2f,
            textDrawHeight - paintText.textSize,
            paintText
        )
        canvas.drawText(
            context.getString(R.string.to_continue_to),
            width.toFloat() / 2f,
            textDrawHeight,
            paintText
        )
        canvas.drawText(
            context.getString(R.string.next_level),
            width.toFloat() / 2f,
            textDrawHeight + paintText.textSize,
            paintText
        )
    }

    private fun drawLoseMessage(canvas: Canvas) {
        canvas.drawRect(
            0f + width.toFloat() / 10f,
            0f + height.toFloat() / 10f,
            width.toFloat() * 0.9f,
            height.toFloat() * 0.9f,
            paintLoseRect
        )
        val textDrawHeight = height.toFloat() / 2f + paintText.textSize / 2f
        canvas.drawText(
            context.getString(R.string.you_lose),
            width.toFloat() / 2f,
            textDrawHeight - paintText.textSize,
            paintText
        )
        canvas.drawText(
            context.getString(R.string.click_here),
            width.toFloat() / 2f,
            textDrawHeight,
            paintText
        )
        canvas.drawText(
            context.getString(R.string.to_try_again),
            width.toFloat() / 2f,
            textDrawHeight + paintText.textSize,
            paintText
        )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            if (DrenchModel.checkForWin()) {
                (context as MainActivity).resetGame()
            } else if (DrenchModel.gameOver) {
                DrenchModel.startingMoves = DrenchModel.STARTING_MOVES_INIT
                (context as MainActivity).resetGame()
            }
        }

        return super.onTouchEvent(event)
    }
}