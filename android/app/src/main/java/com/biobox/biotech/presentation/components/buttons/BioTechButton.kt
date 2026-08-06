package com.biobox.biotech.presentation.components.buttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biobox.biotech.presentation.theme.*

@Composable
fun BioTechButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    colors: List<Color> = listOf(PrimaryGreen, PrimaryBlue)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "button_scale"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .shadow(if (enabled) 8.dp else 0.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Gris300.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(0.dp),
        enabled = enabled && !isLoading,
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled && !isLoading) Brush.horizontalGradient(colors)
                    else Brush.horizontalGradient(listOf(Gris300, Gris300))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = text.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (enabled) Color.White else Gris600,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }
        }
    }
}

@Composable
fun BioTechOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "button_scale"
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            if (enabled) PrimaryCyan else Gris300
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (enabled) PrimaryCyan else Gris300
        ),
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
    }
}
