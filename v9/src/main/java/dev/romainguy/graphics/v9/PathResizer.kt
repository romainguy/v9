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
import android.graphics.PointF
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Slice

        if (start != other.start) return false
        if (end != other.end) return false

        return true
    }

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + end.hashCode()
        return result
    }
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
    val verticalSlices: Array<Slice>
    private val verticalTotal: Float

    val horizontalSlices: Array<Slice>
    private val horizontalTotal: Float

    init {
        require(verticalSlices.isNotEmpty()) { "At least 1 vertical slice is required" }
        require(horizontalSlices.isNotEmpty()) { "At least 1 horizontal slice is required" }

        // TODO: merge overlapping/connected slices
        this.verticalSlices = verticalSlices
            .filter { it.size > 0 }
            .sortedBy { it.start }
            .toTypedArray()
        this.verticalTotal = this.verticalSlices.sumOf { it.size.toDouble() }.toFloat()

        this.horizontalSlices = horizontalSlices
            .filter { it.size > 0 }
            .sortedBy { it.start }
            .toTypedArray()
        this.horizontalTotal = this.horizontalSlices.sumOf { it.size.toDouble() }.toFloat()
    }

    override fun toString(): String {
        return "Slices(" +
                "verticalSlices=${verticalSlices.contentToString()}, " +
                "horizontalSlices=${horizontalSlices.contentToString()})"
    }
}

fun Path.slice(slices: Slices) = PathResizer(this, slices)

class PathResizer(val path: Path, val slices: Slices) {
    val bounds: RectF = RectF()

    private val segments: ArrayList<PathSegment>

    // We only need 3 points but it makes an algorithm easier later
    private val points = Array(4) { PointF(0.0f, 0.0f) }

    private var stretchableWidth = 0.0f
    private var stretchableHeight = 0.0f

    init {
        path.computeBounds(bounds, true)

        val iterator = path.iterator()

        segments = ArrayList(iterator.rawSize())
        // TODO: optimize using a large array and next(FloatArray, Int)
        //       even if we waste 8 floats per segment, we should get better locality
        //       compared to PathSegment instances with arrays of PointF
        for (segment in iterator) {
            if (segment.type != PathSegment.Type.Done) segments.add(segment)
        }

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

        for (i in 0 until segments.size) {
            val segment = segments[i]
            val offsetPositions = points
            when (segment.type) {
                PathSegment.Type.Move -> {
                    offset(segment.points, 0, 0, offsetPositions, slices, stretchX, stretchY)
                    dstPath.moveTo(offsetPositions[0].x, offsetPositions[0].y)
                }
                PathSegment.Type.Line -> {
                    offset(segment.points, 1, 1, offsetPositions, slices, stretchX, stretchY)
                    dstPath.lineTo(offsetPositions[1].x, offsetPositions[1].y)
                }
                PathSegment.Type.Quadratic -> {
                    offset(segment.points, 1, 2, offsetPositions, slices, stretchX, stretchY)
                    dstPath.quadTo(
                        offsetPositions[1].x, offsetPositions[1].y,
                        offsetPositions[2].x, offsetPositions[2].y
                    )
                }
                PathSegment.Type.Conic -> {
                    // Cannot happen since we convert conics to quadratics
                }
                PathSegment.Type.Cubic -> {
                    offset(segment.points, 1, 3, offsetPositions, slices, stretchX, stretchY)
                    dstPath.cubicTo(
                        offsetPositions[1].x, offsetPositions[1].y,
                        offsetPositions[2].x, offsetPositions[2].y,
                        offsetPositions[3].x, offsetPositions[3].y
                    )
                }
                PathSegment.Type.Close -> dstPath.close()
                else -> { }
            }
        }

        return dstPath
    }
}

private fun offset(
    positions: Array<PointF>,
    startPosition: Int,
    endPosition: Int,
    offsetPositions: Array<PointF>,
    slices: Slices,
    stretchX: Float,
    stretchY: Float
) {
    for (i in startPosition..endPosition) {
        offsetPositions[i].x = positions[i].x
        offsetPositions[i].y = positions[i].y
    }

    // NOTE: We could maybe optimize this a little bit using a precomputed sum table.
    //       We would however only save a multiply and a few adds so probably not worth it?
    var position = positions[endPosition].x
    for (slice in slices.verticalSlices) {
        if (position > slice.start) {
            var offset = slice.size * stretchX
            if (position <= slice.end) {
                offset *= (position - slice.start) / slice.size
            }
            for (i in startPosition..endPosition) {
                offsetPositions[i].x += offset
            }
        }
    }

    position = positions[endPosition].y
    for (slice in slices.horizontalSlices) {
        if (position > slice.start) {
            var offset = slice.size * stretchY
            if (position <= slice.end) {
                offset *= (position - slice.start) / slice.size
            }
            for (i in startPosition..endPosition) {
                offsetPositions[i].y += offset
            }
        }
    }
}
