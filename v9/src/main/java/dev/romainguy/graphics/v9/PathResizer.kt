/*
 * Copyright (C) 2022 Romain Guy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.romainguy.graphics.v9

import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import dev.romainguy.graphics.path.PathSegment
import dev.romainguy.graphics.path.iterator

class Slice(val start: Float, val end: Float) {
    val size = end - start

    init {
        require(start <= end) {
            """|Invalid slice, start must be <= end:
               |    start = $start
               |    end   = $end
            """.trimMargin()
        }
    }

    override fun toString(): String = "Slice(start=$start, end=$end)"
}

fun Slices(slices: Rect) = Slices(
    slices.left.toFloat(),
    slices.top.toFloat(),
    slices.right.toFloat(),
    slices.bottom.toFloat()
)

fun Slices(left: Int, top: Int, right: Int, bottom: Int) = Slices(
    left.toFloat(),
    top.toFloat(),
    right.toFloat(),
    bottom.toFloat()
)

fun Slices(slices: RectF) = Slices(slices.left, slices.top, slices.right, slices.bottom)

fun Slices(left: Float, top: Float, right: Float, bottom: Float) = Slices(
    listOf(Slice(left, right)),
    listOf(Slice(top, bottom))
)

class Slices(verticalSlices: List<Slice>, horizontalSlices: List<Slice>) {
    val verticalSlices: List<Slice>
    private val verticalTotal: Float

    val horizontalSlices: List<Slice>
    private val horizontalTotal: Float

    init {
        require(verticalSlices.isNotEmpty()) { "At least 1 vertical slice is required" }
        require(horizontalSlices.isNotEmpty()) { "At least 1 horizontal slice is required" }

        // TODO: merge overlapping/connected slices
        this.verticalSlices = verticalSlices.filter { it.size > 0 }.sortedBy { it.start }
        this.verticalTotal = this.verticalSlices.sumOf { it.size.toDouble() }.toFloat()

        this.horizontalSlices = horizontalSlices.filter { it.size > 0 }.sortedBy { it.start }
        this.horizontalTotal = this.horizontalSlices.sumOf { it.size.toDouble() }.toFloat()
    }

    override fun toString(): String =
        "Slices(verticalSlices=$verticalSlices, horizontalSlices=$horizontalSlices)"
}

fun Path.slice(slices: Slices) = PathResizer(this, slices)

class PathResizer(val path: Path, val slices: Slices) {
    val bounds: RectF = RectF()

    private val segments: List<PathSegment>

    private var stretchableWidth = 0.0f
    private var stretchableHeight = 0.0f

    init {
        path.computeBounds(bounds, true)

        val iterator = path.iterator()
        val filteredSegments = ArrayList<PathSegment>(iterator.rawSize())

        // TODO: optimize using a large array and next(FloatArray, Int)
        while (iterator.hasNext()) {
            iterator.next().apply { if (type != PathSegment.Type.Done) filteredSegments.add(this) }
        }

        segments = filteredSegments

        for (slice in slices.verticalSlices) stretchableWidth += slice.size
        for (slice in slices.horizontalSlices) stretchableHeight += slice.size
    }

    fun resize(width: Float, height: Float, dstPath: Path = Path()): Path {
        require(width >= bounds.width()) {
            """|The destination width must be >= original path width:
               |    destination width = $width
               |    source path width = ${bounds.width()}
            """.trimMargin()
        }
        require(height >= bounds.height()) {
            """|The destination height must be >= original path height:
               |    destination height = $height
               |    source path height = ${bounds.height()}
            """.trimMargin()
        }

        dstPath.rewind()

        val stretchX = (width - bounds.width()) / stretchableWidth
        val stretchY = (height - bounds.height()) / stretchableHeight

        // TODO: optimize!
        // TODO: only offset control points when end point moves
        for (segment in segments) {
            val points = segment.points
            when (segment.type) {
                PathSegment.Type.Move -> dstPath.moveTo(
                    offset(points[0].x, slices.verticalSlices, stretchX),
                    offset(points[0].y, slices.horizontalSlices, stretchY)
                )
                PathSegment.Type.Line -> dstPath.lineTo(
                    offset(points[1].x, slices.verticalSlices, stretchX),
                    offset(points[1].y, slices.horizontalSlices, stretchY)
                )
                PathSegment.Type.Quadratic -> dstPath.quadTo(
                    offset(points[1].x, slices.verticalSlices, stretchX),
                    offset(points[1].y, slices.horizontalSlices, stretchY),
                    offset(points[2].x, slices.verticalSlices, stretchX),
                    offset(points[2].y, slices.horizontalSlices, stretchY)
                )
                PathSegment.Type.Conic -> {
                    // Cannot happen since we convert conics to quadratics
                }
                PathSegment.Type.Cubic -> dstPath.cubicTo(
                    offset(points[1].x, slices.verticalSlices, stretchX),
                    offset(points[1].y, slices.horizontalSlices, stretchY),
                    offset(points[2].x, slices.verticalSlices, stretchX),
                    offset(points[2].y, slices.horizontalSlices, stretchY),
                    offset(points[3].x, slices.verticalSlices, stretchX),
                    offset(points[3].y, slices.horizontalSlices, stretchY)
                )
                PathSegment.Type.Close -> dstPath.close()
                else -> { }
            }
        }

        return dstPath
    }

    // TODO: optimize with summed table
    private fun offset(position: Float, slices: List<Slice>, stretch: Float): Float {
        var offsetPosition = position
        for (slice in slices) {
            if (position > slice.start) {
                var offset = slice.size * stretch
                if (position <= slice.end) {
                    offset *= (position - slice.start) / slice.size
                }
                offsetPosition += offset
            }
        }
        return offsetPosition
    }
}
