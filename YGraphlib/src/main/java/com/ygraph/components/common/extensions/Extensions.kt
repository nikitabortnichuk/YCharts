package com.ygraph.components.common.extensions

import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.ygraph.components.common.model.Point
import com.ygraph.components.graph.linegraph.model.GridLines
import java.text.DecimalFormat


/**
return the width of text in canvas drawn text
 */
fun String.getTextWidth(paint: Paint): Float {
    return paint.measureText(this)
}

/**
return the height of text in canvas drawn text
 */
fun String.getTextHeight(paint: Paint): Int {
    val bounds = Rect()
    paint.getTextBounds(
        this,
        0,
        this.length,
        bounds
    )
    return bounds.height()
}

/**
return the shape that is used to mask a particular area for given leftPadding & rightPadding
 */
internal class RowClip(
    private val leftPadding: Float,
    private val rightPadding: Dp,
    private val topPadding: Float = 0f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rectangle(
            androidx.compose.ui.geometry.Rect(
                leftPadding,
                topPadding,
                size.width - rightPadding.value * density.density,
                size.height
            )
        )
    }
}

fun Any?.isNotNull() = this != null


/**
 * returns the background rect for the highlighted text.
 * @param x : X point.
 * @param y: Y point.
 * @param text: Text to be drawn inside the background.
 * @param paint: Background paint.
 */
fun getTextBackgroundRect(
    x: Float,
    y: Float,
    text: String,
    paint: TextPaint
): Rect {
    val fontMetrics = paint.fontMetrics
    val textLength = paint.measureText(text)
    return Rect(
        (x - (textLength / 2)).toInt(),
        (y + fontMetrics.top).toInt(),
        (x + (textLength / 2)).toInt(),
        (y + fontMetrics.bottom).toInt()
    )
}

/**
return the maximum and minimum points of X axis
 */
fun getXMaxAndMinPoints(
    points: List<Point>,
): Pair<Float, Float> {
    val xMin = points.minOf { it.x }
    val xMax = points.maxOf { it.x }
    return Pair(xMin, xMax)
}


/**
 * @param points List of points
return the maximum and minimum points of Y axis
 */
fun getYMaxAndMinPoints(
    points: List<Point>,
): Pair<Float, Float> {
    if (points.isEmpty())
        return Pair(0f, 0f)
    val xMin = points.minOf { it.y }
    val xMax = points.maxOf { it.y }
    return Pair(xMin, xMax)
}

/**
 * @param yMax Maximum value in the Y axis
 * @param yStepSize size of one step in the Y axis
return the maximum value of Y axis
 */
fun getMaxElementInYAxis(yMax: Float, yStepSize: Int): Int {
    var reqYLabelsQuo =
        (yMax / yStepSize)
    val reqYLabelsRem = yMax.rem(yStepSize)
    if (reqYLabelsRem > 0f) {
        reqYLabelsQuo += 1
    }
    return reqYLabelsQuo.toInt() * yStepSize
}


fun Offset.isDragLocked(dragOffset: Float, xOffset: Float) =
    ((dragOffset) > x - xOffset / 2) && ((dragOffset) < x + xOffset / 2)


/**
 * @param tapOffset Tapped offset
 * @param xOffset in the X axis
 * @param bottom bottom Value
return true if the point is selected
 */
fun Offset.isTapped(tapOffset: Offset, xOffset: Float, bottom: Float, tapPadding: Float) =
    ((tapOffset.x) > x - (xOffset + tapPadding) / 2) && ((tapOffset.x) < x + (xOffset + tapPadding) / 2) &&
            ((tapOffset.plus(Offset(0f, tapPadding))).y > y) && ((tapOffset.y) < bottom)


/**
 * Returns true if the tapped point is withing the given boundries else false
 * @param tapOffset Tapped offset
 * @param tapPadding plus or minus padding from the point or clickable padding
 */
fun Offset.isPointTapped(tapOffset: Offset, tapPadding: Float) =
    ((tapOffset.x) > x - tapPadding) && ((tapOffset.x) < x + tapPadding) &&
            ((tapOffset.plus(Offset(0f, tapPadding))).y > y) &&
            ((tapOffset.minus(Offset(0f, tapPadding))).y < y)


/***
 * Returns converted single precision string from float value
 */
fun Float.formatToSinglePrecision(): String = DecimalFormat("#.#").format(this)


/**
 *
 * DrawScope.drawGridLines is the extension method used to draw the grid lines on any graph
 * @param yBottom : Bottom value for Y-Axis
 * @param top: Top value for Y axis
 * @param xLeft: Total left padding in X-Axis.
 * @param paddingRight : Total right padding.
 * @param scrollOffset : Total scroll offset.
 * @param verticalPointsSize : Total points in the X-Axis.
 * @param xZoom : Total zoom offset.
 * @param xAxisScale: Scale of each point in X-Axis.
 * @param ySteps : Number of steps in y-Axis.
 * @param xAxisStepSize: The size of each X-Axis step.
 * @param gridLines: Data class to handle styling related to grid lines.
 */
fun DrawScope.drawGridLines(
    yBottom: Float,
    top: Float,
    xLeft: Float,
    paddingRight: Dp,
    scrollOffset: Float,
    verticalPointsSize: Int,
    xZoom: Float,
    xAxisScale: Float,
    ySteps: Int,
    xAxisStepSize: Dp,
    gridLines: GridLines
) {
    val availableHeight = yBottom - top
    val steps = ySteps + 1 // Considering 0 as step
    val gridOffset = availableHeight / if (steps > 1) steps - 1 else 1
    // Should start from 1 as we don't consider the XAxis
    if (gridLines.enableHorizontalLines) {
        (1 until steps).forEach {
            val y = yBottom - (it * gridOffset)
            gridLines.drawHorizontalLines(this, xLeft, y, size.width - paddingRight.toPx())
        }
    }
    if (gridLines.enableVerticalLines) {
        var xPos = xLeft - scrollOffset
        (0 until verticalPointsSize).forEach { _ ->
            gridLines.drawVerticalLines(this, xPos, yBottom, top)
            xPos += ((xAxisStepSize.toPx() * (xZoom * xAxisScale)))
        }
    }
}
