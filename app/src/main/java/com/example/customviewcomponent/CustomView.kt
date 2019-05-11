package com.example.customviewcomponent

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import java.util.*

class CustomView : View {

    private val SQUEEZE_SIZE = 100
    private lateinit var rectSquare: Rect
    private lateinit var paintSquare: Paint
    private lateinit var paintCircle: Paint
    private lateinit var imgBitmap: Bitmap


    private var squareColor = 0
    private var squareSize = 0
    private var circleX = 0f
    private var circleY = 0f
    private var circleRadius = 100f

    constructor(context: Context) : this (context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int)
            : super(context, attributeSet, defStyleAttr){
        attributeSet?.let {
            initView(attributeSet)
        }
    }

    private fun initView(attributeSet: AttributeSet?){
        rectSquare = Rect()
        paintSquare = Paint(Paint.ANTI_ALIAS_FLAG)

        paintCircle = Paint()
        paintCircle.isAntiAlias = true
        paintCircle.color = Color.parseColor("#00ccff")

        if (attributeSet == null){
            return
        }

        val ta = context.obtainStyledAttributes(attributeSet, R.styleable.CustomView)
        squareColor = ta.getColor(R.styleable.CustomView_square_color, Color.GREEN)
        squareSize = ta.getDimensionPixelSize(R.styleable.CustomView_square_size, SQUEEZE_SIZE)

        paintSquare.color = squareColor

        imgBitmap = BitmapFactory.decodeResource(resources, R.drawable.frame)

        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {

            override fun onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                } else {
                    viewTreeObserver.removeGlobalOnLayoutListener(this)
                }

                val padding = 50

                imgBitmap = getResizedBitmap(imgBitmap, width - padding, height - padding)

                Timer().scheduleAtFixedRate(object : TimerTask(){
                    override fun run() {
                        val newWidth = imgBitmap.width - 20
                        val newHeight = imgBitmap.height - 20

                        if (newWidth <= 0 || newHeight <= 0){
                            cancel()
                            return
                        }

                        imgBitmap = getResizedBitmap(imgBitmap, newWidth, newHeight)
                        postInvalidate()
                    }
                }, 2000L, 500L)
            }
        })

        ta.recycle()
    }

    fun swapColor(){
        if(paintSquare.color == Color.GREEN){
            paintSquare.color = Color.RED
        }else{
            paintSquare.color = Color.GREEN
        }

        postInvalidate()
    }

    override fun onDraw(canvas: Canvas?) {

        rectSquare.left = 10
        rectSquare.top = 10
        rectSquare.right = rectSquare.left + squareSize
        rectSquare.bottom = rectSquare.top + squareSize

        canvas?.drawRect(rectSquare, paintSquare)

        if (circleX == 0f || circleY == 0f) {
            circleX = (width / 2).toFloat()
            circleY = (height / 2).toFloat()
        }

        canvas?.drawCircle(circleX, circleY, circleRadius, paintCircle)

        val imageX = ((width - imgBitmap.width) / 2).toFloat()
        val imageY = ((width - imgBitmap.height) / 2).toFloat()


        canvas?.drawBitmap(imgBitmap, imageX, imageY, null)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val value = super.onTouchEvent(event)

        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y

                if (rectSquare.left < x && rectSquare.right > x){
                    if (rectSquare.top < y && rectSquare.bottom > y){
                        circleRadius += 10f
                        postInvalidate()
                    }
                }

                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val x  = event.x
                val y = event.y

                val dx = Math.pow((x - circleX).toDouble(), 2.0)
                val dy = Math.pow((y - circleY).toDouble(), 2.0)

                if (dx + dy < Math.pow(circleRadius.toDouble(), 2.0)){
                    circleX = x
                    circleY = y

                    postInvalidate()

                    return true
                }

                return value
            }
        }

        return value
    }

    private fun getResizedBitmap(bitmap: Bitmap, reqWidth: Int, reqHeight: Int): Bitmap{
        val matrix = Matrix()

        val src = RectF(0f,0f,bitmap.width.toFloat(), bitmap.height.toFloat())
        val dst = RectF(0f, 0f, reqWidth.toFloat(), reqHeight.toFloat())

        matrix.setRectToRect(src, dst,Matrix.ScaleToFit.CENTER)

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

}