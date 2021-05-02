package com.example.kidsdrawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context,attrs: AttributeSet): View(context, attrs)
{

    //the path which is drawn on the canvas(A variable of class customPath to use it further
    private var mDrawPath: CustomPath? = null

    //creating an instance of bitmap(Bitmap is an Image made of pixels)
    private var mCanvasBitmap: Bitmap? = null

    //The Paint class holds the style and color information about how to draw geometries, text and bitmaps.
    private var mDrawPaint: Paint? = null

    //an instance of canvas paint view
    private var mCanvasPaint: Paint? = null

    //the size of the brush or thickness of the brush/stroke on canvas
    private var mBrushSize: Float = 0.toFloat()

    //color of the lines or stroke
    private var color = Color.BLACK

    //
    private val mUndoPaths = ArrayList<CustomPath>()

    /**
     * A variable for canvas which will be initialized later and used.
     *
     *The Canvas class holds the "draw" calls. To draw something, you need 4 basic components: A Bitmap to hold the pixels, a Canvas to host
     * the draw calls (writing into the bitmap), a drawing primitive (e.g. Rect,
     * Path, text, Bitmap), and a paint (to describe the colors and styles for the
     * drawing)
     */
    private var canvas: Canvas? = null

    //the collection of all the lines or specifically paths which will be drawn on screen(An array list for paths)
    private val mPaths = ArrayList<CustomPath>()

    //Initializing the properties of the view when created
    init
    {
        setupDrawing()
    }

    //the function which is used to initialize the properties
    private fun setupDrawing()
    {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color,mBrushSize)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND

    }

    fun onClickUndo(){
        if(mPaths.size>=0){
            mUndoPaths.add(mPaths.removeAt(mPaths.size - 1))
            invalidate()
        }
    }

    //this function is called when the size of the view will change
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mCanvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for(path in mPaths)
        {
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas.drawPath(path,mDrawPaint!!)
        }

        canvas.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)
        if(!mDrawPath!!.isEmpty)
        {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action)
        {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize

                mDrawPath!!.reset()
                mDrawPath!!.moveTo(touchX!!,touchY!!)
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.lineTo(touchX,touchY)
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(color,mBrushSize)
            }

            else ->{
                return false
            }
        }
        invalidate()

        return true
    }

    fun setSizeForBrush(newSize: Float)
    {
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize,resources.displayMetrics)

        mDrawPaint!!.strokeWidth = mBrushSize
        
    }

    fun setColor(newColor: String)
    {
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color

    }

    internal inner class CustomPath(var color: Int,
                                    var brushThickness: Float): android.graphics.Path()
    {
        
    }
}
