package hu.ait.drench.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import hu.ait.drench.MainActivity
import hu.ait.drench.R
import hu.ait.drench.model.DrenchModel
import kotlin.math.min

class ButtonView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var paintBackground: Paint = Paint()
    private var paintCircle: Paint = Paint()

    init {
        paintBackground.color = ContextCompat.getColor(context!!, R.color.sky_blue)
        paintBackground.style = Paint.Style.FILL

        paintCircle.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintBackground)
        val radius = min(width/6f, height/4f)
        for (i in 0 until 2) {
            for (j in 0 until 3) {
                paintCircle.color = (context as MainActivity).colors[i*3+j]
                canvas?.drawCircle(
                    j * (width / 3f) + radius,
                    i * (height / 2f) + radius,
                    radius*0.7f,
                    paintCircle
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN && !DrenchModel.gameOver) {
            val tX = event.x.toInt() / (width / 3)
            val tY = event.y.toInt() / (height / 2)

            if (tX < 3 && tY < 2) {
                DrenchModel.colorPicked(tY*3+tX)

                DrenchModel.movesLeft--
                (context as MainActivity).binding.tvMovesLeft.text =
                    DrenchModel.movesLeft.toString()

                (context as MainActivity).binding.drenchView.invalidate()
                if (DrenchModel.checkForWin()) {
                    Snackbar.make(
                        (context as MainActivity).binding.root,
                        context.getString(R.string.you_win),
                        Snackbar.LENGTH_LONG
                    ).show()
                    DrenchModel.gameWon = true
                    DrenchModel.startingMoves--
                } else if (DrenchModel.movesLeft <= 0) {
                    Snackbar.make(
                        (context as MainActivity).binding.root,
                        context.getString(R.string.you_lose_exclaim),
                        Snackbar.LENGTH_LONG
                    ).show()
                    DrenchModel.gameOver = true
                    (context as MainActivity).saveHighScore(
                        if (DrenchModel.startingMoves == 30) 30 else DrenchModel.startingMoves + 1,
                        false)
                    (context as MainActivity).uploadHighScore()
                    DrenchModel.startingMoves = DrenchModel.STARTING_MOVES_INIT
                }
            }
        }

        return super.onTouchEvent(event)
    }

}