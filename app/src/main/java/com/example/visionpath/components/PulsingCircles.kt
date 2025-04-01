package com.example.visionpath.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PulsingCircles(
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    val numberOfCircles = 8
    val transitions = List(numberOfCircles) { index ->
        rememberInfiniteTransition(label = "circle_$index")
    }

    val radiusMultipliers = transitions.mapIndexed { index, transition ->
        transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 2000,
                    delayMillis = (2000 / numberOfCircles) * index,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "radius_$index"
        )
    }

    val rotationAnim = rememberInfiniteTransition(label = "rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier.size(200.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.width.coerceAtMost(size.height) / 3
        val baseRadius = if (isSpeaking) maxRadius else maxRadius * 0.6f

        radiusMultipliers.forEachIndexed { index, radiusMultiplier ->
            val rotation = rotationAnim.value + (360f / numberOfCircles) * index
            val radiusValue = radiusMultiplier.value

            val x = center.x + cos(Math.toRadians(rotation.toDouble())).toFloat() * (baseRadius * 0.2f)
            val y = center.y + sin(Math.toRadians(rotation.toDouble())).toFloat() * (baseRadius * 0.2f)

            drawCircle(
                color = Color.White.copy(
                    alpha = if (isSpeaking) {
                        (1f - radiusValue) * 0.8f
                    } else {
                        (1f - radiusValue) * 0.4f
                    }
                ),
                radius = baseRadius * radiusValue,
                center = Offset(x, y)
            )
        }
    }
}
