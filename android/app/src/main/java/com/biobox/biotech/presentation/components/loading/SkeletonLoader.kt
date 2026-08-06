package com.biobox.biotech.presentation.components.loading

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.biobox.biotech.presentation.theme.*

fun Modifier.biotechShimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translation"
    )

    val shimmerColors = listOf(
        Gris200.copy(alpha = 0.6f),
        Gris300.copy(alpha = 0.2f),
        Gris200.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    return@composed this.background(brush)
}

@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 20.dp,
    width: androidx.compose.ui.unit.Dp = 100.dp,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    Box(
        modifier = modifier
            .height(height)
            .width(width)
            .biotechShimmer()
            .background(Color.Transparent, shape)
    )
}

@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
    color: Color = PrimaryCyan
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        CircularProgressIndicator(
            color = color,
            strokeWidth = 4.dp
        )
    }
}
