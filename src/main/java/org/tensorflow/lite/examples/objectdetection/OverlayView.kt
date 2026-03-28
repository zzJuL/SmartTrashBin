package org.tensorflow.lite.examples.objectdetection

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import org.tensorflow.lite.task.vision.detector.Detection
import java.util.LinkedList

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: List<Detection> = LinkedList<Detection>()
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()

    private var scaleFactor: Float = 1f
    private var bounds = Rect()

    init {
        initPaints()
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.alpha = 160

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.bounding_box_color)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        for (result in results) {
            val boundingBox = result.boundingBox

            val top = boundingBox.top * scaleFactor
            val bottom = boundingBox.bottom * scaleFactor
            val left = boundingBox.left * scaleFactor
            val right = boundingBox.right * scaleFactor

            // 画识别框
            canvas.drawRect(left, top, right, bottom, boxPaint)

            // --- 核心修改：全英文垃圾分类逻辑 ---
            val originalLabel = result.categories[0].label
            val displayTag = when (originalLabel.lowercase()) {
                "bottle", "wine glass", "cup", "can" -> "RECYCLABLE: Plastic/Glass"
                "knife", "fork", "spoon" -> "RECYCLABLE: Metal"
                "banana", "apple", "sandwich", "orange", "broccoli" -> "KITCHEN WASTE: Organic"
                "cell phone", "laptop", "mouse", "keyboard" -> "HAZARDOUS: Electronic"
                "book", "paper" -> "RECYCLABLE: Paper"
                else -> "OTHER WASTE: General"
            }
            // ----------------------------------

            val drawableText = "$displayTag " + String.format("%.2f", result.categories[0].score)

            textPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()

            // 画文字背景
            canvas.drawRect(
                left,
                top,
                left + textWidth + 8,
                top + textHeight + 8,
                textBackgroundPaint
            )

            // 画全英文分类文字
            canvas.drawText(drawableText, left, top + textHeight, textPaint)
        }
    }

    fun setResults(
        detectionResults: MutableList<Detection>,
        outputHeight: Int,
        outputWidth: Int,
    ) {
        results = detectionResults
        scaleFactor = Math.max(width * 1f / outputWidth, height * 1f / outputHeight)
        invalidate()
    }
}