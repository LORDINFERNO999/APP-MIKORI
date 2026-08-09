package com.mikori.kids.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mikori.kids.core.ui.theme.Ink
import com.mikori.kids.core.ui.theme.MatchaLight

enum class KoriMood { HAPPY, RESTING, CURIOUS, SHUSH }

@Composable
fun KoriMascot(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    mood: KoriMood = KoriMood.HAPPY,
    bodyColor: Color = Color(0xFFFFFDF9),
    leafColor: Color = MatchaLight,
    cheekColor: Color = Color(0xFFF2C6CE),
    faceColor: Color = Ink,
) {
    Canvas(modifier = modifier.size(size)) {
        drawKori(mood, bodyColor, leafColor, cheekColor, faceColor)
    }
}

private fun DrawScope.drawKori(mood: KoriMood, body: Color, leaf: Color, cheek: Color, face: Color) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val headR = w * 0.30f
    val headCy = h * 0.56f

    drawLine(leaf, Offset(cx, headCy - headR * 0.9f), Offset(cx, headCy - headR * 1.45f), w * 0.022f, StrokeCap.Round)

    val leafPath = Path().apply {
        val tipX = cx
        val tipY = headCy - headR * 1.45f
        moveTo(tipX, tipY)
        cubicTo(cx + w * 0.03f, tipY - h * 0.06f, cx + w * 0.11f, tipY - h * 0.055f, cx + w * 0.12f, tipY - h * 0.01f)
        cubicTo(cx + w * 0.10f, tipY + h * 0.03f, cx + w * 0.03f, tipY + h * 0.02f, tipX, tipY)
        close()
    }
    drawPath(leafPath, color = leaf)

    val earR = headR * 0.34f
    drawCircle(body, earR, Offset(cx - headR * 0.62f, headCy - headR * 0.78f))
    drawCircle(body, earR, Offset(cx + headR * 0.62f, headCy - headR * 0.78f))
    drawCircle(body, headR, Offset(cx, headCy))

    val cheekR = headR * 0.15f
    drawCircle(cheek, cheekR, Offset(cx - headR * 0.52f, headCy + headR * 0.28f))
    drawCircle(cheek, cheekR, Offset(cx + headR * 0.52f, headCy + headR * 0.28f))

    val eyeR = headR * 0.12f
    val eyeY = headCy - headR * 0.05f
    val eyeDx = headR * 0.42f
    when (mood) {
        KoriMood.HAPPY, KoriMood.CURIOUS -> {
            drawCircle(face, eyeR, Offset(cx - eyeDx, eyeY))
            drawCircle(face, eyeR, Offset(cx + eyeDx, eyeY))
            val mouth = Path().apply {
                moveTo(cx - headR * 0.16f, headCy + headR * 0.30f)
                quadraticTo(cx, headCy + headR * 0.46f, cx + headR * 0.16f, headCy + headR * 0.30f)
            }
            drawPath(mouth, color = face, style = Stroke(width = w * 0.016f, cap = StrokeCap.Round))
        }
        KoriMood.RESTING -> {
            listOf(-eyeDx, eyeDx).forEach { dx ->
                val closed = Path().apply {
                    moveTo(cx + dx - eyeR, eyeY)
                    quadraticTo(cx + dx, eyeY + eyeR, cx + dx + eyeR, eyeY)
                }
                drawPath(closed, color = face, style = Stroke(width = w * 0.015f, cap = StrokeCap.Round))
            }
        }
        KoriMood.SHUSH -> {
            drawCircle(face, eyeR, Offset(cx - eyeDx, eyeY))
            drawCircle(face, eyeR, Offset(cx + eyeDx, eyeY))
            drawCircle(face, headR * 0.06f, Offset(cx, headCy + headR * 0.34f))
        }
    }
}
