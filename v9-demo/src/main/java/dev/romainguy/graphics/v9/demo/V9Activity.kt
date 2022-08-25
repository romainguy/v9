package dev.romainguy.graphics.v9.demo

import android.graphics.PointF
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import dev.romainguy.graphics.path.PathSegment
import dev.romainguy.graphics.path.iterator
import dev.romainguy.graphics.v9.Slices
import dev.romainguy.graphics.v9.demo.ui.theme.V9Theme
import dev.romainguy.graphics.v9.slice

val Bubble = Path().apply {
    moveTo(20.0f, 2.0f)
    lineTo(4.0f, 2.0f)
    relativeCubicTo(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
    relativeLineTo(0.0f, 18.0f)
    relativeLineTo(4.0f, -4.0f)
    relativeLineTo(14.0f, 0.0f)
    relativeCubicTo(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
    lineTo(22.0f, 4.0f)
    relativeCubicTo(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
    close()
}
val Assessment = Path().apply {
    moveTo(19.0f, 3.0f)
    lineTo(5.0f, 3.0f)
    relativeCubicTo(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
    relativeLineTo(0.0f, 14.0f)
    relativeCubicTo(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
    relativeLineTo(14.0f, 0.0f)
    relativeCubicTo(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
    lineTo(21.0f, 5.0f)
    relativeCubicTo(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
    close()
    moveTo(9.0f, 17.0f)
    lineTo(7.0f, 17.0f)
    relativeLineTo(0.0f, -7.0f)
    relativeLineTo(2.0f, 0.0f)
    relativeLineTo(0.0f, 7.0f)
    close()
    moveTo(13.0f, 17.0f)
    relativeLineTo(-2.0f, 0.0f)
    lineTo(11.0f, 7.0f)
    relativeLineTo(2.0f, 0.0f)
    relativeLineTo(0.0f, 10.0f)
    close()
    moveTo(17.0f, 17.0f)
    relativeLineTo(-2.0f, 0.0f)
    relativeLineTo(0.0f, -4.0f)
    relativeLineTo(2.0f, 0.0f)
    relativeLineTo(0.0f, 4.0f)
    close()
}

class V9Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            V9Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column {
                        val pathWidth = remember { mutableStateOf(68.0f) }
                        val pathHeight = remember { mutableStateOf(32.0f) }
                        val path = remember { mutableStateOf(Bubble) }
                        val slices = remember {
                            Slices(9.0f, 7.0f, 15.0f, 13.0f)
//                            Slices(
//                                listOf(Slice(9.0f, 10.0f), Slice(14.0f, 15.0f)),
//                                listOf(Slice(5.0f, 6.0f), Slice(18.0f, 19.0f))
//                            )
                        }
                        val resizablePath = remember(path.value) {
                            path.value.asAndroidPath().slice(slices)
                        }
                        val resizedPath = remember(resizablePath, pathWidth.value, pathHeight.value) {
                            resizablePath.resize(pathWidth.value, pathHeight.value).asComposePath()
                        }

                        ResizablePathViewer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.5f),
                            path.value,
                            resizedPath,
                            slices
                        )

                        Row(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                        ) {
                            Text(
                               modifier = Modifier.align(CenterVertically),
                               text  = "Width"
                            )
                            Slider(
                               modifier = Modifier
                                   .weight(1.0f)
                                   .align(CenterVertically),
                               value = pathWidth.value,
                               onValueChange = { pathWidth.value = it },
                               valueRange = resizablePath.bounds.width()..68.0f
                            )
                        }

                        Row(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                        ) {
                            Text(
                                modifier = Modifier.align(CenterVertically),
                                text  = "Height"
                            )
                            Slider(
                                modifier = Modifier
                                    .weight(1.0f)
                                    .align(CenterVertically),
                                value = pathHeight.value,
                                onValueChange = { pathHeight.value = it },
                                valueRange = resizablePath.bounds.height()..48.0f
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResizablePathViewer(
    modifier: Modifier = Modifier,
    path: Path,
    resizedPath: Path,
    slices: Slices
) {
    val pathColor = SolidColor(MaterialTheme.colors.surface)
    val accent1Color = SolidColor(Color(0.925f, 0.251f, 0.478f, 1.0f))
    val accent1FillColor = SolidColor(accent1Color.value.copy(alpha = 0.1f))
    val accent2Color = SolidColor(Color(0.149f, 0.776f, 0.855f, 1.0f))
    val accent2FillColor = SolidColor(accent2Color.value.copy(alpha = 0.1f))
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(1.0f, 0.5f))

    val pathSize = 24.0f
    val strokeWidth = 0.25f

    Canvas(
        modifier = modifier
    ) {
        scale(14.0f, pivot = Offset.Zero) {
            drawPath(
                path,
                brush = pathColor
            )

            translate(left = pathSize) {
                drawPath(
                    path,
                    brush = pathColor
                )

                drawPathControls(path, accent1Color, strokeWidth)
            }

            translate(left = pathSize * 2.0f) {
                drawPath(
                    path,
                    brush = pathColor
                )

                slices.horizontalSlices.forEach { slice ->
                    if (slice.size > 0) {
                        drawLine(
                            brush = accent2Color,
                            start = Offset(0.0f, slice.start),
                            end = Offset(pathSize, slice.start),
                            strokeWidth = strokeWidth,
                            pathEffect = dashEffect
                        )
                        drawLine(
                            brush = accent2Color,
                            start = Offset(0.0f, slice.end),
                            end = Offset(pathSize, slice.end),
                            strokeWidth = strokeWidth,
                            pathEffect = dashEffect
                        )
                        drawRect(
                            brush = accent2FillColor,
                            topLeft = Offset(0.0f, slice.start),
                            size = Size(pathSize, slice.end - slice.start)
                        )
                    }
                }

                slices.verticalSlices.forEach { slice ->
                    if (slice.size > 0) {
                        drawLine(
                            brush = accent1Color,
                            start = Offset(slice.start, 0.0f),
                            end = Offset(slice.start, pathSize),
                            strokeWidth = strokeWidth,
                            pathEffect = dashEffect
                        )
                        drawLine(
                            brush = accent1Color,
                            start = Offset(slice.end, 0.0f),
                            end = Offset(slice.end, pathSize),
                            strokeWidth = strokeWidth,
                            pathEffect = dashEffect
                        )
                        drawRect(
                            brush = accent1FillColor,
                            topLeft = Offset(slice.start, 0.0f),
                            size = Size(slice.end - slice.start, pathSize)
                        )
                    }
                }
            }

            translate(top = pathSize + 4.0f) {
                drawPath(
                    resizedPath,
                    brush = pathColor
                )
            }
        }
    }
}

private fun DrawScope.drawPathControls(
    path: Path,
    accentColor: SolidColor,
    strokeWidth: Float
) {
    for (segment in path.asAndroidPath()) {
        when (segment.type) {
            PathSegment.Type.Quadratic -> {
                drawLine(
                    brush = accentColor,
                    start = segment.points[0].toOffset(),
                    end = segment.points[1].toOffset(),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    brush = accentColor,
                    start = segment.points[1].toOffset(),
                    end = segment.points[2].toOffset(),
                    strokeWidth = strokeWidth
                )
            }
            PathSegment.Type.Conic,
            PathSegment.Type.Cubic -> {
                drawLine(
                    brush = accentColor,
                    start = segment.points[0].toOffset(),
                    end = segment.points[1].toOffset(),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    brush = accentColor,
                    start = segment.points[2].toOffset(),
                    end = segment.points[3].toOffset(),
                    strokeWidth = strokeWidth
                )
            }
            else -> {}
        }
        for (point in segment.points) {
            drawCircle(
                brush = accentColor,
                radius = 0.4f,
                center = point.toOffset()
            )
        }
    }
}

private fun PointF.toOffset() = Offset(x, y)
